#!/usr/bin/env python3
"""
Sneaker Scraper - Single script to scrape products and update SQL Server
Prompts for product URLs, scrapes data, downloads images, and updates database
"""

import os
import re
import json
import time
from urllib.parse import urlparse
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import requests
from bs4 import BeautifulSoup
from dotenv import load_dotenv
import pyodbc
import cloudinary
import cloudinary.uploader
from hashlib import md5
from io import BytesIO
try:
    from PIL import Image
except ImportError:
    Image = None
    print("Warning: PIL/Pillow not installed. Image dimension validation disabled.")
    print("Install with: pip install Pillow")

# Load environment variables - load from script directory
script_dir = Path(__file__).parent
env_path = script_dir / '.env'
load_dotenv(dotenv_path=env_path)

# Configuration
SQL_SERVER = os.getenv('SQL_SERVER', '127.0.0.1')  # Default to 127.0.0.1 for WARP compatibility
SQL_DATABASE = os.getenv('SQL_DATABASE', 'GearUp')
SQL_USERNAME = os.getenv('SQL_USERNAME', 'sa')
SQL_PASSWORD = os.getenv('SQL_PASSWORD', 'Manhduy@2005')
SQL_PORT = os.getenv('SQL_PORT', '1433')

# Cloudinary configuration
CLOUDINARY_CLOUD_NAME = os.getenv('CLOUDINARY_CLOUD_NAME', '')
CLOUDINARY_API_KEY = os.getenv('CLOUDINARY_API_KEY', '')
CLOUDINARY_API_SECRET = os.getenv('CLOUDINARY_API_SECRET', '')

# Currency conversion rates
USD_TO_VND_RATE = float(os.getenv('USD_TO_VND_RATE', '24500'))
EUR_TO_VND_RATE = float(os.getenv('EUR_TO_VND_RATE', '26500'))

# Debug mode
DEBUG_MODE = os.getenv('DEBUG_MODE', 'false').lower() == 'true'

# Debug: Show what values are being used
if __name__ == '__main__':
    print(f"DEBUG - SQL_SERVER from env: {os.getenv('SQL_SERVER', 'NOT SET')}")
    print(f"DEBUG - SQL_SERVER final value: {SQL_SERVER}")

IMAGES_DIR = Path('images')
DEFAULT_ORIGIN_ID = 1  # Việt Nam
DEFAULT_MATERIAL_ID = 1  # Da tổng hợp
DEFAULT_SOLE_ID = 1  # Đế cao su
DEFAULT_WEIGHT_ID = 1  # 250g
DEFAULT_STOCK_QTY = 10  # Default stock quantity
MIN_IMAGE_RESOLUTION = int(os.getenv('MIN_IMAGE_RESOLUTION', '800'))  # Minimum image width to consider (filters low quality)

# Brand detection mapping
BRAND_MAPPING = {
    'nike.com': 'Nike',
    'adidas.com': 'Adidas',
    'adidas': 'Adidas',
    'puma.com': 'Puma',
    'footlocker.com': 'Nike',  # Retail site, may have multiple brands
    'finishline.com': 'Nike',
    'champssports.com': 'Nike',
    'eastbay.com': 'Nike',
}

# Headers for requests - mimic real browser to avoid 403 errors
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
    'Accept-Language': 'en-US,en;q=0.9,vi;q=0.8',
    'Accept-Encoding': 'gzip, deflate, br',
    'Connection': 'keep-alive',
    'Upgrade-Insecure-Requests': '1',
    'Sec-Fetch-Dest': 'document',
    'Sec-Fetch-Mode': 'navigate',
    'Sec-Fetch-Site': 'none',
    'Cache-Control': 'max-age=0',
    'DNT': '1',
}


