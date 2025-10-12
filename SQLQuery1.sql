-- =============================================
-- GearUp Database Setup Script
-- Description: Complete database schema and sample data
-- Compatible with: SQL Server 2016 and later
-- 
-- Recent Updates:
-- - Added phieu_giam_gia_history table for tracking coupon changes
-- - Added dot_giam_gia_history table for tracking promotion campaign changes
-- - Added created_at and updated_at audit columns to phieu_giam_gia and dot_giam_gia
-- - Support for tracking customer and product assignments
-- - Support for employee attribution in history records
-- =============================================

USE [master]
GO

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'GearUp')
BEGIN
    CREATE DATABASE [GearUp]
END
GO
-- Set database options (compatible with most SQL Server versions)
ALTER DATABASE [GearUp] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [GearUp] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [GearUp] SET RECURSIVE_TRIGGERS OFF 
GO
USE [GearUp]
GO

-- =============================================
-- Create Tables
-- =============================================

/****** Object:  Table [dbo].[anh_san_pham] ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[anh_san_pham](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[duong_dan_anh] [nvarchar](255) NOT NULL,
	[ten_anh] [nvarchar](255) NULL,
	[mau_anh] [nvarchar](255) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chat_lieu] ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chat_lieu](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_chat_lieu]  AS ('CL'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_chat_lieu] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chi_tiet_dot_giam_gia]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_dot_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_dot_giam_gia] [int] NOT NULL,
	[id_chi_tiet_san_pham] [int] NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chi_tiet_phieu_giam_gia]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_phieu_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_phieu_giam_gia] [int] NOT NULL,
	[id_chi_tiet_san_pham] [int] NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chi_tiet_san_pham]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_san_pham](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_san_pham] [int] NOT NULL,
	[id_mau_sac] [int] NOT NULL,
	[id_kich_thuoc] [int] NOT NULL,
	[id_de_giay] [int] NOT NULL,
	[id_chat_lieu] [int] NOT NULL,
	[id_trong_luong] [int] NOT NULL,
	[ma_chi_tiet_san_pham]  AS ('CTSP'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_chi_tiet_san_pham] [nvarchar](255) NULL,
	[so_luong] [int] NULL,
	[gia_ban] [decimal](18, 2) NULL,
	[trang_thai] [bit] NULL,
	[ghi_chu] [nvarchar](255) NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
	[ten_san_pham_chi_tiet] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[chi_tiet_san_pham_anh]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[chi_tiet_san_pham_anh](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_chi_tiet_san_pham] [int] NOT NULL,
	[id_anh_san_pham] [int] NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[de_giay]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[de_giay](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_de_giay]  AS ('DG'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_de_giay] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[dia_chi_khach_hang]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[dia_chi_khach_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NOT NULL,
	[ma_dia_chi]  AS ('DC'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_dia_chi] [nvarchar](255) NULL,
	[thanh_pho] [nvarchar](255) NULL,
	[quan] [nvarchar](255) NULL,
	[phuong] [nvarchar](255) NULL,
	[dia_chi_cu_the] [nvarchar](255) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[dot_giam_gia]     ******/
