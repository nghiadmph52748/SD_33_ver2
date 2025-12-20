-- Create GearUp database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'GearUp')
BEGIN
    CREATE DATABASE GearUp;
    PRINT 'Database GearUp created successfully.';
END
ELSE
BEGIN
    PRINT 'Database GearUp already exists.';
END
GO

-- Switch to GearUp database
USE GearUp;
GO

PRINT 'Successfully switched to GearUp database.';
GO
