-- Add mo_ta (description) column to san_pham table
-- This allows storing rich text HTML descriptions for products

ALTER TABLE [dbo].[san_pham]
ADD [mo_ta] NVARCHAR(MAX) NULL;

-- Add comment for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Product description in HTML format for display on customer-facing pages',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'san_pham',
    @level2type = N'COLUMN', @level2name = N'mo_ta';