-- Promotion campaign management
-- created_at: Timestamp when campaign was created (auto-set on insert)
-- updated_at: Timestamp when campaign was last updated (auto-updated on save)
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[dot_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_dot_giam_gia]  AS ('DGG'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_dot_giam_gia] [nvarchar](255) NOT NULL,
	[gia_tri_giam_gia] [int] NULL,
	[ngay_bat_dau] [datetime] NULL,
	[ngay_ket_thuc] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[created_at] [datetime] NULL DEFAULT GETDATE(),
	[updated_at] [datetime] NULL DEFAULT GETDATE(),
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[dot_giam_gia_history]     ******/
-- Change history tracking for promotion campaign (dot_giam_gia)
-- Tracks all changes made to promotion campaigns including:
--   - Basic field changes (name, discount value, dates, status)
-- hanh_dong: Action type (TẠO MỚI, CẬP NHẬT, XÓA)
-- mo_ta_thay_doi: Detailed description of what changed (e.g., "Giá trị giảm: 10% → 15%")
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[dot_giam_gia_history](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[id_dot_giam_gia] [int] NOT NULL,
	[id_nhan_vien] [int] NOT NULL,
	[hanh_dong] [nvarchar](50) NOT NULL,
	[mo_ta_thay_doi] [nvarchar](max) NULL,
	[ngay_thay_doi] [datetime2](7) NOT NULL DEFAULT GETDATE(),
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hinh_thuc_thanh_toan]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hinh_thuc_thanh_toan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_hoa_don] [int] NOT NULL,
	[id_phuong_thuc_thanh_toan] [int] NOT NULL,
	[ma_hinh_thuc_thanh_toan]  AS ('HTTT'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[tien_chuyen_khoan] [decimal](18, 2) NULL,
	[tien_mat] [decimal](18, 2) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hoa_don]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hoa_don](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NOT NULL,
	[id_phieu_giam_gia] [int] NULL,
	[id_nhan_vien] [int] NULL,
	[ma_hoa_don]  AS ('HD'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_hoa_don] [nvarchar](255) NULL,
	[loai_don] [bit] NULL,
	[phi_van_chuyen] [decimal](18, 2) NULL,
	[tong_tien] [decimal](18, 2) NULL,
	[tong_tien_sau_giam] [decimal](18, 2) NULL,
	[ghi_chu] [nvarchar](255) NULL,
	[ten_khach_hang] [nvarchar](255) NULL,
	[dia_chi_khach_hang] [nvarchar](255) NULL,
	[so_dien_thoai_khach_hang] [varchar](12) NULL,
	[email_khach_hang] [varchar](255) NULL,
	[ten_nhan_vien] [nvarchar](255) NULL,
	[ma_nhan_vien] [nvarchar](7) NULL,
	[ten_phieu_giam_gia] [nvarchar](255) NULL,
	[ma_phieu_giam_gia] [nvarchar](8) NULL,
	[ngay_tao] [date] NULL,
	[ngay_thanh_toan] [date] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hoa_don_chi_tiet]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hoa_don_chi_tiet](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_hoa_don] [int] NOT NULL,
	[id_chi_tiet_san_pham] [int] NOT NULL,
	[ma_hoa_don_chi_tiet]  AS ('HDCT'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[so_luong] [int] NULL,
	[gia_ban] [decimal](18, 2) NULL,
	[thanh_tien] [decimal](18, 2) NULL,
	[trang_thai] [bit] NULL,
	[ghi_chu] [nvarchar](255) NULL,
	[ten_san_pham_chi_tiet] [nvarchar](255) NULL,
	[ma_san_pham_chi_tiet] [nvarchar](20) NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[khach_hang]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[khach_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_khach_hang]  AS ('KH'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_khach_hang] [nvarchar](255) NULL,
	[ten_tai_khoan] [varchar](255) NULL,
	[mat_khau] [varchar](255) NULL,
	[email] [varchar](255) NULL,
	[so_dien_thoai] [varchar](12) NULL,
	[gioi_tinh] [bit] NULL,
	[ngay_sinh] [date] NULL,
	[phan_loai] [int] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[kich_thuoc]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[kich_thuoc](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_kich_thuoc]  AS ('KT'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_kich_thuoc] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[mau_sac]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[mau_sac](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_mau_sac]  AS ('MS'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_mau_sac] [nvarchar](255) NOT NULL,
	[ma_mau] [nvarchar](8) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[nha_san_xuat]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[nha_san_xuat](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_nha_san_xuat]  AS ('NSX'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_nha_san_xuat] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[nhan_vien]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[nhan_vien](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_quyen_han] [int] NOT NULL,
	[ma_nhan_vien]  AS ('NV'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_nhan_vien] [nvarchar](255) NULL,
	[ten_tai_khoan] [varchar](255) NULL,
	[mat_khau] [varchar](255) NULL,
	[email] [varchar](255) NULL,
	[so_dien_thoai] [varchar](12) NULL,
	[anh_nhan_vien] [varchar](255) NULL,
	[ngay_sinh] [date] NULL,
	[ghi_chu] [nvarchar](255) NULL,
	[thanh_pho] [nvarchar](255) NULL,
	[quan] [nvarchar](255) NULL,
	[phuong] [nvarchar](255) NULL,
	[dia_chi_cu_the] [nvarchar](255) NULL,
	[gioi_tinh] [bit] NULL,
	[cccd] [varchar](20) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[phieu_giam_gia]     ******/
-- Coupon/Voucher management
-- created_at: Timestamp when coupon was created (auto-set on insert)
-- updated_at: Timestamp when coupon was last updated (auto-updated on save)
-- noi_bat: Featured flag - when true, requires customer assignment via phieu_giam_gia_ca_nhan
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_phieu_giam_gia]  AS ('PGG'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_phieu_giam_gia] [nvarchar](255) NULL,
	[loai_phieu_giam_gia] [bit] NULL,
	[gia_tri_giam_gia] [decimal](18, 2) NULL,
	[so_tien_toi_da] [decimal](18, 2) NULL,
	[hoa_don_toi_thieu] [decimal](18, 2) NULL,
	[so_luong_dung] [int] NULL,
	[ngay_bat_dau] [datetime] NULL,
	[ngay_ket_thuc] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[mo_ta] [nvarchar](255) NULL,
	[noi_bat] [bit] NULL,
	[deleted] [bit] NULL,
	[created_at] [datetime] NULL DEFAULT GETDATE(),
	[updated_at] [datetime] NULL DEFAULT GETDATE(),
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[phieu_giam_gia_history]     ******/
-- Change history tracking for coupon (phieu_giam_gia)
-- Tracks all changes made to coupons including:
--   - Basic field changes (name, value, quantity, dates, status, description)
--   - Customer assignments (for featured coupons)
--   - Product assignments (for product-specific coupons)
-- hanh_dong: Action type (TẠO MỚI, CẬP NHẬT, XÓA)
-- mo_ta_thay_doi: Detailed description of what changed (e.g., "Số lượng: 60 → 40")
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_giam_gia_history](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[id_phieu_giam_gia] [int] NOT NULL,
	[id_nhan_vien] [int] NOT NULL,
	[hanh_dong] [nvarchar](50) NOT NULL,
	[mo_ta_thay_doi] [nvarchar](max) NULL,
	[ngay_thay_doi] [datetime2](7) NOT NULL DEFAULT GETDATE(),
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[phieu_giam_gia_ca_nhan]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_giam_gia_ca_nhan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NOT NULL,
	[id_phieu_giam_gia] [int] NOT NULL,
	[ma_phieu_giam_gia_ca_nhan]  AS ('PGGCN'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_phieu_giam_gia_ca_nhan] [nvarchar](255) NULL,
	[ngay_nhan] [datetime] NULL,
	[ngay_het_han] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[phuong_thuc_thanh_toan]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phuong_thuc_thanh_toan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_phuong_thuc_thanh_toan]  AS ('PTTT'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_phuong_thuc_thanh_toan] [nvarchar](255) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[quyen_han]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[quyen_han](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_quyen_han]  AS ('QH'+right('0'+CONVERT([nvarchar](1),[ID]),(1))) PERSISTED,
	[ten_quyen_han] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[san_pham]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[san_pham](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_nha_san_xuat] [int] NOT NULL,
	[id_xuat_xu] [int] NOT NULL,
	[ma_san_pham]  AS ('SP'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_san_pham] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[thong_tin_don_hang]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[thong_tin_don_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_hoa_don] [int] NOT NULL,
	[id_trang_thai_don_hang] [int] NOT NULL,
	[ma_thong_tin_don_hang]  AS ('TTDH'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[thoi_gian] [datetime] NULL,
	[ghi_chu] [nvarchar](255) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[token_blacklist]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[token_blacklist](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[token] [nvarchar](255) NOT NULL,
	[username] [nvarchar](255) NOT NULL,
	[expiry_date] [datetime2](7) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[trang_thai_don_hang]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[trang_thai_don_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_trang_thai_don_hang]  AS ('TTDH'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_trang_thai_don_hang] [nvarchar](255) NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[trong_luong]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[trong_luong](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_trong_luong]  AS ('TL'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_trong_luong] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[xuat_xu]     ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[xuat_xu](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_xuat_xu]  AS ('XX'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_xuat_xu] [nvarchar](255) NOT NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [date] NULL,
	[create_by] [int] NULL,
	[update_at] [date] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
-- =============================================
-- Insert Sample Data
-- =============================================

SET IDENTITY_INSERT [dbo].[anh_san_pham] ON 
GO
INSERT [dbo].[anh_san_pham] ([id], [duong_dan_anh], [ten_anh], [mau_anh], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'https://res.cloudinary.com/dlgbdwd96/image/upload/v1759817800/SD_73/bb70c82e45f17b28c1d2fd401eefd419.png', N'Abc - Đen', N'Đen', 1, 0, CAST(N'2025-10-12' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[anh_san_pham] ([id], [duong_dan_anh], [ten_anh], [mau_anh], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'https://res.cloudinary.com/dlgbdwd96/image/upload/v1760208626/SD_73/01b279a2ca6826fb59139e3fa8ca3dac.jpg', N'Giày Nike Air Max 270 - Trắng', N'Trắng', 1, 0, CAST(N'2025-10-12' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[anh_san_pham] OFF
GO
SET IDENTITY_INSERT [dbo].[chat_lieu] ON 
GO
INSERT [dbo].[chat_lieu] ([id], [ten_chat_lieu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Da tổng hợp', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chat_lieu] ([id], [ten_chat_lieu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Vải canvas', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chat_lieu] ([id], [ten_chat_lieu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Vải mesh', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[chat_lieu] OFF
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_dot_giam_gia] ON 
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 1, 2, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, 2, 1, 1, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, 2, 3, 1, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, 3, 2, 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (6, 3, 3, 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (7, 4, 1, 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (8, 4, 2, 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_dot_giam_gia] ([id], [id_dot_giam_gia], [id_chi_tiet_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (9, 4, 3, 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_dot_giam_gia] OFF
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham] ON 
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (1, 1, 1, 1, 1, 1, 1, NULL, 50, CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày thể thao cao cấp Nike', 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Đen - Size 39')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (2, 1, 2, 2, 1, 1, 1, NULL, 45, CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày thể thao cao cấp Nike', 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Trắng - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (3, 2, 3, 3, 2, 2, 2, NULL, 40, CAST(3200000.00 AS Decimal(18, 2)), 1, N'Giày chạy bộ Adidas', 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Adidas Ultraboost 22 - Đỏ - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (4, 4, 1, 3, 1, 1, 1, NULL, 1, CAST(100.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc - Đen - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (5, 4, 1, 2, 1, 1, 1, NULL, 1, CAST(100.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc - Đen - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (6, 1, 1, 3, 1, 1, 1, NULL, 6, CAST(100.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, CAST(N'2025-10-11' AS Date), 1, N'Giày Nike Air Max 270 - Đen - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (7, 1, 1, 2, 1, 1, 1, NULL, 5, CAST(100.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Đen - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (8, 1, 2, 2, 2, 1, 1, NULL, 1, CAST(1.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Trắng - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (9, 4, 2, 4, 2, 1, 1, NULL, 2, CAST(3.00 AS Decimal(18, 2)), 1, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc + Trắng + 38')
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham] OFF
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham_anh] ON 
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 4, 1, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 5, 1, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, 6, 1, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, 7, 1, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, 6, 1, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (6, 8, 2, 1, 0, NULL, NULL, NULL, NULL)
GO
INSERT [dbo].[chi_tiet_san_pham_anh] ([id], [id_chi_tiet_san_pham], [id_anh_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (7, 9, 2, 1, 0, NULL, NULL, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham_anh] OFF
GO
SET IDENTITY_INSERT [dbo].[de_giay] ON 
GO
INSERT [dbo].[de_giay] ([id], [ten_de_giay], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Đế cao su', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[de_giay] ([id], [ten_de_giay], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Đế EVA', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[de_giay] ([id], [ten_de_giay], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Đế PU', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[de_giay] OFF
GO
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] ON 
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [trang_thai], [deleted]) VALUES (1, 1, N'Địa chỉ nhà riêng', N'Hà Nội', N'Ba Đình', N'Phúc Xá', N'Số 10 Ngõ ABC, Phúc Xá, Ba Đình', 1, 0)
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [trang_thai], [deleted]) VALUES (2, 2, N'Địa chỉ công ty', N'Hà Nội', N'Hai Bà Trưng', N'Bạch Mai', N'Số 20 Đường XYZ, Bạch Mai, Hai Bà Trưng', 1, 0)
GO
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[dot_giam_gia] ON 
GO
INSERT [dbo].[dot_giam_gia] ([id], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Khuyến mãi mùa thu', 15, CAST(N'2025-09-27T00:00:00.000' AS DateTime), CAST(N'2025-11-27T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Flash Sale cuối tuần', 20, CAST(N'2025-10-11T00:00:00.000' AS DateTime), CAST(N'2025-10-13T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Khuyến mãi Black Friday', 30, CAST(N'2025-11-25T00:00:00.000' AS DateTime), CAST(N'2025-11-30T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'Giảm giá Tết Nguyên Đán', 25, CAST(N'2026-01-20T00:00:00.000' AS DateTime), CAST(N'2026-02-10T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, N'Sale giữa năm', 10, CAST(N'2025-06-01T00:00:00.000' AS DateTime), CAST(N'2025-06-30T23:59:59.000' AS DateTime), 0, 0, CAST(N'2025-05-15' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[dot_giam_gia] OFF
GO
SET IDENTITY_INSERT [dbo].[hinh_thuc_thanh_toan] ON 
GO
INSERT [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [trang_thai], [deleted]) VALUES (1, 1, 2, CAST(2250000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), 1, 0)
GO
INSERT [dbo].[hinh_thuc_thanh_toan] ([id], [id_hoa_don], [id_phuong_thuc_thanh_toan], [tien_chuyen_khoan], [tien_mat], [trang_thai], [deleted]) VALUES (2, 2, 2, CAST(3000000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), 1, 0)
GO
SET IDENTITY_INSERT [dbo].[hinh_thuc_thanh_toan] OFF
GO
SET IDENTITY_INSERT [dbo].[hoa_don] ON 
GO
INSERT [dbo].[hoa_don] ([id], [id_khach_hang], [id_phieu_giam_gia], [id_nhan_vien], [ten_hoa_don], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email_khach_hang], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, 2, N'Đơn hàng giày Nike mới', 0, CAST(30000.00 AS Decimal(18, 2)), CAST(2500000.00 AS Decimal(18, 2)), CAST(2250000.00 AS Decimal(18, 2)), N'Giao hàng trong ngày', N'Phạm Văn A', N'Số 10 Ngõ ABC, Phúc Xá, Ba Đình, Hà Nội', N'0123456789', N'khachhang1@gmail.com', CAST(N'2025-09-27' AS Date), CAST(N'2025-09-27' AS Date), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[hoa_don] ([id], [id_khach_hang], [id_phieu_giam_gia], [id_nhan_vien], [ten_hoa_don], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email_khach_hang], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 2, 2, N'Đơn hàng giày Adidas mới', 0, CAST(30000.00 AS Decimal(18, 2)), CAST(3200000.00 AS Decimal(18, 2)), CAST(3000000.00 AS Decimal(18, 2)), N'Giao hàng sáng mai', N'Hoàng Thị B', N'Số 20 Đường XYZ, Bạch Mai, Hai Bà Trưng, Hà Nội', N'0987654321', N'khachhang2@gmail.com', CAST(N'2025-09-27' AS Date), CAST(N'2025-09-27' AS Date), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[hoa_don] OFF
GO
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] ON 
GO
INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_san_pham], [so_luong], [gia_ban], [thanh_tien], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, 1, CAST(2500000.00 AS Decimal(18, 2)), CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày Nike Air Max 270 đen size 39', 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_san_pham], [so_luong], [gia_ban], [thanh_tien], [trang_thai], [ghi_chu], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 3, 1, CAST(3200000.00 AS Decimal(18, 2)), CAST(3200000.00 AS Decimal(18, 2)), 1, N'Giày Adidas Ultraboost 22 đỏ size 41', 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] OFF
GO
SET IDENTITY_INSERT [dbo].[khach_hang] ON 
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Phạm Văn A', N'khachhang1', N'khach123', N'khachhang1@gmail.com', N'0123456789', 1, CAST(N'1998-03-10' AS Date), NULL, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Hoàng Thị B', N'khachhang2', N'khach123', N'khachhang2@gmail.com', N'0987654321', 0, CAST(N'1996-07-25' AS Date), NULL, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[khach_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[kich_thuoc] ON 
GO
INSERT [dbo].[kich_thuoc] ([id], [ten_kich_thuoc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'39', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[kich_thuoc] ([id], [ten_kich_thuoc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'40', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[kich_thuoc] ([id], [ten_kich_thuoc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'41', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[kich_thuoc] ([id], [ten_kich_thuoc], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'38', 1, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[kich_thuoc] OFF
GO
SET IDENTITY_INSERT [dbo].[mau_sac] ON 
GO
INSERT [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Đen', N'#000000', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Trắng', N'#FFFFFF', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Đỏ', N'#FF0000', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[mau_sac] ([id], [ten_mau_sac], [ma_mau], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'Xanh dương', N'#0000FF', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[mau_sac] OFF
GO
SET IDENTITY_INSERT [dbo].[nha_san_xuat] ON 
GO
INSERT [dbo].[nha_san_xuat] ([id], [ten_nha_san_xuat], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Nike', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[nha_san_xuat] ([id], [ten_nha_san_xuat], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Adidas', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[nha_san_xuat] ([id], [ten_nha_san_xuat], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Puma', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[nha_san_xuat] OFF
GO
SET IDENTITY_INSERT [dbo].[nhan_vien] ON 
GO
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 2, N'Nguyễn Đại Ca', N'admin', N'$2a$10$BAlCSfey81iEY5CTIAH7zOPNTP/9zNe9d6cUUCc7SeF.O8bc0nekG', N'daica@gearup.vn', N'0987654321', NULL, CAST(N'1990-01-01' AS Date), N'Quản trị viên hệ thống', N'Hà Nội', N'Hoàn Kiếm', N'Phúc Tân', N'Tầng 5, Tòa nhà ABC, số 123 Đường Phúc Tân', 1, N'001234567890', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 1, N'Trần Thị Em', N'them', N'$2a$10$8K2L0IgfzC3QHICobxwVOXdKUOz5hdzrQyLj8LrQ8D6cjzVzVzQG', N'them.tran@gearup.vn', N'0978123456', NULL, CAST(N'1995-02-15' AS Date), N'Nhân viên bán hàng tại quầy', N'Hà Nội', N'Ba Đình', N'Ngọc Hà', N'Số 45 Ngõ 89 Đường Ngọc Hà', 0, N'012345678901', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[nhan_vien] OFF
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] ON 
GO
INSERT [dbo].[phieu_giam_gia] ([id], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [gia_tri_giam_gia], [so_tien_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [mo_ta], [noi_bat], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Giảm giá 10% cho đơn hàng đầu tiên', 0, CAST(10.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)), 100, CAST(N'2025-09-27T00:00:00.000' AS DateTime), CAST(N'2025-12-27T23:59:59.000' AS DateTime), 1, N'Áp dụng cho khách hàng mới', 0, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phieu_giam_gia] ([id], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [gia_tri_giam_gia], [so_tien_toi_da], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [mo_ta], [noi_bat], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Giảm giá 200.000đ cho đơn hàng từ 2 triệu', 1, CAST(200000.00 AS Decimal(18, 2)), CAST(200000.00 AS Decimal(18, 2)), CAST(2000000.00 AS Decimal(18, 2)), 50, CAST(N'2025-09-27T00:00:00.000' AS DateTime), CAST(N'2025-11-27T23:59:59.000' AS DateTime), 1, N'Áp dụng cho tất cả khách hàng', 0, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] OFF
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia_ca_nhan] ON 
GO
INSERT [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_khach_hang], [id_phieu_giam_gia], [ten_phieu_giam_gia_ca_nhan], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, N'Phiếu giảm giá cá nhân - Khách 1', CAST(N'2025-09-27T00:00:00.000' AS DateTime), CAST(N'2025-12-27T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_khach_hang], [id_phieu_giam_gia], [ten_phieu_giam_gia_ca_nhan], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 2, N'Phiếu giảm giá cá nhân - Khách 2', CAST(N'2025-09-27T00:00:00.000' AS DateTime), CAST(N'2025-11-27T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia_ca_nhan] OFF
GO
SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] ON 
GO
INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ten_phuong_thuc_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Tiền mặt', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ten_phuong_thuc_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Chuyển khoản ngân hàng', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ten_phuong_thuc_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Ví điện tử', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] OFF
GO
SET IDENTITY_INSERT [dbo].[quyen_han] ON 
GO
INSERT [dbo].[quyen_han] ([id], [ten_quyen_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Nhân viên', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[quyen_han] ([id], [ten_quyen_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Quản lý', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[quyen_han] OFF
GO
SET IDENTITY_INSERT [dbo].[san_pham] ON 
GO
INSERT [dbo].[san_pham] ([id], [id_nha_san_xuat], [id_xuat_xu], [ten_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, N'Giày Nike Air Max 270', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[san_pham] ([id], [id_nha_san_xuat], [id_xuat_xu], [ten_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 2, N'Giày Adidas Ultraboost 22', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[san_pham] ([id], [id_nha_san_xuat], [id_xuat_xu], [ten_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, 3, 3, N'Giày Puma RS-X', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[san_pham] ([id], [id_nha_san_xuat], [id_xuat_xu], [ten_san_pham], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, 1, 1, N'Abc', 1, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[san_pham] OFF
GO
SET IDENTITY_INSERT [dbo].[thong_tin_don_hang] ON 
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (1, 1, 1, CAST(N'2025-09-27T08:00:00.000' AS DateTime), N'Đơn hàng mới được tạo', 1, 0)
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (2, 1, 2, CAST(N'2025-09-27T08:15:00.000' AS DateTime), N'Đơn hàng đã được xác nhận', 1, 0)
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (3, 1, 5, CAST(N'2025-09-27T10:00:00.000' AS DateTime), N'Đơn hàng đã giao thành công', 1, 0)
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (4, 2, 1, CAST(N'2025-09-27T08:00:00.000' AS DateTime), N'Đơn hàng mới được tạo', 1, 0)
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (5, 2, 2, CAST(N'2025-09-27T08:10:00.000' AS DateTime), N'Đơn hàng đã được xác nhận', 1, 0)
GO
INSERT [dbo].[thong_tin_don_hang] ([id], [id_hoa_don], [id_trang_thai_don_hang], [thoi_gian], [ghi_chu], [trang_thai], [deleted]) VALUES (6, 2, 4, CAST(N'2025-09-27T09:00:00.000' AS DateTime), N'Đơn hàng đang được giao', 1, 0)
GO
SET IDENTITY_INSERT [dbo].[thong_tin_don_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[token_blacklist] ON 
GO
INSERT [dbo].[token_blacklist] ([id], [token], [username], [expiry_date], [created_at]) VALUES (1, N'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MDIwODg4OSwiZXhwIjoxNzYwMjEyNDg5fQ.rU2qfBk-MQNMkFAZPeDVdHtlhIgw7nkxUCIOAT2Lca0', N'admin', CAST(N'2025-10-12T02:54:49.0000000' AS DateTime2), CAST(N'2025-10-12T02:06:56.7199040' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[token_blacklist] OFF
GO
SET IDENTITY_INSERT [dbo].[trang_thai_don_hang] ON 
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Chờ xác nhận', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Đã xác nhận', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Đang xử lý', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'Đang giao hàng', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, N'Đã giao hàng', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[trang_thai_don_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[trong_luong] ON 
GO
INSERT [dbo].[trong_luong] ([id], [ten_trong_luong], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'250g', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trong_luong] ([id], [ten_trong_luong], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'300g', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trong_luong] ([id], [ten_trong_luong], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'350g', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[trong_luong] OFF
GO
SET IDENTITY_INSERT [dbo].[xuat_xu] ON 
GO
INSERT [dbo].[xuat_xu] ([id], [ten_xuat_xu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Việt Nam', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[xuat_xu] ([id], [ten_xuat_xu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Trung Quốc', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[xuat_xu] ([id], [ten_xuat_xu], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Thái Lan', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[xuat_xu] OFF
GO

-- =============================================
-- Create Indexes and Constraints
-- =============================================

SET ANSI_PADDING ON
GO
-- Unique index on anh_san_pham.duong_dan_anh
ALTER TABLE [dbo].[anh_san_pham] ADD UNIQUE NONCLUSTERED 
(
	[duong_dan_anh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__chat_lie__47FA28728CC9655E]     ******/
ALTER TABLE [dbo].[chat_lieu] ADD UNIQUE NONCLUSTERED 
(
	[ten_chat_lieu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__de_giay__C3D409554500624B]     ******/
ALTER TABLE [dbo].[de_giay] ADD UNIQUE NONCLUSTERED 
(
	[ten_de_giay] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__dot_giam__E885712B582EF1BD]     ******/
ALTER TABLE [dbo].[dot_giam_gia] ADD UNIQUE NONCLUSTERED 
(
	[ten_dot_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__17112F092BED8155]     ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__AB6E61646BB2E302]     ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__BD03D94C027C7ECA]     ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__kich_thu__06BE2795A9A4D306]     ******/
ALTER TABLE [dbo].[kich_thuoc] ADD UNIQUE NONCLUSTERED 
(
	[ten_kich_thuoc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__mau_sac__25764485DCF447EF]     ******/
ALTER TABLE [dbo].[mau_sac] ADD UNIQUE NONCLUSTERED 
(
	[ten_mau_sac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nha_san___700B0905522F5C2E]     ******/
ALTER TABLE [dbo].[nha_san_xuat] ADD UNIQUE NONCLUSTERED 
(
	[ten_nha_san_xuat] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__17112F0955ED1F43]     ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__AB6E616472C56921]     ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__BD03D94C4D9D9A83]     ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__quyen_ha__1EABFF4956C1C261]     ******/
ALTER TABLE [dbo].[quyen_han] ADD UNIQUE NONCLUSTERED 
(
	[ten_quyen_han] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__san_pham__BA66C031656DB860]     ******/
ALTER TABLE [dbo].[san_pham] ADD UNIQUE NONCLUSTERED 
(
	[ten_san_pham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [uk_token_blacklist_token]     ******/
ALTER TABLE [dbo].[token_blacklist] ADD  CONSTRAINT [uk_token_blacklist_token] UNIQUE NONCLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_token_blacklist_expiry_date]     ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_expiry_date] ON [dbo].[token_blacklist]
(
	[expiry_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_token]     ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_token] ON [dbo].[token_blacklist]
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_username]     ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_username] ON [dbo].[token_blacklist]
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__trong_lu__399742A0B04F3192]     ******/
ALTER TABLE [dbo].[trong_luong] ADD UNIQUE NONCLUSTERED 
(
	[ten_trong_luong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__xuat_xu__BA9B8924EEEE2B76]     ******/
ALTER TABLE [dbo].[xuat_xu] ADD UNIQUE NONCLUSTERED 
(
	[ten_xuat_xu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- =============================================
-- Set Default Constraints
-- =============================================

ALTER TABLE [dbo].[anh_san_pham] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[anh_san_pham] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[chat_lieu] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[chat_lieu] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[chi_tiet_dot_giam_gia] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[chi_tiet_dot_giam_gia] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD  DEFAULT ((0)) FOR [so_luong]
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[chi_tiet_san_pham] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[chi_tiet_san_pham_anh] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[chi_tiet_san_pham_anh] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[de_giay] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[de_giay] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [loai_don]
GO
ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT ((0)) FOR [so_luong]
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[khach_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[khach_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[kich_thuoc] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[kich_thuoc] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[mau_sac] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[mau_sac] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[nha_san_xuat] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[nha_san_xuat] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((0)) FOR [loai_phieu_giam_gia]
GO
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((0)) FOR [noi_bat]
GO
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[phuong_thuc_thanh_toan] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[phuong_thuc_thanh_toan] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[quyen_han] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[quyen_han] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[san_pham] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[san_pham] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[thong_tin_don_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[thong_tin_don_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[token_blacklist] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[trang_thai_don_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[trang_thai_don_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[trong_luong] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[trong_luong] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[xuat_xu] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[xuat_xu] ADD  DEFAULT ((0)) FOR [deleted]
GO

-- =============================================
-- Create Foreign Key Relationships
-- =============================================

ALTER TABLE [dbo].[chi_tiet_dot_giam_gia]  WITH CHECK ADD FOREIGN KEY([id_chi_tiet_san_pham])
REFERENCES [dbo].[chi_tiet_san_pham] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_dot_giam_gia]  WITH CHECK ADD FOREIGN KEY([id_dot_giam_gia])
REFERENCES [dbo].[dot_giam_gia] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_phieu_giam_gia]  WITH CHECK ADD FOREIGN KEY([id_chi_tiet_san_pham])
REFERENCES [dbo].[chi_tiet_san_pham] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_phieu_giam_gia]  WITH CHECK ADD FOREIGN KEY([id_phieu_giam_gia])
REFERENCES [dbo].[phieu_giam_gia] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_chat_lieu])
REFERENCES [dbo].[chat_lieu] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_de_giay])
REFERENCES [dbo].[de_giay] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_kich_thuoc])
REFERENCES [dbo].[kich_thuoc] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_mau_sac])
REFERENCES [dbo].[mau_sac] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_san_pham])
REFERENCES [dbo].[san_pham] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD FOREIGN KEY([id_trong_luong])
REFERENCES [dbo].[trong_luong] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham_anh]  WITH CHECK ADD FOREIGN KEY([id_anh_san_pham])
REFERENCES [dbo].[anh_san_pham] ([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham_anh]  WITH CHECK ADD FOREIGN KEY([id_chi_tiet_san_pham])
REFERENCES [dbo].[chi_tiet_san_pham] ([id])
GO
ALTER TABLE [dbo].[dia_chi_khach_hang]  WITH CHECK ADD FOREIGN KEY([id_khach_hang])
REFERENCES [dbo].[khach_hang] ([id])
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan]  WITH CHECK ADD FOREIGN KEY([id_phuong_thuc_thanh_toan])
REFERENCES [dbo].[phuong_thuc_thanh_toan] ([id])
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD FOREIGN KEY([id_khach_hang])
REFERENCES [dbo].[khach_hang] ([id])
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD FOREIGN KEY([id_nhan_vien])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD FOREIGN KEY([id_phieu_giam_gia])
REFERENCES [dbo].[phieu_giam_gia] ([id])
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD FOREIGN KEY([id_chi_tiet_san_pham])
REFERENCES [dbo].[chi_tiet_san_pham] ([id])
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO
ALTER TABLE [dbo].[nhan_vien]  WITH CHECK ADD FOREIGN KEY([id_quyen_han])
REFERENCES [dbo].[quyen_han] ([id])
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan]  WITH CHECK ADD FOREIGN KEY([id_khach_hang])
REFERENCES [dbo].[khach_hang] ([id])
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan]  WITH CHECK ADD FOREIGN KEY([id_phieu_giam_gia])
REFERENCES [dbo].[phieu_giam_gia] ([id])
GO
ALTER TABLE [dbo].[phieu_giam_gia_history]  WITH CHECK ADD FOREIGN KEY([id_phieu_giam_gia])
REFERENCES [dbo].[phieu_giam_gia] ([id])
GO
ALTER TABLE [dbo].[phieu_giam_gia_history]  WITH CHECK ADD FOREIGN KEY([id_nhan_vien])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD FOREIGN KEY([id_nha_san_xuat])
REFERENCES [dbo].[nha_san_xuat] ([id])
GO
ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD FOREIGN KEY([id_xuat_xu])
REFERENCES [dbo].[xuat_xu] ([id])
GO
ALTER TABLE [dbo].[thong_tin_don_hang]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO
ALTER TABLE [dbo].[thong_tin_don_hang]  WITH CHECK ADD FOREIGN KEY([id_trang_thai_don_hang])
REFERENCES [dbo].[trang_thai_don_hang] ([id])
GO

-- =============================================
-- Create Check Constraints
-- =============================================

ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([gia_ban]>=(0)))
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([so_luong]>=(0)))
GO
ALTER TABLE [dbo].[dot_giam_gia]  WITH CHECK ADD CHECK  (([gia_tri_giam_gia]>=(0)))
GO
ALTER TABLE [dbo].[dot_giam_gia]  WITH CHECK ADD CHECK  (([ngay_ket_thuc]>[ngay_bat_dau]))
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan]  WITH CHECK ADD CHECK  (([tien_chuyen_khoan]>=(0)))
GO
ALTER TABLE [dbo].[hinh_thuc_thanh_toan]  WITH CHECK ADD CHECK  (([tien_mat]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD CHECK  (([ngay_thanh_toan]>=[ngay_tao]))
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD CHECK  (([tong_tien_sau_giam]<=[tong_tien]))
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD CHECK  (([phi_van_chuyen]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD CHECK  (([tong_tien]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD CHECK  (([tong_tien_sau_giam]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD CHECK  (([gia_ban]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD CHECK  (([so_luong]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD CHECK  (([thanh_tien]>=(0)))
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([gia_tri_giam_gia]>=(0)))
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([hoa_don_toi_thieu]>=(0)))
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([so_luong_dung]>=(0)))
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([so_tien_toi_da]>=(0)))
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([ngay_ket_thuc]>[ngay_bat_dau]))
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan]  WITH CHECK ADD CHECK  (([ngay_het_han]>[ngay_nhan]))
GO

-- =============================================
-- Create Indexes for History Table
-- =============================================

CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_id_phieu] ON [dbo].[phieu_giam_gia_history]
(
	[id_phieu_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_ngay_thay_doi] ON [dbo].[phieu_giam_gia_history]
(
	[ngay_thay_doi] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- =============================================
-- Complete Database Setup
-- =============================================

USE [master]
GO
ALTER DATABASE [GearUp] SET  READ_WRITE 
GO

-- =============================================
-- Script completed successfully
-- =============================================
