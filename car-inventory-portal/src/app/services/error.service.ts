import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

export interface ValidationError {
  field: string;
  message: string;
}

export interface ErrorReturn {
  message: string;
  errors: string[];
  validationErrors?: ValidationError[];
}

interface BackendError {
  message: string;
  errors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  handleError = (error: HttpErrorResponse): Observable<never> => {
    console.log('ErrorService receiving:', error);

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      return throwError(() => ({
        message: error.error.message,
        errors: [error.error.message]
      }));
    }

    // Server-side error
    try {
      if (error.status === 400 && error.error) {
        console.log('Processing 400 error:', error.error);
        
        const backendError = error.error as BackendError;
        
        // Handle validation errors
        if (backendError.errors && Array.isArray(backendError.errors)) {
          const validationErrors: ValidationError[] = backendError.errors.map(err => {
            const [field, ...messageParts] = err.split(':');
            return {
              field: field.trim(),
              message: messageParts.join(':').trim()
            };
          });

          console.log('Processed validation errors:', validationErrors);

          return throwError(() => ({
            message: backendError.message,
            errors: backendError.errors,
            validationErrors: validationErrors
          }));
        }
      }

      // Generic error handling
      const errorMessage = (error.error as BackendError)?.message || 'An error occurred';
      return throwError(() => ({
        message: errorMessage,
        errors: [errorMessage]
      }));

    } catch (e) {
      console.error('Error processing error response:', e);
      return throwError(() => ({
        message: 'An unexpected error occurred',
        errors: ['An unexpected error occurred']
      }));
    }
  };
}