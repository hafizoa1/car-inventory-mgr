import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Vehicle } from '../../../../services/vehicle.service';


@Component({
  selector: 'app-edit-vehicle-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './edit-vehicle-dialog.component.html',
  styleUrl: './edit-vehicle-dialog.component.scss'
})
export class EditVehicleDialogComponent {
  vehicleForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<EditVehicleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Vehicle
  ) {
    this.vehicleForm = this.fb.group({
      make: [data.make, Validators.required],
      model: [data.model, Validators.required],
      year: [data.year, [Validators.required, Validators.min(1900)]],
      vin: [data.vin, Validators.required],
      price: [data.price, [Validators.required, Validators.min(0)]],
      status: [data.status, Validators.required],
      syncStatus: [data.syncStatus]
    });
  }

  onSubmit() {
    if (this.vehicleForm.valid) {
      const updatedVehicle: Vehicle = {
        ...this.data,
        ...this.vehicleForm.value
      };
      this.dialogRef.close(updatedVehicle);
    }
  }
}

