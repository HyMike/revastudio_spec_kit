package com.revature.revastudio.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revature.revastudio.dto.EmployeeSalesMetricsDTO;
import com.revature.revastudio.services.EmployeeMetricsService;
import com.revature.revastudio.util.RetrieveUser;

import java.util.UUID;

@RestController
@RequestMapping("/employee")
public class EmployeeMetricsController {

    private final EmployeeMetricsService employeeMetricsService;
    private final RetrieveUser retrieveUser;

    public EmployeeMetricsController(EmployeeMetricsService employeeMetricsService, RetrieveUser retrieveUser) {
        this.employeeMetricsService = employeeMetricsService;
        this.retrieveUser = retrieveUser;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/sales-metrics")
    public ResponseEntity<EmployeeSalesMetricsDTO> getSalesMetricsForCurrentUser() {
        UUID userId = retrieveUser.getUser();
        EmployeeSalesMetricsDTO metrics = employeeMetricsService.getMetricsForUser(userId);
        return ResponseEntity.ok(metrics);
    }
}
