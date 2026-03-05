package com.revature.revastudio.services;

import com.revature.revastudio.controllers.TicketThreadController;
import com.revature.revastudio.dto.TicketThreadDTO;
import com.revature.revastudio.dto.TicketThreadRequestDTO;
import com.revature.revastudio.entity.Ticket;
import com.revature.revastudio.entity.TicketThread;
import com.revature.revastudio.repositories.TicketRepository;
import com.revature.revastudio.repositories.TicketThreadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketThreadService {

    private final TicketThreadRepository ticketThreadRepository;
    private final TicketRepository ticketRepository;

    public TicketThreadService(
        TicketThreadRepository ticketThreadRepository,
        TicketRepository ticketRepository
    ) {
        this.ticketThreadRepository = ticketThreadRepository;
        this.ticketRepository = ticketRepository;
    }

    // implement to get all tickets in the database and return it to the frontend.

    public List<TicketThreadDTO> getThreadMessages(Integer ticketId) {

        List<TicketThread> ticketThreads = this.ticketThreadRepository.findByTicket_TicketId(ticketId);
        return ticketThreads.stream()
                .map((ticketThread) ->  {
                    return new TicketThreadDTO(
                            ticketThread.getTicketThreadId(),
                            ticketId,
                            ticketThread.getThread(),
                            ticketThread.getCreatedAt()
                    );

                }).toList();

    }


    public TicketThreadDTO createThread(TicketThreadRequestDTO ticketThreadDTO) {
            System.out.println("=== Creating thread for ticketId: " + ticketThreadDTO.getTicketId() + " ===");

//        Ticket ticket = this.ticketRepository.findById(ticketThreadDTO.getTicketId()).orElseThrow(() -> new RuntimeException("Ticket not found"));
        Optional<Ticket> ticketOpt = this.ticketRepository.findById(ticketThreadDTO.getTicketId());

        Ticket ticket = ticketOpt.orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketThread ticketThread = this.ticketThreadRepository.save(new TicketThread(
            null, 
            ticket,
            ticketThreadDTO.getThread(),
            null
        ));

        return new TicketThreadDTO(
                ticketThread.getTicketThreadId(),
                ticket.getTicketId(),
                ticketThread.getThread(),
                ticketThread.getCreatedAt()
        );
    }

}
