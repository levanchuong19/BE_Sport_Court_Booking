package com.example.BE_SportCourtBooking.model.Response;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    String fullName;
    String email;
    String phone;
    Role role;
    String token;
}