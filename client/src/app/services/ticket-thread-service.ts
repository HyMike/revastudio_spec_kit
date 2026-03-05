import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TicketThreadInterface } from '../interfaces/ticket-thread-interface';
import { Observable } from 'rxjs';

const API_BASE_URL = "http://localhost:8080/api";

@Injectable({
  providedIn: 'root',
})
export class TicketThreadService {

  constructor(private http: HttpClient) {};

  getAllThreadMessages(ticketId: number): Observable<TicketThreadInterface[]> {
    return this.http.get<TicketThreadInterface[]>(`${API_BASE_URL}/ticket-threads/${ticketId}`);
  }

  createThreadMessage(ticketId: number, threadMessage: string): Observable<TicketThreadInterface> {
    return this.http.post<TicketThreadInterface>(`${API_BASE_URL}/ticket-threads/create`, {
      ticketId,
      thread: threadMessage
    });
  } 
}

