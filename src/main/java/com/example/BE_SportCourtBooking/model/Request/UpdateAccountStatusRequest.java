package com.example.BE_SportCourtBooking.model.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountStatusRequest {
    private boolean isDeleted;
}