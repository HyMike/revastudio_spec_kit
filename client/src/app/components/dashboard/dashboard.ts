import { Component, inject } from '@angular/core';
import { CustomerService } from '../../services/customer-service';
import { PurchasedTracks } from '../../interfaces/tracks';
import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { catchError, exhaustMap, Observable, of, Subject, tap } from 'rxjs';
import { EmployeeMetricsService } from '../../services/employee-metrics.service';
import { Role } from '../../type/role';
import { EmployeeSalesMetrics } from '../../interfaces/employee-metrics';

@Component({
  selector: 'app-dashboard',
  imports: [AsyncPipe, FormsModule, CurrencyPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  customerService = inject(CustomerService);
  employeeMetricsService = inject(EmployeeMetricsService);
  authService = inject(AuthService);
  router = inject(Router);

  showCreateTicketModal = false;
  role: Role | null = this.authService.getRole();
  isEmployee = this.role === 'EMPLOYEE';
  allTracks$ = this.isEmployee ? of([] as PurchasedTracks[]) : this.customerService.getAllTracks();
  employeeMetrics$ = this.isEmployee
    ? this.employeeMetricsService.getSalesMetrics()
    : of(null as EmployeeSalesMetrics | null);
  ticketSubject: string = "";
  ticketBody: string = "";

  logout(): void {
    this.authService.logout();
    this.router.navigate(["/login"]);
  }

  createTicket(): void {
    if (this.isEmployee) {
      return;
    }

    this.showCreateTicketModal = true;
  }

  private submitTrigger$ = new Subject<{subject: string, body: string }>();

  submitTriggerResult$ = this.submitTrigger$.pipe(
    exhaustMap((payload) => {
      return this.customerService.submitTicket(payload).pipe(
        tap(() => {
          this.closeTicketModal();
        }),
        catchError((err) => {
          console.log(err);
          return of(null);
        })
      )
    })
  )

  onSubmitTicket(): void {
    this.submitTrigger$.next({
      subject: this.ticketSubject, 
      body: this.ticketBody
    })
  }

  closeTicketModal(): void {
    this.showCreateTicketModal = false;
  }
}
