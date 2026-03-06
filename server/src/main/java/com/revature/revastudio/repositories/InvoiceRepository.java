package com.revature.revastudio.repositories;

import com.revature.revastudio.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

	// All invoices for customers whose support representative has the given employeeId
	List<Invoice> findByCustomer_SupportRep_EmployeeId(Integer employeeId);
}
