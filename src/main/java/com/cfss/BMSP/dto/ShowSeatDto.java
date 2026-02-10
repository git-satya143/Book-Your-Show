package com.cfss.BMSP.dto;

import com.cfss.BMSP.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatDto {
    private Long id;
    private SeatDto seat;
    private String status;
    private  Double price;



}
