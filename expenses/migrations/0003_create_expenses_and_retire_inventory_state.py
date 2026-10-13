from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


def rename_old_expense_table(apps, schema_editor):
    table_names = set(schema_editor.connection.introspection.table_names())
    if "expenses_expense" in table_names:
        schema_editor.execute(
            "ALTER TABLE expenses_expense RENAME TO expenses_legacy_expense"
        )
        # SQLite keeps index names when a table is renamed. These indexes
        # belong to the legacy table and would collide with the new model.
        schema_editor.execute(
            "DROP INDEX IF EXISTS expenses_expense_user_id_ab1aae2b"
        )
        schema_editor.execute(
            "DROP INDEX IF EXISTS expenses_expense_product_id_9b810fa1"
        )


def import_purchase_records(apps, schema_editor):
    table_names = set(schema_editor.connection.introspection.table_names())
    if "expenses_purchase" not in table_names:
        return

    schema_editor.execute(
        """
        INSERT INTO expenses_expense
            (id, user_id, category, description, amount, date, created_at)
        SELECT p.id,
               p.user_id,
               'other',
               COALESCE('Imported purchase: ' || product.name, 'Imported purchase'),
               p.total_amount,
               p.date,
               p.created_at
        FROM expenses_purchase AS p
        LEFT JOIN expenses_product AS product ON product.id = p.product_id
        WHERE NOT EXISTS (
            SELECT 1 FROM expenses_expense AS e WHERE e.id = p.id
        )
        """
    )


class Migration(migrations.Migration):
    dependencies = [
        ("expenses", "0002_repair_transaction_tables"),
    ]

    operations = [
        migrations.RunPython(rename_old_expense_table, migrations.RunPython.noop),
        migrations.CreateModel(
            name="Expense",
            fields=[
                (
                    "id",
                    models.BigAutoField(
                        auto_created=True,
                        primary_key=True,
                        serialize=False,
                        verbose_name="ID",
                    ),
                ),
                (
                    "category",
                    models.CharField(
                        choices=[
                            ("food", "Food"),
                            ("transport", "Transport"),
                            ("bills", "Bills"),
                            ("shopping", "Shopping"),
                            ("health", "Health"),
                            ("education", "Education"),
                            ("entertainment", "Entertainment"),
                            ("other", "Other"),
                        ],
                        max_length=100,
                    ),
                ),
                ("description", models.TextField(blank=True)),
                ("amount", models.DecimalField(decimal_places=2, max_digits=12)),
                ("date", models.DateField()),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                (
                    "user",
                    models.ForeignKey(
                        blank=True,
                        null=True,
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="expenses",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
            options={"ordering": ["-date", "-created_at"]},
        ),
        migrations.RunPython(import_purchase_records, migrations.RunPython.noop),
        migrations.SeparateDatabaseAndState(
            state_operations=[
                migrations.DeleteModel(name="Sale"),
                migrations.DeleteModel(name="Purchase"),
                migrations.DeleteModel(name="Product"),
            ],
            database_operations=[],
        ),
    ]
