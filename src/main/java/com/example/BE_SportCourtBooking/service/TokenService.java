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
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private AccountRepository accountRepository;

    public final String SECRET_KEY = "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407cnt"; //tạo secretkey chỉ lưu ở back-end để bảo mật
    private SecretKey getSigninKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    //Tạo ra token
    public String generateToken(Account account) { // 1account~ 1token khac nhau
        String token = Jwts.builder()
                .subject(account.getId()+ "")//save unique(id ,..) information from user to token
                .issuedAt(new Date(System.currentTimeMillis()))//10:30
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))//1 day ( 1000 : 1s ) * 60 = 1min
                //1 day ( 1000 : 1s ) * 60 * 60 = 1h
                // 1 day ( 1000 : 1s ) * 60 *  60  * 24 = 1day
                .signWith(getSigninKey())
                .compact();
        return token;
    }


    //Verify cái token
    public Account getAccountByToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigninKey())
                .build().parseSignedClaims(token)
                .getPayload();
        String idString = claims.getSubject();
        UUID id = UUID.fromString(idString); // convert to UUID
        Account account = accountRepository.findAccountById(id);
        return account;
    }
}
