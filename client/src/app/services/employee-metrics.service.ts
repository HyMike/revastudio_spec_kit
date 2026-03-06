import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EmployeeSalesMetrics } from '../interfaces/employee-metrics';

const API_BASE_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root',
})
export class EmployeeMetricsService {
  constructor(private http: HttpClient) {}

  getSalesMetrics(): Observable<EmployeeSalesMetrics> {
    return this.http.get<EmployeeSalesMetrics>(`${API_BASE_URL}/employee/sales-metrics`);
  }
}
