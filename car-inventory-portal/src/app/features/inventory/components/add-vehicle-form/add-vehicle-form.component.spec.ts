import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddVehicleFormComponent } from './add-vehicle-form.component';

describe('AddVehicleFormComponent', () => {
  let component: AddVehicleFormComponent;
  let fixture: ComponentFixture<AddVehicleFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddVehicleFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddVehicleFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
