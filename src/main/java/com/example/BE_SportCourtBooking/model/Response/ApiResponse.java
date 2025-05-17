package com.example.BE_SportCourtBooking.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    public int code ;
    public boolean status ;
    public String message ;
    public Object data ;
}
