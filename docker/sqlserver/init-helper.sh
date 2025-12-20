#!/bin/bash
# SQL Database Initialization Helper
# This script runs once to initialize the SQL Server database with SQLQuery_new.sql

set -e

echo "==============================================="
echo "SQL Database Initialization Helper Started"
echo "==============================================="

# Wait for SQL Server to be truly ready (not just accepting connections)
echo "Waiting for SQL Server to be ready..."
sleep 15

# Check if already initialized
if [ -f "/var/opt/mssql/.db_schema_initialized" ]; then
    echo "Database schema already initialized. Skipping."
    exit 0
fi

SQL_FILE="/SQLQuery_new.sql"

# Execute the SQL file
if [ -f "$SQL_FILE" ]; then
    echo "Executing $SQL_FILE using sqlcmd..."
    echo "This may take a few minutes..."
    
    # Connect to master database first (GearUp may not exist yet)
    # The SQL script will handle creating and switching to GearUp
    /opt/mssql-tools/bin/sqlcmd \
        -S gearup-sqlserver \
        -U sa \
        -P "$MSSQL_SA_PASSWORD" \
        -d master \
        -i "$SQL_FILE" \
        -I \
        2>&1 | grep -v "Changed database context" || true
    
    RESULT=${PIPESTATUS[0]}
    
    if [ $RESULT -eq 0 ]; then
        echo "✓ Database initialization completed successfully!"
        touch /var/opt/mssql/.db_schema_initialized
        exit 0
    else
        echo "✗ Database initialization failed with exit code: $RESULT"
        exit 1
    fi
else
    echo "✗ SQL file not found: $SQL_FILE"
    exit 1
fi
