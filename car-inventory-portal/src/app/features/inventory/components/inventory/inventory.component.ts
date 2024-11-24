// inventory.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { Vehicle, VehicleService } from '../../../../services/vehicle.service';
import { AddVehicleFormComponent } from '../add-vehicle-form/add-vehicle-form.component';
import { DeleteDialogComponent } from '../delete-dialog/delete-dialog.component';
import { EditVehicleDialogComponent } from '../edit-vehicle-dialog/edit-vehicle-dialog.component';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    MatDialogModule,
    FormsModule
  ],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent implements OnInit {
  searchTerm = '';
  selectedVehicle: Vehicle | null = null;
  vehicles: Vehicle[] = [];

  constructor(
    private dialog: MatDialog,
    private vehicleService: VehicleService
  ) {}

  ngOnInit() {
    this.loadVehicles();
  }

  selectVehicle(vehicle: Vehicle) {
    this.selectedVehicle = vehicle;
  }

  addNewVehicle() {
    const dialogRef = this.dialog.open(AddVehicleFormComponent, {
      width: '500px',
      disableClose: true
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadVehicles();
      }
    });
  }

  loadVehicles() {
    this.vehicleService.getAllVehicles().subscribe({
      next: (vehicles) => {
        this.vehicles = vehicles;
      },
      error: (error) => {
        console.error('Error loading vehicles:', error);
      }
    });
  }

  searchVehicles(): void {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.loadVehicles();
      return;
    }
    
    this.vehicles = this.vehicles.filter(vehicle => 
      vehicle.make.toLowerCase().includes(term) ||
      vehicle.model.toLowerCase().includes(term) ||
      vehicle.year.toString().includes(term) ||
      vehicle.vin.toLowerCase().includes(term)
    );
  }

  resetSearch() {
    this.searchTerm = '';
    this.loadVehicles();
  }

  deleteVehicle() {
    const vehicleId = this.selectedVehicle?.id;
    if (!vehicleId) {
      console.error('Vehicle has no ID');
      return;
    }
  
    const dialogRef = this.dialog.open(DeleteDialogComponent);
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.vehicleService.deleteVehicle(vehicleId).subscribe({
          next: () => {
            this.vehicles = this.vehicles.filter(v => v.id !== vehicleId);
            this.selectedVehicle = null;
          },
          error: (error) => console.error('Error deleting vehicle:', error)
        });
      }
    });
  }

  editVehicle() {
    if (!this.selectedVehicle?.id) {
      return;
    }
  
    const vehicleId = this.selectedVehicle.id;
  
    const dialogRef = this.dialog.open(EditVehicleDialogComponent, {
      width: '500px',
      data: this.selectedVehicle,
      disableClose: true
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (result.imageAction === 'delete') {
          this.vehicleService.deleteVehicleImage(vehicleId).subscribe({
            next: () => {
              console.log('Image deleted successfully');
              // Refresh vehicle data after image deletion
              this.loadVehicles();
            },
            error: (err) => console.error('Error deleting image:', err)
          });
        }

        this.vehicleService.updateVehicle(vehicleId, result.vehicleData).subscribe({
          next: (updatedVehicle) => {
            this.vehicles = this.vehicles.map(v =>
              v.id === updatedVehicle.id ? updatedVehicle : v
            );
            this.selectedVehicle = updatedVehicle;
  
            if (result.imageAction === 'update' && result.imageFile) {
              const formData = new FormData();
              formData.append('file', result.imageFile);
              
              this.vehicleService.updateVehicleImage(vehicleId, formData).subscribe({
                next: () => {
                  console.log('Image uploaded successfully');
                  // Refresh vehicle data after image upload
                  this.loadVehicles();
                },
                error: (err) => console.error('Error uploading image:', err)
              });
            }
          },
          error: (error) => {
            console.error('Error updating vehicle:', error);
            if (error.validationErrors) {
              console.log('Validation errors:', error.validationErrors);
            }
          }
        });
      }
    });
  }
}