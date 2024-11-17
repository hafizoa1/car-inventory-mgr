import { Component } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    MatListModule,
    MatIconModule,
    RouterModule,
    CommonModule
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
 })
 export class SidebarComponent {
  navItems = [
    { path: '/inventory', icon: 'directions_car', label: 'Inventory' },
    { path: '/leads', icon: 'people', label: 'Leads' },
    { path: '/advertisements', icon: 'campaign', label: 'Ads' }
  ];
 }
