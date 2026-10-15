from django.contrib.auth import views as auth_views
from django.urls import include, path
from django.contrib import admin
from django.views.generic import RedirectView
from expenses.auth_views import PasswordResetConfirmView
from expenses.auth_api import (
    current_user as api_current_user,
    login as api_login,
    logout as api_logout,
    register as api_register,
    request_password_reset as api_request_password_reset,
    reset_password as api_reset_password,
)
urlpatterns = [
    path('', RedirectView.as_view(pattern_name='login', permanent=False)),
    path('login/', auth_views.LoginView.as_view(template_name='registration/login.html'), name='login'),
    path('accounts/logout/', auth_views.LogoutView.as_view(next_page='login'), name='logout'),
    path(
        'password-reset/',
        auth_views.PasswordResetView.as_view(
            template_name='registration/password_reset_form.html',
            email_template_name='registration/password_reset_email.txt',
            subject_template_name='registration/password_reset_subject.txt',
            success_url='/password-reset/done/',
        ),
        name='password_reset',
    ),
    path(
        'password-reset/done/',
        auth_views.PasswordResetDoneView.as_view(template_name='registration/password_reset_done.html'),
        name='password_reset_done',
    ),
    path(
        'password-reset/<uidb64>/<token>/',
        PasswordResetConfirmView.as_view(
            template_name='registration/password_reset_confirm.html',
            success_url='/password-reset/complete/',
        ),
        name='password_reset_confirm',
    ),
    path(
        'password-reset/complete/',
        auth_views.PasswordResetCompleteView.as_view(template_name='registration/password_reset_complete.html'),
        name='password_reset_complete',
    ),
    path('api/auth/register/', api_register, name='api_auth_register'),
    path('api/auth/login/', api_login, name='api_auth_login'),
    path('api/auth/me/', api_current_user, name='api_auth_me'),
    path('api/auth/logout/', api_logout, name='api_auth_logout'),
    path(
        'api/auth/password-reset/',
        api_request_password_reset,
        name='api_auth_password_reset',
    ),
    path(
        'api/auth/password-reset/confirm/',
        api_reset_password,
        name='api_auth_password_reset_confirm',
    ),
    path("admin/", admin.site.urls, name="admin"),
    path('accounts/', include('allauth.urls')),
    path('expenses/', include('expenses.urls')),
]
