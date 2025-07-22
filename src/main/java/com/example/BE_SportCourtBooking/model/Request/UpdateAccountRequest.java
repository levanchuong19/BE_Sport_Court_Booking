package com.example.BE_SportCourtBooking.model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    String fullName;
    Date dateOfBirth;
    String phone;
    String gender;
    String address;
    String image;
}
