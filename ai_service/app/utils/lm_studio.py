from openai import OpenAI
from app.config import get_settings
import logging

logger = logging.getLogger(__name__)
settings = get_settings()

class LMStudioClient:
    def __init__(self):
        self.client = OpenAI(
            base_url=settings.lm_studio_base_url,
            api_key="lm-studio"
        )
        self.model = settings.lm_studio_model
        logger.info(f"LM Studio client initialized: {settings.lm_studio_base_url}")
    
    def chat(
        self, 
        messages: list, 
        temperature: float = 0.3, 
        max_tokens: int = 200,
        stream: bool = False
    ):
        """
        Send chat completion request to LM Studio
        
        Args:
            messages: List of message dicts
            temperature: Sampling temperature
            max_tokens: Maximum tokens to generate
            stream: Whether to stream the response
        """
        try:
            base_params = {
                "model": self.model,
                "messages": messages,
                "temperature": temperature,
                "max_tokens": max_tokens,
                "stream": stream
            }
            
            response = self.client.chat.completions.create(**base_params)
            return response
        except Exception as e:
            logger.error(f"LM Studio error: {e}")
            raise
    
    def test_connection(self):
        """Test LM Studio connection"""
        try:
            response = self.chat(
                messages=[{"role": "user", "content": "Hello"}],
                max_tokens=10
            )
            return True, response.choices[0].message.content
        except Exception as e:
            return False, str(e)

# Singleton instance
lm_client = LMStudioClient()
