# Contract: Employee Sales Metrics API

## Endpoint

- **Method**: `GET`
- **Path**: `/api/employee/sales-metrics`
- **Auth**: JWT required, role `EMPLOYEE`

## Request

- Headers:
  - `Authorization: Bearer <token>`
- Query params (future-friendly, optional):
  - `from` (ISO date, optional) – filter metrics from this date
  - `to` (ISO date, optional) – filter metrics up to this date

## Response (200 OK)

```json
{
  "summary": {
    "totalSalesCount": 42,
    "totalRevenue": 1234.56
  },
  "customers": [
    {
      "customerId": 1,
      "customerName": "Jane Doe",
      "totalSales": 5
    }
  ],
  "sales": [
    {
      "invoiceId": 1001,
      "invoiceDate": "2026-03-05",
      "invoiceTotal": 123.45
    }
  ]
}
```

**Status**: US1 (P1) summary metrics implemented. Future user stories (US2, US3) may extend `EmployeeSaleDetailDTO` with track/line-item details.

## Error Responses

- **401 Unauthorized** – missing/invalid JWT.
- **403 Forbidden** – JWT present but role is not `EMPLOYEE`.
- **500 Internal Server Error** – unexpected server failure (logged with correlation ID).
