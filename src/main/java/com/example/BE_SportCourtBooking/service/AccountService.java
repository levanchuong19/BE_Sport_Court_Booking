package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.exception.AccountNotFoundException;
import com.example.BE_SportCourtBooking.exception.DuplicateEntity;
import com.example.BE_SportCourtBooking.model.Request.NewAccountRequest;
import com.example.BE_SportCourtBooking.model.Request.UpdateAccountRequest;
import com.example.BE_SportCourtBooking.model.Response.GetAccountResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    @Lazy
    PasswordEncoder passwordEncoder;

    ModelMapper modelMapper = new ModelMapper();

    public Account createAccount(NewAccountRequest newAccountRequest) {
        Account account = modelMapper.map(newAccountRequest, Account.class);

        try {
            account.setPassword(passwordEncoder.encode(newAccountRequest.getPassword()));
            return accountRepository.save(account);
        } catch (Exception e) {
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
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findAccountById(account.getId());
    }

    public Account getAccount(UUID id) {
        Account account = accountRepository.findAccountById(id);
        if (account == null) throw new AccountNotFoundException("Account không tồn tại");
        return account;
    }
}
