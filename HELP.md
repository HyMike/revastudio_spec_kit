# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.2/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.2/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.2/reference/web/servlet.html)
* [Validation](https://docs.spring.io/spring-boot/4.0.2/reference/io/validation.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.2/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

## Local setup notes
This project expects runtime configuration via environment variables. See `CONTRIBUTING.md` for details. Important variables:

- `JWT_SECRET` — JWT signing secret
- `DATABASE_URL` — JDBC URL for PostgreSQL (e.g. `jdbc:postgresql://localhost:5432/revastudio`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Quick commands:

```bash
# Start backend
cd server && ./gradlew bootRun

# Start frontend
cd client && npm install && npm start

# Run server tests
cd server && ./gradlew test

# Run client tests
cd client && npm run test
```

See `CONTRIBUTING.md` for full contribution and PR guidance.

