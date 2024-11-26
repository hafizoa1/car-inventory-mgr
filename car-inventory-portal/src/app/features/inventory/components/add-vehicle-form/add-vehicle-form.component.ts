import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { VehicleService } from '../../../../services/vehicle.service';
import { ErrorReturn, ValidationError } from '../../../../services/error.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-add-vehicle-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './add-vehicle-form.component.html',
  styleUrl: './add-vehicle-form.component.scss'
})
export class AddVehicleFormComponent {
  vehicleForm: FormGroup;
  isSubmitting = false;
  serverErrors: { [key: string]: string } = {};

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddVehicleFormComponent>,
    private vehicleService: VehicleService,
    private snackBar: MatSnackBar
  ) {
    this.vehicleForm = this.fb.group({
      vin: ['', [Validators.required]],
      make: ['', [Validators.required]],
      model: ['', [Validators.required]],
      year: ['', [Validators.required, Validators.min(1900)]],
      price: ['', [Validators.required, Validators.min(0)]],
      status: ['AVAILABLE', [Validators.required]],
      syncStatus: ['PROCESSING']
    });
  }

  private handleValidationErrors(validationErrors: ValidationError[]) {
    validationErrors.forEach(error => {
      const control = this.vehicleForm.get(error.field.toLowerCase());
      if (control) {
        control.setErrors({
          serverError: error.message
        });
        this.serverErrors[error.field.toLowerCase()] = error.message;
        control.markAsTouched();
      }
    });
  }

  private clearServerErrors() {
    Object.keys(this.serverErrors).forEach(key => {
      const control = this.vehicleForm.get(key);
      if (control) {
        const currentErrors = { ...control.errors };
        delete currentErrors['serverError'];
        control.setErrors(Object.keys(currentErrors).length ? currentErrors : null);
      }
    });
    this.serverErrors = {};
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  onSubmit() {
    if (this.vehicleForm.valid) {
      this.isSubmitting = true;
      this.clearServerErrors();

      this.vehicleService.createVehicle(this.vehicleForm.value)
        .pipe(
          finalize(() => {
            this.isSubmitting = false;
          })
        )
        .subscribe({
          next: (response) => {
            console.log('Vehicle created:', response);
            this.snackBar.open('Vehicle successfully created!', 'Close', {
              duration: 3000,
              horizontalPosition: 'right',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
            this.dialogRef.close(response);
          },
          error: (errorReturn: ErrorReturn) => {
            console.log('Component received error:', errorReturn);
            
            if (errorReturn.validationErrors) {
              this.handleValidationErrors(errorReturn.validationErrors);
            }
            
            if (errorReturn.errors && errorReturn.errors.length > 0) {
              // Get the first error message
              const errorParts = errorReturn.errors[0].split(':');
              const errorMessage = errorParts.length > 1 
                ? errorParts[1].trim() 
                : errorReturn.errors[0];
                
              this.snackBar.open(`Validation Error: ${errorMessage}`, 'Close', {
                duration: 5000,
                horizontalPosition: 'right',
                verticalPosition: 'top',
                panelClass: ['error-snackbar']
              });
            } else {
              this.snackBar.open(errorReturn.message, 'Close', {
                duration: 5000,
                horizontalPosition: 'right',
                verticalPosition: 'top',
                panelClass: ['error-snackbar']
              });
            }
          }
        });
    }
  }
}