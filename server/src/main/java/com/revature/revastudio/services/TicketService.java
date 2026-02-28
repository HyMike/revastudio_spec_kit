package com.revature.revastudio.services;

import com.revature.revastudio.dto.TicketResponseDTO;
import com.revature.revastudio.dto.TicketThreadDTO;
import com.revature.revastudio.entity.Ticket;
import com.revature.revastudio.entity.TicketThread;
import com.revature.revastudio.entity.User;
import com.revature.revastudio.repositories.TicketRepository;
import com.revature.revastudio.repositories.TicketThreadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class TicketService {

    private final TicketThreadRepository ticketThreadRepository;
    private final TicketRepository ticketRepository;

    public TicketService(
        TicketThreadRepository ticketThreadRepository,
        TicketRepository ticketRepository
    ) {
        this.ticketThreadRepository = ticketThreadRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<TicketResponseDTO> getTicketsByCustomer(UUID userId) {

        List<Ticket> tickets = this.ticketRepository.findByCustomer_User_Id(userId);
        List<TicketResponseDTO> ticketDTO = tickets.stream().map(this::toTicketResponseDTO).toList();
        return ticketDTO;
    }


    public List<TicketResponseDTO> getTicketsByEmployee(UUID userId) {

        List<Ticket> tickets = this.ticketRepository.findByEmployee_User_Id(userId);
        List<TicketResponseDTO> ticketDTO = tickets.stream()
        .map(this::toTicketResponseDTO)
        .toList();
        return ticketDTO;

    }

    private TicketResponseDTO toTicketResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getTicketId(),
                ticket.getSubject(),
                ticket.getBody(),
                ticket.getCreatedAt(),
                ticket.getStatus(),
                ticket.getCustomer() != null ? ticket.getCustomer().getCustomerId() : null,
                ticket.getEmployee() != null ? ticket.getEmployee().getEmployeeId() : null
        );
    }

//    public List<> getThread(Integer userId){
//
//
//
//    }

    public TicketThreadDTO addThreadMessage(Integer ticketId, String thread) {

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                ()-> new IllegalArgumentException("There is no Ticket for this query")
        );
        LocalDateTime currentTime = LocalDateTime.now();
        TicketThread ticketThread = this.ticketThreadRepository.save(
            new TicketThread(
                    null,
                    ticket,
                    thread,
                    currentTime
            )
        );
        TicketThreadDTO ticketThreadDTO = new TicketThreadDTO(
                ticketThread.getTicketThreadId(),
                ticketId,
                thread,
                currentTime
        );
        return ticketThreadDTO;
    }


}
