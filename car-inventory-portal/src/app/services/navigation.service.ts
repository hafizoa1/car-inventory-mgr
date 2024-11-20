import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  navItems = [
    { path: 'home', icon: 'home', label: 'Home' },
    { path: 'inventory', icon: 'directions_car', label: 'Inventory' },
    { path: 'leads', icon: 'people', label: 'Leads' },
    { path: 'advertisements', icon: 'campaign', label: 'Ads' }
  ];

  constructor(private router: Router) {}

  navigate(path: string) {
    this.router.navigate([path]);
  }
}