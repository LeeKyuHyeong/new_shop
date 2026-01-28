# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build (tests are skipped by default)
mvn clean package -DskipTests

# Run development server (port 8081)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run production profile (port 8080)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Docker deployment
docker-compose up -d
```

## Technology Stack

- **Java 17** with Spring Boot 3.4.1
- **Database**: MariaDB with Spring Data JPA
- **View**: JSP with JSTL (WAR packaging)
- **Build**: Maven (wrapper included: `mvnw`)

## Architecture

### Layered Structure
```
Controller → Service → Repository → Entity
```

### Package Organization (`com.kh.shop`)
- `controller/admin/` - Admin dashboard endpoints
- `controller/client/` - Customer-facing endpoints
- `controller/common/` - Shared endpoints
- `service/` - Business logic (16 services)
- `repository/` - Spring Data JPA repositories (24)
- `entity/` - JPA entities (24)
- `scheduler/` - Batch schedulers (23)
- `config/` - Configuration beans
- `util/` - Utilities (ProfanityFilter)
- `common/dto/` - PageRequestDTO/PageResponseDTO for pagination

### Views (`src/main/webapp/WEB-INF/views/`)
- `/admin/` - Admin dashboard views
- `/client/` - Customer storefront views
- `/common/` - Shared components

### Static Resources (`src/main/resources/static/`)
- `css/admin/`, `css/client/`, `css/common/` - Stylesheets by area
- `js/admin/`, `js/client/`, `js/common/` - JavaScript by area
- CSS uses custom properties (variables) for theming with dark mode support (`body.dark-mode`)

### Key Entities
User, Product, ProductImage, Category, Order, OrderItem, Cart, Review, Wishlist, Point, Coupon, SocialAccount, DailyStats

## Configuration Profiles

- `application.properties` - Default settings
- `application-dev.properties` - Development (port 8081, local MariaDB)
- `application-prod.properties` - Production (port 8080, Docker)
- `application-secret.properties` - API keys and credentials (gitignored)

## External Integrations

- **Payment**: Portone (KG이니시스)
- **OAuth**: Kakao, Naver, Google
- **AI**: Google Gemini API for image generation

## Batch Scheduling

23 scheduled batch jobs handle background processing:
- Product management (rankings, image generation)
- Order processing (status updates, auto-cancellation)
- User management (dormant users)
- Cleanup (carts, temp files, sessions, logs)
- Alerts (low stock, reviews, wishlist price changes)
- Stats aggregation

## Deployment

CI/CD via GitHub Actions (`.github/workflows/deploy.yml`):
1. Maven build with JDK 17
2. Docker image build and push to Docker Hub
3. SSH deploy to server via docker-compose

## Security Features

- **CSRF Protection**: Token-based with `CsrfFilter` (excludes `/login`, `/signup`, webhooks)
- **XSS Prevention**: Input sanitization via `XssSanitizer`, Content Security Policy headers
- **Duplicate Login Prevention**: `SessionRegistry` tracks active sessions per user; new login terminates existing session with notification
- **Security Headers**: Configured in `security-headers.jsp` (X-Frame-Options, X-Content-Type-Options, etc.)

## Notes

- UI is in Korean (한국어)
- Session-based authentication with `loggedInUser` session attribute
- Review system requires purchase with DELIVERED order status before writing
