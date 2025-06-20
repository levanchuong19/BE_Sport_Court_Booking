package com.example.BE_SportCourtBooking.config;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(AccountRepository accountRepository) {
        return args -> {

            if (accountRepository.findAccountByEmail("admin@gmail.com").isEmpty()) {
                Account admin = Account.builder()
                        .fullName("Admin User")
                        .email("admin@gmail.com")
                        .phone("0900000001")
                        .role(Role.ADMIN)
                        .password(passwordEncoder.encode("123456"))
                        .build();
                accountRepository.save(admin);
                log.warn("ADMIN created: 0900000001 / 123456");
            }

            if (accountRepository.findAccountByEmail("staff@gmail.com").isEmpty()) {
                Account staff = Account.builder()
                        .fullName("Staff User")
                        .email("staff@gmail.com")
                        .phone("0900000002")
                        .role(Role.STAFF)
                        .password(passwordEncoder.encode("123456"))
                        .build();
                accountRepository.save(staff);
                log.warn("STAFF created: 0900000002 / 123456");
            }

            if (accountRepository.findAccountByEmail("manager@gmail.com").isEmpty()) {
                Account manager = Account.builder()
                        .fullName("Manager User")
                        .email("manager@gmail.com")
                        .phone("0900000003")
                        .role(Role.MANAGER)
                        .password(passwordEncoder.encode("123456"))
                        .build();
                accountRepository.save(manager);
                log.warn("MANAGER created: 0900000003 / 123456");
            }

        };
    }

}
