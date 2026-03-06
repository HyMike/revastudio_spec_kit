import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Dashboard } from './dashboard';
import { CustomerService } from '../../services/customer-service';
import { EmployeeMetricsService } from '../../services/employee-metrics.service';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';

describe('Dashboard', () => {
  async function createComponent(role: 'CUSTOMER' | 'EMPLOYEE') {
    const customerServiceMock = {
      getAllTracks: vi.fn().mockReturnValue(
        of(role === 'CUSTOMER' ? [{ track: 'Track A', artist: 'Artist A', albums: 'Album A' }] : [])
      ),
      submitTicket: vi.fn().mockReturnValue(of(null)),
    };

    const employeeMetricsServiceMock = {
      getSalesMetrics: vi.fn().mockReturnValue(
        of({
          summary: { totalSalesCount: 2, totalRevenue: 20 },
          customers: [{ customerId: 1, customerName: 'Jane Doe', totalSales: 2 }],
          sales: [
            {
              invoiceId: 1001,
              invoiceDate: '2026-03-05',
              customerName: 'Jane Doe',
              trackName: 'Track One',
              billedAmount: 5,
            },
          ],
        })
      ),
    };

    const authServiceMock = {
      getRole: vi.fn().mockReturnValue(role),
      logout: vi.fn(),
    };

    const routerMock = {
      navigate: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: CustomerService, useValue: customerServiceMock },
        { provide: EmployeeMetricsService, useValue: employeeMetricsServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    return { fixture };
  }

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should render employee sales metrics', async () => {
    const { fixture } = await createComponent('EMPLOYEE');
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('My Sales Dashboard');
    expect(text).toContain('Total Sales');
    expect(text).toContain('Jane Doe');
    expect(text).toContain('Track One');
  });

  it('should render purchased tracks for customers', async () => {
    const { fixture } = await createComponent('CUSTOMER');
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('My Purchased Tracks');
    expect(text).toContain('Track A');
    expect(text).toContain('create ticket');
  });
});
