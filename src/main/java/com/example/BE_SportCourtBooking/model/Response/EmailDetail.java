package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Account;
import lombok.Data;

@Data
public class EmailDetail {
    Account account;
    String subject;
    String link;
}

