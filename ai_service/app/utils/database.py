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
            logger.error(f"Database error: {e}")
            raise
    
    def execute_non_query(self, query: str, params: tuple = None) -> int:
        """Execute INSERT/UPDATE/DELETE and return affected row count"""
        try:
            with pyodbc.connect(self.connection_string) as conn:
                cursor = conn.cursor()
                if params:
                    cursor.execute(query, params)
                else:
                    cursor.execute(query)
                conn.commit()
                return cursor.rowcount
        except Exception as e:
            logger.error(f"Database error: {e}")
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

    # ===== Voucher helpers =====
    def voucher_code_exists(self, code: str) -> bool:
        query = """
        SELECT 1 FROM phieu_giam_gia WHERE ma_phieu_giam_gia = ?
        """
        return len(self.execute_query(query, (code,))) > 0

    def generate_voucher_code(self) -> str:
        """Generate unique voucher code like PGG00001"""
        import random
        for _ in range(50):
            number = random.randint(1, 99999)
            code = f"PGG{number:05d}"
            if not self.voucher_code_exists(code):
                return code
        raise RuntimeError("Could not generate unique voucher code after many attempts")

    def create_voucher(
        self,
        *,
        name: str,
        percent_type: bool,
        value: float,
        start_datetime: str,
        end_datetime: str,
        min_order: float = 0,
        usage_limit: int = 1,
        code: str | None = None,
        description: str | None = None,
    ) -> Dict:
        """Create a voucher in phieu_giam_gia and return the inserted data"""
        if code is None:
            code = self.generate_voucher_code()
        insert_sql = """
        INSERT INTO phieu_giam_gia (
            ma_phieu_giam_gia,
            ten_phieu_giam_gia,
            loai_phieu_giam_gia,
            gia_tri_giam_gia,
            hoa_don_toi_thieu,
            so_luong_dung,
            ngay_bat_dau,
            ngay_ket_thuc,
            trang_thai,
            mo_ta
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)
        """
        params = (
            code,
            name,
            0 if percent_type else 1,
            value,
            min_order,
            usage_limit,
            start_datetime,
            end_datetime,
            description or ""
        )
        self.execute_non_query(insert_sql, params)
        select_sql = """
        SELECT TOP 1 * FROM phieu_giam_gia WHERE ma_phieu_giam_gia = ?
        """
        rows = self.execute_query(select_sql, (code,))
        return rows[0] if rows else {}
    
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

    def search_products(self, keyword: str, limit: int = 10) -> List[Dict]:
        """Search products by name with basic info and total variants/stock"""
        query = """
        SELECT TOP (?)
            sp.id AS product_id,
            sp.ten_san_pham AS product_name,
            COUNT(ctsp.id) AS variant_count,
            ISNULL(SUM(ctsp.so_luong), 0) AS total_stock,
            MIN(ctsp.gia_ban) AS min_price,
            MAX(ctsp.gia_ban) AS max_price
        FROM san_pham sp
        LEFT JOIN chi_tiet_san_pham ctsp ON ctsp.id_san_pham = sp.id AND ctsp.trang_thai = 1
        WHERE sp.trang_thai = 1 AND sp.ten_san_pham LIKE ?
        GROUP BY sp.id, sp.ten_san_pham
        ORDER BY sp.ten_san_pham ASC
        """
        return self.execute_query(query, (limit, f"%{keyword}%"))

    def get_product_variants(self, product_id: int) -> List[Dict]:
        """Get variants for a product with color/size/price/stock"""
        query = """
        SELECT 
            ctsp.id AS variant_id,
            ctsp.ten_san_pham_chi_tiet AS variant_name,
            ms.ten_mau_sac AS color,
            kt.ten_kich_thuoc AS size,
            ctsp.gia_ban AS price,
            ctsp.so_luong AS stock
        FROM chi_tiet_san_pham ctsp
        LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
        LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id
        WHERE ctsp.id_san_pham = ? AND ctsp.trang_thai = 1
        ORDER BY color, size
        """
        return self.execute_query(query, (product_id,))

    def get_variant_stock(self, variant_id: int) -> Dict:
        """Get stock and price for a specific variant"""
        query = """
        SELECT 
            ctsp.id AS variant_id,
            ctsp.so_luong AS stock,
            ctsp.gia_ban AS price
        FROM chi_tiet_san_pham ctsp
        WHERE ctsp.id = ?
        """
        results = self.execute_query(query, (variant_id,))
        return results[0] if results else {}

    def get_order_items(self, order_id: int) -> List[Dict]:
        """Get line items of an order with product and variant info"""
        query = """
        SELECT 
            hdct.id AS order_item_id,
            hdct.so_luong AS quantity,
            hdct.gia_ban AS unit_price,
            hdct.thanh_tien AS line_total,
            sp.ten_san_pham AS product_name,
            ctsp.ten_san_pham_chi_tiet AS variant_name,
            ms.ten_mau_sac AS color,
            kt.ten_kich_thuoc AS size
        FROM hoa_don_chi_tiet hdct
        JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id
        JOIN san_pham sp ON ctsp.id_san_pham = sp.id
        LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
        LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id
        WHERE hdct.id_hoa_don = ?
        ORDER BY hdct.id ASC
        """
        return self.execute_query(query, (order_id,))

    def get_order_timeline(self, order_id: int) -> List[Dict]:
        """Get order timeline events (prefer timeline_don_hang; fallback to thong_tin_don_hang)"""
        timeline_query = """
        SELECT 
            th.id,
            th.trang_thai_cu,
            th.trang_thai_moi,
            th.hanh_dong,
            th.mo_ta,
            th.ghi_chu,
            th.thoi_gian
        FROM timeline_don_hang th
        WHERE th.id_hoa_don = ? AND th.deleted = 0
        ORDER BY th.thoi_gian ASC
        """
        rows = self.execute_query(timeline_query, (order_id,))
        if rows:
            return rows
        fallback_query = """
        SELECT 
            ttdh.id,
            NULL AS trang_thai_cu,
            ttd.ten_trang_thai_don_hang AS trang_thai_moi,
            'Cập nhật trạng thái' AS hanh_dong,
            ttdh.ghi_chu AS mo_ta,
            NULL AS ghi_chu,
            ttdh.thoi_gian
        FROM thong_tin_don_hang ttdh
        JOIN trang_thai_don_hang ttd ON ttdh.id_trang_thai_don_hang = ttd.id
        WHERE ttdh.id_hoa_don = ? AND ttdh.trang_thai = 1
        ORDER BY ttdh.thoi_gian ASC
        """
        return self.execute_query(fallback_query, (order_id,))

    def get_orders_by_latest_status(self, status_name: str, limit: int = 20) -> List[Dict]:
        """List orders whose latest status matches the given status name"""
        query = """
        WITH latest_status AS (
            SELECT 
                hd.id AS order_id,
                ls.id_trang_thai_don_hang AS status_id,
                ls.thoi_gian
            FROM hoa_don hd
            OUTER APPLY (
                SELECT TOP 1 ttdh.id_trang_thai_don_hang, ttdh.thoi_gian
                FROM thong_tin_don_hang ttdh
                WHERE ttdh.id_hoa_don = hd.id
                ORDER BY ttdh.thoi_gian DESC
            ) ls
            WHERE hd.trang_thai = 1 AND hd.deleted = 0
        )
        SELECT TOP (?)
            hd.id AS order_id,
            hd.ma_hoa_don,
            hd.ten_khach_hang,
            hd.tong_tien_sau_giam,
            ttd.ten_trang_thai_don_hang AS status_name,
            ls.thoi_gian AS updated_at
        FROM latest_status ls
        JOIN trang_thai_don_hang ttd ON ls.status_id = ttd.id
        JOIN hoa_don hd ON hd.id = ls.order_id
        WHERE ttd.ten_trang_thai_don_hang = ?
        ORDER BY ls.thoi_gian DESC
        """
        return self.execute_query(query, (limit, status_name))

    def get_recent_orders(self, limit: int = 10) -> List[Dict]:
        """Get most recent orders by created datetime if available, else by id desc"""
        query = """
        SELECT TOP (?)
            hd.id AS order_id,
            hd.ma_hoa_don,
            hd.ten_khach_hang,
            hd.tong_tien_sau_giam,
            COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) AS created_at
        FROM hoa_don hd
        WHERE hd.deleted = 0
        ORDER BY COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) DESC, hd.id DESC
        """
        return self.execute_query(query, (limit,))

    def get_out_of_stock_products(self, threshold: int = 0, limit: int = 20) -> List[Dict]:
        """Get variants whose stock is at or below threshold"""
        query = """
        SELECT TOP (?)
            sp.ten_san_pham AS product_name,
            ctsp.id AS variant_id,
            ctsp.ten_san_pham_chi_tiet AS variant_name,
            ms.ten_mau_sac AS color,
            kt.ten_kich_thuoc AS size,
            ctsp.so_luong AS stock_quantity
        FROM chi_tiet_san_pham ctsp
        JOIN san_pham sp ON ctsp.id_san_pham = sp.id
        LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
        LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id
        WHERE ctsp.so_luong <= ? AND ctsp.trang_thai = 1
        ORDER BY ctsp.so_luong ASC
        """
        return self.execute_query(query, (limit, threshold))

    def get_customer_orders(self, customer_id: int, limit: int = 20) -> List[Dict]:
        """Get recent orders for a customer"""
        query = """
        SELECT TOP (?)
            hd.id AS order_id,
            hd.ma_hoa_don,
            hd.tong_tien_sau_giam,
            COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) AS created_at
        FROM hoa_don hd
        WHERE hd.id_khach_hang = ? AND hd.deleted = 0
        ORDER BY COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) DESC, hd.id DESC
        """
        return self.execute_query(query, (limit, customer_id))

    def get_employee_orders(self, employee_id: int, limit: int = 20) -> List[Dict]:
        """Get recent orders handled by an employee"""
        query = """
        SELECT TOP (?)
            hd.id AS order_id,
            hd.ma_hoa_don,
            hd.ten_khach_hang,
            hd.tong_tien_sau_giam,
            COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) AS created_at
        FROM hoa_don hd
        WHERE hd.id_nhan_vien = ? AND hd.deleted = 0
        ORDER BY COALESCE(hd.thoi_gian_tao, CAST(hd.ngay_tao AS datetime)) DESC, hd.id DESC
        """
        return self.execute_query(query, (limit, employee_id))

    def get_discount_by_code(self, code: str) -> Dict:
        """Get discount campaign or voucher by code"""
        # Try dot_giam_gia by ma_dot_giam_gia
        dgg_query = """
        SELECT TOP 1 
            dg.id,
            dg.ma_dot_giam_gia AS code,
            dg.ten_dot_giam_gia AS name,
            dg.gia_tri_giam_gia AS value,
            dg.ngay_bat_dau AS start_date,
            dg.ngay_ket_thuc AS end_date,
            CASE WHEN GETDATE() BETWEEN dg.ngay_bat_dau AND dg.ngay_ket_thuc AND dg.trang_thai = 1 THEN 1 ELSE 0 END AS is_active
        FROM dot_giam_gia dg
        WHERE dg.ma_dot_giam_gia = ?
        """
        res = self.execute_query(dgg_query, (code,))
        if res:
            return res[0]
        # Try phieu_giam_gia by ma_phieu_giam_gia
        pgg_query = """
        SELECT TOP 1 
            pgg.id,
            pgg.ma_phieu_giam_gia AS code,
            pgg.ten_phieu_giam_gia AS name,
            pgg.gia_tri_giam_gia AS value,
            pgg.ngay_bat_dau AS start_date,
            pgg.ngay_ket_thuc AS end_date,
            CASE WHEN GETDATE() BETWEEN pgg.ngay_bat_dau AND pgg.ngay_ket_thuc AND pgg.trang_thai = 1 THEN 1 ELSE 0 END AS is_active
        FROM phieu_giam_gia pgg
        WHERE pgg.ma_phieu_giam_gia = ?
        """
        res2 = self.execute_query(pgg_query, (code,))
        return res2[0] if res2 else {}

    def get_active_personal_vouchers(self, customer_id: int) -> List[Dict]:
        """List active personal vouchers for a customer"""
        query = """
        SELECT 
            pggcn.id,
            pgg.ma_phieu_giam_gia AS code,
            pgg.ten_phieu_giam_gia AS name,
            pgg.gia_tri_giam_gia AS value,
            pgg.loai_phieu_giam_gia AS type,
            pgg.ngay_bat_dau,
            pgg.ngay_ket_thuc,
            pggcn.ngay_het_han
        FROM phieu_giam_gia_ca_nhan pggcn
        JOIN phieu_giam_gia pgg ON pggcn.id_phieu_giam_gia = pgg.id
        WHERE pggcn.id_khach_hang = ?
          AND pgg.trang_thai = 1
          AND GETDATE() BETWEEN pgg.ngay_bat_dau AND pgg.ngay_ket_thuc
          AND pggcn.ngay_het_han >= GETDATE()
        ORDER BY pgg.ngay_ket_thuc ASC
        """
        return self.execute_query(query, (customer_id,))

    def get_invoice_full_info(self, order_id: int) -> Dict:
        """Get full invoice info from view v_hoa_don_full_info"""
        query = """
        SELECT * FROM v_hoa_don_full_info WHERE id = ?
        """
        results = self.execute_query(query, (order_id,))
        return results[0] if results else {}

    def get_conversations_for_employee(self, employee_id: int, limit: int = 20) -> List[Dict]:
        """Get conversation list for an employee from view v_cuoc_trao_doi_chi_tiet"""
        query = """
        SELECT TOP (?) *
        FROM v_cuoc_trao_doi_chi_tiet
        WHERE id_nhan_vien_1 = ? OR id_nhan_vien_2 = ?
        ORDER BY thoi_gian_tin_nhan_cuoi DESC
        """
        return self.execute_query(query, (limit, employee_id, employee_id))

    def get_messages_between(self, sender_id: int, receiver_id: int, page: int = 1, page_size: int = 50) -> List[Dict]:
        """Paginated messages between two employees"""
        # SQL Server OFFSET/FETCH requires ORDER BY
        query = """
        SELECT 
            tn.id,
            tn.ma_tin_nhan,
            tn.id_nguoi_gui,
            tn.id_nguoi_nhan,
            tn.noi_dung,
            tn.loai_tin_nhan,
            tn.da_doc,
            tn.thoi_gian_gui,
            tn.thoi_gian_doc
        FROM tin_nhan tn
        WHERE 
            ((tn.id_nguoi_gui = ? AND tn.id_nguoi_nhan = ?) OR (tn.id_nguoi_gui = ? AND tn.id_nguoi_nhan = ?))
            AND tn.deleted = 0
        ORDER BY tn.thoi_gian_gui DESC
        OFFSET (? - 1) * ? ROWS FETCH NEXT ? ROWS ONLY
        """
        return self.execute_query(query, (sender_id, receiver_id, receiver_id, sender_id, page, page_size, page_size))

# Singleton instance
db_client = DatabaseClient()
