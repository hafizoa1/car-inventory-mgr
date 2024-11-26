import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { VehicleService, Vehicle } from '../../../../services/vehicle.service';
import { ErrorReturn, ValidationError } from '../../../../services/error.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-edit-vehicle-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './edit-vehicle-dialog.component.html',
  styleUrl: './edit-vehicle-dialog.component.scss'
})
export class EditVehicleDialogComponent {
  vehicleForm: FormGroup;
  isSubmitting = false;
  serverErrors: { [key: string]: string } = {};
  currentImage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EditVehicleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Vehicle,
    private vehicleService: VehicleService,
    private snackBar: MatSnackBar
  ) {
    this.vehicleForm = this.fb.group({
      vin: [data.vin, [Validators.required]],
      make: [data.make, [Validators.required]],
      model: [data.model, [Validators.required]],
      year: [data.year, [Validators.required, Validators.min(1900)]],
      price: [data.price, [Validators.required, Validators.min(0)]],
      status: [data.status, [Validators.required]],
      syncStatus: [data.syncStatus]
    });
    
    this.currentImage = data.image || null;
  }

  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const formData = new FormData();
      formData.append('image', file);
      
      this.vehicleService.updateVehicleImage(this.data.id!, formData)
        .subscribe({
          next: () => {
            // Handle image update success
            const reader = new FileReader();
            reader.onload = (e) => {
              this.currentImage = e.target?.result as string;
            };
            reader.readAsDataURL(file);
            
            this.snackBar.open('Image successfully updated!', 'Close', {
              duration: 3000,
              horizontalPosition: 'right',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
          },
          error: (error: ErrorReturn) => {
            this.snackBar.open('Failed to update image', 'Close', {
              duration: 5000,
              horizontalPosition: 'right',
              verticalPosition: 'top',
              panelClass: ['error-snackbar']
            });
          }
        });
    }
  }

  removeImage() {
    if (this.data.id) {
      this.vehicleService.deleteVehicleImage(this.data.id)
        .subscribe({
          next: () => {
            this.currentImage = null;
            this.snackBar.open('Image successfully removed!', 'Close', {
              duration: 3000,
              horizontalPosition: 'right',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
          },
          error: (error: ErrorReturn) => {
            this.snackBar.open('Failed to remove image', 'Close', {
              duration: 5000,
              horizontalPosition: 'right',
              verticalPosition: 'top',
              panelClass: ['error-snackbar']
            });
          }
        });
    }
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

      this.vehicleService.updateVehicle(this.data.id!, this.vehicleForm.value)
        .pipe(
          finalize(() => {
            this.isSubmitting = false;
          })
        )
        .subscribe({
          next: (response) => {
            console.log('Vehicle updated:', response);
            this.snackBar.open('Vehicle successfully updated!', 'Close', {
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
    } else {
      this.markFormGroupTouched(this.vehicleForm);
      this.snackBar.open('Please fill in all required fields correctly', 'Close', {
        duration: 3000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
    }
  }
}