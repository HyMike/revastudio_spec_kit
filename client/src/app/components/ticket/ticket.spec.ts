import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { Ticket } from './ticket';
import { TicketService } from '../../services/ticket-service';

describe('Ticket', () => {
  let component: Ticket;
  let fixture: ComponentFixture<Ticket>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Ticket],
      providers: [
        {
          provide: TicketService,
          useValue: {
            getAllTickets: () => of([]),
          },
        },
        {
          provide: MatDialog,
          useValue: {
            open: () => {},
          },
        },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(Ticket);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
