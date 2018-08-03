import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsPaginationComponent } from './results-pagination.component';

describe('ResultsPaginationComponent', () => {
  let component: ResultsPaginationComponent;
  let fixture: ComponentFixture<ResultsPaginationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultsPaginationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsPaginationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
