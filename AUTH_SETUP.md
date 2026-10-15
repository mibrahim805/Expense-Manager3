# RupeyaWise authentication setup

The Android app authenticates against this project's Django server. It uses the same Django user accounts as the website.

## Local development

Run Django from the project root:

```sh
.venv/bin/python manage.py migrate
.venv/bin/python manage.py runserver 0.0.0.0:8000
```

The Android debug build defaults to `http://10.0.2.2:8000/`, which reaches the host machine from an Android emulator. To point the app at another server, pass a Gradle property:

```sh
./gradlew :androidApp:installDebug -PrupeyawiseApiBaseUrl=https://your-domain.example/
```

For a USB-connected physical device during local development, use ADB reverse and loopback:

```sh
adb reverse tcp:8000 tcp:8000
./gradlew :androidApp:installDebug -PrupeyawiseApiBaseUrl=http://127.0.0.1:8000/
```

The debug build permits cleartext HTTP for local development. Release builds disallow cleartext traffic, so provide an HTTPS URL for a release build.

## Password reset email

Local Django settings print reset emails to the server console. Configure SMTP through environment variables for real email delivery:

- `DJANGO_EMAIL_BACKEND` (use `django.core.mail.backends.smtp.EmailBackend`)
- `DJANGO_DEFAULT_FROM_EMAIL`
- `DJANGO_EMAIL_HOST`, `DJANGO_EMAIL_PORT`
- `DJANGO_EMAIL_HOST_USER`, `DJANGO_EMAIL_HOST_PASSWORD`
- `DJANGO_EMAIL_USE_TLS`

Set `DJANGO_SECRET_KEY` to a private value, `DJANGO_DEBUG=False`, and `DJANGO_ALLOWED_HOSTS` to the deployed hostnames in production. Password reset links for Android use the `rupeyawise://auth/reset` deep link.

Authentication protects the app session. Transactions and budgets remain on the device; they are partitioned by Django user ID and are not uploaded or synchronized to the Django website.
