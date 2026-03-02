from django import forms
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.auth.models import User
from django.contrib.auth.forms import UserCreationForm
from django import forms
from .models import Product, Expense


class RegistrationForm(UserCreationForm):
    email=forms.EmailField(widget=forms.EmailInput(attrs={"class":"form-control","placeholder":"Enter email"}))

    class Meta:
        model=User
        fields=["username","email","password1","password2"]

    def __init__(self,*args,**kwargs):
        super().__init__(*args,**kwargs)
        self.fields["username"].widget.attrs.update({"class":"form-control","placeholder":"Enter username"})
        self.fields["password1"].widget.attrs.update({"class":"form-control","placeholder":"Enter password"})
        self.fields["password2"].widget.attrs.update({"class":"form-control","placeholder":"Confirm password"})

class LoginForm(AuthenticationForm):
    username=forms.CharField(widget=forms.TextInput(attrs={"class":"form-control","placeholder":"Enter username"}))
    password=forms.CharField(widget=forms.PasswordInput(attrs={"class":"form-control","placeholder":"Enter password"}))


class ProductForm(forms.ModelForm):
    class Meta:
        model=Product
        fields=["name","description","price"]

class ExpenseForm(forms.ModelForm):
    class Meta:
        model=Expense
        fields=["product","quantity","date"]










    #
    # def __init__(self,*args,**kwargs):
    #     user=kwargs.pop("user",None)
    #     super().__init__(*args,**kwargs)
        # if user and hasattr(Product,"user"):
        #     self.fields["product"].queryset=Product.objects.filter(user=user)
        # else:
        #     self.fields["product"].queryset=Product.objects.all()


