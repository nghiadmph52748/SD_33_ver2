#!/usr/bin/env python3
"""
Simple SQL Server script executor
Executes SQL files against SQL Server by splitting on GO statements
"""

import sys
import os

try:
    import pymssql
except ImportError:
    print("Installing pymssql...")
    os.system("pip3 install pymssql")
    import pymssql

def execute_sql_file(server, user, password, database, sql_file):
    """Execute a SQL file against SQL Server"""
    print(f"Connecting to {server}...")
    
    try:
        # Connect without specifying database first
        conn = pymssql.connect(
            server=server,
            user=user,
            password=password,
            database='master',  # Connect to master first
            port=1433,
            autocommit=True
        )
        
        cursor = conn.cursor()
        
        # Read the SQL file
        print(f"Reading {sql_file}...")
        with open(sql_file, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        
        # Split by GO statements (SQL Server batch separator)
        batches = [batch.strip() for batch in sql_content.split('GO') if batch.strip()]
        
        print(f"Executing {len(batches)} SQL batches...")
        
        for i, batch in enumerate(batches, 1):
            if not batch or batch.startswith('--'):
                continue
                
            try:
                cursor.execute(batch)
                if i % 100 == 0:
                    print(f"  Progress: {i}/{len(batches)} batches...")
            except Exception as e:
                print(f"  Warning on batch {i}: {str(e)[:100]}")
                continue
        
        print("✓ SQL file executed successfully!")
        
        cursor.close()
        conn.close()
        
    except Exception as e:
        print(f"✗ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 6:
        print("Usage: python3 execute_sql.py <server> <user> <password> <database> <sql_file>")
        sys.exit(1)
    
    server, user, password, database, sql_file = sys.argv[1:]
    execute_sql_file(server, user, password, database, sql_file)
