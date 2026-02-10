package com.cfss.BMSP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.transform.sax.SAXResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Long id;
    private String title;
    private String description;
    private  String language;
    private String genre;
    private  String releaseDate;
    private String posterUrl;
    private Integer durationMins;


}
