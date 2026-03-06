# Quickstart: Employee Sales Metrics Dashboard

## Backend (Spring Boot + JPA)

1. Ensure PostgreSQL is running and app properties point to it.
2. Start the backend:

```bash
cd server
./gradlew bootRun
```

3. Authenticate as an employee user to obtain a JWT.
4. Call the metrics endpoint:

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/employee/sales-metrics
```

## Frontend (Angular)

1. Install dependencies and start dev server:

```bash
cd client
npm install
npm start
```

2. Log in as an employee, then navigate to the employee sales dashboard route (e.g., `/employee/dashboard`).
3. Verify:
   - Summary metrics show total sales count and revenue.
   - Customers list shows customers you’ve assisted.
   - Sales table shows track, customer, date, and billed amount.
