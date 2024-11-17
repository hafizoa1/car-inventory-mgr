import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardStats } from '../../../interfaces/dashboard-stats.interface';

@Component({
  selector: 'app-main-dashboard',
  templateUrl: './main-dashboard.component.html',
  styleUrls: ['./main-dashboard.component.scss'],
  standalone: true,
  imports: [
    CommonModule,

  ]
})
export class MainDashboardComponent implements OnInit {
  stats: DashboardStats = {
    inventory: {
      total: 1500,
      active: 1200,
      sold: 300
    },
    leads: {
      new: 45,
      open: 120,
      closed: 80
    },
    ads: {
      active: 25,
      views: 15000,
      ctr: 2.5,    // Click-through rate as percentage
      conversion: 1.8  // Conversion rate as percentage
    }
  };

  loading = false;

  constructor() {}

  ngOnInit(): void {
    // Mock data is already loaded
  }
}