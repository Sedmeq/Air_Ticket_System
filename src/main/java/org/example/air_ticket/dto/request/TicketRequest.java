package org.example.air_ticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.air_ticket.enums.SeatType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    @NotNull(message = "Flight ID cannot be null")
    private Long flightId;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @NotBlank(message = "Passenger surname cannot be blank")
    private String passengerSurname;

    @NotBlank(message = "Passport number cannot be blank")
    private String passportNumber;

    @NotNull(message = "Seat type cannot be null")
    private SeatType seatType;

    @NotBlank(message = "Seat number cannot be blank")
    private String seatNumber;
}