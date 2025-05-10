package org.example.air_ticket.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.air_ticket.enums.PaymentStatus;
import org.example.air_ticket.enums.SeatType;
import org.example.air_ticket.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponse {
    private Long id;
    private FlightResponse flight;
    private String passengerName;
    private String passengerSurname;
    private String passportNumber;
    private SeatType seatType;
    private String seatNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm:ss", example = "2025-01-01 12:00:00")
    private LocalDateTime creationDate;
    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;

    public void setPayment(Payment payment) {
        if (payment != null) {
            this.paymentStatus = payment.getStatus();
        }
    }
}