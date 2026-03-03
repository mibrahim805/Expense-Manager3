from django.urls import path
from .views import ExpenseListView, ExpenseCreateView, ExpenseUpdateView, RegisterUserView, DeleteExpenseView, \
    ProductCreateView, ProductListView, ProductDeleteView, ProductUpdateView

# UserLoginView, ProductListView, ProductCreateView, ProductUpdateView, delete_product, ), delete_expense,

app_name="expenses"

urlpatterns=[
    path("list/",ExpenseListView.as_view(),name="expense_list"),
    path("add/",ExpenseCreateView.as_view(),name="add_expense"),
    path('add_product/',ProductCreateView.as_view(),name="add_product"),
    path("edit/<int:pk>/",ExpenseUpdateView.as_view(),name="update_expense"),
    path("delete/<int:pk>/",DeleteExpenseView.as_view(),name="delete_expense"),
    path("register/",RegisterUserView.as_view(),name="register"),
    path("product_list/", ProductListView.as_view(),name="product_list"),
    path("delete/<int:pk>/",ProductDeleteView.as_view(),name="delete_product"),
    path("update/<int:pk>/",ProductUpdateView.as_view(),name="update_product"),
    # path("login/",UserLoginView.as_view(),name="login"),
]

