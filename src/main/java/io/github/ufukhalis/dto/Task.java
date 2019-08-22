package io.github.ufukhalis.dto;

import lombok.Data;

import java.util.Date;

@Data
public class Task {

    private String name;
    private String command;
    private Date startDate;
    private Integer period;

}
