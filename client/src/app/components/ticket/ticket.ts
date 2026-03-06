import { Component, OnInit } from '@angular/core';
import { TicketResponse } from '../../interfaces/ticket';
import { TicketService } from '../../services/ticket-service';
import { Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { TicketThread } from '../ticket-thread/ticket-thread';
import { TicketThreadModal } from '../ticket-thread-modal/ticket-thread-modal';

@Component({
  selector: 'app-ticket',
  imports: [AsyncPipe],
  templateUrl: './ticket.html',
  styleUrl: './ticket.css',
})
export class Ticket implements OnInit {

  allTickets!: Observable<TicketResponse[]>;
  loadError: boolean = false;

  constructor(
    private ticketService: TicketService,
    private dialog: MatDialog
  ) {};

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loadError = false;
    this.allTickets = this.ticketService.getAllTickets();
  }

  openThread(ticket: TicketResponse) {
    const ref = this.dialog.open(TicketThreadModal, {
      data: { ticketId: ticket.ticketId, ticketStatus: ticket.status, ticketSubject: ticket.subject },
      width: '500px'
    });
    ref.afterClosed().subscribe(result => {
      if (result?.closed) {
        this.loadTickets();
      }
    });
  }


}
