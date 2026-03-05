package com.revature.revastudio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketThreadRequestDTO {
    public Integer ticketId;
    public String thread;
}
