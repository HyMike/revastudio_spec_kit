import { Injectable } from '@angular/core';
import { TicketResponse } from '../interfaces/ticket';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth-service';


const API_BASE_URL = "http://localhost:8080/api";

@Injectable({
  providedIn: 'root',
})
export class TicketService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getAllTickets(): Observable<TicketResponse[]> {
    if (this.authService.getRole() == "CUSTOMER"){
      return this.http.get<TicketResponse[]>(`${API_BASE_URL}/ticket/customer`);
    } else {
      return this.http.get<TicketResponse[]>(`${API_BASE_URL}/ticket/employee`);
    }
  }
  
}
