from django.contrib.auth import views as auth_views
from django.urls import include, path
from django.contrib import admin
from django.views.generic import RedirectView
urlpatterns = [
    path('', RedirectView.as_view(pattern_name='login', permanent=False)),
    path('login/', auth_views.LoginView.as_view(template_name='registration/login.html'), name='login'),
    path('accounts/logout/', auth_views.LogoutView.as_view(next_page='login'), name='logout'),
    path("admin/", admin.site.urls, name="admin"),
    path('accounts/', include('allauth.urls')),
    path('expenses/', include('expenses.urls')),
]
