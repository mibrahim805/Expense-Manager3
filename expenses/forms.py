from django.contrib.auth.models import User
from django.contrib.auth.forms import UserCreationForm
from django import forms
from .models import Product, Expense


class RegistrationForm(UserCreationForm):
    class Meta:
        model=User
        fields=["username" ,"email","password1","password2"]


class ProductForm(forms.ModelForm):
    class Meta:
        model=Product
        fields=["name","description","price"]


class ExpenseForm(forms.ModelForm):
    class Meta:
        model=Expense
        fields=["product","quantity","date"]
