"""
Embedding Service for generating vector embeddings
Uses multilingual sentence transformer model supporting Vietnamese
"""
from sentence_transformers import SentenceTransformer
import logging
from typing import List, Union
import hashlib
import json

logger = logging.getLogger(__name__)


class EmbeddingService:
    """Service for generating embeddings using SentenceTransformer"""
    
    def __init__(self):
        self.model = None
        self.model_name = 'paraphrase-multilingual-MiniLM-L12-v2'
        self.initialized = False
        self.cache = {}  # Simple in-memory cache
        
    def initialize(self):
        """Load the embedding model"""
        try:
            logger.info(f"Loading embedding model: {self.model_name}")
            self.model = SentenceTransformer(self.model_name)
            self.initialized = True
            logger.info(f"Embedding model loaded successfully (dimension: {self.model.get_sentence_embedding_dimension()})")
        except Exception as e:
            logger.error(f"Failed to load embedding model: {e}", exc_info=True)
            self.initialized = False
    
    def _get_cache_key(self, text: str) -> str:
        """Generate cache key for text"""
        return hashlib.md5(text.encode('utf-8')).hexdigest()
    
    def embed(self, text: Union[str, List[str]], use_cache: bool = True) -> Union[List[float], List[List[float]]]:
        """
        Generate embeddings for text
        
        Args:
            text: Single string or list of strings
            use_cache: Whether to use caching for single strings
        
        Returns:
            Single embedding vector or list of embedding vectors
        """
        if not self.initialized:
            raise Exception("Embedding service not initialized")
        
        # Handle single string
        if isinstance(text, str):
            if use_cache:
                cache_key = self._get_cache_key(text)
                if cache_key in self.cache:
                    return self.cache[cache_key]
            
            embedding = self.model.encode(text, convert_to_numpy=True).tolist()
            
            if use_cache:
                # Limit cache size
                if len(self.cache) > 1000:
                    # Remove oldest entries
                    self.cache = dict(list(self.cache.items())[-500:])
                self.cache[cache_key] = embedding
            
            return embedding
        
        # Handle list of strings (batch)
        else:
            embeddings = self.model.encode(text, convert_to_numpy=True, show_progress_bar=len(text) > 10)
            return embeddings.tolist()
    
    def embed_batch(self, texts: List[str], batch_size: int = 32) -> List[List[float]]:
        """
        Generate embeddings for multiple texts in batches
        
        Args:
            texts: List of text strings
            batch_size: Batch size for processing
        
        Returns:
            List of embedding vectors
        """
        if not self.initialized:
            raise Exception("Embedding service not initialized")
        
        try:
            embeddings = self.model.encode(
                texts,
                batch_size=batch_size,
                convert_to_numpy=True,
                show_progress_bar=len(texts) > 50
            )
            return embeddings.tolist()
        except Exception as e:
            logger.error(f"Failed to generate batch embeddings: {e}")
            raise
    
    def get_dimension(self) -> int:
        """Get embedding dimension"""
        if not self.initialized:
            return 0
        return self.model.get_sentence_embedding_dimension()
    
    def test_embedding(self) -> bool:
        """Test if embedding generation works"""
        try:
            if not self.initialized:
                self.initialize()
            
            # Test embedding generation
            test_text = "giày chạy bộ Nike"
            embedding = self.embed(test_text)
            
            if embedding and len(embedding) == self.get_dimension():
                logger.info(f"Embedding test successful (dimension: {len(embedding)})")
                return True
            else:
                logger.error("Embedding test failed: Invalid dimension")
                return False
        except Exception as e:
            logger.error(f"Embedding test failed: {e}")
            return False


# Singleton instance
embedding_service = EmbeddingService()
