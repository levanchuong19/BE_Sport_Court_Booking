package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.exception.AccountNotFoundException;
import com.example.BE_SportCourtBooking.exception.DuplicateEntity;
import com.example.BE_SportCourtBooking.model.Request.*;
import com.example.BE_SportCourtBooking.model.Response.*;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.*;

@Service
public class AuthenticationService implements UserDetailsService {

    @Value("${google.client-id}")
    private String googleClientId;

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

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void initGoogleVerifier() {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public AccountResponse register(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateEntity("Email already exists");
        }

        if (accountRepository.existsByPhone(registerRequest.getPhone())) {
            throw new DuplicateEntity("Phone number already exists");
        }

        Account account = modelMapper.map(registerRequest, Account.class);
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        account.setFullName(registerRequest.getFullName());
        account.setRole(Role.CUSTOMER);

        accountRepository.save(account);

        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setAccount(account);
        emailDetail.setSubject("Chào mừng bạn đến với SportZone");
        emailDetail.setLink("http://localhost:5173/");
        emailService.sendEmail(emailDetail);

        return modelMapper.map(account, AccountResponse.class);
    }


    public AccountResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getPhone(),
                    loginRequest.getPassword()
            ));
            Account account = (Account) authentication.getPrincipal();
            AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
            accountResponse.setToken(tokenService.generateToken(account));
            return accountResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EntityNotFoundException("Username or password is incorrect");
        }
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

    public Account getCurrentAccount(){
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findAccountById(account.getId());
    }

    public Account getAccount(UUID id){
        Account account = accountRepository.findAccountById(id);
        if (account == null) throw new AccountNotFoundException("Account không tồn tại");
        return account;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByPhone(phone);
        if (account == null) {
            throw new EntityNotFoundException("User not found");
        }
        return account;
    }

    // send email, set new password for user
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        String newPassword = generateRandomPassword(8);
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        emailService.sendEmailForgotPassword(account, newPassword);

        ForgotPasswordResponse response = new ForgotPasswordResponse();
        response.setMessage("A new password has been sent to your email.");
        return response;
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        return new ChangePasswordResponse("Password changed successfully");
    }

    public GoogleLoginResponse authenticateWithGoogle(GoogleLoginRequest request) {
        try {
            System.out.println("Frontend token: " + request.getToken());
            GoogleIdToken idToken = verifier.verify(request.getToken());
            if (idToken == null) {
                System.out.println("Token invalid or audience mismatch");
                return null;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            Account account = accountRepository.findAccountByEmail(email).orElse(null);

            if (account == null) {
                String randomPassword = generateRandomPassword(12);
                account = new Account();
                account.setEmail(email);
                account.setFullName((String) payload.get("name"));
                account.setPassword(passwordEncoder.encode(randomPassword));
                account.setRole(Role.CUSTOMER);
                account.setIsDelete(false);
                accountRepository.save(account);
            }

            account = accountRepository.findAccountByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found after save"));

            // Gán user vào context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    account, null, account.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (Boolean.TRUE.equals(account.getIsDelete())) {
                throw new RuntimeException("Account is marked as deleted");
            }

            String token = tokenService.generateToken(account);

            return GoogleLoginResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .account(account)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Google login authentication failed", e);
        }
    }


    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
