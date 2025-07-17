package com.example.BE_SportCourtBooking.model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {

    String location;
    Double latitude;
    Double longitude;
}
