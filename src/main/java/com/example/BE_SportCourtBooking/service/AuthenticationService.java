package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.exception.AccountNotFoundException;
import com.example.BE_SportCourtBooking.exception.DuplicateEntity;
import com.example.BE_SportCourtBooking.model.Request.ChangePasswordRequest;
import com.example.BE_SportCourtBooking.model.Request.ForgotPasswordRequest;
import com.example.BE_SportCourtBooking.model.Request.LoginRequest;
import com.example.BE_SportCourtBooking.model.Request.RegisterRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.ChangePasswordResponse;
import com.example.BE_SportCourtBooking.model.Response.EmailDetail;
import com.example.BE_SportCourtBooking.model.Response.ForgotPasswordResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }


    // send email, set new password for user
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findAccountByEmail(request.getEmail());
        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        String newPassword = generateRandomPassword(8);
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        // Truyền thêm newPassword vào hàm gửi email
        emailService.sendEmailForgotPassword(account, newPassword);

        ForgotPasswordResponse response = new ForgotPasswordResponse();
        response.setMessage("A new password has been sent to your email.");
        return response;
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request, UUID accountId) {
        // Lấy account theo ID (hoặc từ security context)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Kiểm tra mật khẩu hiện tại đúng không
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Kiểm tra newPassword và confirmNewPassword giống nhau không
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        // Cập nhật mật khẩu mới
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        return new ChangePasswordResponse("Password changed successfully");
    }
}
