import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { EmployeeMetricsService } from './employee-metrics.service';

describe('EmployeeMetricsService', () => {
  let service: EmployeeMetricsService;
  let httpClientSpy: { get: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    httpClientSpy = {
      get: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        EmployeeMetricsService,
        { provide: HttpClient, useValue: httpClientSpy },
      ],
    });

    service = TestBed.inject(EmployeeMetricsService);
  });

  it('should request employee sales metrics', () => {
    httpClientSpy.get.mockReturnValue(
      of({
        summary: { totalSalesCount: 1, totalRevenue: 12.5 },
        customers: [],
        sales: [],
      })
    );

    service.getSalesMetrics().subscribe();

    expect(httpClientSpy.get).toHaveBeenCalledWith(
      'http://localhost:8080/api/employee/sales-metrics'
    );
  });
});
