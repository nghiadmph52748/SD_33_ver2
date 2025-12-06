"""
Smart Product Recommendation System
Uses multiple strategies for intelligent product recommendations
"""
import re
import logging
from typing import List, Dict, Optional, Tuple
from collections import Counter

logger = logging.getLogger(__name__)


class SmartRecommendationEngine:
    """Intelligent product recommendation engine"""
    
    def __init__(self, db_client):
        self.db_client = db_client
        
        # Brand keywords mapping
        self.brand_keywords = {
            "nike": ["nike", "nike air", "air max", "air force", "jordan"],
            "adidas": ["adidas", "ultraboost", "yeezy", "stan smith", "superstar"],
            "puma": ["puma", "suede", "rs-x"],
            "vans": ["vans", "old skool", "authentic", "sk8-hi"],
            "converse": ["converse", "chuck taylor", "all star"],
            "new balance": ["new balance", "newbalance", "nb", "574", "990"],
            "reebok": ["reebok", "classic", "club c"],
            "asics": ["asics", "gel", "kayano", "gt-2000"]
        }
        
        # Product type keywords
        self.product_type_keywords = {
            "running": ["chạy bộ", "running", "chạy", "marathon", "jogging", "sprint"],
            "basketball": ["bóng rổ", "basketball", "nba", "court", "hoop"],
            "football": ["bóng đá", "football", "soccer", "futsal", "sân cỏ"],
            "tennis": ["tennis", "quần vợt", "court"],
            "training": ["tập luyện", "training", "gym", "thể hình", "workout", "fitness"],
            "lifestyle": ["lifestyle", "phong cách", "thời trang", "casual", "đi chơi", "đi phố"],
            "walking": ["đi bộ", "walking", "dạo phố"],
            "hiking": ["leo núi", "hiking", "trekking", "đi bộ đường dài"]
        }
        
        # Price range keywords
        self.price_keywords = {
            "budget": ["rẻ", "giá rẻ", "dưới", "khoảng", "tầm", "budget", "affordable"],
            "premium": ["cao cấp", "premium", "đắt", "xịn", "sang", "luxury"]
        }
    
    def extract_price_range(self, message: str) -> Optional[Tuple[float, float]]:
        """Extract price range from message"""
        message_lower = message.lower()
        
        # Pattern: "dưới X triệu", "khoảng X triệu", "từ X đến Y"
        patterns = [
            (r'dưới\s+(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)', lambda m: (0, float(m.group(1)) * 1000000)),
            (r'khoảng\s+(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)', lambda m: (0, float(m.group(1)) * 1000000)),
            (r'tầm\s+(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)', lambda m: (0, float(m.group(1)) * 1000000)),
            (r'từ\s+(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)\s+đến\s+(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)', 
             lambda m: (float(m.group(1)) * 1000000, float(m.group(2)) * 1000000)),
            (r'(\d+(?:\.\d+)?)\s*-\s*(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)',
             lambda m: (float(m.group(1)) * 1000000, float(m.group(2)) * 1000000)),
            (r'(\d+(?:\.\d+)?)\s*(?:triệu|tr|vnđ|đồng|k)\s+trở\s+xuống',
             lambda m: (0, float(m.group(1)) * 1000000)),
        ]
        
        for pattern, extractor in patterns:
            match = re.search(pattern, message_lower)
            if match:
                try:
                    return extractor(match)
                except (ValueError, IndexError):
                    continue
        
        # Check for budget/premium keywords
        if any(kw in message_lower for kw in self.price_keywords["budget"]):
            return (0, 2000000)  # Budget: under 2M
        elif any(kw in message_lower for kw in self.price_keywords["premium"]):
            return (3000000, float('inf'))  # Premium: above 3M
        
        return None
    
    def extract_smart_keywords(self, message: str, chat_history: List[Dict] = None) -> Dict:
        """Extract smart keywords with improved NLP"""
        message_lower = message.lower()
        
        # Extract brands
        found_brands = []
        for brand, patterns in self.brand_keywords.items():
            for pattern in patterns:
                if pattern in message_lower:
                    found_brands.append(brand)
                    break
        
        # Extract product types
        found_types = []
        for ptype, patterns in self.product_type_keywords.items():
            for pattern in patterns:
                if pattern in message_lower:
                    found_types.append(ptype)
                    break
        
        # Extract price range
        price_range = self.extract_price_range(message)
        
        # Extract mentioned products from chat history
        mentioned_products = []
        if chat_history:
            for msg in chat_history[-5:]:  # Last 5 messages
                content = msg.get("content", "").lower()
                # Look for product names (capitalized words, brand + model patterns)
                words = content.split()
                for i, word in enumerate(words):
                    if i < len(words) - 1:
                        potential_product = f"{word} {words[i+1]}"
                        if any(brand in potential_product.lower() for brand in self.brand_keywords.keys()):
                            mentioned_products.append(potential_product)
        
        # Extract color preferences
        color_keywords = ["đen", "trắng", "xanh", "đỏ", "vàng", "hồng", "tím", "xám", "nâu",
                         "black", "white", "blue", "red", "yellow", "pink", "purple", "gray", "brown"]
        found_colors = [color for color in color_keywords if color in message_lower]
        
        # Extract size preferences
        size_patterns = [
            r'size\s+(\d+(?:\.\d+)?)',
            r'(\d+(?:\.\d+)?)\s*cm',
            r'kích\s+thước\s+(\d+(?:\.\d+)?)'
        ]
        found_sizes = []
        for pattern in size_patterns:
            matches = re.findall(pattern, message_lower)
            found_sizes.extend(matches)
        
        # Build search terms
        search_terms = []
        if found_brands:
            search_terms.extend(found_brands)
        if found_types:
            search_terms.extend(found_types)
        if found_colors:
            search_terms.extend(found_colors[:2])  # Limit to 2 colors
        
        # Add generic keywords if no specific ones found
        if not search_terms:
            generic_keywords = ["giày", "shoe", "sản phẩm", "product"]
            for keyword in generic_keywords:
                if keyword in message_lower:
                    search_terms.append(keyword)
                    break
        
        return {
            "brands": found_brands,
            "product_types": found_types,
            "search_terms": search_terms,
            "mentioned_products": mentioned_products,
            "price_range": price_range,
            "colors": found_colors,
            "sizes": found_sizes
        }
    
    def calculate_product_score(self, product: Dict, keywords: Dict, intent: str) -> float:
        """Calculate relevance score for a product"""
        score = 0.0
        product_name_lower = product.get("product_name", "").lower()
        min_price = product.get("min_price", 0) or 0
        max_price = product.get("max_price", 0) or 0
        total_stock = product.get("total_stock", 0) or 0
        
        # Brand match (high weight)
        if keywords.get("brands"):
            for brand in keywords["brands"]:
                if brand in product_name_lower:
                    score += 10.0
                    break
        
        # Product type match (high weight)
        if keywords.get("product_types"):
            for ptype in keywords["product_types"]:
                type_map = {
                    "running": ["chạy", "running", "run"],
                    "basketball": ["bóng rổ", "basketball"],
                    "football": ["bóng đá", "football", "soccer"],
                    "tennis": ["tennis"],
                    "training": ["training", "gym", "tập"],
                    "lifestyle": ["lifestyle", "casual"]
                }
                keywords_list = type_map.get(ptype, [ptype])
                for kw in keywords_list:
                    if kw in product_name_lower:
                        score += 8.0
                        break
        
        # Search term match (medium weight)
        if keywords.get("search_terms"):
            for term in keywords["search_terms"]:
                if term in product_name_lower:
                    score += 5.0
        
        # Price range match (medium weight)
        price_range = keywords.get("price_range")
        if price_range:
            min_range, max_range = price_range
            avg_price = (min_price + max_price) / 2 if max_price > min_price else min_price
            if min_range <= avg_price <= max_range:
                score += 6.0
            elif avg_price < min_range * 1.2 or avg_price > max_range * 0.8:
                score += 3.0  # Partial match
        
        # Stock availability (medium weight)
        if total_stock > 0:
            score += 4.0
            if total_stock > 10:
                score += 2.0  # Bonus for good stock
        
        # Intent-specific scoring
        if intent == "promotion_inquiry":
            # Prefer products with stock
            if total_stock > 0:
                score += 3.0
        
        return score
    
    def rank_products(self, products: List[Dict], keywords: Dict, intent: str) -> List[Dict]:
        """Rank products by relevance score"""
        if not products:
            return []
        
        # Calculate scores
        scored_products = []
        for product in products:
            score = self.calculate_product_score(product, keywords, intent)
            scored_products.append((score, product))
        
        # Sort by score (descending), then by stock, then by name
        scored_products.sort(key=lambda x: (
            -x[0],  # Score descending
            -(x[1].get("total_stock", 0) or 0),  # Stock descending
            x[1].get("product_name", "").lower()  # Name ascending
        ))
        
        return [p[1] for p in scored_products]
    
    def get_similar_products(self, reference_product: Dict, limit: int = 5) -> List[Dict]:
        """Get products similar to a reference product"""
        if not reference_product:
            return []
        
        product_name = reference_product.get("product_name", "")
        
        # Extract brand and key terms from product name
        words = product_name.lower().split()
        brand = None
        for b, patterns in self.brand_keywords.items():
            for pattern in patterns:
                if pattern in product_name.lower():
                    brand = b
                    break
            if brand:
                break
        
        # Search for similar products
        similar_products = []
        if brand:
            # Search by brand
            products = self.db_client.search_products(brand, limit=limit * 2)
            # Filter out the reference product
            similar_products = [p for p in products if p.get("product_id") != reference_product.get("product_id")]
        
        return similar_products[:limit]
    
    def get_trending_products(self, days: int = 7, limit: int = 10) -> List[Dict]:
        """Get trending products (recently popular)"""
        try:
            return self.db_client.get_top_selling_products(limit=limit, days=days)
        except Exception as e:
            logger.error(f"Error getting trending products: {e}")
            return []
    
    def recommend_products(
        self,
        message: str,
        intent: str,
        chat_history: List[Dict] = None,
        limit: int = 15
    ) -> Tuple[List[Dict], Dict]:
        """
        Main recommendation method
        Returns: (products_list, metadata)
        """
        # Extract smart keywords
        keywords = self.extract_smart_keywords(message, chat_history)
        
        logger.info(f"Smart keywords: {keywords}")
        
        products = []
        search_strategies = []
        
        # Strategy 1: Search by mentioned products from history
        if keywords.get("mentioned_products"):
            for product_name in keywords["mentioned_products"][:2]:
                found = self.db_client.search_products(product_name, limit=5)
                if found:
                    # Get similar products
                    for ref_product in found[:1]:
                        similar = self.get_similar_products(ref_product, limit=3)
                        products.extend(similar)
                    products.extend(found)
                    if products:
                        break
        
        # Strategy 2: Search by brand
        if not products and keywords.get("brands"):
            for brand in keywords["brands"]:
                found = self.db_client.search_products(brand, limit=limit)
                products.extend(found)
                if products:
                    break
        
        # Strategy 3: Search by product type
        if not products and keywords.get("product_types"):
            for ptype in keywords["product_types"]:
                # Map product type to multiple search terms
                type_search_terms = {
                    "running": ["ultraboost", "air max", "pegasus", "chạy", "running", "nike", "adidas"],
                    "basketball": ["basketball", "bóng rổ", "nike", "jordan"],
                    "football": ["football", "bóng đá", "soccer", "adidas"],
                    "tennis": ["tennis", "quần vợt", "court"],
                    "training": ["training", "tập luyện", "gym", "fitness"],
                    "lifestyle": ["lifestyle", "casual", "phong cách", "air max", "stan smith"]
                }
                search_terms_list = type_search_terms.get(ptype, [ptype])
                for search_term in search_terms_list:
                    found = self.db_client.search_products(search_term, limit=limit)
                    if found:
                        products.extend(found)
                        break
                if products:
                    break
        
        # Strategy 4: Search by generic keywords (try each keyword separately)
        if not products and keywords.get("search_terms"):
            for search_term in keywords["search_terms"][:3]:
                found = self.db_client.search_products(search_term, limit=limit)
                if found:
                    products.extend(found)
                    break
        
        # Strategy 5: For promotion inquiries, get products on sale first
        if intent == "promotion_inquiry" and not products:
            # Try to get products that are actually on sale
            if hasattr(self.db_client, 'get_products_on_sale'):
                products = self.db_client.get_products_on_sale(limit=limit)
                logger.info(f"Strategy 5 (products on sale): found {len(products)} products")
            # Fallback to trending products if no products on sale
            if not products:
                products = self.get_trending_products(days=30, limit=limit)
                logger.info(f"Strategy 5 fallback (trending for promotion): found {len(products)} products")
            # Final fallback to all products
            if not products:
                products = self.db_client.search_products("", limit=limit)
                logger.info(f"Strategy 5 final fallback (all products): found {len(products)} products")
        
        # Strategy 6: Fallback to trending products
        if not products:
            products = self.get_trending_products(days=30, limit=limit)
            logger.info(f"Strategy 6 (trending): found {len(products)} products")
        
        # Strategy 7: Final fallback - get any products
        if not products:
            products = self.db_client.search_products("", limit=limit)
            logger.info(f"Strategy 7 (final fallback - all products): found {len(products)} products")
        
        # Remove duplicates by product_id
        seen_ids = set()
        unique_products = []
        for p in products:
            product_id = p.get("product_id")
            if product_id and product_id not in seen_ids:
                seen_ids.add(product_id)
                unique_products.append(p)
        products = unique_products
        
        # Apply price filter if specified
        price_range = keywords.get("price_range")
        if price_range:
            min_range, max_range = price_range
            filtered_products = []
            for p in products:
                min_price = p.get("min_price", 0) or 0
                max_price = p.get("max_price", 0) or 0
                avg_price = (min_price + max_price) / 2 if max_price > min_price else min_price
                if min_range <= avg_price <= max_range:
                    filtered_products.append(p)
            if filtered_products:
                products = filtered_products
        
        # Rank products by relevance
        ranked_products = self.rank_products(products, keywords, intent)
        
        # Limit results
        final_products = ranked_products[:limit]
        
        metadata = {
            "keywords_extracted": keywords,
            "strategies_used": len([s for s in search_strategies if s]),
            "total_candidates": len(products),
            "final_count": len(final_products)
        }
        
        return final_products, metadata

