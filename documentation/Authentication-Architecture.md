# Cook Lion Authentication Architecture Documentation

## Overview

The Cook Lion application implements a hybrid authentication architecture that combines **OAuth2/OpenID Connect** with **Keycloak** for user authentication and **JWT tokens** for API access. This dual approach provides both web-based authentication flows and stateless API authentication.

## Architecture Components

### 1. Identity Provider - Keycloak
- **Realm**: `cooklion` realm configured with OpenID Connect protocol
- **Client Configuration**: `cooklion` client with authorization code flow enabled
- **User Management**: Centralized user store with role-based access control
- **Default Users**: Pre-configured admin and test users for development

### 2. Authentication Flows

#### Web Authentication Flow (OAuth2)
1. **Initiation**: User accesses protected web resource
2. **Redirect**: Application redirects to Keycloak login page
3. **Authentication**: User authenticates with Keycloak
4. **Authorization Code**: Keycloak redirects back with authorization code
5. **Token Exchange**: Application exchanges code for access/ID tokens
6. **User Creation**: Custom OAuth2 service processes user info and creates/updates local user account
7. **Session**: Web session established with Spring Security

#### API Authentication Flow (JWT)
1. **Token Generation**: JWT service generates access tokens for API clients
2. **Request Authentication**: JWT filter validates JWT tokens in API requests
3. **Authorization**: Role-based access control enforced on API endpoints

### 3. User Management
- **Local Storage**: User accounts stored in PostgreSQL with audit fields
- **Role Assignment**: Automatic role assignment based on email configuration
- **Unique Constraints**: Email and username uniqueness enforced
- **Last Login Tracking**: User activity monitoring

## Security Configuration

### Dual Security Filter Chains

#### API Security (`/api/**`)
- **Stateless**: Session-free authentication using JWT tokens
- **CORS Enabled**: Cross-origin requests supported for frontend integration
- **Role-based Authorization**: Different access levels for public, user, and admin endpoints
- **Custom Error Handling**: JSON-formatted authentication/authorization errors

#### Web Security (All other paths)
- **Session-based**: Traditional web application security with sessions
- **OAuth2 Login**: Integration with Keycloak for user authentication
- **Static Resource Access**: Public access to CSS, JS, and image files
- **Logout Handling**: Proper session invalidation and cleanup

### Role-Based Access Control
- **USER**: Standard access to authenticated API endpoints
- **ADMIN**: Full system access including administrative endpoints and DELETE operations
- **Dynamic Assignment**: Admin roles assigned based on configured email addresses

## Key Features

### Security Headers
- HTTP Strict Transport Security (HSTS) with 1-year max age
- Referrer Policy for cross-origin requests
- Proper CORS configuration for API endpoints

### Token Management
- **Access Tokens**: Short-lived (15 minutes) for API access
- **Configurable Expiration**: Token lifetime configurable via properties
- **Secure Signing**: HMAC-SHA256 signature with configurable secret

### Error Handling
- **Graceful Degradation**: Invalid tokens don't break the application flow
- **Comprehensive Logging**: Authentication events logged for monitoring
- **Client-Friendly Errors**: Clear error messages for different failure scenarios

## Development Setup

The system is designed for easy local development with:
- **Docker Compose**: Keycloak and PostgreSQL services orchestrated
- **Auto-Import**: Keycloak realm configuration automatically imported
- **Default Credentials**: Pre-configured test users for immediate testing
- **Health Checks**: Database connectivity verification

## Configuration

### Key Configuration Properties

#### JWT Configuration
