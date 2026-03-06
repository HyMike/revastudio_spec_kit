package com.revature.revastudio.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSaleDetailDTO {
    private Integer invoiceId;
    private String invoiceDate;
    private String customerName;
    private String trackName;
    private BigDecimal billedAmount;
}
