import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry, map } from 'rxjs/operators';

export interface Vehicle {
  id?: string;
  vin: string;
  make: string;
  model: string;
  year: number;
  price: number;
  status: 'AVAILABLE' | 'SOLD' | 'PENDING';
  autoTraderListingId?: string;
  lastSyncAttempt?: string;
  syncStatus: 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details?: ValidationError[];
  path?: string;
}

@Injectable({
  providedIn: 'root'
})
export class VehicleService {
  private apiUrl = 'http://localhost:8080/api/v1/inventory-service/vehicles';

  constructor(private http: HttpClient) {}

  getAllVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.apiUrl).pipe(
      retry(1), 
      catchError(this.handleError),
      map(vehicles => this.sortVehicles(vehicles))
    );
  }

  getVehicle(id: string): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  createVehicle(vehicle: Partial<Vehicle>): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.apiUrl, vehicle).pipe(
      catchError(this.handleError)
    );
  }

  updateVehicle(id: string, vehicle: Partial<Vehicle>): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${id}`, vehicle).pipe(
      catchError(this.handleError)
    );
  }

  deleteVehicle(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      console.error('Client error:', error.error.message);
      errorMessage = error.error.message;
    } else {
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${JSON.stringify(error.error)}`
      );
      
      if (error.status === 400) {
        const backendError = error.error as ErrorResponse;
        if (backendError.details && backendError.details.length > 0) {
          return throwError(() => ({
            status: error.status,
            message: 'Validation failed',
            validationErrors: backendError.details
          }));
        }
      }

      switch (error.status) {
        case 404:
          errorMessage = 'Resource not found';
          break;
        case 400:
          errorMessage = 'Invalid request';
          break;
        case 403:
          errorMessage = 'Access denied';
          break;
        case 500:
          errorMessage = 'Server error';
          break;
        default:
          errorMessage = 'Something went wrong';
      }
    }

    return throwError(() => ({
      status: error.status,
      message: errorMessage,
      error: error.error
    }));
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
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'GBP'
    }).format(price);
  }
}