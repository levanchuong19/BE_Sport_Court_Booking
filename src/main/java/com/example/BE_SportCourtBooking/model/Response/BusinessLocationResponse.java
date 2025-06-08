package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Court;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLocationResponse {
    private String id;
    private String name;
    private String address;
    private String openTime;
    private String closeTime;
    private AccountResponse owner;
    List<CourtResponse> courts;
}
