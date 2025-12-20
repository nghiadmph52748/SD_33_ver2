#!/bin/bash
# Quick fix script to add sp_GenerateMaHoaDon stored procedure

set -e

echo "Adding sp_GenerateMaHoaDon stored procedure to SQL Server..."

# Create SQL script
cat > /tmp/add_sp.sql << 'EOFSP'
USE [GearUp]

IF OBJECT_ID('dbo.sp_GenerateMaHoaDon', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_GenerateMaHoaDon;

CREATE PROCEDURE [dbo].[sp_GenerateMaHoaDon]
    @idHoaDon INT,
    @maMoiGenerated NVARCHAR(10) OUTPUT
AS
BEGIN
    DECLARE @counter INT = 0;
    DECLARE @maThoiGian NVARCHAR(10);
    DECLARE @randomSeed INT;
    
    SELECT @randomSeed = ABS(CHECKSUM(NEWID()));
    
    WHILE @counter < 100
    BEGIN
        SET @maThoiGian = 'HD' + 
            RIGHT('0000000000' + CAST(ABS(CHECKSUM(NEWID())) % 10000000000 AS NVARCHAR(10)), 10);
        
        IF NOT EXISTS(SELECT 1 FROM [dbo].[hoa_don] WHERE [ma_hoa_don] = @maThoiGian)
        BEGIN
            UPDATE [dbo].[hoa_don] 
            SET [ma_hoa_don] = @maThoiGian 
            WHERE [id] = @idHoaDon;
            
            SELECT @maMoiGenerated = [ma_hoa_don] FROM [dbo].[hoa_don] WHERE [id] = @idHoaDon;
            
            RETURN 0;
        END
        
        SET @counter = @counter + 1;
    END
    
    SET @maMoiGenerated = NULL;
    RETURN 1;
END

SELECT OBJECT_ID('dbo.sp_GenerateMaHoaDon', 'P') as StoredProcID;
EOFSP

# Copy to container
docker cp /tmp/add_sp.sql gearup-sqlserver:/tmp/add_sp.sql

# Install sqlcmd tools and execute
docker exec gearup-sqlserver bash -c '
    # Try different sqlcmd locations
    if command -v sqlcmd &> /dev/null; then
        echo "Using sqlcmd from PATH"
        sqlcmd -S localhost -U sa -P "Manhduy@2005" -C -i /tmp/add_sp.sql
    elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
        echo "Using /opt/mssql-tools/bin/sqlcmd"
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "Manhduy@2005" -i /tmp/add_sp.sql
    elif [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
        echo "Using /opt/mssql-tools18/bin/sqlcmd"
        /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Manhduy@2005" -C -i /tmp/add_sp.sql
    else
        echo "ERROR: sqlcmd not found. Installing mssql-tools..."
        export ACCEPT_EULA=Y
        apt-get update > /dev/null 2>&1
        apt-get install -y curl gnupg2 > /dev/null 2>&1
        curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
        curl https://packages.microsoft.com/config/ubuntu/20.04/prod.list | tee /etc/apt/sources.list.d/msprod.list
        apt-get update > /dev/null 2>&1
        apt-get install -y mssql-tools unixodbc-dev > /dev/null 2>&1
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "Manhduy@2005" -i /tmp/add_sp.sql
    fi
'

if [ $? -eq 0 ]; then
    echo "✓ Stored procedure added successfully!"
else
    echo "✗ Failed to add stored procedure"
    exit 1
fi
