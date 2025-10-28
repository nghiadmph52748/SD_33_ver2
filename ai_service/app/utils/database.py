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

# Singleton instance
db_client = DatabaseClient()
