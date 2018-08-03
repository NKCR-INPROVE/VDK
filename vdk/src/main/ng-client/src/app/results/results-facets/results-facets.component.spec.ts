import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsFacetsComponent } from './results-facets.component';

describe('ResultsFacetsComponent', () => {
  let component: ResultsFacetsComponent;
  let fixture: ComponentFixture<ResultsFacetsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultsFacetsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsFacetsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
