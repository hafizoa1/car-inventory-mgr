import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';
import { InventoryComponent } from './features/inventory/components/inventory/inventory.component';
import { MainDashboardComponent } from './core/layout/main-dashboard/main-dashboard.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        component: MainDashboardComponent  // Changed from redirect to direct component
      },
      {
        path: 'home',
        component: MainDashboardComponent
      },
      {
        path: 'inventory',
        component: InventoryComponent
      }
    ]
  }
];