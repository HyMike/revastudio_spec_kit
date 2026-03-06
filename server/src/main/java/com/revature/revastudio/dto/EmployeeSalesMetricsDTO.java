package com.revature.revastudio.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalesMetricsDTO {
    private EmployeeSalesSummaryDTO summary;
    private List<EmployeeCustomerDTO> customers;
    private List<EmployeeSaleDetailDTO> sales;
}
