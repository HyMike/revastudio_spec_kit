import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { TicketThreadModal } from './ticket-thread-modal';
import { TicketThreadService } from '../../services/ticket-thread-service';

describe('TicketThreadModal', () => {
  let component: TicketThreadModal;
  let fixture: ComponentFixture<TicketThreadModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketThreadModal],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { ticketId: 1 } },
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: TicketThreadService,
          useValue: {
            getAllThreadMessages: () => of([]),
            createThreadMessage: () => of({}),
          },
        },
        { provide: ChangeDetectorRef, useValue: { detectChanges: () => {} } },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketThreadModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
