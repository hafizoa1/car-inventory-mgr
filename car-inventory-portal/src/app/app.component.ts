import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';
import { MatIconRegistry } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MainLayoutComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})

export class AppComponent {
  constructor(
    private matIconRegistry: MatIconRegistry
  ) {
    this.matIconRegistry.setDefaultFontSetClass('material-icons');
  }

  title = 'car-inventory-portal';
}
