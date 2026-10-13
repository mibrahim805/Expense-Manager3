from decimal import Decimal

from django.contrib import messages
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib.auth.models import User
from django.db.models import Count, Sum
from django.db.models.functions import Coalesce
from django.urls import reverse_lazy
from django.views.generic import CreateView, DeleteView, ListView, TemplateView, UpdateView

from .forms import ExpenseForm, RegistrationForm
from .models import Expense


class RegisterUserView(CreateView):
    model = User
    form_class = RegistrationForm
    template_name = "registration/register.html"
    success_url = reverse_lazy("expenses:dashboard")


class ExpenseCreateView(LoginRequiredMixin, CreateView):
    model = Expense
    form_class = ExpenseForm
    template_name = "expenses/add_expense.html"
    success_url = reverse_lazy("expenses:expense_list")

    def form_valid(self, form):
        form.instance.user = self.request.user
        return super().form_valid(form)

    def form_invalid(self, form):
        messages.error(self.request, "Please correct the errors below.")
        return super().form_invalid(form)


class ExpenseUpdateView(LoginRequiredMixin, UpdateView):
    model = Expense
    form_class = ExpenseForm
    template_name = "expenses/add_expense.html"
    success_url = reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Expense.objects.filter(user=self.request.user)


class ExpenseListView(LoginRequiredMixin, ListView):
    model = Expense
    template_name = "expenses/expense_list.html"
    context_object_name = "expenses"

    def get_queryset(self):
        queryset = Expense.objects.filter(user=self.request.user)
        start_date = self.request.GET.get("start_date")
        end_date = self.request.GET.get("end_date")
        category = self.request.GET.get("category")
        if start_date:
            queryset = queryset.filter(date__gte=start_date)
        if end_date:
            queryset = queryset.filter(date__lte=end_date)
        if category:
            queryset = queryset.filter(category=category)
        return queryset

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        queryset = self.get_queryset()
        context.update(
            start_date=self.request.GET.get("start_date", ""),
            end_date=self.request.GET.get("end_date", ""),
            selected_category=self.request.GET.get("category", ""),
            categories=Expense.CATEGORY_CHOICES,
            total=queryset.aggregate(
                total=Coalesce(Sum("amount"), Decimal("0.00"))
            )["total"],
        )
        return context


class ExpenseDeleteView(LoginRequiredMixin, DeleteView):
    model = Expense
    template_name = "expenses/delete_confirm.html"
    context_object_name = "expense"
    success_url = reverse_lazy("expenses:expense_list")

    def get_queryset(self):
        return Expense.objects.filter(user=self.request.user)


class DashboardView(LoginRequiredMixin, TemplateView):
    template_name = "expenses/dashboard.html"

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        user_expenses = Expense.objects.filter(user=self.request.user)
        context["total_expenses"] = user_expenses.aggregate(
            total=Coalesce(Sum("amount"), Decimal("0.00"))
        )["total"]
        context["expense_count"] = user_expenses.count()
        context["category_totals"] = user_expenses.values(
            "category"
        ).annotate(total=Sum("amount"), count=Count("id")).order_by("-total")
        context["recent_expenses"] = user_expenses[:5]
        return context
