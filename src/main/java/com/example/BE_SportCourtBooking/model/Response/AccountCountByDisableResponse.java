package com.example.BE_SportCourtBooking.model.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCountByDisableResponse {
    private boolean isDelete;
    private Long countNum;
}
