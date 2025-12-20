#!/usr/bin/env python3
"""
Add missing stored procedure to GearUp database
"""
import os
import sys

# Try to import pymssql, install if needed
try:
    import pymssql
except ImportError:
    print("pymssql not installed. Please install it first:")
    print("pip3 install pymssql")
    sys.exit(1)

# Database connection details
server = os.getenv('SQL_SERVER', 'localhost')
database = os.getenv('SQL_DATABASE', 'GearUp')
username = os.getenv('SQL_USERNAME', 'sa')
password = os.getenv('SQL_PASSWORD', 'Manhduy@2005')

sql_script = """
USE [GearUp]

IF OBJECT_ID('dbo.sp_GenerateMaHoaDon', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_GenerateMaHoaDon;

"""

sql_create = """
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
"""

try:
    print(f"Connecting to {server}...")
    conn = pymssql.connect(
        server=server,
        user=username,
        password=password,
        database=database,
        port=1433
    )
    
    cursor = conn.cursor()
    
    # Drop existing procedure if it exists
    print("Dropping existing procedure if exists...")
    cursor.execute(sql_script)
    conn.commit()
    
    # Create the stored procedure
    print("Creating stored procedure sp_GenerateMaHoaDon...")
    cursor.execute(sql_create)
    conn.commit()
    
    # Verify it was created
    cursor.execute("SELECT OBJECT_ID('dbo.sp_GenerateMaHoaDon', 'P') AS proc_id")
    result = cursor.fetchone()
    
    if result and result[0]:
        print(f"✓ Stored procedure created successfully! (Object ID: {result[0]})")
        exit_code = 0
    else:
        print("✗ Failed to create stored procedure")
        exit_code = 1
    
    cursor.close()
    conn.close()
    
    sys.exit(exit_code)
    
except Exception as e:
    print(f"✗ Error: {e}")
    sys.exit(1)
