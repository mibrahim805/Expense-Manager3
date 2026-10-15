import hashlib
import hmac
import json
import logging
import secrets
from datetime import timedelta

from django.contrib.auth import authenticate, get_user_model
from django.contrib.auth.forms import PasswordResetForm, SetPasswordForm
from django.contrib.auth.tokens import default_token_generator
from django.http import JsonResponse
from django.utils import timezone
from django.utils.encoding import force_str
from django.utils.http import urlsafe_base64_decode
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_GET, require_POST

from .forms import RegistrationForm
from .models import ApiAuthToken

logger = logging.getLogger(__name__)
TOKEN_LIFETIME = timedelta(days=30)


def _json_body(request):
    try:
        payload = json.loads(request.body or b"{}")
    except (json.JSONDecodeError, UnicodeDecodeError):
        return None
    return payload if isinstance(payload, dict) else None


def _token_digest(raw_token):
    return hashlib.sha256(raw_token.encode("utf-8")).hexdigest()


def _issue_token(user):
    raw_token = secrets.token_urlsafe(32)
    ApiAuthToken.objects.create(
        user=user,
        digest=_token_digest(raw_token),
        expires_at=timezone.now() + TOKEN_LIFETIME,
    )
    return raw_token


def _authenticated_token(request):
    scheme, _, raw_token = request.headers.get("Authorization", "").partition(" ")
    if scheme.lower() != "bearer" or not raw_token:
        return None
    token = ApiAuthToken.objects.select_related("user").filter(
        digest=_token_digest(raw_token), revoked_at__isnull=True, expires_at__gt=timezone.now(),
    ).first()
    if token is None or not token.user.is_active:
        return None
    if not hmac.compare_digest(token.digest, _token_digest(raw_token)):
        return None
    return token


def _user_payload(user):
    return {"id": user.pk, "username": user.get_username(), "email": user.email}


@csrf_exempt
@require_POST
def register(request):
    payload = _json_body(request)
    if payload is None:
        return JsonResponse({"error": "Send a valid JSON request."}, status=400)
    email = str(payload.get("email", "")).strip()
    form = RegistrationForm({
        "username": str(payload.get("username", "")).strip(),
        "email": email,
        "password1": payload.get("password", ""),
        "password2": payload.get("password_confirmation", ""),
    })
    if not form.is_valid():
        errors = {field: [str(error) for error in messages] for field, messages in form.errors.items()}
        return JsonResponse({"error": "Check the information and try again.", "fields": errors}, status=400)
    user = form.save()
    return JsonResponse({"token": _issue_token(user), "user": _user_payload(user)}, status=201)


@csrf_exempt
@require_POST
def login(request):
    payload = _json_body(request)
    if payload is None:
        return JsonResponse({"error": "Send a valid JSON request."}, status=400)
    identifier = str(payload.get("username_or_email", "")).strip()
    password = str(payload.get("password", ""))
    username = identifier
    if "@" in identifier:
        matching_users = get_user_model().objects.filter(email__iexact=identifier)
        if matching_users.count() == 1:
            username = matching_users.first().get_username()
    user = authenticate(request, username=username, password=password)
    if user is None:
        return JsonResponse({"error": "The username/email or password is incorrect."}, status=401)
    return JsonResponse({"token": _issue_token(user), "user": _user_payload(user)})


@require_GET
def current_user(request):
    token = _authenticated_token(request)
    if token is None:
        return JsonResponse({"error": "Your session has expired. Please log in again."}, status=401)
    return JsonResponse({"user": _user_payload(token.user)})


@csrf_exempt
@require_POST
def logout(request):
    token = _authenticated_token(request)
    if token is None:
        return JsonResponse({"error": "Your session has expired. Please log in again."}, status=401)
    token.revoked_at = timezone.now()
    token.save(update_fields=["revoked_at"])
    return JsonResponse({"message": "You have been logged out."})


@csrf_exempt
@require_POST
def request_password_reset(request):
    payload = _json_body(request)
    if payload is None:
        return JsonResponse({"error": "Send a valid JSON request."}, status=400)
    email = str(payload.get("email", "")).strip()
    form = PasswordResetForm({"email": email})
    if form.is_valid():
        try:
            form.save(
                request=request,
                use_https=request.is_secure(),
                email_template_name="registration/password_reset_mobile_email.txt",
                subject_template_name="registration/password_reset_subject.txt",
                html_email_template_name=None,
                extra_email_context={"app_name": "RupeyaWise"},
            )
        except Exception:
            logger.exception("Password reset email could not be sent")
    # Avoid exposing whether an email address is registered.
    return JsonResponse({"message": "If an account uses that email, reset instructions have been sent."})


@csrf_exempt
@require_POST
def reset_password(request):
    payload = _json_body(request)
    if payload is None:
        return JsonResponse({"error": "Send a valid JSON request."}, status=400)
    try:
        uid = force_str(urlsafe_base64_decode(payload.get("uid", "")))
        user = get_user_model().objects.get(pk=uid, is_active=True)
    except (TypeError, ValueError, OverflowError, get_user_model().DoesNotExist):
        return JsonResponse({"error": "This reset link is invalid or has expired."}, status=400)
    token = str(payload.get("token", ""))
    if not default_token_generator.check_token(user, token):
        return JsonResponse({"error": "This reset link is invalid or has expired."}, status=400)
    form = SetPasswordForm(user, {
        "new_password1": payload.get("password", ""),
        "new_password2": payload.get("password_confirmation", ""),
    })
    if not form.is_valid():
        errors = {field: [str(error) for error in messages] for field, messages in form.errors.items()}
        return JsonResponse({"error": "Choose a valid password.", "fields": errors}, status=400)
    form.save()
    ApiAuthToken.objects.filter(user=user, revoked_at__isnull=True).update(revoked_at=timezone.now())
    return JsonResponse({"message": "Your password has been reset. You can now log in."})
