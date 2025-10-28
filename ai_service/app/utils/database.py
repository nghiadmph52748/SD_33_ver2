import pyodbc
from app.config import get_settings
import logging
from typing import List, Dict, Any

logger = logging.getLogger(__name__)
settings = get_settings()

class DatabaseClient:
    def __init__(self):
        self.connection_string = (
            f"DRIVER={{ODBC Driver 18 for SQL Server}};"
            f"SERVER={settings.db_host},{settings.db_port};"
            f"DATABASE={settings.db_name};"
            f"UID={settings.db_user};"
            f"PWD={settings.db_password};"
            f"TrustServerCertificate=yes;"
        )
    
    def execute_query(self, query: str, params: tuple = None) -> List[Dict[str, Any]]:
        """Execute SQL query and return results as list of dicts"""
        try:
            with pyodbc.connect(self.connection_string) as conn:
                cursor = conn.cursor()
                
                if params:
                    cursor.execute(query, params)
                else:
                    cursor.execute(query)
                
                columns = [column[0] for column in cursor.description]
                results = []
                for row in cursor.fetchall():
                    results.append(dict(zip(columns, row)))
                
                return results
        except Exception as e:
            logger.error(f"❌ Database error: {e}")
            raise
    
    def get_top_selling_products(self, limit: int = 5, days: int = 365) -> List[Dict]:
        """Get top selling products"""
        query = """
        SELECT TOP (?) 
            sp.id,
            sp.ten_san_pham as product_name,
            SUM(hdct.so_luong) as total_sold,
            SUM(hdct.thanh_tien) as total_revenue
        FROM hoa_don_chi_tiet hdct
        JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id
        JOIN san_pham sp ON ctsp.id_san_pham = sp.id
        JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
        WHERE hd.trang_thai = 1
        GROUP BY sp.id, sp.ten_san_pham
        ORDER BY total_sold DESC
        """
        return self.execute_query(query, (limit,))
    
    def get_revenue_summary(self, days: int = 365) -> Dict:
        """Get revenue summary"""
        query = """
        SELECT 
            COUNT(DISTINCT hd.id) as total_orders,
            SUM(hd.tong_tien) as total_revenue,
            AVG(hd.tong_tien) as avg_order_value,
            COUNT(DISTINCT hd.id_khach_hang) as unique_customers
        FROM hoa_don hd
        WHERE hd.trang_thai = 1
        """
        results = self.execute_query(query)
        return results[0] if results else {}
    
    def get_low_stock_products(self, threshold: int = 10) -> List[Dict]:
        """Get products with low stock"""
        query = """
        SELECT TOP 10
            sp.ten_san_pham as product_name,
            ctsp.id as variant_id,
            ms.ten_mau_sac as color,
            kt.ten_kich_thuoc as size,
            ctsp.so_luong as stock_quantity
        FROM chi_tiet_san_pham ctsp
        JOIN san_pham sp ON ctsp.id_san_pham = sp.id
        LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
        LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id
        WHERE ctsp.so_luong <= ?
        AND ctsp.trang_thai = 1
        ORDER BY ctsp.so_luong ASC
        """
        return self.execute_query(query, (threshold,))
    
    def get_order_status_distribution(self) -> List[Dict]:
        """Get order status distribution"""
        query = """
        WITH latest_status AS (
            SELECT 
                hd.id AS order_id,
                ls.id_trang_thai_don_hang AS status_id
            FROM hoa_don hd
            OUTER APPLY (
                SELECT TOP 1 ttdh.id_trang_thai_don_hang
                FROM thong_tin_don_hang ttdh
                WHERE ttdh.id_hoa_don = hd.id
                ORDER BY ttdh.thoi_gian DESC
            ) ls
            WHERE hd.trang_thai = 1
        )
        SELECT 
            ttd.ten_trang_thai_don_hang AS status_name,
            COUNT(ls.order_id) AS order_count
        FROM latest_status ls
        JOIN trang_thai_don_hang ttd ON ls.status_id = ttd.id
        GROUP BY ttd.ten_trang_thai_don_hang
        ORDER BY order_count DESC
        """
        return self.execute_query(query)
    
    def get_top_customers_by_spending(self, limit: int = 5) -> List[Dict]:
        """Get top customers by total spending"""
        query = """
        SELECT TOP (?)
            kh.ten_khach_hang as customer_name,
            kh.ma_khach_hang as customer_code,
            COUNT(DISTINCT hd.id) as total_orders,
            SUM(hd.tong_tien) as total_spent
        FROM khach_hang kh
        LEFT JOIN hoa_don hd ON kh.id = hd.id_khach_hang
        WHERE hd.trang_thai = 1 OR hd.trang_thai IS NULL
        GROUP BY kh.id, kh.ten_khach_hang, kh.ma_khach_hang
        HAVING SUM(hd.tong_tien) > 0
        ORDER BY total_spent DESC
        """
        return self.execute_query(query, (limit,))
    
    def get_active_discount_campaigns(self) -> List[Dict]:
        """Get active discount campaigns"""
        query = """
        SELECT 
            dg.ten_dot_giam_gia as campaign_name,
            dg.ma_dot_giam_gia as campaign_code,
            dg.gia_tri_giam_gia as discount_value,
            dg.ngay_bat_dau as start_date,
            dg.ngay_ket_thuc as end_date
        FROM dot_giam_gia dg
        WHERE dg.trang_thai = 1
        AND GETDATE() BETWEEN dg.ngay_bat_dau AND dg.ngay_ket_thuc
        ORDER BY dg.ngay_bat_dau DESC
        """
        return self.execute_query(query)
    
    def get_employee_sales_performance(self, limit: int = 5) -> List[Dict]:
        """Get employee sales performance"""
        query = """
        SELECT TOP (?)
            nv.ten_nhan_vien as employee_name,
            nv.ma_nhan_vien as employee_code,
            COUNT(DISTINCT hd.id) as total_orders,
            SUM(hd.tong_tien) as total_revenue
        FROM nhan_vien nv
        LEFT JOIN hoa_don hd ON nv.id = hd.id_nhan_vien
        WHERE hd.trang_thai = 1 OR hd.trang_thai IS NULL
        GROUP BY nv.id, nv.ten_nhan_vien, nv.ma_nhan_vien
        HAVING COUNT(DISTINCT hd.id) > 0
        ORDER BY total_revenue DESC
        """
        return self.execute_query(query, (limit,))
    
    def get_top_product_colors(self, limit: int = 5) -> List[Dict]:
        """Get top selling product colors"""
        query = """
        SELECT TOP (?)
            ms.ten_mau_sac as color_name,
            SUM(hdct.so_luong) as total_sold
        FROM hoa_don_chi_tiet hdct
        JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id
        JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
        JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
        WHERE hd.trang_thai = 1
        GROUP BY ms.ten_mau_sac
        ORDER BY total_sold DESC
        """
        return self.execute_query(query, (limit,))
    
    def get_top_product_sizes(self, limit: int = 5) -> List[Dict]:
        """Get top selling product sizes"""
        query = """
        SELECT TOP (?)
            kt.ten_kich_thuoc as size_name,
            SUM(hdct.so_luong) as total_sold
        FROM hoa_don_chi_tiet hdct
        JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id
        JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id
        JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
        WHERE hd.trang_thai = 1
        GROUP BY kt.ten_kich_thuoc
        ORDER BY total_sold DESC
        """
        return self.execute_query(query, (limit,))
    
    def get_online_vs_pos_stats(self) -> Dict:
        """Get online vs POS sales statistics"""
        query = """
        SELECT 
            SUM(CASE WHEN hd.loai_don = 0 THEN hd.tong_tien ELSE 0 END) as pos_revenue,
            SUM(CASE WHEN hd.loai_don = 1 THEN hd.tong_tien ELSE 0 END) as online_revenue,
            COUNT(CASE WHEN hd.loai_don = 0 THEN 1 END) as pos_orders,
            COUNT(CASE WHEN hd.loai_don = 1 THEN 1 END) as online_orders
        FROM hoa_don hd
        WHERE hd.trang_thai = 1
        """
        results = self.execute_query(query)
        return results[0] if results else {}

# Singleton instance
db_client = DatabaseClient()
