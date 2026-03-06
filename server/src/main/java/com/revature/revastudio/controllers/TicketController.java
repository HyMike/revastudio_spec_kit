package com.revature.revastudio.controllers;

import com.revature.revastudio.dto.TicketResponseDTO;
import com.revature.revastudio.dto.TicketThreadDTO;
import com.revature.revastudio.services.TicketService;
import com.revature.revastudio.util.RetrieveUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final RetrieveUser retrieveUser;

    public TicketController(
            TicketService ticketService,
            RetrieveUser retrieveUser

    ) {
        this.ticketService = ticketService;
        this.retrieveUser = retrieveUser;
    }


    @PostMapping("{ticketId}/thread")
    public ResponseEntity<TicketThreadDTO> addThreadMessages(@PathVariable Integer ticketId, @RequestBody String thread){
         TicketThreadDTO ticketThreadDTO = this.ticketService.addThreadMessage(ticketId, thread);
         return ResponseEntity.ok(ticketThreadDTO);
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("customer")
    public ResponseEntity<List<TicketResponseDTO>> getCustomerTickets() {
        UUID userId  = retrieveUser.getUser();
        List<TicketResponseDTO> allTickets = this.ticketService.getTicketsByCustomer(userId);
        return ResponseEntity.ok(allTickets);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("employee")
    public ResponseEntity<List<TicketResponseDTO>> getEmployeeTickets() {
        UUID userId = retrieveUser.getUser();
        List<TicketResponseDTO> allTickets = this.ticketService.getTicketsByEmployee(userId);
        return ResponseEntity.ok(allTickets);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("{ticketId}/close")
    public ResponseEntity<TicketResponseDTO> closeTicket(@PathVariable Integer ticketId) {
        UUID userId = retrieveUser.getUser();
        TicketResponseDTO result = this.ticketService.closeTicket(ticketId, userId);
        return ResponseEntity.ok(result);
    }
}


