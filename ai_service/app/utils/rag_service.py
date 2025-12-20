"""
RAG (Retrieval-Augmented Generation) Service
Combines vector search with database queries for intelligent product recommendations
"""
import logging
from typing import List, Dict, Tuple, Optional
from app.utils.vector_db import vector_db
from app.utils.embeddings import embedding_service
from app.utils.database import db_client

logger = logging.getLogger(__name__)


class RAGService:
    """RAG orchestration service for semantic search"""
    
    def __init__(self):
        self.initialized = False
        self.sync_in_progress = False
    
    async def initialize(self):
        """Initialize RAG service components"""
        try:
            logger.info("Initializing RAG service...")
            
            # Initialize embedding service
            if not embedding_service.initialized:
                embedding_service.initialize()
            
            # Initialize vector database
            if not vector_db.initialized:
                vector_db.initialize()
            
            # Test components
            if embedding_service.test_embedding() and vector_db.test_connection():
                self.initialized = True
                logger.info("RAG service initialized successfully")
            else:
                logger.error("RAG service initialization failed")
                self.initialized = False
                
        except Exception as e:
            logger.error(f"Failed to initialize RAG service: {e}", exc_info=True)
            self.initialized = False
    
    def is_available(self) -> bool:
        """Check if RAG service is ready"""
        return self.initialized and embedding_service.initialized and vector_db.initialized
    
    async def sync_products_from_db(self, force_resync: bool = False):
        """
        Sync products from SQL Server database to vector database
        
        Args:
            force_resync: If True, clear existing data and reindex all products
        """
        if not self.is_available():
            logger.warning("RAG service not available, skipping product sync")
            return
        
        if self.sync_in_progress:
            logger.warning("Product sync already in progress")
            return
        
        try:
            self.sync_in_progress = True
            logger.info("Starting product sync to vector database...")
            
            # Check if we need to sync
            current_count = vector_db.index.ntotal if vector_db.index else 0
            
            if current_count > 0 and not force_resync:
                logger.info(f"Vector DB already has {current_count} products. Skipping sync (use force_resync=True to reindex)")
                return
            
            if force_resync and current_count > 0:
                logger.info(f"Force resync: Clearing {current_count} existing products")
                vector_db.clear_products()
            
            # Fetch all products from SQL Server
            products = db_client.search_products("", limit=1000)  # Get all products
            
            if not products:
                logger.warning("No products found in database")
                return
            
            logger.info(f"Found {len(products)} products to index")
            
            # Prepare embeddings
            product_texts = []
            product_data = []
            
            for product in products:
                # Create enriched text representation for embedding
                product_name = product.get("product_name", "")
                brand_name = product.get("brand_name", "")
                min_price = product.get("min_price", 0)
                max_price = product.get("max_price", 0)
                total_stock = product.get("total_stock", 0)
                variant_count = product.get("variant_count", 0)
                
                # Build rich text for semantic search
                text_parts = [product_name]
                
                if brand_name:
                    text_parts.append(brand_name)
                
                # Add price context for better semantic matching
                if min_price and max_price:
                    if min_price == max_price:
                        price_desc = f"{int(min_price/1000000)}tr"
                    else:
                        price_desc = f"{int(min_price/1000000)}tr-{int(max_price/1000000)}tr"
                    text_parts.append(price_desc)
                
                # Add availability info
                if total_stock:
                    if total_stock > 50:
                        text_parts.append("còn hàng nhiều")
                    elif total_stock > 10:
                        text_parts.append("còn hàng")
                    else:
                        text_parts.append("sắp hết")
                
                # Add variant richness
                if variant_count > 5:
                    text_parts.append("nhiều màu nhiều size")
                
                # Combine all parts
                text = " ".join(text_parts).strip()
                
                if not text:
                    continue
                
                product_texts.append(text)
                product_data.append(product)
            
            if not product_texts:
                logger.warning("No valid product texts to embed")
                return
            
            # Generate embeddings in batch
            logger.info(f"Generating embeddings for {len(product_texts)} products...")
            embeddings = embedding_service.embed_batch(product_texts, batch_size=32)
            
            # Prepare batch insert
            products_to_insert = []
            for i, (product, text, embedding) in enumerate(zip(product_data, product_texts, embeddings)):
                product_id = product.get("product_id")
                
                if not product_id:
                    continue
                
                products_to_insert.append({
                    "product_id": product_id,
                    "text": text,
                    "embedding": embedding,
                    "metadata": {
                        "product_name": product.get("product_name", ""),
                        "brand_name": product.get("brand_name", ""),
                        "min_price": float(product.get("min_price", 0) or 0),
                        "max_price": float(product.get("max_price", 0) or 0),
                        "total_stock": int(product.get("total_stock", 0) or 0),
                    }
                })
            
            # Insert in batches
            batch_size = 100
            for i in range(0, len(products_to_insert), batch_size):
                batch = products_to_insert[i:i + batch_size]
                vector_db.add_products_batch(batch)
                logger.info(f"Indexed {min(i + batch_size, len(products_to_insert))}/{len(products_to_insert)} products")
            
            logger.info(f"✅ Product sync complete: {len(products_to_insert)} products indexed")
            
        except Exception as e:
            logger.error(f"Failed to sync products: {e}", exc_info=True)
        finally:
            self.sync_in_progress = False
    
    async def search_products_semantic(
        self,
        query: str,
        limit: int = 15,
        min_similarity: float = 0.3
    ) -> Tuple[List[Dict], Dict]:
        """
        Search products using semantic similarity
        
        Args:
            query: User query text
            limit: Maximum number of results
            min_similarity: Minimum similarity score threshold
        
        Returns:
            (products_list, metadata)
        """
        if not self.is_available():
            logger.warning("RAG service not available")
            return [], {"strategy": "rag_unavailable"}
        
        try:
            # Generate query embedding
            query_embedding = embedding_service.embed(query)
            
            # Search vector database
            vector_results = vector_db.search_products(query_embedding, limit=limit * 2)
            
            if not vector_results:
                return [], {"strategy": "rag_no_results"}
            
            # Filter by similarity threshold
            filtered_results = [r for r in vector_results if r["similarity_score"] >= min_similarity]
            
            if not filtered_results:
                logger.info(f"No results above similarity threshold {min_similarity}")
                return [], {"strategy": "rag_below_threshold"}
            
            # Fetch full product data from SQL Server
            product_ids = [r["product_id"] for r in filtered_results[:limit]]
            products = []
            
            for product_id in product_ids:
                # Search by product_id to get full data
                product_results = db_client.search_products("", limit=1000)
                matching_product = next((p for p in product_results if p.get("product_id") == product_id), None)
                
                if matching_product:
                    # Add similarity score
                    similarity_score = next((r["similarity_score"] for r in filtered_results if r["product_id"] == product_id), 0)
                    matching_product["similarity_score"] = similarity_score
                    products.append(matching_product)
            
            # Sort by similarity score
            products.sort(key=lambda x: x.get("similarity_score", 0), reverse=True)
            
            metadata = {
                "strategy": "rag_semantic",
                "total_candidates": len(vector_results),
                "filtered_count": len(filtered_results),
                "final_count": len(products),
                "min_similarity": min_similarity
            }
            
            logger.info(f"RAG search: query='{query[:50]}', results={len(products)}, avg_similarity={sum(p.get('similarity_score', 0) for p in products) / len(products) if products else 0:.3f}")
            
            return products, metadata
            
        except Exception as e:
            logger.error(f"RAG search failed: {e}", exc_info=True)
            return [], {"strategy": "rag_error", "error": str(e)}
    
    async def search_faqs(self, query: str, limit: int = 5) -> str:
        """
        Search FAQ/policy documents for customer support
        
        Args:
            query: User query
            limit: Maximum number of results
        
        Returns:
            Formatted FAQ context string
        """
        if not self.is_available():
            return ""
        
        try:
            query_embedding = embedding_service.embed(query)
            faqs = vector_db.search_faqs(query_embedding, limit=limit)
            
            if not faqs:
                return ""
            
            # Format FAQ context
            context = "\n\n**Thông tin liên quan:**\n\n"
            for i, faq in enumerate(faqs, 1):
                if faq["similarity_score"] >= 0.5:  # Only include relevant FAQs
                    context += f"{i}. {faq['content']}\n"
            
            return context
            
        except Exception as e:
            logger.error(f"FAQ search failed: {e}")
            return ""
    
    def get_stats(self) -> Dict:
        """Get RAG service statistics"""
        return {
            "initialized": self.initialized,
            "available": self.is_available(),
            "sync_in_progress": self.sync_in_progress,
            "embedding_model": embedding_service.model_name if embedding_service.initialized else None,
            "embedding_dimension": embedding_service.get_dimension() if embedding_service.initialized else 0,
            "vector_db_stats": vector_db.get_stats()
        }


# Singleton instance
rag_service = RAGService()
