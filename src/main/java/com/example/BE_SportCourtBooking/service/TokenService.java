package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private AccountRepository accountRepository;

    private static final String SECRET_KEY = "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407cnt";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", account.getEmail());
        claims.put("fullName", account.getFullName());
        claims.put("role", account.getRole().toString());

        return Jwts.builder()
                .claims(claims)
                .subject(account.getId().toString()) // dùng ID để tìm lại account
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h
                .signWith(getSigningKey())
                .compact();
    }

    public Account getAccountByToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String idString = claims.getSubject();
        UUID id = UUID.fromString(idString);

        return accountRepository.findAccountById(id);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
