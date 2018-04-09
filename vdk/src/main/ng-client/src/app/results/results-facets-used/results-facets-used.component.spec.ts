import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsFacetsUsedComponent } from './results-facets-used.component';

describe('ResultsFacetsUsedComponent', () => {
  let component: ResultsFacetsUsedComponent;
  let fixture: ComponentFixture<ResultsFacetsUsedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultsFacetsUsedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsFacetsUsedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
