# Contributing to RevaStudio

Thanks for contributing! This document explains how to set up a development environment, run tests, and open quality pull requests.

## Table of Contents
- Getting started
- Environment variables
- Running the app
- Tests
- Branching & PRs
- Code style & linting
- Security & secrets

## Getting started
1. Clone the repo.
2. Backend requires Java 21 and Gradle; frontend requires Node 18+ and npm.

## Environment variables
Provide runtime secrets via environment variables. Example names used in the repo:

- `JWT_SECRET` — secret for signing JWT tokens
- `DATABASE_URL` — JDBC URL for PostgreSQL (e.g. `jdbc:postgresql://host:5432/dbname`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Do NOT commit secrets into the repository. For local development, use a `.env` file with your tool of choice (gitignored) or export variables in your shell.

## Running the app
Backend (from repo root):

```
cd server
./gradlew bootRun
```

Frontend:

```
cd client
npm install
npm start
```

## Tests
- Server: `./gradlew test` (run from `server`)
- Client: `npm run test` (run from `client`)

PRs must include tests for new behavior or a clear explanation if tests are not applicable.

## Branching & PRs
- Branch naming: `feature/<short-desc>` or `fix/<short-desc>`.
- Open a PR against `main` (or as directed by release branches). At least 1 reviewer is required.
- PR checklist: description, linked issue (if applicable), tests passing, no committed secrets.

## Code style & linting
- Client: Prettier is configured in `client/package.json`; run `npm run format` where available.
- Server: Follow a Java formatter (Spotless/Checkstyle recommended). CI will enforce formatting checks.

## Security
- Never commit credentials or secrets. If you accidentally commit secrets, contact the maintainers immediately and rotate the secret.

## Questions
Open an issue or contact the module owners listed in the Constitution.

Thanks — maintainers
