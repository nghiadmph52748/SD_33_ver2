#!/usr/bin/env python3
"""
Sneaker Scraper - Single script to scrape products and update SQL Server
Prompts for product URLs, scrapes data, downloads images, and updates database
"""

import os
import re
import json
import time
import subprocess
from urllib.parse import urlparse
from pathlib import Path
from typing import Dict, List, Optional

import requests
from bs4 import BeautifulSoup
from dotenv import load_dotenv

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

# Debug: Show what values are being used
if __name__ == '__main__':
    print(f"DEBUG - SQL_SERVER from env: {os.getenv('SQL_SERVER', 'NOT SET')}")
    print(f"DEBUG - SQL_SERVER final value: {SQL_SERVER}")

IMAGES_DIR = Path('images')
DEFAULT_ORIGIN_ID = 1  # Việt Nam
DEFAULT_MATERIAL_ID = 1  # Da tổng hợp
DEFAULT_SOLE_ID = 1  # Đế cao su
DEFAULT_WEIGHT_ID = 1  # 250g

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


class SneakerScraper:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update(HEADERS)
        self.sql_commands = []
        self.processed_count = 0
        self.images_saved = 0
        
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
        
        # Images - try multiple selectors
        product['images'] = []
        img_elems = (soup.find_all('img', {'data-testid': 'product-image'}) or
                    soup.find_all('img', {'alt': lambda x: x and product['name'].lower() in x.lower()}) or
                    soup.find_all('img', class_='product-image') or
                    soup.find_all('img', class_=lambda x: x and 'product' in x.lower()))
        
        # Also try to extract images from JSON data
        if product_data:
            try:
                if isinstance(product_data, dict):
                    product_info = (product_data.get('product') or 
                                  product_data.get('Product') or
                                  product_data.get('products', {}).get('byId', {}))
                    if isinstance(product_info, dict):
                        if 'byId' in product_info:
                            product_info = list(product_info['byId'].values())[0] if product_info['byId'] else {}
                        
                        images = (product_info.get('images') or 
                                product_info.get('imageUrls') or
                                product_info.get('media', {}).get('images', []))
                        if isinstance(images, list):
                            for img_url in images:
                                if isinstance(img_url, str) and 'http' in img_url:
                                    if img_url not in product['images']:
                                        product['images'].append(img_url)
                                elif isinstance(img_url, dict):
                                    img_src = img_url.get('url') or img_url.get('src') or img_url.get('imageUrl')
                                    if img_src and 'http' in str(img_src):
                                        if img_src not in product['images']:
                                            product['images'].append(str(img_src))
            except:
                pass
        
        for img in img_elems:
            src = img.get('src') or img.get('data-src') or img.get('data-lazy-src') or img.get('data-zoom-src')
            if src and 'http' in src and 'nike' in src.lower():
                # Avoid small icons and thumbnails
                if 'thumbnail' not in src.lower() and 'icon' not in src.lower():
                    if src not in product['images']:
                        product['images'].append(src)
        
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
                    continue
            
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
                        
                        # Extract images
                        images = (product_info.get('images') or 
                                product_info.get('imageUrls') or
                                product_info.get('media', {}).get('images', []) or
                                product_info.get('gallery', []))
                        
                        if isinstance(images, list):
                            for img_url in images:
                                if isinstance(img_url, str) and 'http' in img_url:
                                    if img_url not in product.get('images', []):
                                        product.setdefault('images', []).append(img_url)
                                elif isinstance(img_url, dict):
                                    img_src = (img_url.get('url') or 
                                             img_url.get('src') or 
                                             img_url.get('imageUrl') or
                                             img_url.get('href'))
                                    if img_src and 'http' in str(img_src):
                                        if img_src not in product.get('images', []):
                                            product.setdefault('images', []).append(str(img_src))
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
        
        # Images - try multiple selectors (fallback)
        if not product.get('images'):
            product['images'] = []
            img_elems = (soup.find_all('img', class_='product-image') or
                        soup.find_all('img', {'data-testid': 'product-image'}) or
                        soup.find_all('img', {'alt': lambda x: x and product['name'].lower() in x.lower()}) or
                        soup.find_all('img', class_=lambda x: x and 'product' in x.lower()) or
                        soup.find_all('img', {'src': lambda x: x and 'adidas' in x.lower() and ('product' in x.lower() or 'image' in x.lower())}))
            for img in img_elems:
                src = (img.get('src') or 
                      img.get('data-src') or 
                      img.get('data-lazy-src') or 
                      img.get('data-zoom-src') or
                      img.get('data-original'))
                if src and 'http' in src:
                    # Make sure it's a product image, not a logo or icon
                    if ('thumbnail' not in src.lower() and 
                        'icon' not in src.lower() and 
                        'logo' not in src.lower() and
                        ('product' in src.lower() or 'image' in src.lower() or 'media' in src.lower())):
                        if src not in product['images']:
                            product['images'].append(src)
        
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
    
    def download_images(self, product: Dict) -> List[str]:
        """Download product images and return local paths"""
        if not product['images']:
            return []
        
        brand_folder = product['brand'].lower().replace(' ', '-')
        product_folder = product['name'].lower().replace(' ', '-')[:50]  # Limit folder name length
        product_folder = re.sub(r'[^\w\-]', '', product_folder)
        
        image_dir = IMAGES_DIR / brand_folder / product_folder
        image_dir.mkdir(parents=True, exist_ok=True)
        
        local_paths = []
        for idx, img_url in enumerate(product['images'][:10], 1):  # Limit to 10 images
            try:
                # Add referer header for image requests
                parsed_url = urlparse(img_url)
                domain = f"{parsed_url.scheme}://{parsed_url.netloc}"
                headers = {
                    'Referer': domain,
                    'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
                }
                response = self.session.get(img_url, timeout=30, headers=headers)
                response.raise_for_status()
                
                # Determine file extension
                ext = '.jpg'
                if '.png' in img_url.lower():
                    ext = '.png'
                elif '.webp' in img_url.lower():
                    ext = '.webp'
                
                filename = f"image_{idx}{ext}"
                filepath = image_dir / filename
                
                with open(filepath, 'wb') as f:
                    f.write(response.content)
                
                local_paths.append(str(filepath))
                self.images_saved += 1
                print(f"  Downloaded image {idx}/{min(len(product['images']), 10)}")
                
            except Exception as e:
                print(f"  Error downloading image {idx}: {str(e)}")
        
        return local_paths
    
    def generate_sql(self, product: Dict, image_paths: List[str]) -> str:
        """Generate SQL script for database insertion - returns single script string"""
        manufacturer_name = product['brand']
        product_name = product['name'].replace("'", "''")
        
        # Build SQL script as single batch (all variables must be in same batch)
        sql_parts = []
        
        # 1. Check/Create manufacturer
        sql_parts.append(f"""
-- Check/Create manufacturer
IF NOT EXISTS (SELECT 1 FROM nha_san_xuat WHERE ten_nha_san_xuat = N'{manufacturer_name}' AND deleted = 0)
BEGIN
    INSERT INTO nha_san_xuat (ten_nha_san_xuat, trang_thai, deleted, create_at, create_by)
    VALUES (N'{manufacturer_name}', 1, 0, CAST(GETDATE() AS DATE), 1)
END

DECLARE @id_nha_san_xuat INT = (SELECT id FROM nha_san_xuat WHERE ten_nha_san_xuat = N'{manufacturer_name}' AND deleted = 0)
DECLARE @id_xuat_xu INT = 1
""")
        
        # 2. Check/Create product
        sql_parts.append(f"""
-- Check/Create product (UPDATE if exists, INSERT if new)
DECLARE @id_san_pham INT
SELECT @id_san_pham = id FROM san_pham WHERE ten_san_pham = N'{product_name}' AND id_nha_san_xuat = @id_nha_san_xuat AND deleted = 0

IF @id_san_pham IS NULL
BEGIN
    INSERT INTO san_pham (id_nha_san_xuat, id_xuat_xu, ten_san_pham, trang_thai, deleted, create_at, create_by)
    VALUES (@id_nha_san_xuat, @id_xuat_xu, N'{product_name}', 1, 0, CAST(GETDATE() AS DATE), 1)
    SET @id_san_pham = SCOPE_IDENTITY()
END
ELSE
BEGIN
    UPDATE san_pham SET update_at = CAST(GETDATE() AS DATE), update_by = 1 WHERE id = @id_san_pham
END
""")
        
        # 3. Process colors and sizes
        colors = product.get('colors', []) or ['Unknown']
        sizes = product.get('sizes', []) or ['39', '40', '41', '42', '43']  # Default sizes if not found
        price = product.get('price', 0)
        
        # For each color
        for color_idx, color_name in enumerate(colors):
            color_name_clean = color_name.replace("'", "''")
            hex_code = self._extract_hex_code(color_name) or '#000000'
            
            # Check/Create color
            color_var = f"@id_mau_sac_{color_idx}"
            sql_parts.append(f"""
-- Color {color_idx + 1}: {color_name_clean}
IF NOT EXISTS (SELECT 1 FROM mau_sac WHERE ten_mau_sac = N'{color_name_clean}' AND deleted = 0)
BEGIN
    INSERT INTO mau_sac (ten_mau_sac, ma_mau, trang_thai, deleted, create_at, create_by)
    VALUES (N'{color_name_clean}', N'{hex_code}', 1, 0, CAST(GETDATE() AS DATE), 1)
END
DECLARE {color_var} INT = (SELECT id FROM mau_sac WHERE ten_mau_sac = N'{color_name_clean}' AND deleted = 0)
""")
            
            # For each size
            for size_idx, size_name in enumerate(sizes):
                size_name_clean = size_name.replace("'", "''")
                
                # Check/Create size
                size_var = f"@id_kich_thuoc_{color_idx}_{size_idx}"
                sql_parts.append(f"""
-- Size {size_idx + 1}: {size_name_clean}
IF NOT EXISTS (SELECT 1 FROM kich_thuoc WHERE ten_kich_thuoc = N'{size_name_clean}' AND deleted = 0)
BEGIN
    INSERT INTO kich_thuoc (ten_kich_thuoc, trang_thai, deleted, create_at, create_by)
    VALUES (N'{size_name_clean}', 1, 0, CAST(GETDATE() AS DATE), 1)
END
DECLARE {size_var} INT = (SELECT id FROM kich_thuoc WHERE ten_kich_thuoc = N'{size_name_clean}' AND deleted = 0)
""")
                
                # Create product detail
                ten_chi_tiet = f"{product_name} - {color_name} - Size {size_name}"
                ten_chi_tiet_clean = ten_chi_tiet.replace("'", "''")
                
                detail_var = f"@id_chi_tiet_san_pham_{color_idx}_{size_idx}"
                sql_parts.append(f"""
-- Product detail: {color_name_clean} / Size {size_name_clean}
DECLARE {detail_var} INT
SELECT {detail_var} = id FROM chi_tiet_san_pham 
WHERE id_san_pham = @id_san_pham 
  AND id_mau_sac = {color_var}
  AND id_kich_thuoc = {size_var}
  AND deleted = 0

IF {detail_var} IS NULL
BEGIN
    INSERT INTO chi_tiet_san_pham (id_san_pham, id_mau_sac, id_kich_thuoc, id_de_giay, id_chat_lieu, id_trong_luong, 
                                   so_luong, gia_ban, trang_thai, deleted, ten_san_pham_chi_tiet, create_at, create_by)
    VALUES (@id_san_pham, {color_var}, {size_var}, 
            {DEFAULT_SOLE_ID}, {DEFAULT_MATERIAL_ID}, {DEFAULT_WEIGHT_ID}, 
            0, {price}, 1, 0, N'{ten_chi_tiet_clean}', CAST(GETDATE() AS DATE), 1)
    SET {detail_var} = SCOPE_IDENTITY()
END
ELSE
BEGIN
    UPDATE chi_tiet_san_pham 
    SET gia_ban = {price}, ten_san_pham_chi_tiet = N'{ten_chi_tiet_clean}', update_at = CAST(GETDATE() AS DATE), update_by = 1
    WHERE id = {detail_var}
END
""")
                
                # Link images to product detail
                for img_idx, img_path in enumerate(image_paths):
                    img_name = os.path.basename(img_path)
                    img_path_clean = img_path.replace("'", "''").replace("\\", "\\\\")
                    color_from_name = color_name_clean
                    
                    img_var = f"@id_anh_{color_idx}_{size_idx}_{img_idx}"
                    sql_parts.append(f"""
-- Image {img_idx + 1} for {color_name_clean} / Size {size_name_clean}
DECLARE {img_var} INT
SELECT {img_var} = id FROM anh_san_pham WHERE duong_dan_anh = N'{img_path_clean}' AND deleted = 0

IF {img_var} IS NULL
BEGIN
    INSERT INTO anh_san_pham (duong_dan_anh, ten_anh, mau_anh, trang_thai, deleted, create_at, create_by)
    VALUES (N'{img_path_clean}', N'{img_name}', N'{color_from_name}', 1, 0, CAST(GETDATE() AS DATE), 1)
    SET {img_var} = SCOPE_IDENTITY()
END

IF NOT EXISTS (SELECT 1 FROM chi_tiet_san_pham_anh 
               WHERE id_chi_tiet_san_pham = {detail_var}
                 AND id_anh_san_pham = {img_var} AND deleted = 0)
BEGIN
    INSERT INTO chi_tiet_san_pham_anh (id_chi_tiet_san_pham, id_anh_san_pham, trang_thai, deleted, create_at, create_by)
    VALUES ({detail_var}, {img_var}, 1, 0, CAST(GETDATE() AS DATE), 1)
END
""")
        
        return "".join(sql_parts)
    
    def _extract_hex_code(self, color_name: str) -> str:
        """Extract hex color code from color name"""
        hex_match = re.search(r'#[0-9A-Fa-f]{6}', color_name)
        if hex_match:
            return hex_match.group(0)
        return ''
    
    def execute_sql(self, sql_script: str) -> bool:
        """Execute SQL script using sqlcmd"""
        if not sql_script or not sql_script.strip():
            return False
        
        # Add required SET options at the beginning
        set_options = """
SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
SET ANSI_PADDING ON
"""
        
        # Combine SET options with SQL script (all in one batch)
        full_sql_script = set_options + sql_script
        
        # Create temporary SQL file in current script directory
        script_dir = Path(__file__).parent
        sql_file = script_dir / 'temp_scraper.sql'
        with open(sql_file, 'w', encoding='utf-8') as f:
            f.write(full_sql_script)
        
        try:
            # Build sqlcmd command
            # Add -C flag to trust server certificate (useful when WARP is enabled)
            cmd = [
                'sqlcmd',
                '-S', f'{SQL_SERVER},{SQL_PORT}',
                '-d', SQL_DATABASE,
                '-U', SQL_USERNAME,
                '-P', SQL_PASSWORD,
                '-i', str(sql_file),
                '-C',  # Trust server certificate (bypasses SSL verification for local/dev servers)
                '-b'   # Stop on error
            ]
            
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
            
            if result.returncode == 0:
                return True
            else:
                # Show both stderr and stdout for better debugging
                error_msg = result.stderr.strip() if result.stderr else result.stdout.strip()
                if error_msg:
                    print(f"SQL Error: {error_msg}")
                else:
                    print(f"SQL Error: Command failed with return code {result.returncode}")
                    print(f"stdout: {result.stdout[:500] if result.stdout else 'None'}")
                return False
                
        except FileNotFoundError:
            print("Error: sqlcmd not found. Please install SQL Server Command Line Utilities.")
            return False
        except Exception as e:
            print(f"Error executing SQL: {str(e)}")
            return False
        finally:
            # Clean up temp file
            if sql_file.exists():
                sql_file.unlink()
    
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
        price = product.get('price', 0)
        # Check if price is likely VND (large numbers > 1000 typically indicate VND)
        # VND prices are typically between 100,000 and 10,000,000 VND
        if price >= 100000 and price <= 100000000:
            print(f"Price: {price:,.0f} VND")
        elif price > 0:
            print(f"Price: ${price:.2f}")
        else:
            print("Price: $0.00")
        colors = product.get('colors', [])
        sizes = product.get('sizes', [])
        print(f"Colors: {len(colors)} {f'({colors[:3]})' if colors else '(none found, using defaults)'}")
        print(f"Sizes: {len(sizes)} {f'({sizes[:5]})' if sizes else '(none found, using defaults)'}")
        
        # Download images
        print("Downloading images...")
        image_paths = self.download_images(product)
        print(f"Saving images to: {IMAGES_DIR / product['brand'].lower().replace(' ', '-') / product['name'].lower().replace(' ', '-')[:50]}")
        
        # Generate SQL
        print("Generating SQL statements...")
        sql_script = self.generate_sql(product, image_paths)
        
        # Execute SQL
        print("Updating database...")
        print(f"Connecting to: {SQL_SERVER}:{SQL_PORT}")
        if self.execute_sql(sql_script):
            print("✓ Product processed successfully!")
            self.processed_count += 1
            return True
        else:
            print("✗ Failed to update database")
            print(f"Tip: Make sure your .env file has SQL_SERVER=127.0.0.1 (not localhost)")
            return False


def main():
    """Main entry point"""
    print("=" * 60)
    print("Sneaker Scraper - SQL Server Integration")
    print("=" * 60)
    
    # Show connection info
    print(f"\nDatabase: {SQL_SERVER}:{SQL_PORT} | {SQL_DATABASE}")
    if SQL_SERVER == 'localhost':
        print("⚠ Warning: Using 'localhost'. If WARP is enabled, use '127.0.0.1' in .env file")
    
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
    scraper = SneakerScraper()
    
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
            continue
    
    # Summary
    print("\n" + "=" * 60)
    print("Summary:")
    print(f"Processed: {scraper.processed_count} product(s)")
    print(f"Images saved: {scraper.images_saved}")
    print(f"Database updated: {scraper.processed_count} product(s)")
    print("=" * 60)


if __name__ == '__main__':
    main()

