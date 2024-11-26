import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ErrorService } from './error.service';

export interface Vehicle {
  id?: string;
  vin: string;
  make: string;
  model: string;
  year: number;
  price: number;
  image?: string | null;
  status: 'AVAILABLE' | 'SOLD' | 'PENDING';
  autoTraderListingId?: string;
  lastSyncAttempt?: string;
  syncStatus: 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private apiUrl = 'http://localhost:8080/api/v1/inventory-service/vehicles';

  constructor(
    private http: HttpClient, 
    private errorService: ErrorService
  ) {}

  getAllVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.apiUrl)
      .pipe(catchError(this.errorService.handleError));
  }

  getVehicle(id: string): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.errorService.handleError));
  }

  createVehicle(vehicle: Partial<Vehicle>): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.apiUrl, vehicle).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log('Raw error from API:', error);
        console.log('Error response body:', error.error);
        console.log('Error status:', error.status);
        return this.errorService.handleError(error);
      })
    );
  }


  updateVehicle(id: string, vehicle: Partial<Vehicle>): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${id}`, vehicle)
      .pipe(catchError(this.errorService.handleError));
  }

  deleteVehicle(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.errorService.handleError));
  }

  updateVehicleImage(id: string, formData: FormData): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/image`, formData)
      .pipe(catchError(this.errorService.handleError));
  }

  deleteVehicleImage(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/image`)
      .pipe(catchError(this.errorService.handleError));
  }

  private sortVehicles(vehicles: Vehicle[]): Vehicle[] {
    return vehicles.sort((a, b) => {
      const statusPriority = {
        'AVAILABLE': 0,
        'PENDING': 1,
        'SOLD': 2
      };
      return statusPriority[a.status] - statusPriority[b.status];
    });
  }

  isVehicleAvailable(vehicle: Vehicle): boolean {
    return vehicle.status === 'AVAILABLE';
  }

  formatVehicleTitle(vehicle: Vehicle): string {
    return `${vehicle.year} ${vehicle.make} ${vehicle.model}`;
  }

  getFormattedPrice(price: number): string {
    return new Intl.NumberFormat('en-GB', {
      style: 'currency',
      currency: 'GBP'
    }).format(price);
  }
}