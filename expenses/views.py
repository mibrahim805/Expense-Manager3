from django.contrib import messages, auth
from django.contrib.auth.decorators import login_required
from django.contrib.auth.forms import AuthenticationForm
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib.auth.models import User
from django.contrib.auth.views import LoginView
from django.db.models import Sum
from django.shortcuts import render, redirect, get_object_or_404
from django.urls import reverse_lazy
from django.views.generic import CreateView, ListView
from django.views.generic import UpdateView,DeleteView
from expenses.forms import ExpenseForm, RegistrationForm, ProductForm
from expenses.models import Expense, Product


class RegisterUserView(CreateView):
    model = User
    form_class = RegistrationForm
    template_name = "registration/register.html"
    success_url = reverse_lazy("expenses:expense_list")

    def form_valid(self, form):
        user = form.save(commit=False)
        user.save()
        messages.success(self.request, f"Account created for {user.username}! You are now able to login.")
        return super().form_valid(form)

    def form_invalid(self, form):
        messages.error(self.request, "Error creating account.")
        return super().form_invalid(form)



class UserLoginView(LoginView):
    template_name="registration/login.html"
    authentication_form=AuthenticationForm
    redirect_authenticated_user=True
    next_page=reverse_lazy("expenses:expense_list")



class ExpenseCreateView(LoginRequiredMixin,CreateView):
    model=Expense
    form_class=ExpenseForm
    template_name="expenses/add_expense.html"
    success_url=reverse_lazy("expenses:expense_list")

    def form_valid(self,form):
        form.instance.user=self.request.user
        return super().form_valid(form)

class ExpenseUpdateView(LoginRequiredMixin,UpdateView):
    model=Expense
    form_class=ExpenseForm
    template_name="expenses/add_expense.html"
    success_url=reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Expense.objects.filter(user=self.request.user)


class ExpenseListView(LoginRequiredMixin,ListView):
    model=Expense
    template_name="expenses/expense_list.html"
    context_object_name="expenses"
    ordering=["-date"]

    def get_queryset(self):
        queryset=super().get_queryset().filter(user=self.request.user)
        start_date=self.request.GET.get("start_date")
        end_date=self.request.GET.get("end_date")
        if start_date and end_date:
            queryset=queryset.filter(date__range=[start_date,end_date])
        return queryset

    def get_context_data(self,**kwargs):
        context=super().get_context_data(**kwargs)
        expenses=context["expenses"]
        context["total"]=expenses.aggregate(Sum("total_amount"))["total_amount__sum"] or 0
        context["start_date"]=self.request.GET.get("start_date")
        context["end_date"]=self.request.GET.get("end_date")
        context["name"] = self.request.GET.get("name")
        return context

class DeleteExpenseView(LoginRequiredMixin,DeleteView):
    model=Expense
    template_name = "expenses/delete_confirm.html"
    success_url=reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Expense.objects.filter(user=self.request.user)

# @login_required
# def delete_expense(request, id):
#     expense = get_object_or_404(Expense, id=id, user=request.user)
#     if expense and request.method == "POST":
#         expense.delete()
#         return redirect("expenses:expense_list")
#     return render(request, "expenses/delete_confirm.html", {"expense": expense})


class ProductCreateView(LoginRequiredMixin,CreateView):
    model=Product
    form_class = ProductForm
    template_name = "products/add_product.html"
    success_url=reverse_lazy("expenses:expense_list")

    def form_valid(self,form):
        form.instance.user=self.request.user
        return super().form_valid(form)
    def form_invalid(self,form):
        messages.error(self.request, "Error creating product.")

class ProductUpdateView(LoginRequiredMixin,UpdateView):
    model=Product
    form_class = ProductForm
    template_name = "products/add_product.html"
    success_url=reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Product.objects.filter(user=self.request.user)

class ProductDeleteView(LoginRequiredMixin,DeleteView):
    model=Product
    template_name = "products/delete_product.html"
    success_url=reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Product.objects.filter(user=self.request.user)