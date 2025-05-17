package com.example.BE_SportCourtBooking.model.Response;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    String fullName;
    String email;
    String phone;
    Date dateOfBirth;
    String gender;
    String address;
    Role role;
    String token;

}