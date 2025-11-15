"""
Customer Database Client - Restricted access for customer chatbot
Only allows public queries, blocks all admin queries
"""
from app.utils.database import DatabaseClient
from typing import List, Dict, Any
import logging

logger = logging.getLogger(__name__)


class CustomerDatabaseClient:
    """
    Restricted database client for customer chatbot.
    Only exposes public queries (product search, product info).
    Blocks all admin queries (revenue, orders, employees, etc.)
    """
    
    def __init__(self):
        self._db = DatabaseClient()
        # Whitelist of allowed methods for customer
        self._allowed_methods = {
            'search_products',
            'get_top_selling_products',
            'get_product_variants',
            'get_variant_stock',
            'get_discount_by_code',
            'get_active_personal_vouchers',
        }
    
    def _check_permission(self, method_name: str):
        """Check if method is allowed for customer"""
        if method_name not in self._allowed_methods:
            logger.warning(f"🚨 SECURITY: Customer chatbot attempted to call restricted method: {method_name}")
            raise PermissionError(f"Access denied: Method '{method_name}' is restricted to admin only")
    
    def search_products(self, keyword: str, limit: int = 10) -> List[Dict]:
        """Search products by name - PUBLIC ACCESS"""
        self._check_permission('search_products')
        return self._db.search_products(keyword, limit)
    
    def get_top_selling_products(self, limit: int = 5, days: int = 365) -> List[Dict]:
        """Get top selling products - PUBLIC ACCESS"""
        self._check_permission('get_top_selling_products')
        return self._db.get_top_selling_products(limit, days)
    
    def get_product_variants(self, product_id: int) -> List[Dict]:
        """Get variants for a product - PUBLIC ACCESS"""
        self._check_permission('get_product_variants')
        return self._db.get_product_variants(product_id)
    
    def get_variant_stock(self, variant_id: int) -> Dict:
        """Get stock and price for a specific variant - PUBLIC ACCESS"""
        self._check_permission('get_variant_stock')
        return self._db.get_variant_stock(variant_id)
    
    def get_discount_by_code(self, code: str) -> Dict:
        """Get discount by code - PUBLIC ACCESS (for customers to check vouchers)"""
        self._check_permission('get_discount_by_code')
        return self._db.get_discount_by_code(code)
    
    def get_active_personal_vouchers(self, customer_id: int) -> List[Dict]:
        """Get active personal vouchers for a customer - PUBLIC ACCESS"""
        self._check_permission('get_active_personal_vouchers')
        return self._db.get_active_personal_vouchers(customer_id)
    
    # Explicitly block all admin methods
    def execute_query(self, *args, **kwargs):
        """BLOCKED: Direct SQL query execution is admin-only"""
        raise PermissionError("Access denied: Direct SQL queries are restricted to admin only")
    
    def execute_non_query(self, *args, **kwargs):
        """BLOCKED: Direct SQL write operations are admin-only"""
        raise PermissionError("Access denied: Write operations are restricted to admin only")
    
    def get_revenue_summary(self, *args, **kwargs):
        """BLOCKED: Revenue data is admin-only"""
        raise PermissionError("Access denied: Revenue data is restricted to admin only")
    
    def get_order_status_distribution(self, *args, **kwargs):
        """BLOCKED: Order statistics are admin-only"""
        raise PermissionError("Access denied: Order statistics are restricted to admin only")
    
    def get_top_customers_by_spending(self, *args, **kwargs):
        """BLOCKED: Customer analytics are admin-only"""
        raise PermissionError("Access denied: Customer analytics are restricted to admin only")
    
    def get_employee_sales_performance(self, *args, **kwargs):
        """BLOCKED: Employee performance data is admin-only"""
        raise PermissionError("Access denied: Employee performance data is restricted to admin only")
    
    def get_online_vs_pos_stats(self, *args, **kwargs):
        """BLOCKED: Sales statistics are admin-only"""
        raise PermissionError("Access denied: Sales statistics are restricted to admin only")
    
    def get_low_stock_products(self, *args, **kwargs):
        """BLOCKED: Inventory management is admin-only"""
        raise PermissionError("Access denied: Inventory management is restricted to admin only")
    
    def get_out_of_stock_products(self, *args, **kwargs):
        """BLOCKED: Inventory management is admin-only"""
        raise PermissionError("Access denied: Inventory management is restricted to admin only")
    
    def get_orders_by_latest_status(self, *args, **kwargs):
        """BLOCKED: Order management is admin-only"""
        raise PermissionError("Access denied: Order management is restricted to admin only")
    
    def get_recent_orders(self, *args, **kwargs):
        """BLOCKED: Order management is admin-only"""
        raise PermissionError("Access denied: Order management is restricted to admin only")
    
    def get_customer_orders(self, *args, **kwargs):
        """BLOCKED: Order access requires authentication - use API endpoint instead"""
        raise PermissionError("Access denied: Order access requires authentication. Please use the API endpoint.")
    
    def get_employee_orders(self, *args, **kwargs):
        """BLOCKED: Employee order data is admin-only"""
        raise PermissionError("Access denied: Employee order data is restricted to admin only")
    
    def get_order_items(self, *args, **kwargs):
        """BLOCKED: Order details require authentication - use API endpoint instead"""
        raise PermissionError("Access denied: Order details require authentication. Please use the API endpoint.")
    
    def get_order_timeline(self, *args, **kwargs):
        """BLOCKED: Order timeline requires authentication - use API endpoint instead"""
        raise PermissionError("Access denied: Order timeline requires authentication. Please use the API endpoint.")
    
    def get_invoice_full_info(self, *args, **kwargs):
        """BLOCKED: Invoice data is admin-only"""
        raise PermissionError("Access denied: Invoice data is restricted to admin only")
    
    def get_conversations_for_employee(self, *args, **kwargs):
        """BLOCKED: Employee conversations are admin-only"""
        raise PermissionError("Access denied: Employee conversations are restricted to admin only")
    
    def get_messages_between(self, *args, **kwargs):
        """BLOCKED: Employee messages are admin-only"""
        raise PermissionError("Access denied: Employee messages are restricted to admin only")
    
    def create_voucher(self, *args, **kwargs):
        """BLOCKED: Voucher creation is admin-only"""
        raise PermissionError("Access denied: Voucher creation is restricted to admin only")
    
    def voucher_code_exists(self, *args, **kwargs):
        """BLOCKED: Voucher management is admin-only"""
        raise PermissionError("Access denied: Voucher management is restricted to admin only")
    
    def generate_voucher_code(self, *args, **kwargs):
        """BLOCKED: Voucher code generation is admin-only"""
        raise PermissionError("Access denied: Voucher code generation is restricted to admin only")
    
    def get_active_discount_campaigns(self, *args, **kwargs):
        """BLOCKED: Campaign management is admin-only"""
        raise PermissionError("Access denied: Campaign management is restricted to admin only")
    
    def get_top_product_colors(self, *args, **kwargs):
        """BLOCKED: Product analytics are admin-only"""
        raise PermissionError("Access denied: Product analytics are restricted to admin only")
    
    def get_top_product_sizes(self, *args, **kwargs):
        """BLOCKED: Product analytics are admin-only"""
        raise PermissionError("Access denied: Product analytics are restricted to admin only")

