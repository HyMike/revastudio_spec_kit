export interface EmployeeSalesSummary {
  totalSalesCount: number;
  totalRevenue: number;
}

export interface EmployeeCustomerMetric {
  customerId: number;
  customerName: string;
  totalSales: number;
}

export interface EmployeeSaleDetail {
  invoiceId: number;
  invoiceDate: string;
  customerName: string;
  trackName: string;
  billedAmount: number;
}

export interface EmployeeSalesMetrics {
  summary: EmployeeSalesSummary;
  customers: EmployeeCustomerMetric[];
  sales: EmployeeSaleDetail[];
}
