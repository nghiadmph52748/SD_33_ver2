-- SQL Server script compatible with macOS
-- Removed Windows-specific file paths and settings

CREATE DATABASE [GearUp]
GO
ALTER DATABASE [GearUp] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [GearUp].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [GearUp] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [GearUp] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [GearUp] SET ARITHABORT OFF 
GO
ALTER DATABASE [GearUp] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [GearUp] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [GearUp] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [GearUp] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [GearUp] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [GearUp] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [GearUp] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [GearUp] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [GearUp] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [GearUp] SET  ENABLE_BROKER 
GO
ALTER DATABASE [GearUp] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [GearUp] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [GearUp] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [GearUp] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [GearUp] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [GearUp] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [GearUp] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [GearUp] SET RECOVERY FULL 
GO
ALTER DATABASE [GearUp] SET  MULTI_USER 
GO
ALTER DATABASE [GearUp] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [GearUp] SET DB_CHAINING OFF 
GO
ALTER DATABASE [GearUp] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [GearUp] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [GearUp] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [GearUp] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'GearUp', N'ON'
GO
ALTER DATABASE [GearUp] SET QUERY_STORE = ON
GO
ALTER DATABASE [GearUp] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [GearUp]
GO
/****** Object:  Table [dbo].[anh_san_pham]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[chat_lieu]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[chi_tiet_dot_giam_gia]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[chi_tiet_phieu_giam_gia]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[chi_tiet_san_pham]    Script Date: 10/13/2025 11:09:19 PM ******/
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
	[qrcode] [varchar](255) NULL,
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
/****** Object:  Table [dbo].[chi_tiet_san_pham_anh]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[de_giay]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[dia_chi_khach_hang]    Script Date: 10/13/2025 11:09:19 PM ******/
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
	[mac_dinh] [bit] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[dot_giam_gia]    Script Date: 10/13/2025 11:09:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[dot_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_dot_giam_gia] [nvarchar](50) NOT NULL,
	[ten_dot_giam_gia] [nvarchar](255) NOT NULL,
	[gia_tri_giam_gia] [int] NULL,
	[ngay_bat_dau] [datetime] NULL,
	[ngay_ket_thuc] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
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
/****** Object:  Table [dbo].[dot_giam_gia_history]    Script Date: 10/13/2025 11:09:19 PM ******/
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
	[ngay_thay_doi] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hinh_thuc_thanh_toan]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[hoa_don]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[hoa_don_chi_tiet]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[khach_hang]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[kich_thuoc]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[mau_sac]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[nha_san_xuat]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[nhan_vien]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[phieu_giam_gia]    Script Date: 10/13/2025 11:09:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_giam_gia](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_phieu_giam_gia] [nvarchar](50) NOT NULL,
	[ten_phieu_giam_gia] [nvarchar](255) NULL,
	[loai_phieu_giam_gia] [bit] NULL,
	[gia_tri_giam_gia] [decimal](18, 2) NULL,
	[hoa_don_toi_thieu] [decimal](18, 2) NULL,
	[so_luong_dung] [int] NULL,
	[ngay_bat_dau] [datetime] NULL,
	[ngay_ket_thuc] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[mo_ta] [nvarchar](255) NULL,
	[noi_bat] [bit] NULL,
	[deleted] [bit] NULL,
	[created_at] [datetime] NULL,
	[updated_at] [datetime] NULL,
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
/****** Object:  Table [dbo].[phieu_giam_gia_ca_nhan]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[phieu_giam_gia_history]    Script Date: 10/13/2025 11:09:19 PM ******/
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
	[ngay_thay_doi] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[phuong_thuc_thanh_toan]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[quyen_han]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[san_pham]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[thong_tin_don_hang]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[token_blacklist]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[trang_thai_don_hang]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[trong_luong]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[xuat_xu]    Script Date: 10/13/2025 11:09:19 PM ******/
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
/****** Object:  Table [dbo].[tin_nhan]    Script Date: 10/27/2025 04:26:00 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tin_nhan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_tin_nhan] AS ('TN'+RIGHT('00000'+CONVERT([nvarchar](5),[id]),(5))) PERSISTED,
	[id_nguoi_gui] [int] NOT NULL,
	[id_nguoi_nhan] [int] NOT NULL,
	[noi_dung] [nvarchar](max) NOT NULL,
	[loai_tin_nhan] [varchar](20) NULL,
	[da_doc] [bit] NULL,
	[thoi_gian_doc] [datetime2](7) NULL,
	[thoi_gian_gui] [datetime2](7) NOT NULL,
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
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[cuoc_trao_doi]    Script Date: 10/27/2025 04:26:00 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[cuoc_trao_doi](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_cuoc_trao_doi] AS ('CTD'+RIGHT('00000'+CONVERT([nvarchar](5),[id]),(5))) PERSISTED,
	[id_nhan_vien_1] [int] NOT NULL,
	[id_nhan_vien_2] [int] NOT NULL,
	[tin_nhan_cuoi_cung] [nvarchar](500) NULL,
	[thoi_gian_tin_nhan_cuoi] [datetime2](7) NULL,
	[id_nguoi_gui_cuoi] [int] NULL,
	[so_tin_nhan_chua_doc_nv1] [int] NULL,
	[so_tin_nhan_chua_doc_nv2] [int] NULL,
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
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [trang_thai], [deleted]) VALUES (1, 1, N'Địa chỉ nhà riêng', N'Hà Nội', N'Ba Đình', N'Phúc Xá', N'Số 10 Ngõ ABC, Phúc Xá, Ba Đình', 1, 1, 0)
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [trang_thai], [deleted]) VALUES (2, 2, N'Địa chỉ công ty', N'Hà Nội', N'Hai Bà Trưng', N'Bạch Mai', N'Số 20 Đường XYZ, Bạch Mai, Hai Bà Trưng', 1, 1, 0)
GO
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[dot_giam_gia] ON 
GO
INSERT [dbo].[dot_giam_gia] ([id], [ma_dot_giam_gia], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'DGG000001', N'Khuyến mãi mùa thu', 15, CAST(N'2025-10-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-13T14:42:01.027' AS DateTime), CAST(N'2025-10-13T14:42:01.027' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ma_dot_giam_gia], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'DGG000002', N'Flash Sale cuối tuần', 20, CAST(N'2025-10-10T00:00:00.000' AS DateTime), CAST(N'2025-11-30T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-13T14:42:01.030' AS DateTime), CAST(N'2025-10-13T14:42:01.030' AS DateTime), CAST(N'2025-10-10' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ma_dot_giam_gia], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'DGG000003', N'Khuyến mãi Black Friday', 30, CAST(N'2025-11-25T00:00:00.000' AS DateTime), CAST(N'2025-11-30T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-13T14:42:01.033' AS DateTime), CAST(N'2025-10-13T14:42:01.033' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ma_dot_giam_gia], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'DGG000004', N'Giảm giá Tết Nguyên Đán', 25, CAST(N'2026-01-20T00:00:00.000' AS DateTime), CAST(N'2026-02-10T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-13T14:42:01.037' AS DateTime), CAST(N'2025-10-13T14:42:01.037' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[dot_giam_gia] ([id], [ma_dot_giam_gia], [ten_dot_giam_gia], [gia_tri_giam_gia], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (5, N'DGG000005', N'Sale giữa năm', 10, CAST(N'2025-06-01T00:00:00.000' AS DateTime), CAST(N'2025-06-30T23:59:59.000' AS DateTime), 0, 0, CAST(N'2025-10-13T14:42:01.037' AS DateTime), CAST(N'2025-10-13T14:42:01.037' AS DateTime), CAST(N'2025-05-15' AS Date), 1, NULL, NULL)
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
INSERT [dbo].[hoa_don] ([id], [id_khach_hang], [id_phieu_giam_gia], [id_nhan_vien], [ten_hoa_don], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email_khach_hang], [ten_nhan_vien], [ma_nhan_vien], [ten_phieu_giam_gia], [ma_phieu_giam_gia], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, 2, N'Đơn hàng giày Nike mới', 0, CAST(30000.00 AS Decimal(18, 2)), CAST(2500000.00 AS Decimal(18, 2)), CAST(2250000.00 AS Decimal(18, 2)), N'Giao hàng trong ngày', N'Phạm Văn A', N'Số 10 Ngõ ABC, Phúc Xá, Ba Đình, Hà Nội', N'0123456789', N'khachhang1@gmail.com', NULL, NULL, NULL, NULL, CAST(N'2025-09-27' AS Date), CAST(N'2025-09-27' AS Date), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[hoa_don] ([id], [id_khach_hang], [id_phieu_giam_gia], [id_nhan_vien], [ten_hoa_don], [loai_don], [phi_van_chuyen], [tong_tien], [tong_tien_sau_giam], [ghi_chu], [ten_khach_hang], [dia_chi_khach_hang], [so_dien_thoai_khach_hang], [email_khach_hang], [ten_nhan_vien], [ma_nhan_vien], [ten_phieu_giam_gia], [ma_phieu_giam_gia], [ngay_tao], [ngay_thanh_toan], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 2, 2, N'Đơn hàng giày Adidas mới', 0, CAST(30000.00 AS Decimal(18, 2)), CAST(3200000.00 AS Decimal(18, 2)), CAST(3000000.00 AS Decimal(18, 2)), N'Giao hàng sáng mai', N'Hoàng Thị B', N'Số 20 Đường XYZ, Bạch Mai, Hai Bà Trưng, Hà Nội', N'0987654321', N'khachhang2@gmail.com', NULL, NULL, NULL, NULL, CAST(N'2025-09-27' AS Date), CAST(N'2025-09-27' AS Date), 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[hoa_don] OFF
GO
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] ON 
GO
INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_san_pham], [so_luong], [gia_ban], [thanh_tien], [trang_thai], [ghi_chu], [ten_san_pham_chi_tiet], [ma_san_pham_chi_tiet], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, 1, 1, CAST(2500000.00 AS Decimal(18, 2)), CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày Nike Air Max 270 đen size 39', NULL, NULL, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_chi_tiet_san_pham], [so_luong], [gia_ban], [thanh_tien], [trang_thai], [ghi_chu], [ten_san_pham_chi_tiet], [ma_san_pham_chi_tiet], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 2, 3, 1, CAST(3200000.00 AS Decimal(18, 2)), CAST(3200000.00 AS Decimal(18, 2)), 1, N'Giày Adidas Ultraboost 22 đỏ size 41', NULL, NULL, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] OFF
GO
SET IDENTITY_INSERT [dbo].[khach_hang] ON 
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Phạm Văn A', N'khachhang1', N'khach123', N'khachhang1@gmail.com', N'0123456789', 1, CAST(N'1998-03-10' AS Date), NULL, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Hoàng Thị B', N'khachhang2', N'$2a$10$moruvP4SlJifGfvkFTXuSuaXWQ58UlnCHWEc/PtmteILY9PeCUDU2', N'khachhang2@gmail.com', N'0987654321', 0, CAST(N'1996-07-25' AS Date), NULL, 1, 0, NULL, NULL, CAST(N'2025-10-13' AS Date), 1)
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
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, N'Nguyễn Đại Ca', N'admin', N'$2a$12$kmoYhjBqZILxQaoYmjfjvusmCZmVRwgG8iSzbPVTz4rihcIzdIq1C', N'daica@gearup.vn', N'0987654321', NULL, CAST(N'1990-01-01' AS Date), N'Quản trị viên hệ thống', N'Hà Nội', N'Hoàn Kiếm', N'Phúc Tân', N'', 1, N'', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 1, N'Trần Thị Em', N'them', N'$2a$10$8K2L0IgfzC3QHICobxwVOXdKUOz5hdzrQyLj8LrQ8D6cjzVzVzQG', N'them.tran@gearup.vn', N'0978123456', NULL, CAST(N'1995-02-15' AS Date), N'Nhân viên bán hàng tại quầy', N'Hà Nội', N'Ba Đình', N'Ngọc Hà', N'Số 45 Ngõ 89 Đường Ngọc Hà', 0, N'012345678901', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[nhan_vien] OFF
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] ON 
GO
INSERT [dbo].[phieu_giam_gia] ([id], [ma_phieu_giam_gia], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [gia_tri_giam_gia], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [mo_ta], [noi_bat], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'PGG00001', N'Giảm giá 10% cho đơn hàng đầu tiên', 0, CAST(10.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)), 100, CAST(N'2025-10-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, N'Áp dụng cho khách hàng mới', 0, 0, CAST(N'2025-10-13T14:42:01.113' AS DateTime), CAST(N'2025-10-13T14:42:01.113' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phieu_giam_gia] ([id], [ma_phieu_giam_gia], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [gia_tri_giam_gia], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [mo_ta], [noi_bat], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'PGG00002', N'Giảm giá 200.000đ cho đơn hàng từ 2 triệu', 1, CAST(200000.00 AS Decimal(18, 2)), CAST(2000000.00 AS Decimal(18, 2)), 50, CAST(N'2025-10-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, N'Áp dụng cho tất cả khách hàng', 0, 0, CAST(N'2025-10-13T14:42:01.120' AS DateTime), CAST(N'2025-10-13T14:42:01.120' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
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
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__anh_san___F73EF501BB45809B]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[anh_san_pham] ADD UNIQUE NONCLUSTERED 
(
	[duong_dan_anh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__chat_lie__47FA28723669CEC6]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[chat_lieu] ADD UNIQUE NONCLUSTERED 
(
	[ten_chat_lieu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__de_giay__C3D40955B920A472]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[de_giay] ADD UNIQUE NONCLUSTERED 
(
	[ten_de_giay] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__dot_giam__E885712B8AAAFED3]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[dot_giam_gia] ADD UNIQUE NONCLUSTERED 
(
	[ten_dot_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__17112F0949BFC92D]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__AB6E616482EDF778]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__BD03D94CB406F56E]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__kich_thu__06BE2795F936CA03]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[kich_thuoc] ADD UNIQUE NONCLUSTERED 
(
	[ten_kich_thuoc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__mau_sac__257644855F95B8B0]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[mau_sac] ADD UNIQUE NONCLUSTERED 
(
	[ten_mau_sac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nha_san___700B0905E7B29FAF]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[nha_san_xuat] ADD UNIQUE NONCLUSTERED 
(
	[ten_nha_san_xuat] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__17112F092F83A49B]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__AB6E6164C1BFB090]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__BD03D94C054AE9D6]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_phieu_giam_gia_ma]    Script Date: 10/17/2025 10:13:00 AM ******/