class DatabaseManager:
    """Manages database connection and operations using pyodbc"""
    
    def __init__(self):
        self.conn = None
        self.cursor = None
        self._connect()
    
    def _connect(self):
        """Establish database connection"""
        try:
            conn_str = (
                f"DRIVER={{ODBC Driver 18 for SQL Server}};"
                f"SERVER={SQL_SERVER},{SQL_PORT};"
                f"DATABASE={SQL_DATABASE};"
                f"UID={SQL_USERNAME};"
                f"PWD={SQL_PASSWORD};"
                f"TrustServerCertificate=yes;"
            )
            self.conn = pyodbc.connect(conn_str)
            self.cursor = self.conn.cursor()
        except Exception as e:
            raise Exception(f"Failed to connect to database: {str(e)}")
    
    def __enter__(self):
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.cursor:
            self.cursor.close()
        if self.conn:
            if exc_type:
                self.conn.rollback()
            else:
                self.conn.commit()
            self.conn.close()
    
    def get_or_create_manufacturer(self, name: str) -> int:
        """Get or create manufacturer and return ID"""
        self.cursor.execute(
            "SELECT id FROM nha_san_xuat WHERE ten_nha_san_xuat = ? AND deleted = 0",
            (name,)
        )
        row = self.cursor.fetchone()
        if row:
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO nha_san_xuat (ten_nha_san_xuat, trang_thai, deleted, create_at, create_by)
               VALUES (?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
            (name,)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def get_or_create_product(self, name: str, manufacturer_id: int) -> int:
        """Get or create product and return ID"""
        self.cursor.execute(
            """SELECT id FROM san_pham 
               WHERE ten_san_pham = ? AND id_nha_san_xuat = ? AND deleted = 0""",
            (name, manufacturer_id)
        )
        row = self.cursor.fetchone()
        if row:
            # Update the existing product
            self.cursor.execute(
                "UPDATE san_pham SET update_at = CAST(GETDATE() AS DATE), update_by = 1 WHERE id = ?",
                (row[0],)
            )
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO san_pham (id_nha_san_xuat, id_xuat_xu, ten_san_pham, trang_thai, deleted, create_at, create_by)
               VALUES (?, ?, ?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
            (manufacturer_id, DEFAULT_ORIGIN_ID, name)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def get_or_create_color(self, name: str, hex_code: str) -> int:
        """Get or create color and return ID"""
        self.cursor.execute(
            "SELECT id FROM mau_sac WHERE ten_mau_sac = ? AND deleted = 0",
            (name,)
        )
        row = self.cursor.fetchone()
        if row:
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO mau_sac (ten_mau_sac, ma_mau, trang_thai, deleted, create_at, create_by)
               VALUES (?, ?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
            (name, hex_code)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def get_or_create_size(self, name: str) -> int:
        """Get or create size and return ID"""
        self.cursor.execute(
            "SELECT id FROM kich_thuoc WHERE ten_kich_thuoc = ? AND deleted = 0",
            (name,)
        )
        row = self.cursor.fetchone()
        if row:
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO kich_thuoc (ten_kich_thuoc, trang_thai, deleted, create_at, create_by)
               VALUES (?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
            (name,)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def get_or_create_product_detail(self, product_id: int, color_id: int, size_id: int,
                                     price: float, stock: int, full_name: str) -> int:
        """Get or create product detail and return ID"""
        self.cursor.execute(
            """SELECT id FROM chi_tiet_san_pham 
               WHERE id_san_pham = ? AND id_mau_sac = ? AND id_kich_thuoc = ? AND deleted = 0""",
            (product_id, color_id, size_id)
        )
        row = self.cursor.fetchone()
        if row:
            # Update existing detail
            self.cursor.execute(
                """UPDATE chi_tiet_san_pham 
                   SET gia_ban = ?, so_luong = ?, ten_san_pham_chi_tiet = ?,
                       update_at = CAST(GETDATE() AS DATE), update_by = 1
                   WHERE id = ?""",
                (price, stock, full_name, row[0])
            )
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO chi_tiet_san_pham 
               (id_san_pham, id_mau_sac, id_kich_thuoc, id_de_giay, id_chat_lieu, id_trong_luong,
                so_luong, gia_ban, trang_thai, deleted, ten_san_pham_chi_tiet, create_at, create_by)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, 0, ?, CAST(GETDATE() AS DATE), 1)""",
            (product_id, color_id, size_id, DEFAULT_SOLE_ID, DEFAULT_MATERIAL_ID, DEFAULT_WEIGHT_ID,
             stock, price, full_name)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def get_or_create_image(self, url: str, name: str, color: str) -> int:
        """Get or create image and return ID"""
        self.cursor.execute(
            "SELECT id FROM anh_san_pham WHERE duong_dan_anh = ? AND deleted = 0",
            (url,)
        )
        row = self.cursor.fetchone()
        if row:
            return row[0]
        
        self.cursor.execute(
            """INSERT INTO anh_san_pham (duong_dan_anh, ten_anh, mau_anh, trang_thai, deleted, create_at, create_by)
               VALUES (?, ?, ?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
            (url, name, color)
        )
        self.cursor.execute("SELECT @@IDENTITY")
        return self.cursor.fetchone()[0]
    
    def link_image_to_product_detail(self, detail_id: int, image_id: int):
        """Link image to product detail"""
        self.cursor.execute(
            """SELECT 1 FROM chi_tiet_san_pham_anh 
               WHERE id_chi_tiet_san_pham = ? AND id_anh_san_pham = ? AND deleted = 0""",
            (detail_id, image_id)
        )
        if not self.cursor.fetchone():
            self.cursor.execute(
                """INSERT INTO chi_tiet_san_pham_anh (id_chi_tiet_san_pham, id_anh_san_pham, trang_thai, deleted, create_at, create_by)
                   VALUES (?, ?, 1, 0, CAST(GETDATE() AS DATE), 1)""",
                (detail_id, image_id)
            )


class CloudinaryUploader:
    """Handles image uploads to Cloudinary"""
    
    def __init__(self):
        if not CLOUDINARY_CLOUD_NAME or not CLOUDINARY_API_KEY or not CLOUDINARY_API_SECRET:
            raise Exception("Cloudinary credentials not configured. Please set CLOUDINARY_* environment variables.")
        
        cloudinary.config(
            cloud_name=CLOUDINARY_CLOUD_NAME,
            api_key=CLOUDINARY_API_KEY,
            api_secret=CLOUDINARY_API_SECRET
        )
        self.uploaded_count = 0
        self.upload_cache = {}  # Cache URL by image bytes hash to avoid re-uploading
    
    def upload_image(self, image_bytes: bytes, product_name: str, index: int) -> str:
        """Upload image to Cloudinary and return secure URL"""
        try:
            # Generate hash for caching (avoid re-uploading same image)
            hash_id = md5(image_bytes).hexdigest()
            
            # Check cache first
            if hash_id in self.upload_cache:
                print(f"    [Cache hit] Using cached URL for duplicate image")
                return self.upload_cache[hash_id]
            
            # Generate unique public_id using product name + index + timestamp
            # This ensures each image gets a unique URL even if content is similar
            import time
            safe_name = re.sub(r'[^\w\-]', '_', product_name.lower()[:30])
            timestamp = str(int(time.time() * 1000))  # Milliseconds timestamp
            public_id = f"{safe_name}_{index}_{timestamp}"
            
            result = cloudinary.uploader.upload(
                image_bytes,
                folder="SD_73/images",
                public_id=public_id,
                resource_type="image",
                colors=True,
                unique_filename=True,  # Ensure unique filename
                overwrite=False  # Don't overwrite existing files
            )
            
            self.uploaded_count += 1
            url = result['secure_url']
            
            # Cache for this session (not persistent)
            self.upload_cache[hash_id] = url
            
            return url
        except Exception as e:
            raise Exception(f"Failed to upload image to Cloudinary: {str(e)}")


class CurrencyConverter:
    """Handles currency conversion to VND"""
    
    def __init__(self):
        self.usd_rate = USD_TO_VND_RATE
        self.eur_rate = EUR_TO_VND_RATE
    
    def detect_currency(self, price_text: str) -> str:
        """Detect currency from price text"""
        price_lower = price_text.lower()
        if '€' in price_text or 'eur' in price_lower:
            return 'EUR'
        elif '£' in price_text or 'gbp' in price_lower:
            return 'GBP'
        elif 'vnd' in price_lower or '₫' in price_text or 'đ' in price_text:
            return 'VND'
        else:
            return 'USD'  # Default to USD for Nike/Adidas
    
    def convert_to_vnd(self, amount: float, currency: str = 'USD') -> float:
        """Convert amount to VND"""
        if currency == 'VND':
            return amount
        elif currency == 'USD':
            return amount * self.usd_rate
        elif currency == 'EUR':
            return amount * self.eur_rate
        elif currency == 'GBP':
            return amount * self.usd_rate * 1.27  # Approximate GBP to USD then to VND
        else:
            return amount * self.usd_rate  # Default to USD conversion


class ColorMapper:
    """Maps English color names to Vietnamese with hex codes"""
    
    COLOR_MAP = {
        'black': ('Đen', '#000000'),
        'white': ('Trắng', '#FFFFFF'),
        'red': ('Đỏ', '#FF0000'),
        'blue': ('Xanh Dương', '#0000FF'),
        'green': ('Xanh Lá', '#008000'),
        'grey': ('Xám', '#808080'),
        'gray': ('Xám', '#808080'),
        'navy': ('Xanh Đậm', '#000080'),
        'orange': ('Cam', '#FFA500'),
        'pink': ('Hồng', '#FFC0CB'),
        'brown': ('Nâu', '#8B4513'),
        'gold': ('Vàng', '#FFD700'),
        'silver': ('Bạc', '#C0C0C0'),
        'yellow': ('Vàng', '#FFFF00'),
        'purple': ('Tím', '#800080'),
        'beige': ('Be', '#F5F5DC'),
        'tan': ('Nâu Nhạt', '#D2B48C'),
        'olive': ('Xanh Ô Liu', '#808000'),
        'maroon': ('Nâu Đỏ', '#800000'),
        'cyan': ('Xanh Lơ', '#00FFFF'),
    }
    
    def map_color(self, color_name: str) -> Tuple[str, str]:
        """
        Map English color name to Vietnamese name and hex code
        Returns: (vietnamese_name, hex_code)
        """
        # Check if already has hex code
        hex_match = re.search(r'#[0-9A-Fa-f]{6}', color_name)
        if hex_match:
            return (color_name.split('#')[0].strip(), hex_match.group(0))
        
        # Try to find a color keyword in the name
        color_lower = color_name.lower()
        for eng_color, (viet_name, hex_code) in self.COLOR_MAP.items():
            if eng_color in color_lower:
                # If it's a complex colorway like "Core Black / Cloud White"
                if '/' in color_name or len(color_name.split()) > 2:
                    # Keep the original name but add hex code from first color
                    return (color_name, hex_code)
                else:
                    # Simple color, use Vietnamese translation
                    return (viet_name, hex_code)
        
        # No match found, return original with default black hex
        return (color_name, '#000000')


class SneakerScraper:
    def __init__(self, use_cloudinary: bool = True):
        self.session = requests.Session()
        self.session.headers.update(HEADERS)
        self.processed_count = 0
        self.images_saved = 0
        self.use_cloudinary = use_cloudinary
        
        # Initialize helper classes
        self.currency_converter = CurrencyConverter()
        self.color_mapper = ColorMapper()
        
        # Initialize Cloudinary uploader if enabled
        self.cloudinary_uploader = None
        if use_cloudinary:
            try:
                self.cloudinary_uploader = CloudinaryUploader()
            except Exception as e:
                print(f"Warning: Cloudinary not configured: {str(e)}")
                print("Will fallback to local image storage.")
                self.use_cloudinary = False
        
    def detect_brand(self, url: str) -> str:
        """Detect brand from URL"""
        domain = urlparse(url).netloc.lower()
        for key, brand in BRAND_MAPPING.items():
            if key in domain:
                return brand
        return 'Unknown'
    
    def scrape_product(self, url: str) -> Optional[Dict]:
        """Scrape product data from URL"""
        brand = self.detect_brand(url)
        print(f"Detecting brand from URL: {brand}")
        
        try:
            # Add domain-specific headers to avoid 403 errors
            parsed_url = urlparse(url)
            domain = f"{parsed_url.scheme}://{parsed_url.netloc}"
            headers = {
                'Referer': domain,
                'Origin': domain,
            }
            headers.update(HEADERS)
            
            response = self.session.get(url, timeout=30, headers=headers, allow_redirects=True)
            response.raise_for_status()
            soup = BeautifulSoup(response.content, 'lxml')
            
            # Brand-specific scraping
            if 'nike.com' in url.lower():
                return self._scrape_nike(soup, url, brand)
            elif 'adidas.com' in url.lower():
                return self._scrape_adidas(soup, url, brand)
            elif 'footlocker.com' in url.lower() or 'finishline.com' in url.lower():
                return self._scrape_retail(soup, url, brand)
            else:
                return self._scrape_generic(soup, url, brand)
                
        except Exception as e:
            print(f"Error scraping {url}: {str(e)}")
            return None
    
    def _scrape_nike(self, soup: BeautifulSoup, url: str, brand: str) -> Dict:
        """Scrape Nike product page"""
        product = {}
        
        # Product name - try multiple selectors
        name_elem = (soup.find('h1', {'data-testid': 'product-title'}) or 
                    soup.find('h1', class_='headline-2') or
                    soup.find('h1', class_=lambda x: x and 'title' in x.lower()) or
                    soup.find('h1'))
        product['name'] = name_elem.get_text(strip=True) if name_elem else 'Unknown Product'
        
        # Try to extract from embedded JSON data (Nike often embeds product data in script tags)
        product_data = None
        script_tags = soup.find_all('script')
        for script in script_tags:
            script_text = script.string or ''
            # Look for window.__INITIAL_STATE__ or similar patterns
            if 'window.__INITIAL_STATE__' in script_text or 'window.__INITIAL_DATA__' in script_text:
                try:
                    # Extract JSON from window.__INITIAL_STATE__ = {...}
                    json_match = re.search(r'window\.__INITIAL_STATE__\s*=\s*({.+?});', script_text, re.DOTALL)
                    if json_match:
                        product_data = json.loads(json_match.group(1))
                        break
                except:
                    pass
            
            # Look for product data in other common patterns
            if 'product' in script_text.lower() and 'colorway' in script_text.lower():
                try:
                    # Try to find JSON objects with product data
                    json_matches = re.findall(r'\{[^{}]*"product"[^{}]*"colorway"[^{}]*\}', script_text)
                    for match in json_matches:
                        try:
                            data = json.loads(match)
                            if 'product' in data:
                                product_data = data
                                break
                        except:
                            pass
                except:
                    pass
        
        # Try to extract from JSON-LD structured data
        json_ld = soup.find('script', type='application/ld+json')
        if json_ld:
            try:
                data = json.loads(json_ld.string)
                if isinstance(data, dict):
                    if 'name' in data and (not product['name'] or product['name'] == 'Unknown Product'):
                        product['name'] = data.get('name', product['name'])
                    if 'offers' in data:
                        if isinstance(data['offers'], dict):
                            price = data['offers'].get('price', '0')
                        elif isinstance(data['offers'], list) and len(data['offers']) > 0:
                            price = data['offers'][0].get('price', '0')
                        else:
                            price = '0'
                        product['price'] = self._extract_price(str(price))
            except:
                pass
        
        # Extract from embedded JSON if found
        if product_data:
            try:
                # Navigate through nested JSON structure
                if isinstance(product_data, dict):
                    # Try common paths - Nike often uses nested structures
                    product_info = None
                    
                    # Try different possible paths
                    possible_paths = [
                        lambda d: d.get('product'),
                        lambda d: d.get('Product'),
                        lambda d: d.get('products', {}).get('byId', {}),
                        lambda d: d.get('Threads', {}).get('products', {}).get('byId', {}),
                        lambda d: list(d.get('products', {}).get('byId', {}).values())[0] if d.get('products', {}).get('byId') else None,
                    ]
                    
                    for path_func in possible_paths:
                        try:
                            result = path_func(product_data)
                            if result and isinstance(result, dict):
                                product_info = result
                                break
                        except:
                            continue
                    
                    # If still None, try to find any dict with product-like keys
                    if not product_info:
                        def find_product_dict(d, depth=0):
                            if depth > 5:  # Limit recursion
                                return None
                            if isinstance(d, dict):
                                # Check if this looks like a product dict
                                if any(key in d for key in ['title', 'name', 'colorway', 'sku', 'price', 'productTitle']):
                                    return d
                                # Recurse into nested dicts
                                for value in d.values():
                                    result = find_product_dict(value, depth + 1)
                                    if result:
                                        return result
                            elif isinstance(d, list):
                                for item in d:
                                    result = find_product_dict(item, depth + 1)
                                    if result:
                                        return result
                            return None
                        
                        product_info = find_product_dict(product_data)
                    
                    if isinstance(product_info, dict):
                        # Get first product if it's a dict of products
                        if 'byId' in product_info and isinstance(product_info['byId'], dict):
                            product_info = list(product_info['byId'].values())[0] if product_info['byId'] else {}
                        
                        # Extract name
                        if not product['name'] or product['name'] == 'Unknown Product':
                            product['name'] = (product_info.get('title') or 
                                             product_info.get('name') or 
                                             product_info.get('productTitle') or
                                             product_info.get('productName') or
                                             product['name'])
                        
                        # Extract price
                        price_info = (product_info.get('price') or 
                                    product_info.get('currentPrice') or
                                    product_info.get('priceInfo') or
                                    product_info.get('price', {}))
                        if isinstance(price_info, dict):
                            price = price_info.get('currentPrice') or price_info.get('amount') or price_info.get('fullPrice') or '0'
                        else:
                            price = str(price_info) if price_info else '0'
                        if price and price != '0':
                            product['price'] = self._extract_price(str(price))
                        
                        # Extract colors - try multiple possible keys
                        colorways = (product_info.get('colorways') or 
                                   product_info.get('colorwaysList') or
                                   product_info.get('colorOptions') or
                                   product_info.get('availableColorways') or
                                   product_info.get('colorways', {}).get('byId', {}))
                        
                        # Handle dict of colorways
                        if isinstance(colorways, dict):
                            if 'byId' in colorways:
                                colorways = list(colorways['byId'].values())
                            else:
                                colorways = list(colorways.values())
                        
                        if isinstance(colorways, list):
                            for colorway in colorways:
                                if isinstance(colorway, dict):
                                    color_name = (colorway.get('colorDescription') or 
                                                colorway.get('color') or 
                                                colorway.get('name') or
                                                colorway.get('title'))
                                    if color_name and str(color_name) not in product.get('colors', []):
                                        product.setdefault('colors', []).append(str(color_name))
                        
                        # Extract sizes - try multiple possible keys
                        skus = (product_info.get('skus') or 
                              product_info.get('availableSkus') or
                              product_info.get('sizes') or
                              product_info.get('availableSizes') or
                              product_info.get('skus', {}).get('byId', {}))
                        
                        # Handle dict of skus
                        if isinstance(skus, dict):
                            if 'byId' in skus:
                                skus = list(skus['byId'].values())
                            else:
                                skus = list(skus.values())
                        
                        if isinstance(skus, list):
                            for sku in skus:
                                if isinstance(sku, dict):
                                    size = (sku.get('size') or 
                                          sku.get('nikeSize') or 
                                          sku.get('localizedSize') or
                                          sku.get('usSize') or
                                          sku.get('sizeChart') or
                                          sku.get('sizeDisplaySize'))
                                    if size:
                                        size_str = str(size).strip()
                                        # Extract numeric part if size is like "US 10" or "10.5"
                                        size_match = re.search(r'(\d+(?:\.\d+)?)', size_str)
                                        if size_match:
                                            size_str = size_match.group(1)
                                        if size_str and size_str not in product.get('sizes', []):
                                            product.setdefault('sizes', []).append(size_str)
            except Exception as e:
                # Silently fail - will use fallback methods
                pass
        
        # Price - try multiple selectors (fallback)
        if product.get('price', 0) == 0:
            price_elem = (soup.find('div', {'data-testid': 'product-price'}) or
                         soup.find('div', class_='product-price') or
                         soup.find('span', class_='product-price') or
                         soup.find('div', class_=lambda x: x and 'price' in x.lower()) or
                         soup.find('span', class_=lambda x: x and 'price' in x.lower()))
            price_text = price_elem.get_text(strip=True) if price_elem else '0'
            product['price'] = self._extract_price(price_text)
        
        # Colors - try multiple selectors (fallback)
        if not product.get('colors'):
            product['colors'] = []
            color_elems = (soup.find_all('button', {'data-testid': 'colorway-button'}) or
                          soup.find_all('button', {'aria-label': lambda x: x and 'color' in x.lower()}) or
                          soup.find_all('div', class_='colorway') or
                          soup.find_all('button', class_=lambda x: x and 'color' in x.lower()))
            for elem in color_elems:
                color_name = elem.get('aria-label') or elem.get('title') or elem.get_text(strip=True)
                if color_name and color_name not in product['colors']:
                    product['colors'].append(color_name)
        
        # If still no colors found, try to extract from product name or description
        if not product['colors']:
            # Try to find color in the page text
            color_keywords = ['Black', 'White', 'Red', 'Blue', 'Green', 'Grey', 'Gray', 'Navy', 'Orange', 'Pink', 'Beige', 'Brown']
            page_text = soup.get_text().lower()
            for color in color_keywords:
                if color.lower() in page_text:
                    product['colors'].append(color)
                    break
        
        # Default color if none found
        if not product['colors']:
            product['colors'] = ['Unknown']
        
        # Sizes - try multiple selectors (fallback)
        if not product.get('sizes'):
            product['sizes'] = []
            size_elems = (soup.find_all('button', {'data-testid': 'size-dropdown'}) or
                         soup.find_all('button', {'data-testid': lambda x: x and 'size' in x.lower()}) or
                         soup.find_all('button', class_='size-button') or
                         soup.find_all('button', class_=lambda x: x and 'size' in x.lower()) or
                         soup.find_all('option', value=lambda x: x and x.isdigit()))
            for elem in size_elems:
                size_text = elem.get_text(strip=True) or elem.get('value', '')
                # Extract numeric size (e.g., "39", "40.5", "US 10" -> "10")
                size_match = re.search(r'(\d+(?:\.\d+)?)', str(size_text))
                if size_match:
                    size = size_match.group(1)
                    if size not in product['sizes']:
                        product['sizes'].append(size)
        
        # Default sizes if none found
        if not product['sizes']:
            product['sizes'] = ['39', '40', '41', '42', '43']
        
        # Images - Enhanced extraction with deduplication
        product['images'] = []
        seen_image_bases = set()  # Track base URLs to avoid duplicates with different params
        
        def add_unique_image(url: str, source: str = "unknown"):
            """Add image if it's unique and high quality (ignoring query parameters and resolution variants)"""
            if not url or 'http' not in url:
                return False
            
            # Clean URL - remove query parameters
            base_url = url.split('?')[0].split('#')[0]
            
            # Skip very small thumbnails and icons by keyword
            skip_keywords = ['thumbnail', 'icon', 'thumb_', '_thumb', 'favicon', 'sprite', 'preview']
            if any(x in base_url.lower() for x in skip_keywords):
                print(f"  [DEBUG] Skipping thumbnail/icon: {base_url[:80]}... (from {source})")
                return False
            
            # Extract resolution from URL if present (e.g., _2048x, _1024x512, 800w, etc.)
            # Common patterns: _2048x, _1024x1024, _800w, _512h, etc.
            resolution_match = re.search(r'_(\d{3,4})(?:x(\d{3,4})?|w|h)', base_url, re.IGNORECASE)
            if resolution_match:
                width = int(resolution_match.group(1))
                # Skip low-resolution images (< MIN_IMAGE_RESOLUTION)
                if width < MIN_IMAGE_RESOLUTION:
                    print(f"  [DEBUG] Skipping low-res image ({width}px): {base_url[:80]}... (from {source})")
                    return False
            
            # Skip images explicitly marked as small/low quality
            if any(x in base_url.lower() for x in ['_small.', '_low.', '_preview.', '_xs.', '_s.']):
                print(f"  [DEBUG] Skipping low-quality variant: {base_url[:80]}... (from {source})")
                return False
            
            # Nike/Adidas often add resolution suffixes - normalize to detect duplicates
            # Patterns: _2048x, _1024x512, _small, _medium, etc. before .jpg/.png/.webp
            normalized_url = re.sub(r'_(\d+x\d*|small|medium|large|xl|xxl)\.(jpg|jpeg|png|webp)', r'.\2', base_url, flags=re.IGNORECASE)
            
            # Skip if we've seen this normalized image already
            if normalized_url in seen_image_bases:
                print(f"  [DEBUG] Skipping duplicate/variant: {base_url[:80]}... (from {source})")
                return False
            
            seen_image_bases.add(normalized_url)
            product['images'].append(url)
            
            # Show resolution in debug output if available
            res_info = f" ({width}px)" if resolution_match else ""
            print(f"  [DEBUG] ✓ Added unique image #{len(product['images'])}{res_info}: {base_url[:100]}... (from {source})")
            return True
        
        # 1. Try to extract images from JSON data first (most reliable)
        print(f"  [DEBUG] Searching for images in JSON data...")
        if product_data:
            try:
                if isinstance(product_data, dict):
                    product_info = (product_data.get('product') or 
                                  product_data.get('Product') or
                                  product_data.get('products', {}).get('byId', {}))
                    if isinstance(product_info, dict):
                        if 'byId' in product_info:
                            product_info = list(product_info['byId'].values())[0] if product_info['byId'] else {}
                        
                        # Try multiple image field names (including gallery/thumbnail fields)
                        images = (product_info.get('images') or 
                                product_info.get('imageUrls') or
                                product_info.get('media', {}).get('images', []) or
                                product_info.get('galleryImages') or
                                product_info.get('gallery') or
                                product_info.get('thumbnails') or
                                product_info.get('thumbnailImages') or
                                product_info.get('media', {}).get('thumbnails', []) or
                                product_info.get('media', {}).get('gallery', []) or [])
                        
                        # Also check for nested media arrays
                        if not images or (isinstance(images, list) and len(images) == 0):
                            media_obj = product_info.get('media')
                            if isinstance(media_obj, dict):
                                # Try all possible media arrays
                                for media_key in ['images', 'gallery', 'thumbnails', 'squarishURLs', 'portraitURLs']:
                                    media_array = media_obj.get(media_key, [])
                                    if isinstance(media_array, list) and len(media_array) > 0:
                                        images = media_array
                                        print(f"  [DEBUG] Found images in media.{media_key}")
                                        break
                        
                        print(f"  [DEBUG] Found {len(images) if isinstance(images, list) else 0} images in JSON data")
                        
                        if isinstance(images, list):
                            for idx, img_url in enumerate(images):
                                if isinstance(img_url, str):
                                    add_unique_image(img_url, f"JSON-{idx}")
                                elif isinstance(img_url, dict):
                                    # Try multiple URL keys (including thumbnail-specific keys)
                                    img_src = (img_url.get('url') or 
                                             img_url.get('src') or 
                                             img_url.get('imageUrl') or
                                             img_url.get('portraitURL') or
                                             img_url.get('squarishURL') or
                                             img_url.get('thumbnailUrl') or
                                             img_url.get('thumbnail') or
                                             img_url.get('fullUrl') or
                                             img_url.get('highResUrl'))
                                    if img_src:
                                        add_unique_image(str(img_src), f"JSON-dict-{idx}")
            except Exception as e:
                print(f"  [DEBUG] Error extracting from JSON: {str(e)}")
        else:
            print(f"  [DEBUG] No product_data found")
        
        # 2. Try picture elements (Nike often uses these)
        print(f"  [DEBUG] Searching for <picture> elements...")
        picture_elems = soup.find_all('picture')
        print(f"  [DEBUG] Found {len(picture_elems)} picture elements")
        for pic_idx, picture in enumerate(picture_elems):
            # Check source elements in picture
            sources = picture.find_all('source')
            for src_idx, source in enumerate(sources):
                srcset = source.get('srcset', '')
                if srcset:
                    # srcset can have multiple URLs - extract highest resolution
                    urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                    # Filter for Nike URLs
                    nike_urls = [u for u in urls if 'nike' in u.lower()]
                    if nike_urls:
                        # Filter out low-resolution images first
                        high_res_urls = [u for u in nike_urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                        
                        if high_res_urls:
                            # Sort by resolution and take highest
                            urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                            add_unique_image(urls_sorted[0], f"picture-{pic_idx}-srcset-highres")
                        elif nike_urls:
                            # If no high-res found, fall back to best available
                            urls_sorted = sorted(nike_urls, key=get_resolution, reverse=True)
                            add_unique_image(urls_sorted[0], f"picture-{pic_idx}-srcset-best")
            
            # Also check img within picture
            img = picture.find('img')
            if img:
                src = img.get('src') or img.get('data-src')
                if src:
                    add_unique_image(src, f"picture-{pic_idx}-img")
        
        # 3. Extract from left gallery panel (thumbnail gallery)
        print(f"  [DEBUG] Searching for left gallery panel thumbnails...")
        gallery_containers = [
            soup.find('div', {'data-testid': 'gallery-thumbnails'}),
            soup.find('div', {'data-testid': 'product-gallery'}),
            soup.find('div', class_=lambda x: x and ('gallery' in x.lower() or 'thumbnail' in x.lower())),
            soup.find('nav', {'aria-label': lambda x: x and 'gallery' in x.lower()}),
            soup.find('ul', class_=lambda x: x and ('gallery' in x.lower() or 'thumbnail' in x.lower())),
            soup.find('div', class_=lambda x: x and 'image-gallery' in x.lower()),
        ]
        
        for container in gallery_containers:
            if container:
                print(f"  [DEBUG] Found gallery container: {container.name} with class {container.get('class', [])}")
                # Find all images within this gallery container
                gallery_imgs = container.find_all('img')
                print(f"  [DEBUG] Found {len(gallery_imgs)} images in gallery container")
                for img_idx, img in enumerate(gallery_imgs):
                    # Try multiple src attributes
                    src = (img.get('src') or 
                          img.get('data-src') or 
                          img.get('data-lazy-src') or 
                          img.get('data-zoom-src') or
                          img.get('data-original') or
                          img.get('data-thumbnail') or
                          img.get('data-image'))
                    if src:
                        add_unique_image(src, f"gallery-thumb-{img_idx}")
                    
                    # Check srcset for high-res versions
                    srcset = img.get('srcset', '')
                    if srcset:
                        urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                        if urls:
                            # Filter for high-resolution images
                            high_res_urls = [u for u in urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                            if high_res_urls:
                                urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"gallery-thumb-{img_idx}-srcset")
                            else:
                                urls_sorted = sorted(urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"gallery-thumb-{img_idx}-srcset-best")
                
                # Also check for button/clickable elements that might have image URLs
                buttons = container.find_all(['button', 'a'], {'data-image-url': True})
                for btn_idx, btn in enumerate(buttons):
                    img_url = btn.get('data-image-url')
                    if img_url:
                        add_unique_image(img_url, f"gallery-button-{btn_idx}")
        
        # 4. Try multiple img selectors
        print(f"  [DEBUG] Searching for <img> elements with various selectors...")
        img_selectors = [
            ({'data-testid': 'product-image'}, 'data-testid-product-image'),
            ({'data-testid': 'gallery-thumbnail'}, 'data-testid-gallery-thumbnail'),
            ({'data-testid': lambda x: x and 'image' in x.lower()}, 'data-testid-image'),
            ({'class': 'product-image'}, 'class-product-image'),
            ({'class': lambda x: x and 'gallery' in x.lower()}, 'class-gallery'),
            ({'class': lambda x: x and 'media' in x.lower()}, 'class-media'),
            ({'class': lambda x: x and 'thumbnail' in x.lower()}, 'class-thumbnail'),
            ({'alt': lambda x: x and product['name'].lower() in x.lower()}, 'alt-product-name'),
        ]
        
        def get_resolution(url):
            """Extract resolution from URL (prioritizes width indicator)"""
            # Try to find resolution indicators: 2048x, 1024x512, 800w, etc.
            res_match = re.search(r'[_/-](\d{3,4})(?:x(\d{3,4})?|w|h|px)?', url, re.IGNORECASE)
            if res_match:
                try:
                    return int(res_match.group(1))
                except:
                    pass
        
            # Default: assume medium quality if no resolution found but URL looks like product image
            return 1000
        
        for selector, selector_name in img_selectors:
            img_elems = soup.find_all('img', selector)
            print(f"  [DEBUG] Selector '{selector_name}': found {len(img_elems)} images")
            for img_idx, img in enumerate(img_elems):
                # Try multiple src attributes
                src = (img.get('src') or 
                      img.get('data-src') or 
                      img.get('data-lazy-src') or 
                      img.get('data-zoom-src') or
                      img.get('data-original'))
                if src:
                    add_unique_image(src, f"{selector_name}-{img_idx}")
                
                # Also check srcset - extract highest resolution
                    srcset = img.get('srcset', '')
                    if srcset:
                        urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                        if urls:
                            # Filter for high-resolution images only
                            high_res_urls = [u for u in urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                            
                            if high_res_urls:
                                # Sort by resolution and take highest
                                urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"{selector_name}-{img_idx}-srcset-highres")
                            elif urls:
                                # Fallback to best available
                                urls_sorted = sorted(urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"{selector_name}-{img_idx}-srcset-best")
        
        # 5. If still no images, try broader search for Nike images
        print(f"  [DEBUG] Current image count: {len(product['images'])}")
        if len(product['images']) < 5:
            print(f"  [DEBUG] Fewer than 5 images, doing broader search...")
            all_imgs = soup.find_all('img')
            print(f"  [DEBUG] Total <img> tags on page: {len(all_imgs)}")
            for img_idx, img in enumerate(all_imgs):
                src = img.get('src') or img.get('data-src')
                if src and 'nike' in src.lower() and any(x in src.lower() for x in ['.jpg', '.jpeg', '.png', '.webp']):
                    add_unique_image(src, f"broad-search-{img_idx}")
        
        print(f"  [DEBUG] === Total unique images found: {len(product['images'])} ===")
        
        # 6. Final aggressive extraction - get ALL Nike product images
        if len(product['images']) < 5:
            print(f"  [DEBUG] Still fewer than 5 images, extracting ALL Nike images from page...")
            all_imgs = soup.find_all('img')
            for img_idx, img in enumerate(all_imgs):
                # Check all possible src attributes
                srcs = [
                    img.get('src'),
                    img.get('data-src'),
                    img.get('data-lazy-src'),
                    img.get('data-original'),
                    img.get('data-zoom-src'),
                ]
                
                for src in srcs:
                    if src and 'nike' in src.lower():
                        # Check if it looks like a product image (has .jpg, .png, .webp extension)
                        if any(ext in src.lower() for ext in ['.jpg', '.jpeg', '.png', '.webp']):
                            # Avoid obvious non-product images
                            if not any(x in src.lower() for x in ['logo', 'icon', 'avatar', 'banner', 'nav']):
                                add_unique_image(src, f"aggressive-{img_idx}")
                
                # Also check srcset
                srcset = img.get('srcset', '')
                if srcset and 'nike' in srcset.lower():
                    urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                    nike_urls = [u for u in urls if 'nike' in u.lower()]
                    if nike_urls:
                        # Filter for high-resolution images
                        high_res_urls = [u for u in nike_urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                        
                        if high_res_urls:
                            urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                            add_unique_image(urls_sorted[0], f"aggressive-srcset-{img_idx}")
                        elif nike_urls:
                            # Fallback to best available
                            urls_sorted = sorted(nike_urls, key=get_resolution, reverse=True)
                            add_unique_image(urls_sorted[0], f"aggressive-srcset-best-{img_idx}")
        
        print(f"  [DEBUG] === FINAL unique images found: {len(product['images'])} ===")
        if product['images']:
            print(f"  [DEBUG] Sample images:")
            for i, img in enumerate(product['images'][:5]):
                print(f"    {i+1}. {img[:120]}")
        else:
            print(f"  [DEBUG] WARNING: No images found! Page might use JavaScript to load images.")
        
        product['brand'] = brand
        product['url'] = url
        return product
    
    def _scrape_adidas(self, soup: BeautifulSoup, url: str, brand: str) -> Dict:
        """Scrape Adidas product page"""
        product = {}
        
        # Product name - try multiple selectors
        name_elem = (soup.find('h1', class_='product-title') or
                    soup.find('h1', {'data-testid': 'product-title'}) or
                    soup.find('h1', class_=lambda x: x and 'title' in x.lower()) or
                    soup.find('h1', class_=lambda x: x and 'name' in x.lower()) or
                    soup.find('h1') or
                    soup.find('div', class_=lambda x: x and 'product' in x.lower() and 'title' in x.lower()) or
                    soup.find('div', {'data-testid': 'product-title'}) or
                    soup.find('span', class_=lambda x: x and 'product' in x.lower() and 'title' in x.lower()))
        
        if name_elem:
            product['name'] = name_elem.get_text(strip=True)
        else:
            product['name'] = 'Unknown Product'
        
        # Try to extract from JSON-LD structured data
        json_ld = soup.find('script', type='application/ld+json')
        if json_ld:
            try:
                data = json.loads(json_ld.string)
                # Handle both single object and array of objects
                if isinstance(data, list) and len(data) > 0:
                    data = data[0]
                
                if isinstance(data, dict):
                    if 'name' in data and (not product['name'] or product['name'] == 'Unknown Product'):
                        product['name'] = data.get('name', product['name'])
                    
                    # Extract price from offers
                    if 'offers' in data:
                        offers = data['offers']
                        if isinstance(offers, dict):
                            price = offers.get('price', '0')
                            if price and price != '0':
                                product['price'] = self._extract_price(str(price))
                        elif isinstance(offers, list) and len(offers) > 0:
                            # Get the first offer
                            offer = offers[0]
                            if isinstance(offer, dict):
                                price = offer.get('price', '0')
                            else:
                                price = str(offer) if offer else '0'
                            if price and price != '0':
                                product['price'] = self._extract_price(str(price))
                    
                    # Extract images
                    if 'image' in data:
                        images = data['image']
                        if isinstance(images, str):
                            product['images'] = [images]
                        elif isinstance(images, list):
                            product['images'] = images
            except Exception as e:
                # Debug: print error if needed
                pass
        
        # Try to extract from embedded JSON data (Adidas often uses this)
        product_data = None
        script_tags = soup.find_all('script')
        for script in script_tags:
            if not script.string:
                continue
            script_text = script.string
            
            # Look for common Adidas patterns - try multiple regex patterns
            patterns = [
                r'window\.__INITIAL_STATE__\s*=\s*({.+?});',
                r'window\.__INITIAL_DATA__\s*=\s*({.+?});',
                r'window\.__NEXT_DATA__\s*=\s*({.+?});',
                r'window\.__APOLLO_STATE__\s*=\s*({.+?});',
                r'"product"[:\s]*({.+?});',
                r'productData\s*[:=]\s*({.+?});',
                r'__NEXT_DATA__\s*=\s*({.+?})',
                r'pageProps["\']?\s*[:=]\s*({.+?"product".+?})',
            ]
            
            for pattern in patterns:
                try:
                    # Try with DOTALL flag first
                    json_match = re.search(pattern, script_text, re.DOTALL)
                    if json_match:
                        try:
                            data = json.loads(json_match.group(1))
                            if data and (isinstance(data, dict) and ('product' in data or 'Product' in data or 'products' in data or 'pageProps' in data)):
                                product_data = data
                                break
                        except:
                            # Try without the last character (might be a semicolon)
                            try:
                                data = json.loads(json_match.group(1)[:-1])
                                if data:
                                    product_data = data
                                    break
                            except:
                                continue
                except:
                    pass
            
            # Also try to find JSON objects with product data - look for larger JSON objects
            if not product_data and ('product' in script_text.lower() or 'price' in script_text.lower() or 'pageProps' in script_text.lower()):
                try:
                    # Look for larger JSON objects that might contain product data
                    # Try to find objects with product-related keys
                    large_json_patterns = [
                        r'\{[^{}]*"product"[^{}]*(?:\{[^{}]*\}[^{}]*)*\}',
                        r'\{[^{}]*"pageProps"[^{}]*"product"[^{}]*\}',
                        r'\{"props":\s*\{[^{}]*"pageProps"[^{}]*"product"[^{}]*\}',
                    ]
                    for pattern in large_json_patterns:
                        json_matches = re.findall(pattern, script_text, re.DOTALL)
                        for match in json_matches:
                            try:
                                data = json.loads(match)
                                if isinstance(data, dict) and ('product' in data or 'pageProps' in data):
                                    product_data = data
                                    break
                            except:
                                continue
                        if product_data:
                            break
                except:
                    pass
            
            if product_data:
                break
        
        # Extract from embedded JSON if found
        if product_data:
            try:
                if isinstance(product_data, dict):
                    # Try to find product info in nested structure - try multiple paths
                    product_info = None
                    
                    # Try different possible paths
                    possible_paths = [
                        lambda d: d.get('product'),
                        lambda d: d.get('Product'),
                        lambda d: d.get('products', {}).get('byId', {}),
                        lambda d: d.get('pageProps', {}).get('product', {}),
                        lambda d: d.get('props', {}).get('pageProps', {}).get('product', {}),
                        lambda d: list(d.get('products', {}).get('byId', {}).values())[0] if d.get('products', {}).get('byId') else None,
                    ]
                    
                    for path_func in possible_paths:
                        try:
                            result = path_func(product_data)
                            if result and isinstance(result, dict):
                                product_info = result
                                break
                        except:
                            continue
                    
                    # If still None, try recursive search
                    if not product_info:
                        def find_product_dict(d, depth=0):
                            if depth > 5:  # Limit recursion
                                return None
                            if isinstance(d, dict):
                                # Check if this looks like a product dict
                                if any(key in d for key in ['title', 'name', 'productTitle', 'productName', 'colorway', 'sku', 'price']):
                                    return d
                                # Recurse into nested dicts
                                for value in d.values():
                                    result = find_product_dict(value, depth + 1)
                                    if result:
                                        return result
                            elif isinstance(d, list):
                                for item in d:
                                    result = find_product_dict(item, depth + 1)
                                    if result:
                                        return result
                            return None
                        
                        product_info = find_product_dict(product_data)
                    
                    if isinstance(product_info, dict):
                        if 'byId' in product_info and isinstance(product_info['byId'], dict):
                            product_info = list(product_info['byId'].values())[0] if product_info['byId'] else {}
                        
                        # Extract name - try many possible fields
                        if not product['name'] or product['name'] == 'Unknown Product':
                            product['name'] = (product_info.get('title') or 
                                             product_info.get('name') or 
                                             product_info.get('productTitle') or
                                             product_info.get('productName') or
                                             product_info.get('displayName') or
                                             product_info.get('model') or
                                             product_info.get('modelName') or
                                             product['name'])
                        
                        # Extract price - try many possible fields and paths
                        price = None
                        price_fields = [
                            product_info.get('price'),
                            product_info.get('currentPrice'),
                            product_info.get('priceInfo'),
                            product_info.get('pricing'),
                            product_info.get('listPrice'),
                            product_info.get('salePrice'),
                            product_info.get('finalPrice'),
                            product_info.get('price', {}).get('currentPrice'),
                            product_info.get('price', {}).get('amount'),
                            product_info.get('priceInfo', {}).get('currentPrice'),
                            product_info.get('priceInfo', {}).get('amount'),
                            product_info.get('pricing', {}).get('currentPrice'),
                            product_info.get('pricing', {}).get('amount'),
                        ]
                        
                        for price_field in price_fields:
                            if price_field:
                                if isinstance(price_field, dict):
                                    price = (price_field.get('currentPrice') or 
                                            price_field.get('amount') or 
                                            price_field.get('fullPrice') or 
                                            price_field.get('salePrice') or 
                                            price_field.get('listPrice'))
                                else:
                                    price = price_field
                                
                                if price:
                                    price_str = str(price)
                                    if price_str and price_str != '0' and price_str.lower() not in ['null', 'none', '']:
                                        extracted_price = self._extract_price(price_str)
                                        if extracted_price > 0:
                                            product['price'] = extracted_price
                                            break
                        
                        # Extract colors
                        colorways = (product_info.get('colorways') or 
                                   product_info.get('availableColorways') or
                                   product_info.get('colorOptions') or
                                   product_info.get('variants', {}).get('colors', []) or
                                   product_info.get('colorways', {}).get('byId', {}) or [])
                        
                        # Handle dict of colorways
                        if isinstance(colorways, dict):
                            if 'byId' in colorways:
                                colorways = list(colorways['byId'].values())
                            else:
                                colorways = list(colorways.values())
                        
                        if isinstance(colorways, list):
                            for colorway in colorways:
                                if isinstance(colorway, dict):
                                    # Try multiple fields for color name
                                    color_name = (colorway.get('colorDescription') or 
                                                colorway.get('color') or 
                                                colorway.get('name') or
                                                colorway.get('title') or
                                                colorway.get('colorway') or
                                                colorway.get('colorwayName') or
                                                colorway.get('displayName'))
                                    
                                    # If color name is like "Core Black / Cloud White / Carbon", keep it as is
                                    # Otherwise, extract the first meaningful part
                                    if color_name:
                                        color_name = str(color_name).strip()
                                        # If it's a simple color like "Black", keep it
                                        # If it's a colorway like "Core Black / Cloud White / Carbon", keep it
                                        if color_name and color_name not in product.get('colors', []):
                                            product.setdefault('colors', []).append(color_name)
                                
                                elif isinstance(colorway, str):
                                    # Direct string colorway
                                    if colorway and colorway not in product.get('colors', []):
                                        product.setdefault('colors', []).append(colorway)
                        
                        # Extract sizes
                        skus = (product_info.get('skus') or 
                              product_info.get('availableSkus') or
                              product_info.get('sizes') or
                              product_info.get('variants', {}).get('sizes', []) or [])
                        
                        # Handle dict of skus
                        if isinstance(skus, dict):
                            if 'byId' in skus:
                                skus = list(skus['byId'].values())
                            else:
                                skus = list(skus.values())
                        
                        if isinstance(skus, list):
                            for sku in skus:
                                if isinstance(sku, dict):
                                    size = (sku.get('size') or 
                                          sku.get('localizedSize') or
                                          sku.get('usSize') or
                                          sku.get('euSize') or
                                          sku.get('sizeChart'))
                                    if size:
                                        size_str = str(size).strip()
                                        # Extract numeric part if size is like "US 10" or "10.5"
                                        size_match = re.search(r'(\d+(?:\.\d+)?)', size_str)
                                        if size_match:
                                            size_str = size_match.group(1)
                                        if size_str and size_str not in product.get('sizes', []):
                                            product.setdefault('sizes', []).append(size_str)
                        
                        # Extract images with better deduplication
                        images = (product_info.get('images') or 
                                product_info.get('imageUrls') or
                                product_info.get('media', {}).get('images', []) or
                                product_info.get('gallery', []) or
                                product_info.get('galleryImages', []))
                        
                        if not product.get('images'):
                            product['images'] = []
                        
                        seen_bases = set()
                        if isinstance(images, list):
                            for img_url in images:
                                if isinstance(img_url, str) and 'http' in img_url:
                                    base = img_url.split('?')[0]
                                    if base not in seen_bases:
                                        seen_bases.add(base)
                                        product['images'].append(img_url)
                                elif isinstance(img_url, dict):
                                    img_src = (img_url.get('url') or 
                                             img_url.get('src') or 
                                             img_url.get('imageUrl') or
                                             img_url.get('href') or
                                             img_url.get('portraitURL'))
                                    if img_src and 'http' in str(img_src):
                                        base = str(img_src).split('?')[0]
                                        if base not in seen_bases:
                                            seen_bases.add(base)
                                            product['images'].append(str(img_src))
            except Exception as e:
                # Silently fail - will use fallback methods
                pass
        
        # Product name fallback - extract from URL if still unknown
        if not product.get('name') or product['name'] == 'Unknown Product':
            # Try to extract product name from URL path
            # e.g., /pureboost-23-shoes/IF1544.html -> "Pureboost 23 Shoes"
            try:
                url_path = urlparse(url).path
                if url_path:
                    # Extract the product path segment
                    path_parts = [p for p in url_path.split('/') if p and not p.endswith('.html')]
                    if path_parts:
                        # Get the last meaningful part (usually the product name)
                        product_name = path_parts[-1].replace('-', ' ').title()
                        if product_name and len(product_name) > 3:
                            product['name'] = product_name
            except:
                pass
        
        # Price - try multiple selectors (fallback)
        if product.get('price', 0) == 0:
            # Try many different price selectors
            price_selectors = [
                soup.find('span', class_='price'),
                soup.find('div', class_='product-price'),
                soup.find('span', class_=lambda x: x and 'price' in x.lower()),
                soup.find('div', class_=lambda x: x and 'price' in x.lower()),
                soup.find('span', {'data-testid': 'price'}),
                soup.find('div', {'data-testid': 'price'}),
                soup.find('span', {'data-testid': lambda x: x and 'price' in x.lower()}),
                soup.find('div', {'data-testid': lambda x: x and 'price' in x.lower()}),
                soup.find('span', {'data-auto-id': lambda x: x and 'price' in x.lower()}),
                soup.find('div', {'data-auto-id': lambda x: x and 'price' in x.lower()}),
                soup.find('p', class_=lambda x: x and 'price' in x.lower()),
                soup.find('strong', class_=lambda x: x and 'price' in x.lower()),
                # Try to find any element with price-like text
                soup.find(string=re.compile(r'[\d.,]+\s*[₫đ]|vnd|dong', re.IGNORECASE)),
            ]
            
            for price_elem in price_selectors:
                if price_elem:
                    if hasattr(price_elem, 'get_text'):
                        price_text = price_elem.get_text(strip=True)
                    else:
                        # It's a NavigableString
                        price_text = str(price_elem).strip()
                    
                    if price_text:
                        extracted_price = self._extract_price(price_text)
                        if extracted_price > 0:
                            product['price'] = extracted_price
                            break
        
        # Colors - try multiple selectors (fallback)
        if not product.get('colors'):
            product['colors'] = []
            
            # Try to find color selector buttons/links
            color_elems = (soup.find_all('button', class_='color-selector') or
                          soup.find_all('button', {'aria-label': lambda x: x and 'color' in x.lower()}) or
                          soup.find_all('div', class_='color-option') or
                          soup.find_all('button', class_=lambda x: x and 'color' in x.lower()) or
                          soup.find_all('a', class_=lambda x: x and 'color' in x.lower()) or
                          soup.find_all('div', class_=lambda x: x and 'colorway' in x.lower()) or
                          soup.find_all('li', class_=lambda x: x and 'color' in x.lower()) or
                          soup.find_all('div', {'data-testid': lambda x: x and 'color' in x.lower()}))
            
            for elem in color_elems:
                # Try multiple attributes for color name
                color_name = (elem.get('aria-label') or 
                            elem.get('title') or 
                            elem.get('data-color') or
                            elem.get('data-colorway') or
                            elem.get_text(strip=True))
                
                # Clean up color name (remove "Color:" prefix, etc.)
                if color_name:
                    color_name = re.sub(r'^color\s*:?\s*', '', color_name, flags=re.IGNORECASE).strip()
                    # Handle color names like "Core Black / Cloud White / Carbon"
                    if ' / ' in color_name:
                        # For colorways like "Core Black / Cloud White / Carbon"
                        # Keep the full colorway name as-is
                        full_colorway = color_name.strip()
                        if full_colorway and full_colorway not in product['colors']:
                            product['colors'].append(full_colorway)
                    else:
                        # Simple color name
                        if color_name and color_name not in product['colors']:
                            product['colors'].append(color_name)
            
            # Also try to find color names in text near "Colour" or "Color" headings
            if not product['colors']:
                # Look for sections with "Colour" or "Color" heading
                color_sections = soup.find_all(['h2', 'h3', 'div', 'span'], string=re.compile(r'colou?r', re.IGNORECASE))
                for section in color_sections:
                    # Find parent container
                    parent = section.find_parent(['div', 'section', 'article', 'main'])
                    if parent:
                        # Look for buttons/links/images in the color section
                        color_items = parent.find_all(['button', 'a', 'div', 'li', 'span'], 
                                                      class_=lambda x: x and ('color' in x.lower() or 'swatch' in x.lower() or 'variant' in x.lower()),
                                                      limit=20)  # Limit to avoid too many results
                        for item in color_items:
                            color_name = (item.get('aria-label') or 
                                        item.get('title') or 
                                        item.get('data-color') or
                                        item.get('data-colorway') or
                                        item.get('data-testid') or
                                        item.get('data-auto-id') or
                                        item.get_text(strip=True))
                            if color_name:
                                color_name = re.sub(r'^color\s*:?\s*', '', str(color_name), flags=re.IGNORECASE).strip()
                                # Skip if it's just generic text like "Color", "Select color", etc.
                                if color_name.lower() in ['color', 'colour', 'select color', 'choose color', '']:
                                    continue
                                # Handle colorways like "Core Black / Cloud White / Carbon"
                                if ' / ' in color_name:
                                    # Keep full colorway name
                                    full_colorway = color_name.strip()
                                    if full_colorway and full_colorway not in product['colors'] and len(full_colorway) > 2:
                                        product['colors'].append(full_colorway)
                                else:
                                    if color_name and color_name not in product['colors'] and len(color_name) > 2:
                                        product['colors'].append(color_name)
                        
                        # Also look for any text that looks like color names in the section
                        section_text = parent.get_text()
                        # Look for color patterns like "Core Black", "White", etc.
                        color_patterns = re.findall(r'\b(Black|White|Red|Blue|Green|Yellow|Orange|Pink|Purple|Grey|Gray|Brown|Gold|Silver|Navy|Beige|Carbon|Cloud|Core|Lavender|Peach)\b', section_text, re.IGNORECASE)
                        for color_word in color_patterns:
                            if color_word and color_word not in product['colors']:
                                product['colors'].append(color_word)
            
            # Try to extract from image alt text (often contains color info)
            if not product['colors']:
                img_elems = soup.find_all('img', {'alt': lambda x: x and ('color' in x.lower() or 'shoe' in x.lower())})
                for img in img_elems:
                    alt_text = img.get('alt', '')
                    # Look for color names in alt text
                    # Patterns like "Black shoe", "White Adidas", etc.
                    color_match = re.search(r'\b(black|white|red|blue|green|yellow|orange|pink|purple|grey|gray|brown|gold|silver|navy|beige)\b', alt_text, re.IGNORECASE)
                    if color_match:
                        color_name = color_match.group(1).capitalize()
                        if color_name not in product['colors']:
                            product['colors'].append(color_name)
        
        # Default color if none found
        if not product['colors']:
            product['colors'] = ['Unknown']
        
        # Sizes - try multiple selectors (fallback)
        if not product.get('sizes'):
            product['sizes'] = []
            size_elems = (soup.find_all('button', class_='size-selector') or
                         soup.find_all('button', {'aria-label': lambda x: x and 'size' in x.lower()}) or
                         soup.find_all('option', class_='size-option') or
                         soup.find_all('button', class_=lambda x: x and 'size' in x.lower()) or
                         soup.find_all('select', {'name': lambda x: x and 'size' in x.lower()}) or
                         soup.find_all('option', value=lambda x: x and x.replace('.', '').isdigit()))
            for elem in size_elems:
                size_text = elem.get_text(strip=True) or elem.get('value', '')
                size_match = re.search(r'(\d+(?:\.\d+)?)', str(size_text))
                if size_match:
                    size = size_match.group(1)
                    if size not in product['sizes']:
                        product['sizes'].append(size)
        
        # Default sizes if none found
        if not product['sizes']:
            product['sizes'] = ['39', '40', '41', '42', '43']
        
        # Images - Enhanced fallback with deduplication
        print(f"  [DEBUG] Adidas image extraction starting...")
        if not product.get('images') or len(product['images']) < 3:
            if not product.get("images"):
                product["images"] = []
            
            seen_image_bases = set(re.sub(r'_(\d+x\d*|small|medium|large|xl)\.(jpg|jpeg|png|webp)', r'.\2', img.split('?')[0], flags=re.IGNORECASE) for img in product.get('images', []))
            
            def add_unique_image(url: str, source: str = "unknown"):
                """Add image if unique and high quality"""
                if not url or 'http' not in url:
                    return False
                
                base_url = url.split('?')[0].split('#')[0]
                
                # Skip thumbnails and icons - Expanded list
                skip_keywords = [
                    'thumbnail', 'icon', 'thumb_', '_thumb', 'favicon', 'sprite', 'preview', 'logo',
                    'mini', 'small', 'tiny', 'placeholder', 'avatar', 'loading', 'spinner'
                ]
                if any(x in base_url.lower() for x in skip_keywords):
                    print(f"  [DEBUG] Skipping thumbnail/icon: {base_url[:80]}... (from {source})")
                    return False
                
                # Check resolution in URL - More aggressive check
                # Matches: _2048x, 800w, 500h, 600x600, etc.
                resolution_match = re.search(r'[_/-](\d{3,4})(?:x(\d{3,4})?|w|h|px)?', base_url, re.IGNORECASE)
                
                width = 0
                if resolution_match:
                    try:
                        width = int(resolution_match.group(1))
                        # Skip low-resolution images (< MIN_IMAGE_RESOLUTION)
                        if width < MIN_IMAGE_RESOLUTION:
                            print(f"  [DEBUG] Skipping low-res image ({width}px): {base_url[:80]}... (from {source})")
                            return False
                    except:
                        pass
                
                # Skip images explicitly marked as small/low quality
                if any(x in base_url.lower() for x in ['_small.', '_low.', '_preview.', '_xs.', '_s.', '_m.', '_t.']):
                    print(f"  [DEBUG] Skipping low-quality variant: {base_url[:80]}... (from {source})")
                    return False
                
                # Normalize URL to detect duplicates
                normalized_url = re.sub(r'_(\d+x\d*|small|medium|large|xl)\.(jpg|jpeg|png|webp)', r'.\2', base_url, flags=re.IGNORECASE)
                
                if normalized_url in seen_image_bases:
                    print(f"  [DEBUG] Skipping duplicate: {base_url[:80]}... (from {source})")
                    return False
                
                seen_image_bases.add(normalized_url)
                product['images'].append(url)
                
                res_info = f" ({width}px)" if width > 0 else " (unknown res)"
                print(f"  [DEBUG] ✓ Added unique image #{len(product['images'])}{res_info}: {base_url[:100]}... (from {source})")
                return True
            
            def get_resolution(url):
                """Extract resolution from URL"""
                res_match = re.search(r'[_/-](\d{3,4})(?:x(\d{3,4})?|w|h|px)?', url, re.IGNORECASE)
                return int(res_match.group(1)) if res_match else 0
            
            # Extract from gallery panel (thumbnails)
            print(f"  [DEBUG] Searching for gallery panel thumbnails...")
            gallery_containers = [
                soup.find('div', {'data-testid': 'gallery-thumbnails'}),
                soup.find('div', {'data-testid': 'product-gallery'}),
                soup.find('div', class_=lambda x: x and ('gallery' in x.lower() or 'thumbnail' in x.lower())),
                soup.find('nav', {'aria-label': lambda x: x and 'gallery' in x.lower()}),
                soup.find('ul', class_=lambda x: x and ('gallery' in x.lower() or 'thumbnail' in x.lower())),
                soup.find('div', class_=lambda x: x and 'image-gallery' in x.lower()),
            ]
            
            for container in gallery_containers:
                if container:
                    print(f"  [DEBUG] Found gallery container: {container.name}")
                    gallery_imgs = container.find_all('img')
                    print(f"  [DEBUG] Found {len(gallery_imgs)} images in gallery container")
                    for img_idx, img in enumerate(gallery_imgs):
                        src = (img.get('src') or 
                              img.get('data-src') or 
                              img.get('data-lazy-src') or 
                              img.get('data-zoom-src') or
                              img.get('data-original') or
                              img.get('data-thumbnail') or
                              img.get('data-image'))
                        if src:
                            add_unique_image(src, f"gallery-thumb-{img_idx}")
                        
                        srcset = img.get('srcset', '')
                        if srcset:
                            urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                            if urls:
                                high_res_urls = [u for u in urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                                if high_res_urls:
                                    urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                                    add_unique_image(urls_sorted[0], f"gallery-thumb-{img_idx}-srcset")
            
            # Try picture elements
            print(f"  [DEBUG] Searching for <picture> elements...")
            picture_elems = soup.find_all('picture')
            print(f"  [DEBUG] Found {len(picture_elems)} picture elements")
            for pic_idx, picture in enumerate(picture_elems):
                sources = picture.find_all('source')
                for src_idx, source in enumerate(sources):
                    srcset = source.get('srcset', '')
                    if srcset:
                        urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                        adidas_urls = [u for u in urls if 'adidas' in u.lower()]
                        if adidas_urls:
                            # Filter for high-resolution only
                            high_res_urls = [u for u in adidas_urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                            
                            if high_res_urls:
                                urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"picture-{pic_idx}-srcset-highres")
                            elif adidas_urls:
                                # Fallback to best available
                                urls_sorted = sorted(adidas_urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"picture-{pic_idx}-srcset-best")
                
                img = picture.find('img')
                if img:
                    src = img.get('src') or img.get('data-src')
                    if src:
                        add_unique_image(src, f"picture-{pic_idx}-img")
            
            # Try multiple img selectors
            print(f"  [DEBUG] Searching for <img> elements...")
            img_selectors = [
                ({'class': 'product-image'}, 'class-product-image'),
                ({'data-testid': 'product-image'}, 'data-testid-product-image'),
                ({'data-testid': 'gallery-thumbnail'}, 'data-testid-gallery-thumbnail'),
                ({'data-testid': lambda x: x and 'image' in x.lower()}, 'data-testid-image'),
                ({'class': lambda x: x and 'gallery' in x.lower()}, 'class-gallery'),
                ({'class': lambda x: x and 'media' in x.lower()}, 'class-media'),
                ({'class': lambda x: x and 'thumbnail' in x.lower()}, 'class-thumbnail'),
                ({'alt': lambda x: x and product['name'].lower() in x.lower()}, 'alt-product-name'),
            ]
            
            for selector, selector_name in img_selectors:
                img_elems = soup.find_all('img', selector)
                print(f"  [DEBUG] Selector '{selector_name}': found {len(img_elems)} images")
                for img_idx, img in enumerate(img_elems):
                    src = (img.get('src') or 
                          img.get('data-src') or 
                          img.get('data-lazy-src') or 
                          img.get('data-zoom-src') or
                          img.get('data-original'))
                    if src:
                        add_unique_image(src, f"{selector_name}-{img_idx}")
                    
                    srcset = img.get('srcset', '')
                    if srcset:
                        urls = [u.strip().split()[0] for u in srcset.split(',') if u.strip()]
                        if urls:
                            # Filter for high-resolution only
                            high_res_urls = [u for u in urls if get_resolution(u) >= MIN_IMAGE_RESOLUTION]
                            
                            if high_res_urls:
                                urls_sorted = sorted(high_res_urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"{selector_name}-{img_idx}-srcset-highres")
                            elif urls:
                                # Fallback to best available
                                urls_sorted = sorted(urls, key=get_resolution, reverse=True)
                                add_unique_image(urls_sorted[0], f"{selector_name}-{img_idx}-srcset-best")
            
            # Broader search if still not enough
            print(f"  [DEBUG] Current image count: {len(product['images'])}")
            if len(product['images']) < 5:
                print(f"  [DEBUG] Fewer than 5 images, doing broader search...")
                all_imgs = soup.find_all('img')
                print(f"  [DEBUG] Total <img> tags on page: {len(all_imgs)}")
                for img_idx, img in enumerate(all_imgs):
                    src = img.get('src') or img.get('data-src')
                    if src and 'adidas' in src.lower() and any(x in src.lower() for x in ['.jpg', '.jpeg', '.png', '.webp']):
                        add_unique_image(src, f"broad-search-{img_idx}")
        
        print(f"  [DEBUG] === FINAL unique images found: {len(product['images'])} ===")
        if product['images']:
            print(f"  [DEBUG] Sample images:")
            for i, img in enumerate(product['images'][:5]):
                print(f"    {i+1}. {img[:120]}")
        else:
            print(f"  [DEBUG] WARNING: No images found! Page might use JavaScript to load images.")
        
        product['brand'] = brand
        product['url'] = url
        return product
    
    def _scrape_retail(self, soup: BeautifulSoup, url: str, brand: str) -> Dict:
        """Scrape retail site product page"""
        product = {}
        
        # Product name
        name_elem = soup.find('h1', class_='product-title') or \
                   soup.find('h1')
        product['name'] = name_elem.get_text(strip=True) if name_elem else 'Unknown Product'
        
        # Price
        price_elem = soup.find('span', class_='price') or \
                    soup.find('div', class_='product-price')
        price_text = price_elem.get_text(strip=True) if price_elem else '0'
        product['price'] = self._extract_price(price_text)
        
        # Colors and sizes - try to extract from product options
        product['colors'] = []
        product['sizes'] = []
        
        # Images
        product['images'] = []
        img_elems = soup.find_all('img', class_='product-image')
        for img in img_elems:
            src = img.get('src') or img.get('data-src')
            if src and 'http' in src:
                product['images'].append(src)
        
        product['brand'] = brand
        product['url'] = url
        return product
    
    def _scrape_generic(self, soup: BeautifulSoup, url: str, brand: str) -> Dict:
        """Generic scraper for unknown sites"""
        product = {
            'name': 'Unknown Product',
            'price': 0,
            'colors': [],
            'sizes': [],
            'images': [],
            'brand': brand,
            'url': url
        }
        
        # Try to find product name
        name_elem = soup.find('h1')
        if name_elem:
            product['name'] = name_elem.get_text(strip=True)
        
        # Try to find price
        price_pattern = re.compile(r'[\$]?(\d{1,3}(?:,\d{3})*(?:\.\d{2})?)')
        price_matches = price_pattern.findall(soup.get_text())
        if price_matches:
            product['price'] = self._extract_price(price_matches[0])
        
        # Try to find images
        img_elems = soup.find_all('img')
        for img in img_elems[:10]:  # Limit to first 10 images
            src = img.get('src') or img.get('data-src')
            if src and 'http' in src and any(ext in src.lower() for ext in ['.jpg', '.jpeg', '.png', '.webp']):
                product['images'].append(src)
        
        return product
    
    def _extract_price(self, price_text: str) -> float:
        """Extract numeric price from text - handles VND, USD, EUR, etc."""
        if not price_text:
            return 0.0
        
        # Normalize the input - convert to string if not already
        price_text = str(price_text).strip()
        
        # Handle VND currency (Vietnamese Dong)
        # VND can be written as: "1.000.000 VND", "1,000,000₫", "1.000.000đ", etc.
        # VND amounts are typically large, so we need to handle dots/commas differently
        
        # Check if it's VND currency
        is_vnd = ('vnd' in price_text.lower() or 
                 '₫' in price_text or 
                 'đ' in price_text or
                 'dong' in price_text.lower())
        
        if is_vnd:
            # For VND, dots are thousand separators (e.g., "1.000.000" = 1000000)
            # Extract the first number sequence that looks like a price
            # Look for patterns like: "1.000.000", "1,000,000", etc.
            # VND prices typically have 6-9 digits
            price_match = re.search(r'(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{3})*)', price_text)
            if not price_match:
                # Try simpler pattern - just digits with dots/commas
                price_match = re.search(r'(\d+[.,]\d{3}(?:[.,]\d{3})*)', price_text)
            
            if price_match:
                price_clean = price_match.group(1)
                # Remove all dots (VND uses dots as thousand separators)
                price_clean = price_clean.replace('.', '')
                # Remove commas (some formats use commas)
                price_clean = price_clean.replace(',', '')
                try:
                    price_value = float(price_clean)
                    # Sanity check: VND prices are typically > 1000 and < 100 million
                    if 1000 <= price_value <= 100000000:
                        return price_value
                except:
                    pass
            return 0.0
        
        # For other currencies (USD, EUR, etc.)
        # Remove currency symbols but preserve decimal point
        # Handle formats like: "$100.00", "€50,50", "£99.99"
        
        # Extract the first number sequence (avoid concatenating multiple prices)
        # Look for patterns like: $100.00, 100.00, €50,50, etc.
        # Try to find a price-like number (not too long, has decimal point or reasonable format)
        price_match = re.search(r'(\d{1,6}(?:[.,]\d{2})?)', price_text)
        if not price_match:
            # Fallback: try any number sequence
            price_match = re.search(r'(\d+(?:[.,]\d+)?)', price_text)
        
        if not price_match:
            return 0.0
        
        price_clean = price_match.group(1)
        
        # Sanity check: if the extracted number is too large (> 1 million), it's likely wrong
        # (unless it's VND which we already handled)
        if len(price_clean.replace('.', '').replace(',', '')) > 10:
            return 0.0
        
        # Handle comma as decimal separator (European format)
        if ',' in price_clean and '.' in price_clean:
            # If both comma and dot exist, check which is decimal
            # Usually the last one is decimal separator
            if price_clean.rindex(',') > price_clean.rindex('.'):
                # Comma is decimal separator (e.g., "1.000,50")
                price_clean = price_clean.replace('.', '').replace(',', '.')
            else:
                # Dot is decimal separator (e.g., "1,000.50")
                price_clean = price_clean.replace(',', '')
        elif ',' in price_clean:
            # Only comma - could be decimal or thousand separator
            # If followed by exactly 2 digits, it's likely decimal (e.g., "50,50")
            if re.search(r',\d{2}$', price_clean):
                price_clean = price_clean.replace(',', '.')
            else:
                # Comma as thousand separator, remove it
                price_clean = price_clean.replace(',', '')
        else:
            # Only dot - could be decimal or thousand separator
            # If more than 3 digits after dot, it's likely thousand separator
            if '.' in price_clean:
                parts = price_clean.split('.')
                if len(parts) == 2 and len(parts[1]) > 3:
                    # Likely thousand separator, remove dot
                    price_clean = price_clean.replace('.', '')
        
        try:
            return float(price_clean)
        except:
            return 0.0
    
    def validate_image_dimensions(self, image_bytes: bytes, img_url: str) -> bool:
        """Validate image meets minimum resolution requirements"""
        if not Image:
            # PIL not available, skip validation
            return True
        
        try:
            img = Image.open(BytesIO(image_bytes))
            width, height = img.size
            
            # Check minimum resolution
            if width < MIN_IMAGE_RESOLUTION or height < MIN_IMAGE_RESOLUTION:
                print(f"  ✗ Skipping low-res image: {width}x{height} (min: {MIN_IMAGE_RESOLUTION}px) - {img_url[:60]}...")
                return False
            
            # Check aspect ratio - reject images that are too narrow/wide (likely banners or buttons)
            aspect_ratio = width / height if height > 0 else 0
            if aspect_ratio > 5 or aspect_ratio < 0.2:
                print(f"  ✗ Skipping odd aspect ratio: {width}x{height} ({aspect_ratio:.2f}) - {img_url[:60]}...")
                return False
            
            print(f"  ✓ Valid image: {width}x{height}px")
            return True
            
        except Exception as e:
            print(f"  ⚠ Could not validate image dimensions: {e}")
            # If validation fails, allow the image (fail-safe)
            return True
    
    def process_images(self, product: Dict) -> List[str]:
        """Process product images - upload to Cloudinary or download locally"""
        if not product['images']:
            return []
        
        image_urls = []
        skipped_count = 0
        
        if self.use_cloudinary and self.cloudinary_uploader:
            # Upload to Cloudinary
            for idx, img_url in enumerate(product['images'][:15], 1):  # Limit to 15 images (increased)
                try:
                    # Download image bytes
                    parsed_url = urlparse(img_url)
                    domain = f"{parsed_url.scheme}://{parsed_url.netloc}"
                    headers = {
                        'Referer': domain,
                        'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
                    }
                    response = self.session.get(img_url, timeout=30, headers=headers)
                    response.raise_for_status()
                    
                    # Validate image dimensions
                    if not self.validate_image_dimensions(response.content, img_url):
                        skipped_count += 1
                        continue
                    
                    # Upload to Cloudinary
                    cloudinary_url = self.cloudinary_uploader.upload_image(
                        response.content,
                        product['name'],
                        idx
                    )
                    image_urls.append(cloudinary_url)
                    self.images_saved += 1
                    print(f"  Uploaded image {idx}/{min(len(product['images']), 15)} to Cloudinary")
                    
                except Exception as e:
                    print(f"  Error uploading image {idx}: {str(e)}")
        else:
            # Fallback: Download locally
            brand_folder = product['brand'].lower().replace(' ', '-')
            product_folder = product['name'].lower().replace(' ', '-')[:50]
            product_folder = re.sub(r'[^\w\-]', '', product_folder)
            
            image_dir = IMAGES_DIR / brand_folder / product_folder
            image_dir.mkdir(parents=True, exist_ok=True)
            
            for idx, img_url in enumerate(product['images'][:15], 1):
                try:
                    parsed_url = urlparse(img_url)
                    domain = f"{parsed_url.scheme}://{parsed_url.netloc}"
                    headers = {
                        'Referer': domain,
                        'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
                    }
                    response = self.session.get(img_url, timeout=30, headers=headers)
                    response.raise_for_status()
                    
                    # Validate image dimensions
                    if not self.validate_image_dimensions(response.content, img_url):
                        skipped_count += 1
                        continue
                    
                    ext = '.jpg'
                    if '.png' in img_url.lower():
                        ext = '.png'
                    elif '.webp' in img_url.lower():
                        ext = '.webp'
                    
                    filename = f"image_{idx}{ext}"
                    filepath = image_dir / filename
                    
                    with open(filepath, 'wb') as f:
                        f.write(response.content)
                    
                    image_urls.append(str(filepath))
                    self.images_saved += 1
                    print(f"  Downloaded image {idx}/{min(len(product['images']), 15)}")
                    
                except Exception as e:
                    print(f"  Error downloading image {idx}: {str(e)}")
        
        if skipped_count > 0:
            print(f"  ⚠ Skipped {skipped_count} low-quality image(s)")
        
        return image_urls
    
    def insert_product_to_database(self, product: Dict, image_urls: List[str], stock_quantities: Dict[str, int]) -> bool:
        """Insert product data into database using pyodbc"""
        try:
            with DatabaseManager() as db:
                # 1. Get or create manufacturer
                manufacturer_id = db.get_or_create_manufacturer(product['brand'])
                print(f"  ✓ Manufacturer ID: {manufacturer_id}")
                
                # 2. Get or create product
                product_id = db.get_or_create_product(product['name'], manufacturer_id)
                print(f"  ✓ Product ID: {product_id}")
                
                # 3. Process colors and sizes
                colors = product.get('colors', []) or ['Unknown']
                sizes = product.get('sizes', []) or ['39', '40', '41', '42', '43']
                price_vnd = product.get('price_vnd', 0)
                
                variant_count = 0
                image_link_count = 0
                
                # Process each color-size combination
                for color_name in colors:
                    # Map color to Vietnamese name and hex code
                    viet_color, hex_code = self.color_mapper.map_color(color_name)
                    color_id = db.get_or_create_color(viet_color, hex_code)
                    
                    for size_name in sizes:
                        size_id = db.get_or_create_size(size_name)
                        
                        # Get stock quantity for this variant
                        variant_key = f"{color_name}_{size_name}"
                        stock = stock_quantities.get(variant_key, DEFAULT_STOCK_QTY)
                
                # Create product detail
                        full_name = f"{product['name']} - {viet_color} - Size {size_name}"
                        detail_id = db.get_or_create_product_detail(
                            product_id, color_id, size_id, price_vnd, stock, full_name
                        )
                        variant_count += 1
                        
                        # Link images to this product detail
                        for img_url in image_urls:
                            img_name = os.path.basename(urlparse(img_url).path) or f"image_{product['name']}"
                            image_id = db.get_or_create_image(img_url, img_name, viet_color)
                            db.link_image_to_product_detail(detail_id, image_id)
                            image_link_count += 1
                
                print(f"  ✓ Created {variant_count} variant(s) with {len(image_urls)} image(s) each")
                print(f"  ✓ Total image links: {image_link_count}")
                print(f"\n  ⚠ Note: Products with brand names (Nike, Adidas, Jordan, etc.) will show in banHangMain")
                print(f"  ⚠ If not appearing, refresh the frontend or check browser cache")
                
                return True
                
        except Exception as e:
            print(f"Database error: {str(e)}")
            import traceback
            traceback.print_exc()
            return False
        
    def prompt_stock_quantities(self, product: Dict) -> Dict[str, int]:
        """Prompt user for stock quantities for each color-size variant"""
        colors = product.get('colors', []) or ['Unknown']
        sizes = product.get('sizes', []) or ['39', '40', '41', '42', '43']
        stock_quantities = {}
        
        print("\n" + "=" * 60)
        print("Stock Quantity Input")
        print("=" * 60)
        print(f"Product: {product['name']}")
        print(f"Colors: {len(colors)} - {', '.join(colors[:3])}{'...' if len(colors) > 3 else ''}")
        print(f"Sizes: {len(sizes)} - {', '.join(sizes[:5])}{'...' if len(sizes) > 5 else ''}")
        print(f"Total variants: {len(colors) * len(sizes)}")
        print()
        
        # Ask if user wants to set individual or bulk quantities
        print("Options:")
        print("1. Set same quantity for all variants (Quick)")
        print("2. Set quantity per color (all sizes of a color get same qty)")
        print("3. Skip (use default quantity: 10)")
        
        choice = input("\nSelect option [1-3] (default: 1): ").strip() or "1"
        
        if choice == "1":
            # Same quantity for all
            qty_input = input(f"Enter stock quantity for all variants (default: {DEFAULT_STOCK_QTY}): ").strip()
            qty = int(qty_input) if qty_input.isdigit() else DEFAULT_STOCK_QTY
            for color in colors:
                for size in sizes:
                    stock_quantities[f"{color}_{size}"] = qty
        
        elif choice == "2":
            # Per color
            for color in colors:
                qty_input = input(f"Stock quantity for color '{color}' (all sizes) [default: {DEFAULT_STOCK_QTY}]: ").strip()
                qty = int(qty_input) if qty_input.isdigit() else DEFAULT_STOCK_QTY
                for size in sizes:
                    stock_quantities[f"{color}_{size}"] = qty
        
            else:
                # Use default
                for color in colors:
                    for size in sizes:
                        stock_quantities[f"{color}_{size}"] = DEFAULT_STOCK_QTY
        
        print(f"\n✓ Stock quantities configured for {len(stock_quantities)} variants")
        return stock_quantities
    
    def process_url(self, url: str) -> bool:
        """Process a single product URL"""
        print(f"\nScraping product from: {url}")
        
        # Scrape product data
        product = self.scrape_product(url)
        if not product:
            print("Failed to scrape product data")
            return False
        
        # Abort if product name is unknown
        if not product.get('name') or product['name'] == 'Unknown Product':
            print("✗ Aborted: Could not extract product name. Product name is 'Unknown Product'.")
            print("  Please verify the URL is correct and the page is accessible.")
            return False
        
        print(f"Product: {product['name']}")
        
        # Convert price to VND
        original_price = product.get('price', 0)
        if original_price > 0:
            # Detect currency from scraped data
            currency = 'USD'  # Default for Nike/Adidas
            if original_price >= 100000:  # Already in VND
                currency = 'VND'
                price_vnd = original_price
                print(f"Price: {price_vnd:,.0f} VND")
            else:
                # Convert to VND
                price_vnd = self.currency_converter.convert_to_vnd(original_price, currency)
                print(f"Price: ${original_price:.2f} ({currency}) → {price_vnd:,.0f} VND")
            
            product['price_vnd'] = price_vnd
        else:
            product['price_vnd'] = 0
            print("Price: Not found")
        
        colors = product.get('colors', [])
        sizes = product.get('sizes', [])
        print(f"Colors: {len(colors)} {f'({colors[:3]})' if colors else '(none found, using defaults)'}")
        print(f"Sizes: {len(sizes)} {f'({sizes[:5]})' if sizes else '(none found, using defaults)'}")
        
        # Process images (upload to Cloudinary or download locally)
        if self.use_cloudinary:
            print("Uploading images to Cloudinary...")
        else:
            print("Downloading images locally...")
        
        image_urls = self.process_images(product)
        
        if not image_urls:
            print("Warning: No images processed")
        else:
            if self.use_cloudinary:
                print(f"✓ Uploaded {len(image_urls)} image(s) to Cloudinary")
            else:
                print(f"✓ Downloaded {len(image_urls)} image(s) locally")
                print(f"  Saved to: {IMAGES_DIR / product['brand'].lower().replace(' ', '-') / product['name'].lower().replace(' ', '-')[:50]}")
        
        # Prompt for stock quantities
        stock_quantities = self.prompt_stock_quantities(product)
        
        # Insert into database
        print("\nInserting into database...")
        print(f"Connecting to: {SQL_SERVER}:{SQL_PORT}")
        
        if self.insert_product_to_database(product, image_urls, stock_quantities):
            print("✓ Product processed successfully!")
            self.processed_count += 1
            return True
        else:
            print("✗ Failed to update database")
            return False


def main():
    """Main entry point"""
    print("=" * 60)
    print("Sneaker Scraper - Enhanced Database Integration")
    print("=" * 60)
    
    # Show connection info
    print(f"\nDatabase: {SQL_SERVER}:{SQL_PORT} | {SQL_DATABASE}")
    if SQL_SERVER == 'localhost':
        print("⚠ Warning: Using 'localhost'. If WARP is enabled, use '127.0.0.1' in .env file")
    
    # Check Cloudinary configuration
    use_cloudinary = bool(CLOUDINARY_CLOUD_NAME and CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET)
    if use_cloudinary:
        print(f"Image upload: Cloudinary (configured)")
    else:
        print(f"Image upload: Local storage (Cloudinary not configured)")
    
    print(f"Currency conversion: USD→VND rate = {USD_TO_VND_RATE:,.0f}")
    
    # Get URLs from user
    urls_input = input("\nEnter product URL(s), separated by commas: ").strip()
    if not urls_input:
        print("No URLs provided. Exiting.")
        return
    
    urls = [url.strip() for url in urls_input.split(',') if url.strip()]
    
    if not urls:
        print("No valid URLs found. Exiting.")
        return
    
    # Initialize scraper
    scraper = SneakerScraper(use_cloudinary=use_cloudinary)
    
    # Process each URL
    for url in urls:
        try:
            scraper.process_url(url)
            time.sleep(2)  # Rate limiting
        except KeyboardInterrupt:
            print("\n\nInterrupted by user")
            break
        except Exception as e:
            print(f"\nError processing {url}: {str(e)}")
            import traceback
            traceback.print_exc()
            continue
    
    # Summary
    print("\n" + "=" * 60)
    print("Summary:")
    print(f"Processed: {scraper.processed_count} product(s)")
    if use_cloudinary:
        print(f"Images uploaded: {scraper.images_saved}")
    else:
        print(f"Images downloaded: {scraper.images_saved}")
    print(f"Database updated: {scraper.processed_count} product(s)")
    print("=" * 60)


if __name__ == '__main__':
    main()

