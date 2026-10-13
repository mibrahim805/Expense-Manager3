from datetime import date
from decimal import Decimal

from django.contrib.auth.models import User
from django.test import TestCase
from django.urls import reverse

from .models import Expense


class ExpenseWebFlowTests(TestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            username="owner",
            email="owner@example.com",
            password="strong-test-password",
        )
        self.other_user = User.objects.create_user(
            username="other",
            email="other@example.com",
            password="strong-test-password",
        )
        self.expense = Expense.objects.create(
            category="food",
            description="Lunch",
            amount=Decimal("25.50"),
            date=date(2026, 10, 1),
            user=self.user,
        )
        Expense.objects.create(
            category="transport",
            description="Private expense",
            amount=Decimal("99.00"),
            date=date(2026, 10, 2),
            user=self.other_user,
        )
        self.client.force_login(self.user)

    def test_dashboard_is_scoped_to_user_and_renders_totals(self):
        response = self.client.get(reverse("expenses:dashboard"))

        self.assertEqual(response.status_code, 200)
        self.assertContains(response, "Total Expenses")
        self.assertContains(response, "25.50")
        self.assertContains(response, "Food")
        self.assertNotContains(response, "Private expense")

    def test_expense_create_flow_sets_logged_in_user(self):
        response = self.client.post(
            reverse("expenses:add_expense"),
            {
                "category": "transport",
                "description": "Bus fare",
                "amount": "10.00",
                "date": "2026-10-05",
            },
        )

        self.assertRedirects(response, reverse("expenses:expense_list"))
        expense = Expense.objects.get(description="Bus fare")
        self.assertEqual(expense.user, self.user)
        self.assertEqual(expense.amount, Decimal("10.00"))

    def test_expense_list_filters_by_category_and_date(self):
        Expense.objects.create(
            category="shopping",
            description="Notebook",
            amount=Decimal("8.00"),
            date=date(2026, 10, 5),
            user=self.user,
        )

        response = self.client.get(
            reverse("expenses:expense_list"),
            {"category": "shopping", "start_date": "2026-10-05"},
        )

        self.assertEqual(response.status_code, 200)
        self.assertContains(response, "Notebook")
        self.assertNotContains(response, "Lunch")

    def test_logged_out_users_are_redirected(self):
        self.client.logout()
        response = self.client.get(reverse("expenses:dashboard"))

        self.assertRedirects(
            response,
            f"{reverse('login')}?next={reverse('expenses:dashboard')}",
        )
