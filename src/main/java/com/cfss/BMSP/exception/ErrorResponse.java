package com.cfss.BMSP.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.SqlReturnType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ErrorResponse {
    private Date timeStamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
