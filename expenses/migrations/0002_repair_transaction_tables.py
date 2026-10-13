from django.db import migrations


def repair_existing_database(apps, schema_editor):
    """Bring databases created by the old Expense model up to the current state."""
    connection = schema_editor.connection
    existing_tables = set(connection.introspection.table_names())

    Product = apps.get_model("expenses", "Product")
    Purchase = apps.get_model("expenses", "Purchase")
    Sale = apps.get_model("expenses", "Sale")

    if "expenses_product" in existing_tables:
        product_columns = {
            column.name
            for column in connection.introspection.get_table_description(
                connection.cursor(), "expenses_product"
            )
        }
        if "stock" not in product_columns:
            schema_editor.add_field(Product, Product._meta.get_field("stock"))

    if "expenses_purchase" not in existing_tables:
        schema_editor.create_model(Purchase)
        existing_tables.add("expenses_purchase")

    if "expenses_sale" not in existing_tables:
        schema_editor.create_model(Sale)

    if "expenses_expense" in existing_tables:
        schema_editor.execute(
            """
            INSERT INTO expenses_purchase
                (id, quantity, date, total_amount, created_at, product_id, user_id)
            SELECT e.id, e.quantity, e.date, e.total_amount, e.created_at,
                   e.product_id, e.user_id
            FROM expenses_expense AS e
            WHERE NOT EXISTS (
                SELECT 1 FROM expenses_purchase AS p WHERE p.id = e.id
            )
            """
        )


class Migration(migrations.Migration):
    dependencies = [
        ("expenses", "0001_initial"),
    ]

    operations = [
        migrations.RunPython(repair_existing_database, migrations.RunPython.noop),
    ]
