package org.example.air_ticket.service;


import lombok.RequiredArgsConstructor;
import org.example.air_ticket.dto.request.TicketRequest;
import org.example.air_ticket.dto.response.TicketResponse;
import org.example.air_ticket.enums.PaymentStatus;
import org.example.air_ticket.exception.NotFoundException;
import org.example.air_ticket.model.Flight;
import org.example.air_ticket.model.Payment;
import org.example.air_ticket.model.Ticket;
import org.example.air_ticket.model.User;
import org.example.air_ticket.repository.FlightRepository;
import org.example.air_ticket.repository.PaymentRepository;
import org.example.air_ticket.repository.TicketRepository;
import org.example.air_ticket.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        return modelMapper.map(ticket, TicketResponse.class);
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request, Long userId) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (ticketRepository.existsByPassportNumberAndFlightId(request.getPassportNumber(), request.getFlightId())) {
            throw new RuntimeException("A ticket has already been purchased with this passport number for this flight.");
        }

        if (flight.getAvailableSeats() <= 0) {
            throw new RuntimeException("There are no seats available on this flight.");
        }

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setUser(user);
        ticket.setPassengerName(request.getPassengerName());
        ticket.setPassengerSurname(request.getPassengerSurname());
        ticket.setPassportNumber(request.getPassportNumber());
        ticket.setSeatType(request.getSeatType());
        ticket.setSeatNumber(request.getSeatNumber());
        ticket.setCreationDate(LocalDateTime.now());
        // The calculatePrice method will be automatically called due to @PrePersist

        // Update available seats
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        ticket = ticketRepository.save(ticket);

        return modelMapper.map(ticket, TicketResponse.class);
    }

    @Transactional
    public void cancelTicket(Long id, Long userId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new RuntimeException("This ticket does not belong to you.");
        }

        Optional<Payment> paymentOpt = paymentRepository.findByTicketId(id);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            if (payment.getStatus() == PaymentStatus.CONFIRMED) {
                throw new RuntimeException("A ticket with a confirmed payment cannot be canceled. Please contact the admin for a refund.");
            } else if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.REJECTED);
                paymentRepository.save(payment);
            }
        }

        // Increment available seats on flight
        Flight flight = ticket.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);

        ticketRepository.delete(ticket);
    }
}