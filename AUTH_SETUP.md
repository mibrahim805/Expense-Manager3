# RupeyaWise authentication and deployment

The Django API serves Android authentication. The Android app's transactions and budgets still remain on-device; they are not synced to this server.

## Local development

Run Django from the project root:

```sh
.venv/bin/python manage.py migrate
.venv/bin/python manage.py runserver 0.0.0.0:8000
```

The Android debug build defaults to `http://10.0.2.2:8000/` for an Android emulator. For a USB-connected physical device, use ADB reverse and loopback:

```sh
adb reverse tcp:8000 tcp:8000
./gradlew :androidApp:installDebug -PrupeyawiseApiBaseUrl=http://127.0.0.1:8000/
```

## Kubeletto + Neon deployment

The repository now includes a Dockerfile for the Django service. Kubeletto's free service scales to zero, so keep all durable account data in Neon Postgres; do not use a container-local SQLite file in production.

1. Create a Neon project and database. Copy its PostgreSQL connection string (prefer the pooled connection string for an app service). Keep it private.
2. Create a Kubeletto free account and connect the RupeyaWise GitHub repository, deploying the `main` branch from the repository root. The root `Dockerfile` starts Gunicorn, applies Django migrations, serves static files, and listens on Kubeletto's `PORT`.
3. Set these environment variables on the Kubeletto service. Replace the hostname with the generated service hostname, without `https://` in `DJANGO_ALLOWED_HOSTS`.

   - `DJANGO_DEBUG=False`
   - `DJANGO_SECRET_KEY=<new-long-random-secret>`
   - `DATABASE_URL=<Neon PostgreSQL connection string>`
   - `DJANGO_ALLOWED_HOSTS=<service-name>.kubeletto.app`
   - `DJANGO_CSRF_TRUSTED_ORIGINS=https://<service-name>.kubeletto.app`
   - `DJANGO_SECURE_HSTS_SECONDS=86400` (the production default)

4. Configure real password-reset email delivery. The local default prints messages to the console; it is not suitable for deployed users. Set `DJANGO_EMAIL_BACKEND=django.core.mail.backends.smtp.EmailBackend`, `DJANGO_DEFAULT_FROM_EMAIL`, `DJANGO_EMAIL_HOST`, `DJANGO_EMAIL_PORT`, `DJANGO_EMAIL_HOST_USER`, `DJANGO_EMAIL_HOST_PASSWORD`, and `DJANGO_EMAIL_USE_TLS` using a transactional-email provider's SMTP credentials.
5. After Kubeletto deploys, verify the service URL responds over HTTPS. Build/install the Android app with that URL:

   ```sh
   ./gradlew :androidApp:installDebug -PrupeyawiseApiBaseUrl=https://<service-name>.kubeletto.app/
   ```

The production Android release must also be built with the deployed HTTPS API URL. Never put `DATABASE_URL`, Django secrets, email passwords, or OAuth secrets in Gradle properties committed to Git or in the Android app.

### Free-tier considerations

Kubeletto's free service can cold-start after idle periods and its platform is in beta. Neon Free currently lists 1 GB of database storage and 100 compute-hours per project per month; the database suspends compute when idle. Free-tier terms and quotas can change, so keep a database export/backup and be prepared to move the API or database.

## Credentials and production security

Google and GitHub OAuth client credentials are read from `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET`, `GITHUB_OAUTH_CLIENT_ID`, and `GITHUB_OAUTH_CLIENT_SECRET`. Configure them as provider-side secrets only if those social-login options are needed.

OAuth client secrets were previously committed in this repository. Revoke/rotate those credentials in the Google and GitHub developer consoles before deployment; removing them from the current settings file does not erase them from Git history.
