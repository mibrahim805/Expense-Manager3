from django.contrib.auth import views as auth_views
from django.utils import timezone

from .models import ApiAuthToken


class PasswordResetConfirmView(auth_views.PasswordResetConfirmView):
    def form_valid(self, form):
        response = super().form_valid(form)
        ApiAuthToken.objects.filter(user=self.user, revoked_at__isnull=True).update(revoked_at=timezone.now())
        return response
