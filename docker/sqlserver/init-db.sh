#!/bin/bash

# SQL Server Initialization Script for Docker Container
# This script runs when the container first starts

set -e

echo "==================================="
echo "SQL Server Initialization Starting"
echo "==================================="

# Wait for SQL Server to be ready
echo "Waiting for SQL Server to start..."
sleep 30s

# Check if SQL Server is ready
echo "Checking SQL Server status..."
for i in {1..50}; do
    /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" -b -o /dev/null
    if [ $? -eq 0 ]; then
        echo "SQL Server is ready!"
        break
    else
        echo "Waiting for SQL Server... attempt $i/50"
        sleep 3s
    fi
done

# Check if initialization is needed
INIT_FLAG_FILE="/var/opt/mssql/.initialized"

if [ -f "$INIT_FLAG_FILE" ]; then
    echo "Database already initialized. Skipping initialization scripts."
    exit 0
fi

echo "Running initialization scripts..."

# Run all SQL scripts in the init directory
for sql_file in /docker-entrypoint-initdb.d/*.sql; do
    if [ -f "$sql_file" ]; then
        echo "Executing: $sql_file"
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -i "$sql_file"
        
        if [ $? -eq 0 ]; then
            echo "✓ Successfully executed: $sql_file"
        else
            echo "✗ Failed to execute: $sql_file"
            exit 1
        fi
    fi
done

# Mark initialization as complete
touch "$INIT_FLAG_FILE"
echo "Database initialization completed successfully!"
echo "==================================="
