# Data Model: Employee Sales Metrics Dashboard

## Entities

### Employee
- **Source**: Existing `Employee` entity in `server/src/main/java/com/revature/revastudio/entity/Employee.java`
- **Key fields**:
  - `employeeId` (PK)
  - `firstName`, `lastName`, `title`
- **Relationships**:
  - `customers`: `OneToMany` → `Customer.supportRep`

### Customer
- **Source**: Existing `Customer` entity
- **Key fields**:
  - `customerId` (PK)
  - `firstName`, `lastName`
- **Relationships**:
  - `supportRep`: `ManyToOne` → `Employee`
  - `invoices`: `OneToMany` → `Invoice.customer`

### Invoice
- **Source**: Existing `Invoice` entity
- **Key fields**:
  - `invoiceId` (PK)
  - `invoiceDate`
  - `total`
- **Relationships**:
  - `customer`: `ManyToOne` → `Customer`
  - `invoiceLines`: `OneToMany` → `InvoiceLine.invoice`

### InvoiceLine
- **Source**: Existing `InvoiceLine` entity
- **Key fields**:
  - `invoiceLineId` (PK)
  - `unitPrice`
  - `quantity`
- **Relationships**:
  - `invoice`: `ManyToOne` → `Invoice`
  - `track`: `ManyToOne` → `Track`

### Track
- **Source**: Existing `Track` entity
- **Key fields**:
  - `trackId` (PK)
  - `name`
  - `unitPrice`
- **Relationships**:
  - `album`: `ManyToOne` → `Album`
  - `invoiceLines`: `OneToMany` → `InvoiceLine.track`

## Metrics DTOs (implemented)

### EmployeeSalesSummaryDTO
**Location**: `server/src/main/java/com/revature/revastudio/dto/EmployeeSalesSummaryDTO.java`
- `totalSalesCount: int` – total number of invoices for the employee
- `totalRevenue: BigDecimal` – aggregate revenue across all invoices

### EmployeeCustomerDTO
**Location**: `server/src/main/java/com/revature/revastudio/dto/EmployeeCustomerDTO.java`
- `customerId: Integer`
- `customerName: String` – concatenated first + last name
- `totalSales: Integer` – how many invoices this customer has

### EmployeeSaleDetailDTO
**Location**: `server/src/main/java/com/revature/revastudio/dto/EmployeeSaleDetailDTO.java`
- `invoiceId: Integer`
- `invoiceDate: String` – ISO format (YYYY-MM-DD)
- `invoiceTotal: BigDecimal`

### EmployeeSalesMetricsDTO
**Location**: `server/src/main/java/com/revature/revastudio/dto/EmployeeSalesMetricsDTO.java`
- `summary: EmployeeSalesSummaryDTO` – aggregate metrics
- `customers: List<EmployeeCustomerDTO>` – customers the employee has served
- `sales: List<EmployeeSaleDetailDTO>` – detailed invoice list

## Implementation Notes

### Repository Extension
- `InvoiceRepository` (existing) extended with: `List<Invoice> findByCustomer_SupportRep_EmployeeId(Integer employeeId)`
- Uses Spring Data JPA derived query to safely scope invoices to the employee's customers

## Validation & Constraints

- Only sales where `Customer.supportRep` matches the authenticated employee must be included.
- Null totals or prices should be treated as zero when aggregating.
- If the authenticated user has no associated `Employee` entity, return empty metrics (zero count, zero revenue).
- Data access must go through JPA repositories (no raw SQL in controllers).
- Customer name is computed by concatenating `firstName + ' ' + lastName` at the DTO layer.
