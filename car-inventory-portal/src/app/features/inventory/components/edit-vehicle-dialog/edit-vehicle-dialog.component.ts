import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
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
    MatSelectModule,
    MatIconModule  
  ],
  templateUrl: './edit-vehicle-dialog.component.html',
  styleUrl: './edit-vehicle-dialog.component.scss'
})
export class EditVehicleDialogComponent {
  vehicleForm: FormGroup;
  currentImage: string | null = null;
  selectedFile: File | null = null;

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

    if (data.image) {
      this.currentImage = `data:image/jpeg;base64,${data.image}`;
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.currentImage = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage() {
    this.currentImage = null;
    this.selectedFile = null;
    this.dialogRef.close({ 
      vehicleData: this.vehicleForm.value, 
      imageAction: 'delete' 
    });
  }

  onSubmit() {
    if (this.vehicleForm.valid) {
      const updatedVehicle: Vehicle = {
        ...this.data,
        ...this.vehicleForm.value
      };
  
      this.dialogRef.close({
        vehicleData: updatedVehicle,
        imageAction: this.selectedFile ? 'update' : null,
        imageFile: this.selectedFile || null 
      });
    }
  }
}