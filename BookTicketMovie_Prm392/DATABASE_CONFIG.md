# Database Configuration Guide

This project uses secure environment-based database configuration to protect sensitive credentials.

## Setup Instructions

### 1. Local Development

The database credentials are stored in `local.properties` which is **NOT committed to git** for security.

Your `local.properties` file should look like this:

```properties
# SDK Location (auto-generated)
sdk.dir=C\\:\\Users\\ADMIN\\AppData\\Local\\Android\\Sdk

# Database Configuration (Keep these secret!)
DB_HOST=your-database-host
DB_HOST_FALLBACK=10.0.2.2
DB_PORT=1433
DB_NAME=MovieTicketBookingSystem
DB_USERNAME=your-username
DB_PASSWORD=your-password
```

### 2. Team Setup

When a new team member joins:

1. Copy `local.properties.example` to `local.properties`
2. Update the database credentials with your local/development database info
3. Never commit `local.properties` to version control

### 3. Production Deployment

For production builds, set these environment variables:

```bash
PROD_DB_HOST=your-production-host
PROD_DB_USERNAME=your-production-username
PROD_DB_PASSWORD=your-production-password
```

### 4. CI/CD Setup

In your CI/CD pipeline (GitHub Actions, etc.), set these as secrets:

- `DB_HOST`
- `DB_USERNAME`
- `DB_PASSWORD`
- `PROD_DB_HOST`
- `PROD_DB_USERNAME`
- `PROD_DB_PASSWORD`

## Security Features

✅ **Credentials not in source code** - All sensitive data is in `local.properties` or environment variables

✅ **Git ignored** - `local.properties` is in `.gitignore`

✅ **Build-time injection** - Credentials are injected into `BuildConfig` at compile time

✅ **Environment fallback** - Can use environment variables if `local.properties` is not available

✅ **Production override** - Different credentials for release builds

## Files Involved

- `local.properties` - Local development credentials (git ignored)
- `app/build.gradle.kts` - Reads properties and injects into BuildConfig
- `DatabaseConfig.java` - Uses BuildConfig values instead of hardcoded strings
- `.gitignore` - Ensures sensitive files are not committed

## Troubleshooting

### Error: "BuildConfig.DB_HOST cannot be resolved"

Make sure you have:

1. Added your database config to `local.properties`
2. Added `buildFeatures { buildConfig = true }` to your `build.gradle.kts`
3. Synced/rebuilt the project

### Error: "Database connection failed"

Check that:

1. Your database server is running
2. The host/port in `local.properties` is correct
3. Your username/password are correct
4. Firewall allows connections on the database port
