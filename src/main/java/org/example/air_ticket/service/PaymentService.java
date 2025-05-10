package org.example.air_ticket.service;

import lombok.RequiredArgsConstructor;
import org.example.air_ticket.dto.request.PaymentRequest;
import org.example.air_ticket.dto.response.PaymentResponse;
import org.example.air_ticket.enums.PaymentStatus;
import org.example.air_ticket.enums.UserRole;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(payment -> modelMapper.map(payment , PaymentResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, Long userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));


        // Check if the ticket belongs to the user
        if (!ticket.getUser().getId().equals(userId)) {
            throw new RuntimeException("This ticket does not belong to you.");
        }

        // Check if payment already exists for this ticket
        if (paymentRepository.findByTicketId(ticket.getId()).isPresent()) {
            throw new RuntimeException("Payment for this ticket is already available.");
        }

        Payment payment = Payment.builder()
                .ticket(ticket)
                .cardNumber(request.getCardNumber())
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.PENDING)
                .amount(ticket.getTotalPrice())
                .user(user)
                .build();

        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        // Validate that the provided status is one of the allowed values
        if (status != PaymentStatus.PENDING &&
                status != PaymentStatus.CONFIRMED &&
                status != PaymentStatus.REJECTED &&
                status != PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Invalid payment status: " + status);
        }

        // Status-specific validations
        if (status == PaymentStatus.CONFIRMED && payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be approved.");
        }

        if (status == PaymentStatus.REJECTED && payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be declined.");
        }

        if (status == PaymentStatus.REFUNDED && payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed payments can be refunded.");
        }

        payment.setStatus(status);
        payment.setConfirmedBy(currentUser);
        payment.setConfirmationDate(LocalDateTime.now());

        // Handle seat availability changes for rejections and refunds
        if (status == PaymentStatus.REJECTED || status == PaymentStatus.REFUNDED) {
            Flight flight = payment.getTicket().getFlight();
            flight.setAvailableSeats(flight.getAvailableSeats() + 1);
            flightRepository.save(flight);
        }

        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = modelMapper.map(payment, PaymentResponse.class);

        // Mask card number for security (only show last 4 digits)
        String cardNumber = payment.getCardNumber();
        String maskedCardNumber = "xxxx-xxxx-xxxx-" + cardNumber.substring(cardNumber.length() - 4);
        response.setCardNumber(maskedCardNumber);

        // Map username from confirmedBy object
        if (payment.getConfirmedBy() != null) {
            response.setConfirmedByUsername(payment.getConfirmedBy().getUsername());
        }

        return response;
    }
}
