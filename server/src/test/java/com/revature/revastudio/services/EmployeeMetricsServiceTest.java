package com.revature.revastudio.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.revastudio.dto.EmployeeSalesMetricsDTO;
import com.revature.revastudio.entity.Customer;
import com.revature.revastudio.entity.Employee;
import com.revature.revastudio.entity.Invoice;
import com.revature.revastudio.entity.InvoiceLine;
import com.revature.revastudio.entity.Track;
import com.revature.revastudio.entity.User;
import com.revature.revastudio.repositories.InvoiceLineRepository;
import com.revature.revastudio.repositories.InvoiceRepository;
import com.revature.revastudio.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeMetricsServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineRepository invoiceLineRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeMetricsService employeeMetricsService;

    @Test
    void getMetricsForUser_returnsAggregatedMetrics() {
        UUID userId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setEmployeeId(99);

        User user = new User();
        user.setEmployee(employee);

        Customer customer = new Customer();
        customer.setCustomerId(10);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");

        Invoice firstInvoice = new Invoice();
        firstInvoice.setInvoiceId(1001);
        firstInvoice.setCustomer(customer);
        firstInvoice.setInvoiceDate(LocalDateTime.of(2026, 3, 5, 10, 15));
        firstInvoice.setTotal(new BigDecimal("12.50"));

        Invoice secondInvoice = new Invoice();
        secondInvoice.setInvoiceId(1002);
        secondInvoice.setCustomer(customer);
        secondInvoice.setInvoiceDate(LocalDateTime.of(2026, 3, 6, 11, 0));
        secondInvoice.setTotal(new BigDecimal("7.50"));

        Track firstTrack = new Track();
        firstTrack.setTrackId(501);
        firstTrack.setName("Track One");

        Track secondTrack = new Track();
        secondTrack.setTrackId(502);
        secondTrack.setName("Track Two");

        InvoiceLine firstLine = new InvoiceLine();
        firstLine.setInvoiceLineId(1);
        firstLine.setInvoice(firstInvoice);
        firstLine.setTrack(firstTrack);
        firstLine.setUnitPrice(new BigDecimal("2.50"));
        firstLine.setQuantity(2);

        InvoiceLine secondLine = new InvoiceLine();
        secondLine.setInvoiceLineId(2);
        secondLine.setInvoice(secondInvoice);
        secondLine.setTrack(secondTrack);
        secondLine.setUnitPrice(new BigDecimal("1.50"));
        secondLine.setQuantity(3);

        firstInvoice.setInvoiceLines(Set.of(firstLine));
        secondInvoice.setInvoiceLines(Set.of(secondLine));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invoiceRepository.findByCustomer_SupportRep_EmployeeId(99)).thenReturn(List.of(firstInvoice, secondInvoice));
        when(invoiceLineRepository.findByEmployeeIdWithTrackDetails(99)).thenReturn(List.of(firstLine, secondLine));

        EmployeeSalesMetricsDTO result = employeeMetricsService.getMetricsForUser(userId);

        assertNotNull(result);
        assertEquals(2, result.getSummary().getTotalSalesCount());
        assertEquals(new BigDecimal("20.00"), result.getSummary().getTotalRevenue());
        assertEquals(1, result.getCustomers().size());
        assertEquals("Jane Doe", result.getCustomers().get(0).getCustomerName());
        assertEquals(2, result.getCustomers().get(0).getTotalSales());
        assertEquals(2, result.getSales().size());
        assertEquals("2026-03-05", result.getSales().get(0).getInvoiceDate());
        assertEquals("Jane Doe", result.getSales().get(0).getCustomerName());
        assertEquals("Track One", result.getSales().get(0).getTrackName());
        assertEquals(new BigDecimal("5.00"), result.getSales().get(0).getBilledAmount());
        verify(invoiceRepository).findByCustomer_SupportRep_EmployeeId(99);
        verify(invoiceLineRepository).findByEmployeeIdWithTrackDetails(99);
    }

    @Test
    void getMetricsForUser_returnsEmptyMetricsWhenUserHasNoEmployee() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        EmployeeSalesMetricsDTO result = employeeMetricsService.getMetricsForUser(userId);

        assertNotNull(result);
        assertEquals(0, result.getSummary().getTotalSalesCount());
        assertEquals(BigDecimal.ZERO, result.getSummary().getTotalRevenue());
        assertEquals(0, result.getCustomers().size());
        assertEquals(0, result.getSales().size());
    }
}
