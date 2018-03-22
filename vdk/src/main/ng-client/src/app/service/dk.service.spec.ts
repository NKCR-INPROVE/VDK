import { TestBed, inject } from '@angular/core/testing';

import { DkService } from './dk.service';

describe('DkService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DkService]
    });
  });

  it('should be created', inject([DkService], (service: DkService) => {
    expect(service).toBeTruthy();
  }));
});
