"""
Unified LLM client factory that supports both LM Studio and Google Gemini.
Switches between providers based on configuration.
"""
from app.config import get_settings
from app.utils.lm_studio import LMStudioClient
from app.utils.gemini_client import get_gemini_client, GeminiClient
from typing import Union
import logging

logger = logging.getLogger(__name__)
settings = get_settings()

class UnifiedLLMClient:
    """
    Unified client that wraps either LM Studio or Gemini client
    based on configuration.
    """
    def __init__(self):
        self.provider = settings.llm_provider.lower()
        self.client: Union[LMStudioClient, GeminiClient] = None
        
        if self.provider == "gemini":
            try:
                self.client = get_gemini_client()
                logger.info("Using Google Gemini as LLM provider")
            except Exception as e:
                logger.error(f"Failed to initialize Gemini client: {e}")
                logger.warning("Falling back to LM Studio")
                self.client = LMStudioClient()
                self.provider = "lm_studio"
        else:
            self.client = LMStudioClient()
            logger.info("Using LM Studio as LLM provider")
    
    def chat(self, messages: list, temperature: float = 0.3, max_tokens: int = 200, stream: bool = False):
        """Chat completion - compatible with both providers"""
        if self.provider == "gemini":
            return self.client.chat(messages, temperature, max_tokens, stream)
        else:
            return self.client.chat(messages, temperature, max_tokens, stream)
    
    def test_connection(self):
        """Test connection to LLM provider"""
        return self.client.test_connection()
    
    @property
    def model(self):
        """Get current model name"""
        if self.provider == "gemini":
            return self.client.model_name
        else:
            return self.client.model


# Singleton instance
llm_client = UnifiedLLMClient()

