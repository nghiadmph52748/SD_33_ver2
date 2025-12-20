#!/usr/bin/env python3
import pymssql

conn = pymssql.connect(server='localhost', user='sa', password='Manhduy@2005', database='GearUp', port=1433)
cursor = conn.cursor()

tables_to_check = [
    ('nhan_vien', 'Employees'),
    ('khach_hang', 'Customers'),
    ('san_pham', 'Products'),
    ('chi_tiet_san_pham', 'Product Details'),
    ('mau_sac', 'Colors'),
    ('kich_thuoc', 'Sizes'),
    ('nha_san_xuat', 'Manufacturers'),
]

print("=" * 50)
print("DATABASE VERIFICATION")
print("=" * 50)

for table, label in tables_to_check:
    try:
        cursor.execute(f'SELECT COUNT(*) FROM {table}')
        count = cursor.fetchone()[0]
        print(f"✓ {label:20} ({table:20}): {count:4} records")
    except Exception as e:
        print(f"✗ {label:20} ({table:20}): Error - {str(e)[:50]}")

cursor.close()
conn.close()

print("=" * 50)
print("✓ Database is ready and connected!")
print("=" * 50)
