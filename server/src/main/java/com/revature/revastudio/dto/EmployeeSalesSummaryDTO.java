package com.revature.revastudio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalesSummaryDTO {
    private int totalSalesCount;
    private BigDecimal totalRevenue;
}
