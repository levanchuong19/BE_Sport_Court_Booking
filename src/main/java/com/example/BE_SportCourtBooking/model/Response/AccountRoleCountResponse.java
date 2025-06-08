package com.example.BE_SportCourtBooking.model.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRoleCountResponse {
    private String role;
    private Long countNum;
}
