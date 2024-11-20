import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VehicleService } from '../../../../services/vehicle.service';;

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
    MatDialogModule
  ],
  templateUrl: './add-vehicle-form.component.html',
  styleUrl: './add-vehicle-form.component.scss'
})
export class AddVehicleFormComponent {
  vehicleForm: FormGroup;
  isSubmitting = false; 

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddVehicleFormComponent>,
    private vehicleService: VehicleService,
    private snackBar: MatSnackBar
  ) {
    this.vehicleForm = this.fb.group({
      vin: ['', Validators.required],
      make: ['', Validators.required],
      model: ['', Validators.required],
      year: ['', [Validators.required, Validators.min(1900)]],
      price: ['', [Validators.required, Validators.min(0)]],
      status: ['AVAILABLE', Validators.required],
      syncStatus: ['PROCESSING']
    });
  }

  onSubmit() {
    if (this.vehicleForm.valid) {
      this.isSubmitting = true; 
      this.vehicleService.createVehicle(this.vehicleForm.value).subscribe({
        next: (response) => {
          this.snackBar.open('Vehicle successfully created!', 'Close', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top'
          });
          this.dialogRef.close(response);
        },
        error: (error) => {
          this.isSubmitting = false; 
          this.snackBar.open('Error creating vehicle: ' + error.message, 'Close', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top'
          });
        }
      });
    }
  }
}