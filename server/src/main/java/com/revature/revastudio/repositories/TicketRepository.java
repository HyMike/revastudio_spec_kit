package com.revature.revastudio.repositories;

import com.revature.revastudio.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByCustomer_User_Id(UUID userId);
    List<Ticket> findByEmployee_User_Id(UUID userId);
}
