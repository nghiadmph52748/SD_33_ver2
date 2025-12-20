"""
Vector Database Client using FAISS for semantic search
FAISS is lighter and more compatible than ChromaDB
"""
import faiss
import numpy as np
import pickle
import logging
from typing import List, Dict, Optional
import os

logger = logging.getLogger(__name__)


class VectorDBClient:
    """FAISS-based vector database client"""
    
    def __init__(self):
        self.index = None
        self.product_metadata = {}  # Maps index position to product data
        self.dimension = 384  # MiniLM embedding dimension
        self.initialized = False
        self.data_path = None
        
    def initialize(self):
        """Initialize FAISS index"""
        try:
            # Create data directory
            self.data_path = os.path.join(os.path.dirname(__file__), "..", "..", "data", "faiss")
            os.makedirs(self.data_path, exist_ok=True)
            
            # Try to load existing index
            index_file = os.path.join(self.data_path, "products.index")
            metadata_file = os.path.join(self.data_path, "metadata.pkl")
            
            if os.path.exists(index_file) and os.path.exists(metadata_file):
                self.index = faiss.read_index(index_file)
                with open(metadata_file, 'rb') as f:
                    self.product_metadata = pickle.load(f)
                logger.info(f"Loaded existing FAISS index with {self.index.ntotal} products")
            else:
                # Create new index - using IndexFlatL2 for exact search
                self.index = faiss.IndexFlatL2(self.dimension)
                logger.info("Created new FAISS index")
            
            self.initialized = True
            
        except Exception as e:
            logger.error(f"Failed to initialize FAISS: {e}", exc_info=True)
            self.initialized = False
    
    def add_product(self, product_id: int, text: str, embedding: List[float], metadata: Dict = None):
        """Add a single product embedding"""
        if not self.initialized:
            raise Exception("VectorDB not initialized")
        
        try:
            # Convert to numpy array and add to index
            embedding_array = np.array([embedding], dtype='float32')
            position = self.index.ntotal
            self.index.add(embedding_array)
            
            # Store metadata
            self.product_metadata[position] = {
                "product_id": product_id,
                "text": text,
                "metadata": metadata or {}
            }
            
        except Exception as e:
            logger.error(f"Failed to add product {product_id}: {e}")
            raise
    
    def add_products_batch(self, products: List[Dict]):
        """Add multiple products in batch"""
        if not self.initialized:
            raise Exception("VectorDB not initialized")
        
        if not products:
            return
        
        try:
            # Prepare batch data
            embeddings = np.array([p["embedding"] for p in products], dtype='float32')
            start_position = self.index.ntotal
            
            # Add to index
            self.index.add(embeddings)
            
            # Store metadata
            for i, product in enumerate(products):
                position = start_position + i
                self.product_metadata[position] = {
                    "product_id": product["product_id"],
                    "text": product["text"],
                    "metadata": product.get("metadata", {})
                }
            
            logger.info(f"Added {len(products)} products to FAISS index")
            
            # Save index and metadata
            self._save()
            
        except Exception as e:
            logger.error(f"Failed to add products batch: {e}", exc_info=True)
            raise
    
    def search_products(self, query_embedding: List[float], limit: int = 10) -> List[Dict]:
        """Search for similar products using vector similarity"""
        if not self.initialized or self.index.ntotal == 0:
            return []
        
        try:
            # Convert query to numpy array
            query_array = np.array([query_embedding], dtype='float32')
            
            # Search
            k = min(limit, self.index.ntotal)
            distances, indices = self.index.search(query_array, k)
            
            # Format results
            products = []
            for i, (distance, idx) in enumerate(zip(distances[0], indices[0])):
                if idx < 0:  # Invalid index
                    continue
                    
                metadata = self.product_metadata.get(idx, {})
                
                # Convert L2 distance to similarity score (0-1 range)
                # Lower distance = higher similarity
                similarity_score = 1.0 / (1.0 + distance)
                
                products.append({
                    "product_id": metadata.get("product_id"),
                    "similarity_score": float(similarity_score),
                    "metadata": metadata.get("metadata", {})
                })
            
            return products
            
        except Exception as e:
            logger.error(f"Failed to search products: {e}")
            return []
    
    def clear_products(self):
        """Clear all products from index"""
        if not self.initialized:
            return
        
        try:
            # Create new empty index
            self.index = faiss.IndexFlatL2(self.dimension)
            self.product_metadata = {}
            self._save()
            logger.info("Cleared all products from FAISS index")
        except Exception as e:
            logger.error(f"Failed to clear products: {e}")
    
    def _save(self):
        """Save index and metadata to disk"""
        try:
            index_file = os.path.join(self.data_path, "products.index")
            metadata_file = os.path.join(self.data_path, "metadata.pkl")
            
            faiss.write_index(self.index, index_file)
            with open(metadata_file, 'wb') as f:
                pickle.dump(self.product_metadata, f)
                
        except Exception as e:
            logger.error(f"Failed to save index: {e}")
    
    def get_stats(self) -> Dict:
        """Get index statistics"""
        if not self.initialized:
            return {"initialized": False}
        
        return {
            "initialized": True,
            "products_count": self.index.ntotal if self.index else 0,
            "faqs_count": 0,  # Not implemented yet
            "index_type": "FAISS IndexFlatL2"
        }
    
    def test_connection(self) -> bool:
        """Test if FAISS is working"""
        try:
            if not self.initialized:
                self.initialize()
            return self.initialized
        except Exception as e:
            logger.error(f"FAISS connection test failed: {e}")
            return False


# Singleton instance
vector_db = VectorDBClient()
