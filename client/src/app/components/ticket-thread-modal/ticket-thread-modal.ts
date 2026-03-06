import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Inject } from '@angular/core';
import { BehaviorSubject, delay, map, Observable, shareReplay } from 'rxjs';
import { TicketThreadInterface } from '../../interfaces/ticket-thread-interface';
import { TicketThreadService } from '../../services/ticket-thread-service';
import { AuthService } from '../../services/auth-service';
import { TicketService } from '../../services/ticket-service';
import { TicketStatus } from '../../interfaces/ticket';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ticket-thread-modal',
  imports: [AsyncPipe, FormsModule],
  templateUrl: './ticket-thread-modal.html',
  styleUrl: './ticket-thread-modal.css',
})
export class TicketThreadModal implements OnInit {

  isEmployee: boolean = false;
  errorMessage: string = '';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {ticketId: number, ticketStatus: TicketStatus, ticketSubject: string},
    private dialogRef: MatDialogRef<TicketThreadModal>,
    private ticketThreads: TicketThreadService,
    private authService: AuthService,
    private ticketService: TicketService,
    private cdr: ChangeDetectorRef
  ) {}

  ticketThreadMessages$!: Observable<TicketThreadInterface[]>;
  newThreadMessage: string = '';

  ngOnInit(): void {
      this.isEmployee = this.authService.getRole() === 'EMPLOYEE';
      this.ticketThreadMessages$ = this.ticketThreads.getAllThreadMessages(this.data.ticketId);
  }

  createThreadMessage(): void {
    if (!this.newThreadMessage?.trim()) {
      this.errorMessage = 'Message cannot be empty.';
      return;
    }
    this.errorMessage = '';
    
        
  console.log('Creating thread with message:', this.newThreadMessage);
  
  this.ticketThreads.createThreadMessage(this.data.ticketId, this.newThreadMessage)
    .subscribe({
      next: (response) => {
        console.log('Thread created:', response);
        this.newThreadMessage = '';
        this.ticketThreadMessages$ = this.ticketThreads.getAllThreadMessages(this.data.ticketId);
        this.cdr.detectChanges();
        console.log('Refreshing messages...');
      },
      error: (err) => {
        console.error('Error creating thread:', err);
        this.errorMessage = 'Failed to send message. Please try again.';
      }
    });
}

  onClose(): void {
    this.dialogRef.close();
  }

  closeTicket(): void {
    this.ticketService.closeTicket(this.data.ticketId).subscribe({
      next: () => {
        this.dialogRef.close({ closed: true });
      },
      error: (err) => {
        console.error('Error closing ticket:', err);
        this.errorMessage = err.status === 409
          ? 'This ticket is already resolved.'
          : 'Failed to close ticket. Please try again.';
      }
    });
  }
  
}
