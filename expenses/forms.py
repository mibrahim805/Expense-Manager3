from django import forms
from django.contrib.auth.forms import UserCreationForm
from django.contrib.auth.models import User

from .models import Expense


class RegistrationForm(UserCreationForm):
    class Meta:
        model = User
        fields = ["username", "email", "password1", "password2"]

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.fields["email"].required = True

    def clean_email(self):
        email = self.cleaned_data["email"].strip()
        if User.objects.filter(email__iexact=email).exists():
            raise forms.ValidationError("An account with this email already exists.")
        return email


class ExpenseForm(forms.ModelForm):
    class Meta:
        model = Expense
        fields = ["category", "description", "amount", "date"]
        widgets = {
            "description": forms.Textarea(attrs={"rows": 3}),
            "amount": forms.NumberInput(attrs={"step": "0.01", "min": "0.01"}),
            "date": forms.DateInput(attrs={"type": "date"}),
        }
