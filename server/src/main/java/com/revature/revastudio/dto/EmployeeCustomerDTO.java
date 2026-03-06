package com.revature.revastudio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCustomerDTO {
    private Integer customerId;
    private String customerName;
    private Integer totalSales;
}
