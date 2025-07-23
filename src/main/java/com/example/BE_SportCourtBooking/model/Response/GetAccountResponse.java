package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Enum.Role;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class GetAccountResponse {
    String fullName;
    String email;
    String phone;
    Date dateOfBirth;
    String gender;
    String address;
    Role role;
    String image;
    UUID manageId;
}
