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

  deleteVehicle() {
    const vehicleId = this.selectedVehicle?.id;
    // Runtime check - we know id exists but TypeScript needs convincing
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
    if (!this.selectedVehicle) {
      return;
    }
  
    const dialogRef = this.dialog.open(EditVehicleDialogComponent, {
      width: '500px',
      data: this.selectedVehicle,
      disableClose: true
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result && this.selectedVehicle?.id) {  // Double check ID exists
        this.vehicleService.updateVehicle(this.selectedVehicle.id, result).subscribe({
          next: (updatedVehicle) => {
            // Update vehicles array and selected vehicle
            this.vehicles = this.vehicles.map(v => 
              v.id === updatedVehicle.id ? updatedVehicle : v
            );
            this.selectedVehicle = updatedVehicle;
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