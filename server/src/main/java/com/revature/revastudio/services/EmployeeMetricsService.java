package com.revature.revastudio.services;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revature.revastudio.dto.EmployeeCustomerDTO;
import com.revature.revastudio.dto.EmployeeSaleDetailDTO;
import com.revature.revastudio.dto.EmployeeSalesMetricsDTO;
import com.revature.revastudio.dto.EmployeeSalesSummaryDTO;
import com.revature.revastudio.entity.Invoice;
import com.revature.revastudio.entity.InvoiceLine;
import com.revature.revastudio.repositories.InvoiceLineRepository;
import com.revature.revastudio.repositories.InvoiceRepository;
import com.revature.revastudio.repositories.UserRepository;

@Service
public class EmployeeMetricsService {

    private final InvoiceRepository invoiceRepository;
        private final InvoiceLineRepository invoiceLineRepository;
    private final UserRepository userRepository;

        public EmployeeMetricsService(InvoiceRepository invoiceRepository,
                                                                  InvoiceLineRepository invoiceLineRepository,
                                                                  UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
                this.invoiceLineRepository = invoiceLineRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
        public EmployeeSalesMetricsDTO getMetricsForUser(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow();

        if (user.getEmployee() == null) {
            // No associated employee; return an empty metrics object
            return new EmployeeSalesMetricsDTO(
                    new EmployeeSalesSummaryDTO(0, BigDecimal.ZERO),
                    Collections.emptyList(),
                    Collections.emptyList());
        }

        Integer employeeId = user.getEmployee().getEmployeeId();
        List<Invoice> invoices = invoiceRepository.findByCustomer_SupportRep_EmployeeId(employeeId);

        int totalCount = invoices.size();
        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EmployeeSalesSummaryDTO summary = new EmployeeSalesSummaryDTO(totalCount, totalRevenue);

        // Customers assisted
        List<EmployeeCustomerDTO> customers = invoices.stream()
                .map(Invoice::getCustomer)
                .filter(customer -> customer != null)
                .collect(Collectors.groupingBy(c -> c.getCustomerId()))
                .entrySet()
                .stream()
                .map(entry -> {
                    var anyCustomer = entry.getValue().get(0);
                    int salesCount = (int) invoices.stream()
                            .filter(inv -> inv.getCustomer() != null
                                    && inv.getCustomer().getCustomerId().equals(entry.getKey()))
                            .count();
                    return new EmployeeCustomerDTO(
                            anyCustomer.getCustomerId(),
                            anyCustomer.getFirstName() + " " + anyCustomer.getLastName(),
                            salesCount);
                })
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        List<InvoiceLine> invoiceLines = invoiceLineRepository.findByEmployeeIdWithTrackDetails(employeeId);

        List<EmployeeSaleDetailDTO> sales = invoiceLines.stream()
                .map(line -> new EmployeeSaleDetailDTO(
                        line.getInvoice().getInvoiceId(),
                        line.getInvoice().getInvoiceDate() != null ? line.getInvoice().getInvoiceDate().format(formatter) : null,
                        line.getInvoice().getCustomer().getFirstName() + " " + line.getInvoice().getCustomer().getLastName(),
                        line.getTrack().getName(),
                        calculateLineTotal(line)))
                .collect(Collectors.toList());

        return new EmployeeSalesMetricsDTO(summary, customers, sales);
    }

    private BigDecimal calculateLineTotal(InvoiceLine line) {
        BigDecimal unitPrice = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
        int quantity = line.getQuantity() != null ? line.getQuantity() : 0;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
