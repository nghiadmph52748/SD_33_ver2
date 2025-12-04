USE [master]
GO
/****** Object:  Database [GearUp]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[hoa_don]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hoa_don](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NULL,
	[id_phieu_giam_gia] [int] NULL,
	[id_nhan_vien] [int] NULL,
	[ma_hoa_don] [nvarchar](10) NULL,
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
	[ngay_tao] [datetime] NULL,
	[ngay_thanh_toan] [datetime] NULL,
	[trang_thai] [bit] NULL,
	[deleted] [bit] NULL,
	[create_at] [datetime] NULL,
	[create_by] [int] NULL,
	[update_at] [datetime] NULL,
	[update_by] [int] NULL,
	[so_dien_thoai_nhan_vien] [varchar](12) NULL,
	[mo_ta_loai_don]  AS (CASE WHEN [loai_don]=(1) THEN N'Bán hàng tại quầy' ELSE N'Bán hàng online' END) PERSISTED,
	[thoi_gian_tao] [datetime] NULL,
	[thoi_gian_cap_nhat] [datetime] NULL,
	[ghi_chu_noi_bo] [nvarchar](500) NULL,
	[ma_van_don] [nvarchar](50) NULL,
	[phuong_thuc_giao_hang] [nvarchar](100) NULL,
	[dia_chi_giao_hang_chi_tiet] [nvarchar](500) NULL,
	[trang_thai_thanh_toan] [int] NULL,
	[so_tien_da_thanh_toan] [decimal](18, 2) NULL,
	[so_tien_con_lai] [decimal](18, 2) NULL,
	[phu_phi] [decimal](18, 2) NULL DEFAULT 0,
	[hoan_phi] [decimal](18, 2) NULL DEFAULT 0,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[v_hoa_don_full_info]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
    hd.phu_phi,
	 hd.hoan_phi,
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
/****** Object:  Table [dbo].[nhan_vien]    Script Date: 11/12/2025 10:29:48 AM ******/
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
    [reset_token] VARCHAR(255) NULL,
    [reset_token_expiry] DATETIME NULL
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[quyen_han]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[tin_nhan]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tin_nhan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_tin_nhan]  AS ('TN'+right('00000'+CONVERT([nvarchar](5),[id]),(5))) PERSISTED,
	[id_nguoi_gui] [int] NULL,
	[id_nguoi_nhan] [int] NULL,
	[id_khach_hang_gui] [int] NULL,
	[id_khach_hang_nhan] [int] NULL,
	[loai_tin_nhan_type] [nvarchar](20) NULL DEFAULT 'STAFF_STAFF',
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
/****** Object:  View [dbo].[v_tin_nhan_chi_tiet]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
/****** Object:  Table [dbo].[cuoc_trao_doi]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[cuoc_trao_doi](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ma_cuoc_trao_doi]  AS ('CTD'+right('00000'+CONVERT([nvarchar](5),[id]),(5))) PERSISTED,
	[id_nhan_vien_1] [int] NULL,
	[id_nhan_vien_2] [int] NULL,
	[id_khach_hang] [int] NULL,
	[id_nhan_vien] [int] NULL,
	[id_khach_hang_gui_cuoi] [int] NULL,
	[loai_cuoc_trao_doi] [nvarchar](20) NULL DEFAULT 'STAFF_STAFF',
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
/****** Object:  View [dbo].[v_cuoc_trao_doi_chi_tiet]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
/****** Object:  Table [dbo].[anh_san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[cai_dat_thong_bao]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[cai_dat_thong_bao](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_nguoi_dung] [int] NOT NULL,
	[bat_thong_bao_don_hang] [bit] NOT NULL,
	[bat_canh_bao_ton_kho] [bit] NOT NULL,
	[bat_thong_bao_he_thong] [bit] NOT NULL,
	[bat_tin_nhan_chat] [bit] NOT NULL,
	[bat_thong_bao_web] [bit] NOT NULL,
	[bat_thong_bao_email] [bit] NOT NULL,
	[bat_che_do_im_lang] [bit] NOT NULL,
	[gio_bat_dau_im_lang] [int] NOT NULL,
	[gio_ket_thuc_im_lang] [int] NOT NULL,
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
/****** Object:  Table [dbo].[chat_lieu]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[chi_tiet_dot_giam_gia]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[chi_tiet_phieu_giam_gia]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[chi_tiet_san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[chi_tiet_san_pham_anh]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[de_giay]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[dia_chi_khach_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[dot_giam_gia]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[dot_giam_gia_history]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[hinh_thuc_thanh_toan]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[hoa_don_chi_tiet]    Script Date: 11/12/2025 10:29:48 AM ******/
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
	[create_at] [datetime] NULL,
	[create_by] [int] NULL,
	[update_at] [datetime] NULL,
	[update_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[khach_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  View [dbo].[vw_khach_hang_rfm]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- View để tính toán RFM metrics cho khách hàng
-- RFM: Recency (days_since_last_order), Frequency (orders_count), Monetary (total_spent)
CREATE VIEW [dbo].[vw_khach_hang_rfm] AS
SELECT
    kh.id,
    kh.ma_khach_hang,
    kh.ten_khach_hang,
    kh.email,
    kh.so_dien_thoai,
    kh.ngay_sinh,
    kh.phan_loai,
    kh.trang_thai,
    kh.create_at,
    COUNT(hd.id) AS orders_count,
    MIN(hd.ngay_thanh_toan) AS first_order_at,
    MAX(hd.ngay_thanh_toan) AS last_order_at,
    ISNULL(SUM(ISNULL(hd.tong_tien_sau_giam, hd.tong_tien)), 0) AS total_spent,
    CASE 
        WHEN MAX(hd.ngay_thanh_toan) IS NOT NULL 
        THEN DATEDIFF(DAY, MAX(hd.ngay_thanh_toan), GETDATE())
        ELSE NULL
    END AS days_since_last_order
FROM 
    [dbo].[khach_hang] kh
    LEFT JOIN [dbo].[hoa_don] hd ON hd.id_khach_hang = kh.id 
        AND hd.deleted = 0 
        AND hd.trang_thai_thanh_toan IN (1, 2)
        AND hd.ngay_thanh_toan IS NOT NULL
WHERE 
    kh.deleted = 0
GROUP BY 
    kh.id, 
    kh.ma_khach_hang, 
    kh.ten_khach_hang, 
    kh.email, 
    kh.so_dien_thoai, 
    kh.ngay_sinh, 
    kh.phan_loai,
    kh.trang_thai,
    kh.create_at

GO
/****** Object:  Table [dbo].[kich_thuoc]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[loai_don_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
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
/****** Object:  Table [dbo].[mau_sac]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[nha_san_xuat]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[phieu_giam_gia]    Script Date: 11/12/2025 10:29:48 AM ******/
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
	[so_tien_toi_da] [decimal](18, 2) NULL,
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
/****** Object:  Table [dbo].[phieu_giam_gia_ca_nhan]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[phieu_giam_gia_ca_nhan](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NULL,
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
/****** Object:  Table [dbo].[phieu_giam_gia_history]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[phuong_thuc_thanh_toan]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[thanh_toan_don_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[thanh_toan_don_hang](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[txn_ref] [nvarchar](64) NOT NULL,
	[amount] [bigint] NULL,
	[status] [nvarchar](20) NULL,
	[created_at] [datetimeoffset](7) NOT NULL,
	[updated_at] [datetimeoffset](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[thong_bao]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[thong_bao](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[loai] [varchar](20) NOT NULL,
	[tieu_de] [nvarchar](200) NOT NULL,
	[tieu_de_phu] [nvarchar](200) NULL,
	[anh_dai_dien] [nvarchar](500) NULL,
	[noi_dung] [nvarchar](max) NULL,
	[thoi_gian_tao] [datetime2](7) NOT NULL,
	[trang_thai] [int] NOT NULL,
	[loai_tin_nhan] [int] NULL,
	[id_nguoi_dung] [int] NULL,
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
/****** Object:  Table [dbo].[thong_tin_don_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[lich_su_don_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[lich_su_don_hang](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_hoa_don] [int] NOT NULL,
	[id_nhan_vien] [int] NULL,
	[ma_lich_su_don_hang]  AS ('TL'+right('00000'+CONVERT([nvarchar](5),[ID]),(5))) PERSISTED,
	[trang_thai_cu] [nvarchar](100) NULL,
	[trang_thai_moi] [nvarchar](100) NOT NULL,
	[hanh_dong] [nvarchar](100) NOT NULL,
	[mo_ta] [nvarchar](500) NULL,
	[ghi_chu] [nvarchar](500) NULL,
	[thoi_gian] [datetime] NOT NULL,
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
/****** Object:  Table [dbo].[qr_sessions]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[qr_sessions](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[session_id] [nvarchar](36) NOT NULL,
	[order_code] [nvarchar](50) NOT NULL,
	[id_hoa_don] [int] NULL,
	[qr_code_url] [nvarchar](max) NULL,
	[status] [nvarchar](20) NOT NULL,
	[expires_at] [datetime2](7) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[cart_data_json] [nvarchar](max) NULL,
	[subtotal] [decimal](18, 2) NOT NULL,
	[discount_amount] [decimal](18, 2) NULL,
	[shipping_fee] [decimal](18, 2) NULL,
	[final_price] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[token_blacklist]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[trang_thai_don_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[trong_luong]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[xuat_xu]    Script Date: 11/12/2025 10:29:48 AM ******/
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
/****** Object:  Table [dbo].[ai_chat_history]    Script Date: 11/14/2025 08:00:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ai_chat_history](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_khach_hang] [int] NOT NULL,
	[session_id] [nvarchar](255) NOT NULL,
	[role] [nvarchar](50) NOT NULL,
	[content] [nvarchar](max) NOT NULL,
	[timestamp] [datetime2](7) NULL DEFAULT GETDATE(),
	[created_at] [datetime2](7) NULL DEFAULT GETDATE(),
	[created_by] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
CREATE TABLE dbo.ca_lam_viec (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ten_ca NVARCHAR(100) NOT NULL,
    thoi_gian_bat_dau TIME NOT NULL,
    thoi_gian_ket_thuc TIME NOT NULL,
    trang_thai BIT NULL, 
    ghi_chu NVARCHAR(255) NULL
);


CREATE TABLE dbo.lich_lam_viec ( 
    id INT IDENTITY(1,1) PRIMARY KEY, 
    nhan_vien_id INT NOT NULL, 
    ca_lam_viec_id INT NOT NULL,
    ngay_lam_viec DATE NOT NULL,
    trang_thai BIT NULL, 
    ghi_chu NVARCHAR(255) NULL,

    CONSTRAINT FK_lich_lam_viec_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES dbo.nhan_vien(id),
    CONSTRAINT FK_lich_lam_viec_ca_lam_viec FOREIGN KEY (ca_lam_viec_id) REFERENCES dbo.ca_lam_viec(id)
);


	CREATE TABLE dbo.giao_ca (
		id INT IDENTITY(1,1) PRIMARY KEY,
		nguoi_giao_id INT NOT NULL,
		nguoi_nhan_id INT NOT NULL,
		ca_lam_viec_id INT NOT NULL,
		thoi_gian_giao_ca DATETIME2 NOT NULL,
		tong_tien_ban_dau DECIMAL(18,2) NULL, 
		tong_tien_cuoi_ca DECIMAL(18,2) NULL, 
		tong_tien_mat DECIMAL(18,2) NULL,
		tong_tien_chuyen_khoan DECIMAL(18,2) NULL,
		tong_tien_khac DECIMAL(18,2) NULL,
		tong_doanh_thu DECIMAL(18,2) NULL,
		tien_phat_sinh DECIMAL(18,2) NULL,
		tong_tien_thuc_te DECIMAL(18,2) NULL,
		chenh_lech DECIMAL(18,2) NULL,
		tien_mat_nop_lai DECIMAL(18,2) NULL,
		ghi_chu NVARCHAR(255) NULL,
		so_tien_nhan_thuc_te DECIMAL(18,2) NULL, 
		trang_thai_xac_nhan NVARCHAR(50) NOT NULL DEFAULT (N'Chưa xác nhận'), 
		thoi_gian_xac_nhan DATETIME2 NULL,
		ghi_chu_xac_nhan NVARCHAR(255) NULL, 
		trang_thai_ca NVARCHAR(50) NULL DEFAULT (N'Đang làm'),
		CONSTRAINT FK_giao_ca_nguoi_giao FOREIGN KEY (nguoi_giao_id) REFERENCES dbo.nhan_vien(id),
		CONSTRAINT FK_giao_ca_nguoi_nhan FOREIGN KEY (nguoi_nhan_id) REFERENCES dbo.nhan_vien(id),
		CONSTRAINT FK_giao_ca_ca_lam_viec FOREIGN KEY (ca_lam_viec_id) REFERENCES dbo.ca_lam_viec(id)
	);
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
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (1, 1, 1, 1, 1, 1, 1, NULL, 48, CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày thể thao cao cấp Nike', NULL, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Đen - Size 39')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (2, 1, 2, 2, 1, 1, 1, NULL, 44, CAST(2500000.00 AS Decimal(18, 2)), 1, N'Giày thể thao cao cấp Nike', NULL, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Trắng - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (3, 2, 3, 3, 2, 2, 2, NULL, 40, CAST(3200000.00 AS Decimal(18, 2)), 1, N'Giày chạy bộ Adidas', NULL, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL, N'Giày Adidas Ultraboost 22 - Đỏ - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (4, 4, 1, 3, 1, 1, 1, NULL, 1, CAST(100.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc - Đen - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (5, 4, 1, 2, 1, 1, 1, NULL, 1, CAST(100.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc - Đen - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (6, 1, 1, 3, 1, 1, 1, NULL, 6, CAST(100.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, CAST(N'2025-10-11' AS Date), 1, N'Giày Nike Air Max 270 - Đen - Size 41')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (7, 1, 1, 2, 1, 1, 1, NULL, 5, CAST(100.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Đen - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (8, 1, 2, 2, 2, 1, 1, NULL, 1, CAST(1.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Giày Nike Air Max 270 - Trắng - Size 40')
GO
INSERT [dbo].[chi_tiet_san_pham] ([id], [id_san_pham], [id_mau_sac], [id_kich_thuoc], [id_de_giay], [id_chat_lieu], [id_trong_luong], [ten_chi_tiet_san_pham], [so_luong], [gia_ban], [trang_thai], [ghi_chu], [qrcode], [deleted], [create_at], [create_by], [update_at], [update_by], [ten_san_pham_chi_tiet]) VALUES (9, 4, 2, 4, 2, 1, 1, NULL, 2, CAST(3.00 AS Decimal(18, 2)), 1, NULL, NULL, 0, CAST(N'2025-10-11' AS Date), 1, NULL, NULL, N'Abc + Trắng + 38')
GO
SET IDENTITY_INSERT [dbo].[chi_tiet_san_pham] OFF
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
SET IDENTITY_INSERT [dbo].[dia_chi_khach_hang] ON 
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [trang_thai], [deleted]) VALUES (3, 3, N'Nhà riêng', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'Số 15 Ngõ 68 Cầu Giấy', 1, 1, 0)
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [trang_thai], [deleted]) VALUES (4, 4, N'Công ty', N'Hà Nội', N'Hoàn Kiếm', N'Phan Chu Trinh', N'Phòng 1203, Tòa nhà Prime Center', 1, 1, 0)
GO
INSERT [dbo].[dia_chi_khach_hang] ([id], [id_khach_hang], [ten_dia_chi], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [mac_dinh], [trang_thai], [deleted]) VALUES (5, 5, N'Nhà riêng', N'Đà Nẵng', N'Hải Châu', N'Thanh Bình', N'23 Nguyễn Văn Linh', 1, 1, 0)
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
SET IDENTITY_INSERT [dbo].[khach_hang] ON 
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Phạm Văn A', N'khachhang1', N'$2b$10$0mtckgjPZO1VE87q/FbAK.P/m5sgMZpBvglzziHgAzuJwaQ1G4eum', N'khachhang1@gmail.com', N'0123456789', 1, CAST(N'1998-03-10' AS Date), NULL, 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Hoàng Thị B', N'khachhang2', N'$2a$10$moruvP4SlJifGfvkFTXuSuaXWQ58UlnCHWEc/PtmteILY9PeCUDU2', N'khachhang2@gmail.com', N'0987654321', 0, CAST(N'1996-07-25' AS Date), NULL, 1, 0, NULL, NULL, CAST(N'2025-10-13' AS Date), 1)
GO
SET IDENTITY_INSERT [dbo].[khach_hang] OFF
GO
SET IDENTITY_INSERT [dbo].[khach_hang] ON
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, N'Lê Thành C', N'khachhang3', N'$2b$10$0mtckgjPZO1VE87q/FbAK.P/m5sgMZpBvglzziHgAzuJwaQ1G4eum', N'khachhang3@gmail.com', N'0901122334', 1, CAST(N'1993-05-20' AS Date), NULL, 1, 0, CAST(N'2025-09-15' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, N'Trần Quỳnh D', N'khachhang4', N'$2a$10$moruvP4SlJifGfvkFTXuSuaXWQ58UlnCHWEc/PtmteILY9PeCUDU2', N'khachhang4@gmail.com', N'0912233445', 0, CAST(N'1998-09-15' AS Date), NULL, 1, 0, CAST(N'2025-10-20' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[khach_hang] ([id], [ten_khach_hang], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [gioi_tinh], [ngay_sinh], [phan_loai], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, N'Nguyễn Minh E', N'khachhang5', N'$2b$10$0mtckgjPZO1VE87q/FbAK.P/m5sgMZpBvglzziHgAzuJwaQ1G4eum', N'khachhang5@gmail.com', N'0933344556', 1, CAST(N'1988-01-12' AS Date), NULL, 1, 0, CAST(N'2025-06-10' AS Date), 1, NULL, NULL)
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
SET IDENTITY_INSERT [dbo].[loai_don_hang] ON 
GO
INSERT [dbo].[loai_don_hang] ([id], [ten_loai_don], [mo_ta], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'Bán hàng tại quầy', N'Đơn hàng được bán trực tiếp tại cửa hàng', 1, 0, CAST(N'2025-01-15' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[loai_don_hang] ([id], [ten_loai_don], [mo_ta], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, N'Bán hàng online', N'Đơn hàng được đặt qua website hoặc ứng dụng', 1, 0, CAST(N'2025-01-15' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[loai_don_hang] OFF
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
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (1, 1, N'Nguyễn Đại Ca', N'admin', N'$2b$10$U82w.VimlpY59wUI5WMHI.1POdYFduZtlm9m8fPdd64EyAcL4P/Gi', N'daica@gearup.vn', N'0987654321', NULL, CAST(N'1990-01-01' AS Date), N'Quản trị viên hệ thống', N'Hà Nội', N'Hoàn Kiếm', N'Phúc Tân', N'', 1, N'', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[nhan_vien] ([id], [id_quyen_han], [ten_nhan_vien], [ten_tai_khoan], [mat_khau], [email], [so_dien_thoai], [anh_nhan_vien], [ngay_sinh], [ghi_chu], [thanh_pho], [quan], [phuong], [dia_chi_cu_the], [gioi_tinh], [cccd], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (2, 1, N'Trần Thị Em', N'them', N'$2a$10$8K2L0IgfzC3QHICobxwVOXdKUOz5hdzrQyLj8LrQ8D6cjzVzVzQG', N'them.tran@gearup.vn', N'0978123456', NULL, CAST(N'1995-02-15' AS Date), N'Nhân viên bán hàng tại quầy', N'Hà Nội', N'Ba Đình', N'Ngọc Hà', N'Số 45 Ngõ 89 Đường Ngọc Hà', 0, N'012345678901', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[nhan_vien] OFF
GO
SET IDENTITY_INSERT [dbo].[phieu_giam_gia] ON 
GO
INSERT [dbo].[phieu_giam_gia] ([id], [ma_phieu_giam_gia], [ten_phieu_giam_gia], [loai_phieu_giam_gia], [gia_tri_giam_gia], [hoa_don_toi_thieu], [so_luong_dung], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [mo_ta], [noi_bat], [deleted], [created_at], [updated_at], [create_at], [create_by], [update_at], [update_by]) VALUES (1, N'PGG00001', N'Giảm giá 10% cho đơn hàng đầu tiên', 0, CAST(10.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)), 98, CAST(N'2025-10-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, N'Áp dụng cho khách hàng mới', 0, 0, CAST(N'2025-10-13T14:42:01.113' AS DateTime), CAST(N'2025-10-13T14:42:01.113' AS DateTime), CAST(N'2025-10-01' AS Date), 1, NULL, NULL)
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
SET IDENTITY_INSERT [dbo].[phieu_giam_gia_ca_nhan] ON 
GO
INSERT [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_khach_hang], [id_phieu_giam_gia], [ten_phieu_giam_gia_ca_nhan], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (3, 3, 1, N'Phiếu khách 3 - giảm 10%', CAST(N'2025-10-15T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-10-15' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_khach_hang], [id_phieu_giam_gia], [ten_phieu_giam_gia_ca_nhan], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (4, 3, 2, N'Phiếu khách 3 - giảm 200k', CAST(N'2025-11-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-11-01' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[phieu_giam_gia_ca_nhan] ([id], [id_khach_hang], [id_phieu_giam_gia], [ten_phieu_giam_gia_ca_nhan], [ngay_nhan], [ngay_het_han], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (5, 5, 1, N'Phiếu khách 5 - quay lại mua', CAST(N'2025-07-01T00:00:00.000' AS DateTime), CAST(N'2025-12-31T23:59:59.000' AS DateTime), 1, 0, CAST(N'2025-07-01' AS Date), 1, NULL, NULL)
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
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (6, N'Đã huỷ', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (7, N'Hoàn thành', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
GO
INSERT [dbo].[trang_thai_don_hang] ([id], [ten_trang_thai_don_hang], [trang_thai], [deleted], [create_at], [create_by], [update_at], [update_by]) VALUES (8, N'Thay đổi địa chỉ giao hàng', 1, 0, CAST(N'2025-09-27' AS Date), 1, NULL, NULL)
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
/****** Object:  Index [UQ__anh_san___F73EF501423DF869]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[anh_san_pham] ADD UNIQUE NONCLUSTERED 
(
	[duong_dan_anh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_cai_dat_thong_bao_nguoi_dung]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  CONSTRAINT [UQ_cai_dat_thong_bao_nguoi_dung] UNIQUE NONCLUSTERED 
(
	[id_nguoi_dung] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_cai_dat_thong_bao_nguoi_dung]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_cai_dat_thong_bao_nguoi_dung] ON [dbo].[cai_dat_thong_bao]
(
	[id_nguoi_dung] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__chat_lie__47FA2872C3F37194]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[chat_lieu] ADD UNIQUE NONCLUSTERED 
(
	[ten_chat_lieu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_dot_giam_gia_dot]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_dot_giam_gia_dot] ON [dbo].[chi_tiet_dot_giam_gia]
(
	[id_dot_giam_gia] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_dot_giam_gia_san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_dot_giam_gia_san_pham] ON [dbo].[chi_tiet_dot_giam_gia]
(
	[id_chi_tiet_san_pham] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_san_pham_kich_thuoc]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_san_pham_kich_thuoc] ON [dbo].[chi_tiet_san_pham]
(
	[id_kich_thuoc] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_san_pham_mau_sac]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_san_pham_mau_sac] ON [dbo].[chi_tiet_san_pham]
(
	[id_mau_sac] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_san_pham_san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_san_pham_san_pham] ON [dbo].[chi_tiet_san_pham]
(
	[id_san_pham] ASC
)
INCLUDE([gia_ban],[so_luong],[trang_thai]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_san_pham_so_luong]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_san_pham_so_luong] ON [dbo].[chi_tiet_san_pham]
(
	[so_luong] ASC
)
WHERE ([deleted]=(0) AND [trang_thai]=(1) AND [so_luong]>(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_chi_tiet_san_pham_trang_thai]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_chi_tiet_san_pham_trang_thai] ON [dbo].[chi_tiet_san_pham]
(
	[trang_thai] ASC,
	[id_san_pham] ASC
)
INCLUDE([so_luong],[gia_ban]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_cuoc_trao_doi_nhan_vien]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_cuoc_trao_doi_nhan_vien] ON [dbo].[cuoc_trao_doi]
(
	[id_nhan_vien_1] ASC,
	[id_nhan_vien_2] ASC,
	[thoi_gian_tin_nhan_cuoi] DESC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_cuoc_trao_doi_nhan_vien_1]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_cuoc_trao_doi_nhan_vien_1] ON [dbo].[cuoc_trao_doi]
(
	[id_nhan_vien_1] ASC
)
INCLUDE([thoi_gian_tin_nhan_cuoi],[so_tin_nhan_chua_doc_nv1]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_cuoc_trao_doi_nhan_vien_2]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_cuoc_trao_doi_nhan_vien_2] ON [dbo].[cuoc_trao_doi]
(
	[id_nhan_vien_2] ASC
)
INCLUDE([thoi_gian_tin_nhan_cuoi],[so_tin_nhan_chua_doc_nv2]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
-- Unique index for staff-staff conversations
CREATE UNIQUE NONCLUSTERED INDEX [UQ_cuoc_trao_doi_staff_staff] ON [dbo].[cuoc_trao_doi]
(
	[id_nhan_vien_1] ASC,
	[id_nhan_vien_2] ASC
)
WHERE ([deleted]=(0) AND [loai_cuoc_trao_doi]='STAFF_STAFF' AND [id_nhan_vien_1] IS NOT NULL AND [id_nhan_vien_2] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
-- Unique index for customer-staff conversations
CREATE UNIQUE NONCLUSTERED INDEX [UQ_cuoc_trao_doi_customer_staff] ON [dbo].[cuoc_trao_doi]
(
	[id_khach_hang] ASC,
	[id_nhan_vien] ASC
)
WHERE ([deleted]=(0) AND [loai_cuoc_trao_doi]='CUSTOMER_STAFF' AND [id_khach_hang] IS NOT NULL AND [id_nhan_vien] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__de_giay__C3D40955612A1C73]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[de_giay] ADD UNIQUE NONCLUSTERED 
(
	[ten_de_giay] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_dia_chi_khach_hang_khach_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_dia_chi_khach_hang_khach_hang] ON [dbo].[dia_chi_khach_hang]
(
	[id_khach_hang] ASC
)
INCLUDE([mac_dinh],[trang_thai]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__dot_giam__E885712BA9A1A897]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[dot_giam_gia] ADD UNIQUE NONCLUSTERED 
(
	[ten_dot_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_dot_giam_gia_ma]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[dot_giam_gia] ADD  CONSTRAINT [UQ_dot_giam_gia_ma] UNIQUE NONCLUSTERED 
(
	[ma_dot_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_dot_giam_gia_ngay]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_dot_giam_gia_ngay] ON [dbo].[dot_giam_gia]
(
	[ngay_bat_dau] ASC,
	[ngay_ket_thuc] ASC
)
INCLUDE([ten_dot_giam_gia],[gia_tri_giam_gia]) 
WHERE ([deleted]=(0) AND [trang_thai]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_dot_giam_gia_history_composite]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_dot_giam_gia_history_composite] ON [dbo].[dot_giam_gia_history]
(
	[id_dot_giam_gia] ASC,
	[ngay_thay_doi] DESC
)
INCLUDE([id_nhan_vien],[hanh_dong]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_khach_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_khach_hang] ON [dbo].[hoa_don]
(
	[id_khach_hang] ASC
)
INCLUDE([tong_tien_sau_giam],[ngay_tao],[trang_thai]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_ngay_tao]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_ngay_tao] ON [dbo].[hoa_don]
(
	[ngay_tao] DESC,
	[trang_thai] ASC
)
INCLUDE([tong_tien_sau_giam],[id_khach_hang]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_nhan_vien]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_nhan_vien] ON [dbo].[hoa_don]
(
	[id_nhan_vien] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_phieu_giam_gia]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_phieu_giam_gia] ON [dbo].[hoa_don]
(
	[id_phieu_giam_gia] ASC
)
WHERE ([id_phieu_giam_gia] IS NOT NULL AND [deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_trang_thai]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_trang_thai] ON [dbo].[hoa_don]
(
	[trang_thai] ASC,
	[ngay_tao] DESC
)
INCLUDE([ma_hoa_don],[tong_tien_sau_giam]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_hoa_don_ma_hoa_don]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_hoa_don_ma_hoa_don] ON [dbo].[hoa_don]
(
	[ma_hoa_don] ASC
)
WHERE ([ma_hoa_don] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_chi_tiet_hoa_don]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_chi_tiet_hoa_don] ON [dbo].[hoa_don_chi_tiet]
(
	[id_hoa_don] ASC
)
INCLUDE([so_luong],[gia_ban],[thanh_tien]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_hoa_don_chi_tiet_san_pham]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_hoa_don_chi_tiet_san_pham] ON [dbo].[hoa_don_chi_tiet]
(
	[id_chi_tiet_san_pham] ASC
)
INCLUDE([so_luong],[gia_ban]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__17112F094200345A]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__AB6E61643AD4DF9A]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__khach_ha__BD03D94CDC3358A8]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[khach_hang] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_khach_hang_search]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_khach_hang_search] ON [dbo].[khach_hang]
(
	[ten_khach_hang] ASC,
	[so_dien_thoai] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__kich_thu__06BE27955E7C4A7E]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[kich_thuoc] ADD UNIQUE NONCLUSTERED 
(
	[ten_kich_thuoc] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__mau_sac__25764485595621CD]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[mau_sac] ADD UNIQUE NONCLUSTERED 
(
	[ten_mau_sac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nha_san___700B0905E0E74681]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[nha_san_xuat] ADD UNIQUE NONCLUSTERED 
(
	[ten_nha_san_xuat] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__17112F09DD8B858D]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[ten_tai_khoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__AB6E6164786A580A]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__nhan_vie__BD03D94C35B3FECA]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[nhan_vien] ADD UNIQUE NONCLUSTERED 
(
	[so_dien_thoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_nhan_vien_search]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_nhan_vien_search] ON [dbo].[nhan_vien]
(
	[ten_nhan_vien] ASC,
	[so_dien_thoai] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_phieu_giam_gia_ma]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[phieu_giam_gia] ADD  CONSTRAINT [UQ_phieu_giam_gia_ma] UNIQUE NONCLUSTERED 
(
	[ma_phieu_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_phieu_giam_gia_ngay]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_phieu_giam_gia_ngay] ON [dbo].[phieu_giam_gia]
(
	[ngay_bat_dau] ASC,
	[ngay_ket_thuc] ASC
)
WHERE ([deleted]=(0) AND [trang_thai]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_phieu_giam_gia_noi_bat]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_phieu_giam_gia_noi_bat] ON [dbo].[phieu_giam_gia]
(
	[noi_bat] ASC,
	[trang_thai] ASC
)
INCLUDE([ma_phieu_giam_gia],[ten_phieu_giam_gia],[ngay_bat_dau],[ngay_ket_thuc]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_phieu_giam_gia_ca_nhan_khach_hang]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_phieu_giam_gia_ca_nhan_khach_hang] ON [dbo].[phieu_giam_gia_ca_nhan]
(
	[id_khach_hang] ASC
)
INCLUDE([id_phieu_giam_gia],[trang_thai]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_phieu_giam_gia_ca_nhan_phieu]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_phieu_giam_gia_ca_nhan_phieu] ON [dbo].[phieu_giam_gia_ca_nhan]
(
	[id_phieu_giam_gia] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_phieu_giam_gia_history_id_phieu]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_id_phieu] ON [dbo].[phieu_giam_gia_history]
(
	[id_phieu_giam_gia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_phieu_giam_gia_history_ngay_thay_doi]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [idx_phieu_giam_gia_history_ngay_thay_doi] ON [dbo].[phieu_giam_gia_history]
(
	[ngay_thay_doi] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_phieu_giam_gia_history_composite]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_phieu_giam_gia_history_composite] ON [dbo].[phieu_giam_gia_history]
(
	[id_phieu_giam_gia] ASC,
	[ngay_thay_doi] DESC
)
INCLUDE([id_nhan_vien],[hanh_dong]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__quyen_ha__1EABFF49CA767B83]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[quyen_han] ADD UNIQUE NONCLUSTERED 
(
	[ten_quyen_han] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__san_pham__BA66C03132014C3D]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[san_pham] ADD UNIQUE NONCLUSTERED 
(
	[ten_san_pham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_san_pham_nha_san_xuat]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_san_pham_nha_san_xuat] ON [dbo].[san_pham]
(
	[id_nha_san_xuat] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_san_pham_ten_search]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_san_pham_ten_search] ON [dbo].[san_pham]
(
	[ten_san_pham] ASC
)
INCLUDE([trang_thai]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_san_pham_xuat_xu]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_san_pham_xuat_xu] ON [dbo].[san_pham]
(
	[id_xuat_xu] ASC
)
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__thanh_to__B914BF1B7D6885D9]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[thanh_toan_don_hang] ADD UNIQUE NONCLUSTERED 
(
	[txn_ref] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_thong_bao_chua_doc]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_thong_bao_chua_doc] ON [dbo].[thong_bao]
(
	[id_nguoi_dung] ASC,
	[trang_thai] ASC
)
WHERE ([trang_thai]=(0) AND [deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_thong_bao_nguoi_dung]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_thong_bao_nguoi_dung] ON [dbo].[thong_bao]
(
	[id_nguoi_dung] ASC,
	[trang_thai] ASC,
	[deleted] ASC,
	[thoi_gian_tao] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_lich_su_don_hang_composite]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_lich_su_don_hang_composite] ON [dbo].[lich_su_don_hang]
(
	[id_hoa_don] ASC,
	[thoi_gian] DESC
)
INCLUDE([trang_thai_moi],[ghi_chu]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_qr_sessions_session]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_qr_sessions_session] ON [dbo].[qr_sessions]
(
	[session_id] ASC
)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_qr_sessions_status_expiry]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_qr_sessions_status_expiry] ON [dbo].[qr_sessions]
(
	[status] ASC,
	[expires_at] DESC
)
INCLUDE([order_code],[final_price]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_qr_sessions_order_code]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_qr_sessions_order_code] ON [dbo].[qr_sessions]
(
	[order_code] ASC
)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_chua_doc]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_chua_doc] ON [dbo].[tin_nhan]
(
	[id_nguoi_nhan] ASC,
	[da_doc] ASC
)
WHERE ([da_doc]=(0) AND [deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_conversation]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_conversation] ON [dbo].[tin_nhan]
(
	[id_nguoi_gui] ASC,
	[id_nguoi_nhan] ASC,
	[thoi_gian_gui] DESC
)
INCLUDE([noi_dung],[da_doc],[loai_tin_nhan]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_da_doc]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_da_doc] ON [dbo].[tin_nhan]
(
	[da_doc] ASC,
	[id_nguoi_nhan] ASC
)
INCLUDE([thoi_gian_gui],[id_nguoi_gui]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_nguoi_gui]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_nguoi_gui] ON [dbo].[tin_nhan]
(
	[id_nguoi_gui] ASC
)
INCLUDE([thoi_gian_gui],[da_doc]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_nguoi_nhan]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_nguoi_nhan] ON [dbo].[tin_nhan]
(
	[id_nguoi_nhan] ASC,
	[da_doc] ASC
)
INCLUDE([thoi_gian_gui],[id_nguoi_gui]) 
WHERE ([deleted]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_tin_nhan_thoi_gian]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [IX_tin_nhan_thoi_gian] ON [dbo].[tin_nhan]
(
	[thoi_gian_gui] DESC
)
INCLUDE([id_nguoi_gui],[id_nguoi_nhan],[noi_dung]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tin_nhan_khach_hang_gui' AND object_id = OBJECT_ID('tin_nhan'))
    CREATE NONCLUSTERED INDEX IX_tin_nhan_khach_hang_gui ON [dbo].[tin_nhan]([id_khach_hang_gui]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tin_nhan_khach_hang_nhan' AND object_id = OBJECT_ID('tin_nhan'))
    CREATE NONCLUSTERED INDEX IX_tin_nhan_khach_hang_nhan ON [dbo].[tin_nhan]([id_khach_hang_nhan]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tin_nhan_loai_type' AND object_id = OBJECT_ID('tin_nhan'))
    CREATE NONCLUSTERED INDEX IX_tin_nhan_loai_type ON [dbo].[tin_nhan]([loai_tin_nhan_type]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cuoc_trao_doi_khach_hang' AND object_id = OBJECT_ID('cuoc_trao_doi'))
    CREATE NONCLUSTERED INDEX IX_cuoc_trao_doi_khach_hang ON [dbo].[cuoc_trao_doi]([id_khach_hang]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cuoc_trao_doi_nhan_vien_single' AND object_id = OBJECT_ID('cuoc_trao_doi'))
    CREATE NONCLUSTERED INDEX IX_cuoc_trao_doi_nhan_vien_single ON [dbo].[cuoc_trao_doi]([id_nhan_vien]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_cuoc_trao_doi_loai' AND object_id = OBJECT_ID('cuoc_trao_doi'))
    CREATE NONCLUSTERED INDEX IX_cuoc_trao_doi_loai ON [dbo].[cuoc_trao_doi]([loai_cuoc_trao_doi]) WHERE [deleted] = 0
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_ai_chat_customer' AND object_id = OBJECT_ID('ai_chat_history'))
    CREATE NONCLUSTERED INDEX idx_ai_chat_customer ON [dbo].[ai_chat_history]([id_khach_hang])
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_ai_chat_session' AND object_id = OBJECT_ID('ai_chat_history'))
    CREATE NONCLUSTERED INDEX idx_ai_chat_session ON [dbo].[ai_chat_history]([session_id])
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_ai_chat_timestamp' AND object_id = OBJECT_ID('ai_chat_history'))
    CREATE NONCLUSTERED INDEX idx_ai_chat_timestamp ON [dbo].[ai_chat_history]([timestamp] DESC)
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [uk_token_blacklist_token]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[token_blacklist] ADD  CONSTRAINT [uk_token_blacklist_token] UNIQUE NONCLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [idx_token_blacklist_expiry_date]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_expiry_date] ON [dbo].[token_blacklist]
(
	[expiry_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_token]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_token] ON [dbo].[token_blacklist]
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [idx_token_blacklist_username]    Script Date: 11/12/2025 10:29:48 AM ******/
CREATE NONCLUSTERED INDEX [idx_token_blacklist_username] ON [dbo].[token_blacklist]
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__trong_lu__399742A0B934375E]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[trong_luong] ADD UNIQUE NONCLUSTERED 
(
	[ten_trong_luong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__xuat_xu__BA9B8924F7B3F1AA]    Script Date: 11/12/2025 10:29:48 AM ******/
ALTER TABLE [dbo].[xuat_xu] ADD UNIQUE NONCLUSTERED 
(
	[ten_xuat_xu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[anh_san_pham] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[anh_san_pham] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((1)) FOR [bat_thong_bao_don_hang]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((1)) FOR [bat_canh_bao_ton_kho]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((1)) FOR [bat_thong_bao_he_thong]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((1)) FOR [bat_tin_nhan_chat]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((1)) FOR [bat_thong_bao_web]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((0)) FOR [bat_thong_bao_email]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((0)) FOR [bat_che_do_im_lang]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((22)) FOR [gio_bat_dau_im_lang]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((7)) FOR [gio_ket_thuc_im_lang]
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] ADD  DEFAULT ((0)) FOR [deleted]
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
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoa_don_thoi_gian_tao]  DEFAULT (getdate()) FOR [thoi_gian_tao]
GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoa_don_thoi_gian_cap_nhat]  DEFAULT (getdate()) FOR [thoi_gian_cap_nhat]
GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoa_don_trang_thai_thanh_toan]  DEFAULT ((0)) FOR [trang_thai_thanh_toan]
GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoa_don_so_tien_da_thanh_toan]  DEFAULT ((0)) FOR [so_tien_da_thanh_toan]
GO
ALTER TABLE [dbo].[hoa_don] ADD  CONSTRAINT [DF_hoa_don_so_tien_con_lai]  DEFAULT ((0)) FOR [so_tien_con_lai]
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
ALTER TABLE [dbo].[thanh_toan_don_hang] ADD  CONSTRAINT [DF_thanh_toan_don_hang_created_at]  DEFAULT (sysdatetimeoffset()) FOR [created_at]
GO
ALTER TABLE [dbo].[thanh_toan_don_hang] ADD  CONSTRAINT [DF_thanh_toan_don_hang_updated_at]  DEFAULT (sysdatetimeoffset()) FOR [updated_at]
GO
ALTER TABLE [dbo].[thong_bao] ADD  DEFAULT (getdate()) FOR [thoi_gian_tao]
GO
ALTER TABLE [dbo].[thong_bao] ADD  DEFAULT ((0)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[thong_bao] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[thong_tin_don_hang] ADD  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[thong_tin_don_hang] ADD  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[lich_su_don_hang] ADD  CONSTRAINT [DF_lich_su_don_hang_thoi_gian]  DEFAULT (getdate()) FOR [thoi_gian]
GO
ALTER TABLE [dbo].[lich_su_don_hang] ADD  CONSTRAINT [DF_lich_su_don_hang_trang_thai]  DEFAULT ((1)) FOR [trang_thai]
GO
ALTER TABLE [dbo].[lich_su_don_hang] ADD  CONSTRAINT [DF_lich_su_don_hang_deleted]  DEFAULT ((0)) FOR [deleted]
GO
ALTER TABLE [dbo].[qr_sessions] ADD  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[qr_sessions] ADD  DEFAULT (N'PENDING') FOR [status]
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
ALTER TABLE [dbo].[cai_dat_thong_bao]  WITH CHECK ADD  CONSTRAINT [FK_cai_dat_thong_bao_nhan_vien] FOREIGN KEY([id_nguoi_dung])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[cai_dat_thong_bao] CHECK CONSTRAINT [FK_cai_dat_thong_bao_nhan_vien]
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
ALTER TABLE [dbo].[qr_sessions]  WITH CHECK ADD  CONSTRAINT [FK_qr_sessions_hoa_don] FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[qr_sessions] CHECK CONSTRAINT [FK_qr_sessions_hoa_don]
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [FK_cuoc_trao_doi_nguoi_gui_cuoi] FOREIGN KEY([id_nguoi_gui_cuoi])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [FK_cuoc_trao_doi_nguoi_gui_cuoi]
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT FK_cuoc_trao_doi_khach_hang 
FOREIGN KEY([id_khach_hang]) REFERENCES [dbo].[khach_hang]([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT FK_cuoc_trao_doi_nhan_vien_single 
FOREIGN KEY([id_nhan_vien]) REFERENCES [dbo].[nhan_vien]([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] WITH CHECK 
ADD CONSTRAINT FK_cuoc_trao_doi_khach_hang_gui_cuoi 
FOREIGN KEY([id_khach_hang_gui_cuoi]) REFERENCES [dbo].[khach_hang]([id])
GO
ALTER TABLE [dbo].[ai_chat_history] WITH CHECK 
ADD CONSTRAINT FK_ai_chat_history_khach_hang 
FOREIGN KEY([id_khach_hang]) REFERENCES [dbo].[khach_hang]([id]) ON DELETE CASCADE
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [FK_cuoc_trao_doi_nhan_vien_1] FOREIGN KEY([id_nhan_vien_1])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [FK_cuoc_trao_doi_nhan_vien_1]
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [FK_cuoc_trao_doi_nhan_vien_2] FOREIGN KEY([id_nhan_vien_2])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [FK_cuoc_trao_doi_nhan_vien_2]
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
ALTER TABLE [dbo].[thong_bao]  WITH CHECK ADD  CONSTRAINT [FK_thong_bao_nhan_vien] FOREIGN KEY([id_nguoi_dung])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[thong_bao] CHECK CONSTRAINT [FK_thong_bao_nhan_vien]
GO
ALTER TABLE [dbo].[thong_tin_don_hang]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO
ALTER TABLE [dbo].[thong_tin_don_hang]  WITH CHECK ADD FOREIGN KEY([id_trang_thai_don_hang])
REFERENCES [dbo].[trang_thai_don_hang] ([id])
GO
ALTER TABLE [dbo].[lich_su_don_hang]  WITH CHECK ADD FOREIGN KEY([id_hoa_don])
REFERENCES [dbo].[hoa_don] ([id])
GO
ALTER TABLE [dbo].[lich_su_don_hang]  WITH CHECK ADD FOREIGN KEY([id_nhan_vien])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[tin_nhan]  WITH CHECK ADD  CONSTRAINT [FK_tin_nhan_nguoi_gui] FOREIGN KEY([id_nguoi_gui])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[tin_nhan] CHECK CONSTRAINT [FK_tin_nhan_nguoi_gui]
GO
ALTER TABLE [dbo].[tin_nhan]  WITH CHECK ADD  CONSTRAINT [FK_tin_nhan_nguoi_nhan] FOREIGN KEY([id_nguoi_nhan])
REFERENCES [dbo].[nhan_vien] ([id])
GO
ALTER TABLE [dbo].[tin_nhan] CHECK CONSTRAINT [FK_tin_nhan_nguoi_nhan]
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK 
ADD CONSTRAINT FK_tin_nhan_khach_hang_gui 
FOREIGN KEY([id_khach_hang_gui]) REFERENCES [dbo].[khach_hang]([id])
GO
ALTER TABLE [dbo].[tin_nhan] WITH CHECK 
ADD CONSTRAINT FK_tin_nhan_khach_hang_nhan 
FOREIGN KEY([id_khach_hang_nhan]) REFERENCES [dbo].[khach_hang]([id])
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([gia_ban]>=(0)))
GO
ALTER TABLE [dbo].[chi_tiet_san_pham]  WITH CHECK ADD CHECK  (([so_luong]>=(0)))
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [CHK_cuoc_trao_doi_khac_nguoi] CHECK (
    ([id_nhan_vien_1] IS NOT NULL AND [id_nhan_vien_2] IS NOT NULL AND [id_nhan_vien_1] <> [id_nhan_vien_2])
    OR ([id_khach_hang] IS NOT NULL AND [id_nhan_vien] IS NOT NULL)
)
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [CHK_cuoc_trao_doi_khac_nguoi]
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [CHK_cuoc_trao_doi_so_tin_chua_doc_nv1] CHECK  (([so_tin_nhan_chua_doc_nv1]>=(0)))
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [CHK_cuoc_trao_doi_so_tin_chua_doc_nv1]
GO
ALTER TABLE [dbo].[cuoc_trao_doi]  WITH CHECK ADD  CONSTRAINT [CHK_cuoc_trao_doi_so_tin_chua_doc_nv2] CHECK  (([so_tin_nhan_chua_doc_nv2]>=(0)))
GO
ALTER TABLE [dbo].[cuoc_trao_doi] CHECK CONSTRAINT [CHK_cuoc_trao_doi_so_tin_chua_doc_nv2]
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
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [CK_hoa_don_so_tien_con_lai] CHECK  (([so_tien_con_lai]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [CK_hoa_don_so_tien_con_lai]
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [CK_hoa_don_so_tien_da_thanh_toan] CHECK  (([so_tien_da_thanh_toan]>=(0)))
GO
ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [CK_hoa_don_so_tien_da_thanh_toan]
GO
ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [CK_hoa_don_trang_thai_thanh_toan] CHECK  (([trang_thai_thanh_toan]=(3) OR [trang_thai_thanh_toan]=(2) OR [trang_thai_thanh_toan]=(1) OR [trang_thai_thanh_toan]=(0)))
GO
ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [CK_hoa_don_trang_thai_thanh_toan]
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
ALTER TABLE [dbo].[phieu_giam_gia]  WITH CHECK ADD CHECK  (([ngay_ket_thuc]>[ngay_bat_dau]))
GO
ALTER TABLE [dbo].[phieu_giam_gia_ca_nhan]  WITH CHECK ADD CHECK  (([ngay_het_han]>[ngay_nhan]))
GO
ALTER TABLE [dbo].[tin_nhan]  WITH CHECK ADD  CONSTRAINT [CHK_tin_nhan_khac_nguoi] CHECK (
    ([id_nguoi_gui] IS NOT NULL AND [id_nguoi_nhan] IS NOT NULL AND [id_nguoi_gui] <> [id_nguoi_nhan])
    OR ([id_khach_hang_gui] IS NOT NULL AND [id_khach_hang_nhan] IS NOT NULL AND [id_khach_hang_gui] <> [id_khach_hang_nhan])
    OR ([id_nguoi_gui] IS NOT NULL AND [id_khach_hang_nhan] IS NOT NULL)
    OR ([id_khach_hang_gui] IS NOT NULL AND [id_nguoi_nhan] IS NOT NULL)
)
GO
ALTER TABLE [dbo].[tin_nhan] CHECK CONSTRAINT [CHK_tin_nhan_khac_nguoi]
GO
ALTER TABLE [dbo].[tin_nhan]  WITH CHECK ADD  CONSTRAINT [CHK_tin_nhan_loai] CHECK  (([loai_tin_nhan]='FILE' OR [loai_tin_nhan]='IMAGE' OR [loai_tin_nhan]='TEXT'))
GO
ALTER TABLE [dbo].[tin_nhan] CHECK CONSTRAINT [CHK_tin_nhan_loai]
GO
ALTER TABLE [dbo].[tin_nhan]  WITH CHECK ADD  CONSTRAINT [CHK_tin_nhan_noi_dung] CHECK  ((len(rtrim(ltrim([noi_dung])))>(0)))
GO
ALTER TABLE [dbo].[tin_nhan] CHECK CONSTRAINT [CHK_tin_nhan_noi_dung]
GO
/****** Check totals for qr_sessions ******/
ALTER TABLE [dbo].[qr_sessions]  WITH CHECK ADD  CONSTRAINT [CHK_qr_sessions_amounts] CHECK  (([subtotal]>=(0) AND [final_price]>=(0) AND ([discount_amount] IS NULL OR [discount_amount]>=(0)) AND ([shipping_fee] IS NULL OR [shipping_fee]>=(0))))
GO
ALTER TABLE [dbo].[qr_sessions] CHECK CONSTRAINT [CHK_qr_sessions_amounts]
GO
/****** Object:  StoredProcedure [dbo].[sp_dem_tin_chua_doc]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
/****** Object:  StoredProcedure [dbo].[sp_GenerateMaHoaDon]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- Thêm stored procedure để generate mã hoá đơn ngẫu nhiên 10 ký tự (check trùng với data hiện tại)
-- Randomize RAND seed each time for better randomness
CREATE PROCEDURE [dbo].[sp_GenerateMaHoaDon]
    @idHoaDon INT,
    @maMoiGenerated NVARCHAR(10) OUTPUT
AS
BEGIN
    DECLARE @counter INT = 0;
    DECLARE @maThoiGian NVARCHAR(10);
    DECLARE @randomSeed INT;
    
    -- Seed RAND with current time for better randomness
    SELECT @randomSeed = ABS(CHECKSUM(NEWID()));
    
    -- Vòng lặp để tìm mã chưa được sử dụng (tối đa 100 lần)
    WHILE @counter < 100
    BEGIN
        -- Generate mã gồm 12 ký tự: HD + 10 số ngẫu nhiên (0-9)
        -- Format: HD3828528184 (HD + 10 digits)
        SET @maThoiGian = 'HD' + 
            RIGHT('0000000000' + CAST(ABS(CHECKSUM(NEWID())) % 10000000000 AS NVARCHAR(10)), 10); -- 10 ký tự số (0-9999999999)
        
        -- Kiểm tra xem mã này đã tồn tại trong bảng chưa
        IF NOT EXISTS(SELECT 1 FROM [dbo].[hoa_don] WHERE [ma_hoa_don] = @maThoiGian)
        BEGIN
            -- UPDATE hóa đơn với mã được generate
            UPDATE [dbo].[hoa_don] 
            SET [ma_hoa_don] = @maThoiGian 
            WHERE [id] = @idHoaDon;
            
            -- Verify the update was successful by reading back the value
            SELECT @maMoiGenerated = [ma_hoa_don] FROM [dbo].[hoa_don] WHERE [id] = @idHoaDon;
            
            RETURN 0;
        END
        
        SET @counter = @counter + 1;
    END
    
    -- Nếu không tìm được mã trong 100 lần, trả về NULL
    SET @maMoiGenerated = NULL;
    RETURN 1;
END

GO
/****** Object:  StoredProcedure [dbo].[sp_lay_danh_sach_cuoc_trao_doi]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
/****** Object:  StoredProcedure [dbo].[sp_lay_tin_nhan]    Script Date: 11/12/2025 10:29:48 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Số điện thoại nhân viên xử lý đơn hàng' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'so_dien_thoai_nhan_vien'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Mô tả chi tiết loại đơn hàng (Bán hàng tại quầy, Bán hàng online)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'mo_ta_loai_don'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Thời gian tạo hóa đơn chi tiết (bao gồm giờ phút giây)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'thoi_gian_tao'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Thời gian cập nhật cuối cùng của hóa đơn' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'thoi_gian_cap_nhat'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Ghi chú nội bộ cho nhân viên xử lý' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'ghi_chu_noi_bo'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Mã vận đơn của đơn vị vận chuyển' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'ma_van_don'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Phương thức giao hàng (Giao tận nơi, Tại cửa hàng, Giao nhanh...)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'phuong_thuc_giao_hang'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Địa chỉ giao hàng chi tiết (có thể khác với địa chỉ khách hàng)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'dia_chi_giao_hang_chi_tiet'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Trạng thái thanh toán (0: Chưa thanh toán, 1: Đã thanh toán một phần, 2: Đã thanh toán đủ, 3: Hoàn tiền)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'trang_thai_thanh_toan'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Số tiền đã thanh toán' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'so_tien_da_thanh_toan'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Số tiền còn lại cần thanh toán' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hoa_don', @level2type=N'COLUMN',@level2name=N'so_tien_con_lai'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'Bảng timeline theo dõi quá trình xử lý đơn hàng' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'lich_su_don_hang'
GO
USE [master]
GO
ALTER DATABASE [GearUp] SET  READ_WRITE 
GO
