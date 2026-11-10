# Security

How Waddle handles authentication, authorization and data protection, plus its known limits.

## Passwords
- Hashed with **BCrypt** before saving. The database never holds a plain password.

## Authentication (JWT)
- On sign-in the server returns a **JWT** signed with HMAC-SHA (secret key from the `JWT_SECRET` environment variable).
- The token's subject (`sub`) is the **user id**, not the email.
- Tokens expire after `JWT_EXPIRATION` milliseconds. An expired or tampered token fails signature or expiry checks and the request is treated as anonymous.
- The API is **stateless**: no server sessions, and every request carries `Authorization: Bearer <token>`.
- `TokenFilter` validates the token and stores the user id for the request. Controllers get it through `@AuthenticationPrincipal`, so the user id is never taken from the request body.

## Authorization
- `/waddle/auth/**`, the UI files, Swagger and `/error` are public. Everything else needs a valid token, and is answered with **401** without one.
- `/waddle/admin/**` is guarded by a custom `AuthorizationManager` (`RoleAuth`). It looks up the user's role in the database on every admin request, so removing an admin role takes effect right away. Non-admins get **403**.

## Input validation
- Request bodies are validated with Jakarta Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`). Bad input gets **400** before any logic runs.
- Guesses must be 5 uppercase letters **and** exist in the word list.
- All database access goes through JPA or parameterized queries, so no user input is ever concatenated into SQL (protects against SQL injection).


## Known limitations
- No refresh tokens or token revocation: a token stays valid until it expires.
- No rate limiting on sign-in.
