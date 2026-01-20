package com.subnex.auth.service;

import com.subnex.auth.dto.LoginRequest;
import com.subnex.auth.dto.RegisterRequest;
import com.subnex.auth.model.User;
import com.subnex.auth.repository.UserRepository;
import com.subnex.auth.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final KafkaProducerService kafkaProducerService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void register(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        kafkaProducerService.publishLoginEvent(user.getId(), user.getEmail());

        return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
    }
}
