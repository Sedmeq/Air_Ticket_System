package org.example.air_ticket.repository;


import org.example.air_ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserId(Long userId);
    boolean existsByPassportNumberAndFlightId(String passportNumber, Long flightId);
}