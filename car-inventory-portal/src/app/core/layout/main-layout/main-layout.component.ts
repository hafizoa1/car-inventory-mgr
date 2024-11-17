import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { MainDashboardComponent } from '../main-dashboard/main-dashboard.component';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterModule } from '@angular/router';
import { RouterOutlet } from '@angular/router';


@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  imports: [
    HeaderComponent,
    SidebarComponent,
    MainDashboardComponent,
    MatSidenavModule,
    RouterModule, 
    RouterOutlet
  ],
  standalone: true
})

export class MainLayoutComponent {}