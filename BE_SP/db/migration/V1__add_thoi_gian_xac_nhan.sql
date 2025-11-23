-- Migration: add thoi_gian_xac_nhan to giao_ca
-- Run this on the GearUp database (SQL Server)

IF NOT EXISTS(
  SELECT 1 FROM sys.columns
  WHERE Name = N'thoi_gian_xac_nhan'
    AND Object_ID = Object_ID(N'dbo.giao_ca')
)
BEGIN
  ALTER TABLE dbo.giao_ca
  ADD thoi_gian_xac_nhan DATETIME2 NULL;
END

-- Optionally, you can update existing rows' thoi_gian_xac_nhan here if needed.
