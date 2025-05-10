package org.example.air_ticket.repository;

import org.example.air_ticket.dto.response.FlightResponse;
import org.example.air_ticket.enums.PaymentStatus;
import org.example.air_ticket.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTicketId(Long ticketId);
    List<Payment> findByUserId(Long userId);
}