ALTER TABLE [dbo].[phieu_giam_gia] ADD  CONSTRAINT [UQ_phieu_giam_gia_ma] UNIQUE NONCLUSTERED 
(
	[ma_phieu_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_phieu_giam_gia_history_id_phieu]    Script Date: 10/13/2025 11:09:19 PM ******/
CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_id_phieu] ON [dbo].[phieu_giam_gia_history]
(
	[id_phieu_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_phieu_giam_gia_history_ngay_thay_doi]    Script Date: 10/13/2025 11:09:19 PM ******/
CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_ngay_thay_doi] ON [dbo].[phieu_giam_gia_history]
(
	[ngay_thay_doi] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__quyen_ha__1EABFF49F776D007]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[quyen_han] ADD UNIQUE NONCLUSTERED 
(
	[ten_quyen_han] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__san_pham__BA66C031F688A6C2]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[san_pham] ADD UNIQUE NONCLUSTERED 
(
	[ten_san_pham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [uk_token_blacklist_token]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[token_blacklist] ADD  CONSTRAINT [uk_token_blacklist_token] UNIQUE NONCLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_token_blacklist_expiry_date]    Script Date: 10/13/2025 11:09:19 PM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_expiry_date] ON [dbo].[token_blacklist]
(
	[expiry_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_token]    Script Date: 10/13/2025 11:09:19 PM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_token] ON [dbo].[token_blacklist]
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_username]    Script Date: 10/13/2025 11:09:19 PM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_username] ON [dbo].[token_blacklist]
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__trong_lu__399742A094F96458]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[trong_luong] ADD UNIQUE NONCLUSTERED 
(
	[ten_trong_luong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__xuat_xu__BA9B892473EA3152]    Script Date: 10/13/2025 11:09:19 PM ******/
ALTER TABLE [dbo].[xuat_xu] ADD UNIQUE NONCLUSTERED 
(
	[ten_xuat_xu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
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
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD  DEFAULT ((0)) FOR [mac_dinh]
GO
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[dia_chi_khach_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[dot_giam_gia] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[dot_giam_gia_history] ADD  DEFAULT (getdate()) FOR [ngay_thay_doi]
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
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT (getdate()) FOR [updated_at]
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[phieu_giam_gia_history] ADD  DEFAULT (getdate()) FOR [ngay_thay_doi]
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
ALTER TABLE [dbo].[phieu_giam_gia_history]  WITH CHECK ADD FOREIGN KEY([id_nhan_vien])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[phieu_giam_gia_history]  WITH CHECK ADD FOREIGN KEY([id_phieu_giam_gia])
REFERENCES [dbo].[phieu_giam_gia] ([id])
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
ALTER TABLE [dbo].[tin_nhan] WITH CHECK 
ADD CONSTRAINT FK_tin_nhan_nguoi_gui 
FOREIGN KEY([id_nguoi_gui]) REFERENCES [dbo].[nhan_vien]([id])
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK 
ADD CONSTRAINT FK_tin_nhan_nguoi_nhan 
FOREIGN KEY([id_nguoi_nhan]) REFERENCES [dbo].[nhan_vien]([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK
ADD CONSTRAINT FK_cuoc_trao_doi_nhan_vien_1 
FOREIGN KEY([id_nhan_vien_1]) REFERENCES [dbo].[nhan_vien]([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT FK_cuoc_trao_doi_nhan_vien_2 
FOREIGN KEY([id_nhan_vien_2]) REFERENCES [dbo].[nhan_vien]([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT FK_cuoc_trao_doi_nguoi_gui_cuoi 
FOREIGN KEY([id_nguoi_gui_cuoi]) REFERENCES [dbo].[nhan_vien]([id])
GO
CREATE NONCLUSTERED INDEX IX_tin_nhan_conversation
ON [dbo].[tin_nhan] ([id_nguoi_gui], [id_nguoi_nhan], [thoi_gian_gui] DESC)
INCLUDE ([noi_dung], [da_doc], [loai_tin_nhan])
GO
CREATE NONCLUSTERED INDEX IX_tin_nhan_chua_doc
ON [dbo].[tin_nhan] ([id_nguoi_nhan], [da_doc])
WHERE [da_doc] = 0 AND [deleted] = 0
GO
CREATE NONCLUSTERED INDEX IX_tin_nhan_thoi_gian
ON [dbo].[tin_nhan] ([thoi_gian_gui] DESC)
INCLUDE ([id_nguoi_gui], [id_nguoi_nhan], [noi_dung])
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_cuoc_trao_doi_unique
ON [dbo].[cuoc_trao_doi] ([id_nhan_vien_1], [id_nhan_vien_2])
WHERE [deleted] = 0
GO
CREATE NONCLUSTERED INDEX IX_cuoc_trao_doi_nhan_vien
ON [dbo].[cuoc_trao_doi] ([id_nhan_vien_1], [id_nhan_vien_2], [thoi_gian_tin_nhan_cuoi] DESC)
WHERE [deleted] = 0
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK
ADD CONSTRAINT CHK_tin_nhan_khac_nguoi 
CHECK ([id_nguoi_gui] <> [id_nguoi_nhan])
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK
ADD CONSTRAINT CHK_tin_nhan_noi_dung 
CHECK (LEN(RTRIM(LTRIM([noi_dung]))) > 0)
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK
ADD CONSTRAINT CHK_tin_nhan_loai 
CHECK ([loai_tin_nhan] IN ('TEXT', 'IMAGE', 'FILE'))
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK
ADD CONSTRAINT CHK_cuoc_trao_doi_khac_nguoi 
CHECK ([id_nhan_vien_1] <> [id_nhan_vien_2])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK
ADD CONSTRAINT CHK_cuoc_trao_doi_so_tin_chua_doc_nv1 
CHECK ([so_tin_nhan_chua_doc_nv1] >= 0)
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT CHK_cuoc_trao_doi_so_tin_chua_doc_nv2 
CHECK ([so_tin_nhan_chua_doc_nv2] >= 0)
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([gia_ban]>=(0)))
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([so_luong]>=(0)))
GO
ALTER TABLE [dbo].[dot_giam_gia]  WITH CHECK ADD CHECK  (([gia_tri_giam_gia]>=(0)))
GO
ALTER TABLE [dbo].[dot_giam_gia]  WITH CHECK ADD CHECK  (([ngay_ket_thuc]>[ngay_bat_dau]))
GO
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_dot_giam_gia_ma' AND object_id = OBJECT_ID('dbo.dot_giam_gia'))
BEGIN
    ALTER TABLE [dbo].[dot_giam_gia] ADD CONSTRAINT UQ_dot_giam_gia_ma UNIQUE (ma_dot_giam_gia)
END
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
GO
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([ngay_ket_thuc]>[ngay_bat_dau]))
GO
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_phieu_giam_gia_ma' AND object_id = OBJECT_ID('dbo.phieu_giam_gia'))
BEGIN
    ALTER TABLE [dbo].[phieu_giam_gia] ADD CONSTRAINT UQ_phieu_giam_gia_ma UNIQUE (ma_phieu_giam_gia)
END
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan]  WITH CHECK ADD CHECK  (([ngay_het_han]>[ngay_nhan]))
GO
-- Thêm và cải thiện các trường cho bảng hóa đơn
USE [GearUp]
GO

-- Thêm trường SDT nhân viên
ALTER TABLE [dbo].[hoa_don] 
ADD [so_dien_thoai_nhan_vien] [varchar](12) NULL
GO

-- Thêm trường mô tả loại đơn hàng chi tiết
ALTER TABLE [dbo].[hoa_don] 
ADD [mo_ta_loai_don] [nvarchar](100) NULL
GO

-- Thêm trường thời gian tạo chi tiết (datetime thay vì chỉ date)
ALTER TABLE [dbo].[hoa_don] 
ADD [thoi_gian_tao] [datetime] NULL
GO

-- Cập nhật dữ liệu mẫu cho các trường mới
UPDATE [dbo].[hoa_don] 
SET 
    [so_dien_thoai_nhan_vien] = '0978123456',
    [mo_ta_loai_don] = 'Bán hàng online',
    [thoi_gian_tao] = '2025-09-27 08:30:00'
WHERE [id] = 1
GO

UPDATE [dbo].[hoa_don] 
SET 
    [so_dien_thoai_nhan_vien] = '0978123456',
    [mo_ta_loai_don] = 'Bán hàng tại quầy',
    [thoi_gian_tao] = '2025-09-27 14:15:00'
WHERE [id] = 2
GO

-- Tạo bảng loại đơn hàng
CREATE TABLE [dbo].[loai_don_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_loai_don]  AS ('LDH'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[ten_loai_don] [nvarchar](100) NOT NULL,
	[mo_ta] [nvarchar](255) NULL,
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

-- Thêm dữ liệu cho bảng loại đơn hàng
INSERT [dbo].[loai_don_hang] ([ten_loai_don], [mo_ta], [trang_thai], [deleted], [create_at], [create_by]) VALUES 
(N'Bán hàng tại quầy', N'Đơn hàng được bán trực tiếp tại cửa hàng', 1, 0, CAST(N'2025-01-15' AS Date), 1),
(N'Bán hàng online', N'Đơn hàng được đặt qua website hoặc ứng dụng', 1, 0, CAST(N'2025-01-15' AS Date), 1)
GO

-- Thêm constraint để đảm bảo chỉ có 2 loại đơn hàng
ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [CK_hoa_don_mo_ta_loai_don] 
CHECK ([mo_ta_loai_don] IN ('Bán hàng tại quầy', 'Bán hàng online'))
GO

-- Tạo bảng timeline quản lý đơn hàng
CREATE TABLE [dbo].[timeline_don_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_hoa_don] [int] NOT NULL,
	[id_nhan_vien] [int] NULL,
	[ma_timeline]  AS ('TL'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[trang_thai_cu] [nvarchar](100) NULL,
	[trang_thai_moi] [nvarchar](100) NOT NULL,
	[hanh_dong] [nvarchar](100) NOT NULL,
	[mo_ta] [nvarchar](500) NULL,
	[ghi_chu] [nvarchar](500) NULL,
	[thoi_gian] [datetime] NOT NULL,
	[ip_address] [varchar](50) NULL,
	[user_agent] [nvarchar](500) NULL,
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

-- Thêm dữ liệu mẫu cho timeline
INSERT [dbo].[timeline_don_hang] ([id_hoa_don], [id_nhan_vien], [trang_thai_cu], [trang_thai_moi], [hanh_dong], [mo_ta], [ghi_chu], [thoi_gian], [trang_thai], [deleted], [create_at], [create_by]) VALUES 
(1, 2, NULL, 'Tạo đơn hàng', 'Tạo mới', 'Đơn hàng được tạo thành công', 'Khách hàng đặt hàng online', '2025-09-27 08:30:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(1, 2, 'Tạo đơn hàng', 'Xác nhận đơn hàng', 'Xác nhận', 'Nhân viên xác nhận đơn hàng', 'Kiểm tra thông tin khách hàng', '2025-09-27 08:45:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(1, 2, 'Xác nhận đơn hàng', 'Đang chuẩn bị hàng', 'Chuẩn bị', 'Bắt đầu chuẩn bị sản phẩm', 'Lấy hàng từ kho', '2025-09-27 09:00:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(1, 2, 'Đang chuẩn bị hàng', 'Đang giao hàng', 'Giao hàng', 'Đơn hàng đang được giao', 'Giao cho shipper', '2025-09-27 10:00:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(1, 2, 'Đang giao hàng', 'Đã giao hàng', 'Hoàn thành', 'Đơn hàng đã giao thành công', 'Khách hàng đã nhận hàng', '2025-09-27 11:30:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),

(2, 2, NULL, 'Tạo đơn hàng', 'Tạo mới', 'Đơn hàng được tạo tại quầy', 'Khách hàng mua trực tiếp', '2025-09-27 14:15:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(2, 2, 'Tạo đơn hàng', 'Xác nhận đơn hàng', 'Xác nhận', 'Nhân viên xác nhận đơn hàng', 'Kiểm tra sản phẩm tại quầy', '2025-09-27 14:20:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(2, 2, 'Xác nhận đơn hàng', 'Đang chuẩn bị hàng', 'Chuẩn bị', 'Bắt đầu chuẩn bị sản phẩm', 'Lấy hàng từ kho', '2025-09-27 14:30:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(2, 2, 'Đang chuẩn bị hàng', 'Đang giao hàng', 'Giao hàng', 'Đơn hàng đang được giao', 'Giao cho shipper', '2025-09-27 15:00:00', 1, 0, CAST(N'2025-09-27' AS Date), 2),
(2, 2, 'Đang giao hàng', 'Đã giao hàng', 'Hoàn thành', 'Đơn hàng đã giao thành công', 'Khách hàng đã nhận hàng', '2025-09-27 16:00:00', 1, 0, CAST(N'2025-09-27' AS Date), 2)
GO

-- Thêm trường thời gian cập nhật cuối
ALTER TABLE [dbo].[hoa_don] 
ADD [thoi_gian_cap_nhat] [datetime] NULL
GO

-- Thêm trường ghi chú nội bộ
ALTER TABLE [dbo].[hoa_don] 
ADD [ghi_chu_noi_bo] [nvarchar](500) NULL
GO

-- Thêm trường mã vận đơn
ALTER TABLE [dbo].[hoa_don] 
ADD [ma_van_don] [nvarchar](50) NULL
GO

-- Thêm trường phương thức giao hàng
ALTER TABLE [dbo].[hoa_don] 
ADD [phuong_thuc_giao_hang] [nvarchar](100) NULL
GO

-- Thêm trường địa chỉ giao hàng chi tiết
ALTER TABLE [dbo].[hoa_don] 
ADD [dia_chi_giao_hang_chi_tiet] [nvarchar](500) NULL
GO

-- Thêm trường trạng thái thanh toán
ALTER TABLE [dbo].[hoa_don] 
ADD [trang_thai_thanh_toan] [int] NULL
GO

-- Thêm trường số tiền đã thanh toán
ALTER TABLE [dbo].[hoa_don] 
ADD [so_tien_da_thanh_toan] [decimal](18, 2) NULL
GO

-- Thêm trường số tiền còn lại
ALTER TABLE [dbo].[hoa_don] 
ADD [so_tien_con_lai] [decimal](18, 2) NULL
GO

-- Cập nhật giá trị mặc định cho các trường mới
ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [DF_hoa_don_thoi_gian_tao] DEFAULT (getdate()) FOR [thoi_gian_tao]
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [DF_hoa_don_thoi_gian_cap_nhat] DEFAULT (getdate()) FOR [thoi_gian_cap_nhat]
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [DF_hoa_don_trang_thai_thanh_toan] DEFAULT (0) FOR [trang_thai_thanh_toan]
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [DF_hoa_don_so_tien_da_thanh_toan] DEFAULT (0) FOR [so_tien_da_thanh_toan]
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [DF_hoa_don_so_tien_con_lai] DEFAULT (0) FOR [so_tien_con_lai]
GO

-- Thêm ràng buộc kiểm tra cho các trường mới
ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [CK_hoa_don_so_tien_da_thanh_toan] CHECK ([so_tien_da_thanh_toan] >= 0)
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [CK_hoa_don_so_tien_con_lai] CHECK ([so_tien_con_lai] >= 0)
GO

ALTER TABLE [dbo].[hoa_don] 
ADD CONSTRAINT [CK_hoa_don_trang_thai_thanh_toan] CHECK ([trang_thai_thanh_toan] IN (0, 1, 2, 3))
GO

-- Thêm comment cho các trường mới
EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Số điện thoại nhân viên xử lý đơn hàng', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'so_dien_thoai_nhan_vien'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Mô tả chi tiết loại đơn hàng (Bán hàng tại quầy, Bán hàng online)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'mo_ta_loai_don'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Thời gian tạo hóa đơn chi tiết (bao gồm giờ phút giây)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'thoi_gian_tao'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Thời gian cập nhật cuối cùng của hóa đơn', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'thoi_gian_cap_nhat'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Ghi chú nội bộ cho nhân viên xử lý', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'ghi_chu_noi_bo'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Mã vận đơn của đơn vị vận chuyển', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'ma_van_don'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Phương thức giao hàng (Giao tận nơi, Tại cửa hàng, Giao nhanh...)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'phuong_thuc_giao_hang'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Địa chỉ giao hàng chi tiết (có thể khác với địa chỉ khách hàng)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'dia_chi_giao_hang_chi_tiet'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Trạng thái thanh toán (0: Chưa thanh toán, 1: Đã thanh toán một phần, 2: Đã thanh toán đủ, 3: Hoàn tiền)', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'trang_thai_thanh_toan'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Số tiền đã thanh toán', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'so_tien_da_thanh_toan'
GO

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Số tiền còn lại cần thanh toán', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'hoa_don', 
    @level2type = N'COLUMN', @level2name = N'so_tien_con_lai'
GO

-- Thêm foreign key cho bảng timeline
ALTER TABLE [dbo].[timeline_don_hang]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO

ALTER TABLE [dbo].[timeline_don_hang]  WITH CHECK ADD FOREIGN KEY([id_nhan_vien])
REFERENCES [dbo].[nhan_vien] ([id])
GO

-- Thêm constraints cho bảng timeline
ALTER TABLE [dbo].[timeline_don_hang] 
ADD CONSTRAINT [DF_timeline_don_hang_thoi_gian] DEFAULT (getdate()) FOR [thoi_gian]
GO

ALTER TABLE [dbo].[timeline_don_hang] 
ADD CONSTRAINT [DF_timeline_don_hang_trang_thai] DEFAULT (1) FOR [trang_thai]
GO

ALTER TABLE [dbo].[timeline_don_hang] 
ADD CONSTRAINT [DF_timeline_don_hang_deleted] DEFAULT (0) FOR [deleted]
GO

-- Thêm comment cho bảng timeline
EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Bảng timeline theo dõi quá trình xử lý đơn hàng', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'timeline_don_hang'
GO

-- Query để lấy dữ liệu hóa đơn với đầy đủ thông tin
USE [GearUp]
GO

-- View để lấy thông tin hóa đơn đầy đủ
CREATE VIEW [dbo].[v_hoa_don_full_info] AS
SELECT 
    hd.id,
    hd.ma_hoa_don,
    hd.ten_hoa_don,
    hd.loai_don,
    hd.mo_ta_loai_don,
    hd.thoi_gian_tao,
    hd.ngay_tao,
    hd.ngay_thanh_toan,
    hd.tong_tien,
    hd.tong_tien_sau_giam,
    hd.phi_van_chuyen,
    hd.ghi_chu,
    hd.trang_thai,
    hd.deleted,
    
    -- Thông tin khách hàng
    hd.ten_khach_hang,
    hd.so_dien_thoai_khach_hang,
    hd.email_khach_hang,
    hd.dia_chi_khach_hang,
    
    -- Thông tin nhân viên
    hd.ten_nhan_vien,
    hd.ma_nhan_vien,
    hd.so_dien_thoai_nhan_vien,
    
    -- Thông tin phiếu giảm giá
    hd.ten_phieu_giam_gia,
    hd.ma_phieu_giam_gia,
    
    -- Thông tin bổ sung
    hd.thoi_gian_cap_nhat,
    hd.ghi_chu_noi_bo,
    hd.ma_van_don,
    hd.phuong_thuc_giao_hang,
    hd.dia_chi_giao_hang_chi_tiet,
    hd.trang_thai_thanh_toan,
    hd.so_tien_da_thanh_toan,
    hd.so_tien_con_lai
FROM [dbo].[hoa_don] hd
WHERE hd.deleted = 0
GO

-- Query mẫu để test dữ liệu
SELECT TOP 10 
    id,
    ma_hoa_don,
    ten_khach_hang,
    so_dien_thoai_khach_hang,
    ten_nhan_vien,
    mo_ta_loai_don,
    thoi_gian_tao,
    tong_tien_sau_giam,
    trang_thai
FROM [dbo].[v_hoa_don_full_info]
ORDER BY id DESC
GO

CREATE VIEW [dbo].[v_tin_nhan_chi_tiet] AS
SELECT 
    tn.id,
    tn.ma_tin_nhan,
    tn.noi_dung,
    tn.loai_tin_nhan,
    tn.da_doc,
    tn.thoi_gian_gui,
    tn.thoi_gian_doc,
    tn.id_nguoi_gui,
    ng.ten_nhan_vien AS ten_nguoi_gui,
    ng.ma_nhan_vien AS ma_nguoi_gui,
    ng.anh_nhan_vien AS anh_nguoi_gui,
    qh_gui.ten_quyen_han AS quyen_han_nguoi_gui,
    tn.id_nguoi_nhan,
    nn.ten_nhan_vien AS ten_nguoi_nhan,
    nn.ma_nhan_vien AS ma_nguoi_nhan,
    nn.anh_nhan_vien AS anh_nguoi_nhan,
    qh_nhan.ten_quyen_han AS quyen_han_nguoi_nhan,
    tn.trang_thai,
    tn.deleted
FROM 
    [dbo].[tin_nhan] tn
    INNER JOIN [dbo].[nhan_vien] ng ON tn.id_nguoi_gui = ng.id
    INNER JOIN [dbo].[nhan_vien] nn ON tn.id_nguoi_nhan = nn.id
    LEFT JOIN [dbo].[quyen_han] qh_gui ON ng.id_quyen_han = qh_gui.id
    LEFT JOIN [dbo].[quyen_han] qh_nhan ON nn.id_quyen_han = qh_nhan.id
WHERE 
    tn.deleted = 0
GO

CREATE VIEW [dbo].[v_cuoc_trao_doi_chi_tiet] AS
SELECT 
    ctd.id,
    ctd.ma_cuoc_trao_doi,
    ctd.id_nhan_vien_1,
    nv1.ten_nhan_vien AS ten_nhan_vien_1,
    nv1.ma_nhan_vien AS ma_nhan_vien_1,
    nv1.anh_nhan_vien AS anh_nhan_vien_1,
    qh1.ten_quyen_han AS quyen_han_nv1,
    ctd.id_nhan_vien_2,
    nv2.ten_nhan_vien AS ten_nhan_vien_2,
    nv2.ma_nhan_vien AS ma_nhan_vien_2,
    nv2.anh_nhan_vien AS anh_nhan_vien_2,
    qh2.ten_quyen_han AS quyen_han_nv2,
    ctd.tin_nhan_cuoi_cung,
    ctd.thoi_gian_tin_nhan_cuoi,
    ctd.id_nguoi_gui_cuoi,
    nguoi_gui_cuoi.ten_nhan_vien AS ten_nguoi_gui_cuoi,
    ctd.so_tin_nhan_chua_doc_nv1,
    ctd.so_tin_nhan_chua_doc_nv2,
    ctd.trang_thai,
    ctd.create_at
FROM 
    [dbo].[cuoc_trao_doi] ctd
    INNER JOIN [dbo].[nhan_vien] nv1 ON ctd.id_nhan_vien_1 = nv1.id
    INNER JOIN [dbo].[nhan_vien] nv2 ON ctd.id_nhan_vien_2 = nv2.id
    LEFT JOIN [dbo].[quyen_han] qh1 ON nv1.id_quyen_han = qh1.id
    LEFT JOIN [dbo].[quyen_han] qh2 ON nv2.id_quyen_han = qh2.id
    LEFT JOIN [dbo].[nhan_vien] nguoi_gui_cuoi ON ctd.id_nguoi_gui_cuoi = nguoi_gui_cuoi.id
WHERE 
    ctd.deleted = 0
GO

CREATE PROCEDURE [dbo].[sp_lay_danh_sach_cuoc_trao_doi]
    @id_nhan_vien INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        ctd.id,
        ctd.ma_cuoc_trao_doi,
        CASE 
            WHEN ctd.id_nhan_vien_1 = @id_nhan_vien THEN nv2.ten_nhan_vien
            ELSE nv1.ten_nhan_vien
        END AS ten_nguoi_chat,
        CASE 
            WHEN ctd.id_nhan_vien_1 = @id_nhan_vien THEN nv2.anh_nhan_vien
            ELSE nv1.anh_nhan_vien
        END AS anh_nguoi_chat,
        CASE 
            WHEN ctd.id_nhan_vien_1 = @id_nhan_vien THEN ctd.id_nhan_vien_2
            ELSE ctd.id_nhan_vien_1
        END AS id_nguoi_chat,
        ctd.tin_nhan_cuoi_cung,
        ctd.thoi_gian_tin_nhan_cuoi,
        CASE 
            WHEN ctd.id_nhan_vien_1 = @id_nhan_vien THEN ctd.so_tin_nhan_chua_doc_nv1
            ELSE ctd.so_tin_nhan_chua_doc_nv2
        END AS so_tin_chua_doc
    FROM 
        [dbo].[cuoc_trao_doi] ctd
        INNER JOIN [dbo].[nhan_vien] nv1 ON ctd.id_nhan_vien_1 = nv1.id
        INNER JOIN [dbo].[nhan_vien] nv2 ON ctd.id_nhan_vien_2 = nv2.id
    WHERE 
        (ctd.id_nhan_vien_1 = @id_nhan_vien OR ctd.id_nhan_vien_2 = @id_nhan_vien)
        AND ctd.deleted = 0
    ORDER BY 
        ctd.thoi_gian_tin_nhan_cuoi DESC
END
GO

CREATE PROCEDURE [dbo].[sp_lay_tin_nhan]
    @id_nguoi_1 INT,
    @id_nguoi_2 INT,
    @page INT = 1,
    @page_size INT = 50
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        tn.id,
        tn.ma_tin_nhan,
        tn.id_nguoi_gui,
        tn.id_nguoi_nhan,
        tn.noi_dung,
        tn.loai_tin_nhan,
        tn.da_doc,
        tn.thoi_gian_gui,
        tn.thoi_gian_doc,
        ng.ten_nhan_vien AS ten_nguoi_gui,
        ng.anh_nhan_vien AS anh_nguoi_gui
    FROM 
        [dbo].[tin_nhan] tn
        INNER JOIN [dbo].[nhan_vien] ng ON tn.id_nguoi_gui = ng.id
    WHERE 
        ((tn.id_nguoi_gui = @id_nguoi_1 AND tn.id_nguoi_nhan = @id_nguoi_2) 
         OR (tn.id_nguoi_gui = @id_nguoi_2 AND tn.id_nguoi_nhan = @id_nguoi_1))
        AND tn.deleted = 0
    ORDER BY 
        tn.thoi_gian_gui DESC
    OFFSET (@page - 1) * @page_size ROWS
    FETCH NEXT @page_size ROWS ONLY
END
GO

CREATE PROCEDURE [dbo].[sp_dem_tin_chua_doc]
    @id_nhan_vien INT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT COUNT(*) AS tong_tin_chua_doc
    FROM [dbo].[tin_nhan]
    WHERE 
        id_nguoi_nhan = @id_nhan_vien 
        AND da_doc = 0 
        AND deleted = 0
END
GO

USE [master]
GO
ALTER DATABASE [GearUp] SET  READ_WRITE 
GO
