import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BatteryCircleComponent } from './battery-circle.component';

describe('BatteryCircleComponent', () => {
  let component: BatteryCircleComponent;
  let fixture: ComponentFixture<BatteryCircleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BatteryCircleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BatteryCircleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
