package com.revature.revastudio.controllers;

import com.revature.revastudio.dto.TicketThreadDTO;
import com.revature.revastudio.dto.TicketThreadRequestDTO;
import com.revature.revastudio.services.TicketThreadService;
import org.apache.coyote.Response;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ticket-threads")
public class TicketThreadController {

    private final TicketThreadService ticketThreadService;

    public TicketThreadController(
            TicketThreadService ticketThreadService
    ) {
        this.ticketThreadService = ticketThreadService;
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    @GetMapping("{ticketId}")
    public ResponseEntity<List<TicketThreadDTO>> getThreadMessages(@PathVariable Integer ticketId) {

        List<TicketThreadDTO> threadMessages = this.ticketThreadService.getThreadMessages(ticketId);

        return ResponseEntity.ok(threadMessages);
    }
    @PreAuthorize("hasAnyRole('CUSTOMER', 'EMPLOYEE')")
    @PostMapping("create")
    public ResponseEntity<TicketThreadDTO> createThread(@RequestBody TicketThreadRequestDTO ticketThreadDTO) {

        TicketThreadDTO createdThread = this.ticketThreadService.createThread(ticketThreadDTO);

        return ResponseEntity.ok(createdThread);
    }





}
