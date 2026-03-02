from django.contrib import admin

# Register your models here.


from expenses.models import Expense, Product


@admin.register(Expense)
class ExpenseAdmin(admin.ModelAdmin):
    list_display = ["id", "product", "quantity", "user", "date", "total_amount", "created_at"]


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ["id", "name", "description", "default_price", "price"]
