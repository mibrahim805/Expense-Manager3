from django.urls import path

from .views import (
    DashboardView,
    ExpenseCreateView,
    ExpenseDeleteView,
    ExpenseListView,
    ExpenseUpdateView,
    RegisterUserView,
)

app_name = "expenses"

urlpatterns = [
    path("register/", RegisterUserView.as_view(), name="register"),
    path("dashboard/", DashboardView.as_view(), name="dashboard"),
    path("list/", ExpenseListView.as_view(), name="expense_list"),
    path("add/", ExpenseCreateView.as_view(), name="add_expense"),
    path("edit/<int:pk>/", ExpenseUpdateView.as_view(), name="update_expense"),
    path("delete/<int:pk>/", ExpenseDeleteView.as_view(), name="delete_expense"),
]
