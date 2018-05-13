import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecondaryUiComponent } from './secondary-ui.component';

describe('SecondaryUiComponent', () => {
  let component: SecondaryUiComponent;
  let fixture: ComponentFixture<SecondaryUiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecondaryUiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecondaryUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
