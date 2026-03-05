import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Inject } from '@angular/core';
import { BehaviorSubject, delay, map, Observable, shareReplay } from 'rxjs';
import { TicketThreadInterface } from '../../interfaces/ticket-thread-interface';
import { TicketThreadService } from '../../services/ticket-thread-service';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ticket-thread-modal',
  imports: [AsyncPipe, FormsModule],
  templateUrl: './ticket-thread-modal.html',
  styleUrl: './ticket-thread-modal.css',
})
export class TicketThreadModal implements OnInit {

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {ticketId: number},
  private dialogRef: MatDialogRef<TicketThreadModal>,
  private ticketThreads: TicketThreadService,
  private cdr: ChangeDetectorRef
  ) {}

  ticketThreadMessages$!: Observable<TicketThreadInterface[]>;
  newThreadMessage: string = '';

  ngOnInit(): void {
      this.ticketThreadMessages$ = this.ticketThreads.getAllThreadMessages(this.data.ticketId);
  }

  createThreadMessage(): void {
    if (!this.newThreadMessage?.trim()) return;
    
        
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
      }
    });
}

  onClose(): void {
    this.dialogRef.close();
  }
  

}
