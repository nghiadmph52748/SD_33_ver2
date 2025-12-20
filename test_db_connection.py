
import pyodbc
import os
import time

# Try both service name and container name
hosts = ["sqlserver", "gearup-sqlserver"]
database = os.getenv("DB_NAME", "GearUp")
username = os.getenv("DB_USER", "sa")
password = os.getenv("DB_PASSWORD", "Manhduy@2005")
driver = "ODBC Driver 18 for SQL Server"

print(f"Driver: {driver}")

for server in hosts:
    print(f"\n--- Testing connection to {server} ---")
    conn_str = f"DRIVER={{{driver}}};SERVER={server},1433;DATABASE={database};UID={username};PWD={password};TrustServerCertificate=yes;Connection Timeout=10;"
    
    masked_conn_str = conn_str.replace(password, "*****")
    print(f"Connection string: {masked_conn_str}")

    try:
        start = time.time()
        conn = pyodbc.connect(conn_str)
        elapsed = time.time() - start
        print(f"✅ Successfully connected to {server} in {elapsed:.2f}s!")
        
        cursor = conn.cursor()
        cursor.execute("SELECT @@VERSION")
        row = cursor.fetchone()
        print(f"SQL Server Version: {row[0][:50]}...")
        conn.close()
        break 
    except Exception as e:
        elapsed = time.time() - start
        print(f"❌ Connection to {server} failed after {elapsed:.2f}s")
        print(f"Error: {e}")
