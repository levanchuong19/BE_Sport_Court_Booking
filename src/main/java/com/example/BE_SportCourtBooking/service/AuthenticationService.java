package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.exception.DuplicateEntity;
import com.example.BE_SportCourtBooking.model.Request.LoginRequest;
import com.example.BE_SportCourtBooking.model.Request.RegisterRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.EmailDetail;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TokenService tokenService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EmailService emailService;

    public AccountResponse register (RegisterRequest registerRequest){
        Account account = modelMapper.map(registerRequest, Account.class);
        try {
            account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            account.setRole(Role.CUSTOMER);
            accountRepository.save(account);
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(account);
            emailDetail.setSubject("Chào mừng bạn đến với SportZone");
            emailDetail.setLink("http://localhost:5173/");
            emailService.sendEmail(emailDetail);
            return modelMapper.map(account, AccountResponse.class);
        } catch (Exception e) {
            if (e.getMessage().contains(account.getEmail())) {
                throw new DuplicateEntity("Duplicated  email ");
            } else {
                throw new DuplicateEntity("Duplicated  phone ");
            }
        }
    }


    public AccountResponse login (LoginRequest loginRequest){
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getPhone(),
                    loginRequest.getPassword()
            ));
            Account account = (Account)authentication.getPrincipal();
            AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
            accountResponse.setToken(tokenService.generateToken(account));
            return accountResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EntityNotFoundException("Username or password is incorrect");
        }
    }
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByPhone(phone);
        if (account == null) {
            throw new EntityNotFoundException("User not found");
        }
        return account;
    }
}
