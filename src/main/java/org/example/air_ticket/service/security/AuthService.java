package org.example.air_ticket.service.security;

import org.example.air_ticket.dto.security.AuthResponse;
import org.example.air_ticket.dto.security.LoginDto;
import org.example.air_ticket.dto.security.RegisterDto;

public interface AuthService {
    AuthResponse register(RegisterDto request);
    AuthResponse login(LoginDto request);
}