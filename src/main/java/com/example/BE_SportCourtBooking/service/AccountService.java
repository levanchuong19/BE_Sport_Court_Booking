package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.exception.AccountNotFoundException;
import com.example.BE_SportCourtBooking.exception.DuplicateEntity;
import com.example.BE_SportCourtBooking.model.Request.NewAccountRequest;
import com.example.BE_SportCourtBooking.model.Request.UpdateAccountRequest;
import com.example.BE_SportCourtBooking.model.Request.UpdateAccountStatusRequest;
import com.example.BE_SportCourtBooking.model.Response.GetAccountResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    @Lazy
    PasswordEncoder passwordEncoder;

    ModelMapper modelMapper = new ModelMapper();

    public Account createAccount(NewAccountRequest newAccountRequest, UUID managerId) {
        Account account = modelMapper.map(newAccountRequest, Account.class);

        try {
            account.setPassword(passwordEncoder.encode(newAccountRequest.getPassword()));
            if (newAccountRequest.getRole() == Role.STAFF) {
                if (managerId == null) {
                    throw new IllegalArgumentException("Manager ID is required for STAFF accounts.");
                }

                Account managerAccount = accountRepository.findAccountById(managerId);
                if (managerAccount == null || managerAccount.getRole() != Role.MANAGER) {
                    throw new IllegalArgumentException("Manager ID must be a valid manager account.");
                }

                account.setManagerId(managerId);
            }

            account.setRole(newAccountRequest.getRole());
            return accountRepository.save(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DuplicateEntity("Duplicate account id!");
        }
    }

    public Account updateAccount(UpdateAccountRequest updateAccountRequest, UUID id) {
        Account oldAccount = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("This account does not exist!"));

        oldAccount.setFullName(updateAccountRequest.getFullName());
        oldAccount.setDateOfBirth(updateAccountRequest.getDateOfBirth());
        oldAccount.setGender(updateAccountRequest.getGender());
        oldAccount.setAddress(updateAccountRequest.getAddress());
        oldAccount.setImage(updateAccountRequest.getImage());
        oldAccount.setPhone(updateAccountRequest.getPhoneNumber());
        System.out.println(updateAccountRequest);
        return accountRepository.save(oldAccount);
    }

    public List<GetAccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<GetAccountResponse> mapAccoutList = new ArrayList<>();

        for (Account account : accounts) {
            GetAccountResponse getAccountResponse = new GetAccountResponse();
            getAccountResponse.setFullName(account.getFullName());
            getAccountResponse.setEmail(account.getEmail());
            getAccountResponse.setPhone(account.getPhone());
            getAccountResponse.setGender(account.getGender());
            getAccountResponse.setAddress(account.getAddress());
            getAccountResponse.setRole(account.getRole());
            getAccountResponse.setImage(account.getImage());
            getAccountResponse.setDateOfBirth(account.getDateOfBirth());

            mapAccoutList.add(getAccountResponse);
        }

        return mapAccoutList;
    }

    public Account deleteAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("This account does not exist!"));

        account.setIsDelete(true);

        return accountRepository.save(account);
    }

    public Account getCurrentAccount() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Principal (username): " + principal); // DEBUG
        return accountRepository.findAccountByEmail(principal)
                .orElseThrow(() -> new AccountNotFoundException("Account không tồn tại"));
    }

    public Account getAccount(UUID id) {
        Account account = accountRepository.findAccountById(id);
        if (account == null) throw new AccountNotFoundException("Account không tồn tại");
        return account;
    }

    public List<Map<String, Object>> getAllManagers() {
        List<Account> managerAccounts = accountRepository.findAllByRole(Role.MANAGER);

        return managerAccounts.stream()
                .map(account -> {
                    Map<String, Object> managerObject = new HashMap<>();

                    managerObject.put("id", account.getId());
                    managerObject.put("fullName", account.getFullName());

                    return managerObject;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAccountStatus(UUID id, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        account.setIsDelete(request.isDeleted());
        accountRepository.save(account);
    }
}
