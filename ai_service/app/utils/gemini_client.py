import google.generativeai as genai
from app.config import get_settings
import logging
from typing import List, Dict, Optional, Iterator

logger = logging.getLogger(__name__)
settings = get_settings()

class GeminiClient:
    def __init__(self):
        api_key = settings.gemini_api_key
        if not api_key:
            raise ValueError("GEMINI_API_KEY is required in environment variables")
        
        genai.configure(api_key=api_key)
        self.model_name = settings.gemini_model
        self.api_key = api_key  # Store for health check
        try:
            self.model = genai.GenerativeModel(self.model_name)
            logger.info(f"Gemini client initialized with model: {self.model_name}")
        except Exception as e:
            logger.error(f"Failed to initialize Gemini model: {e}")
            raise
    
    def _convert_messages(self, messages: List[Dict]) -> tuple[str, List[Dict]]:
        """
        Convert OpenAI format messages to Gemini format.
        Returns (system_instruction, chat_history)
        """
        system_instruction = ""
        chat_history = []
        
        for msg in messages:
            role = msg.get("role", "")
            content = msg.get("content", "")
            
            if role == "system":
                system_instruction = content
            elif role == "user":
                chat_history.append({"role": "user", "parts": [content]})
            elif role == "assistant":
                chat_history.append({"role": "model", "parts": [content]})
        
        return system_instruction, chat_history
    
    def chat(
        self,
        messages: List[Dict],
        temperature: float = 0.3,
        max_tokens: int = 200,
        stream: bool = False
    ):
        """
        Send chat completion request to Gemini API.
        Compatible with OpenAI-style interface.
        
        Args:
            messages: List of message dicts with 'role' and 'content'
            temperature: Sampling temperature (0.0-2.0)
            max_tokens: Maximum tokens to generate (Gemini uses max_output_tokens)
            stream: Whether to stream the response
        
        Returns:
            Response object compatible with OpenAI format
        """
        try:
            system_instruction, chat_history = self._convert_messages(messages)
            
            # Configure generation parameters
            generation_config = {
                "temperature": temperature,
                "max_output_tokens": max_tokens,
            }
            
            # Create model with system instruction if provided
            if system_instruction:
                model = genai.GenerativeModel(
                    self.model_name,
                    system_instruction=system_instruction
                )
            else:
                model = self.model
            
            if stream:
                # For streaming, we need to handle the fact that Gemini SDK may raise StopIteration
                # during generate_content() call. We'll use a generator wrapper approach.
                def create_stream_generator():
                    """Create a generator that safely handles Gemini streaming"""
                    gemini_iterator = None
                    try:
                        if len(chat_history) > 1:
                            # Start chat session with history (excluding last message)
                            chat = model.start_chat(history=chat_history[:-1])
                            last_message = chat_history[-1]["parts"][0] if chat_history[-1]["parts"] else ""
                            if not last_message:
                                raise ValueError("Empty message content for streaming")
                            
                            # Get the iterator - wrap in try-except to catch StopIteration during initialization
                            try:
                                gemini_iterator = chat.send_message(
                                    last_message,
                                    generation_config=generation_config,
                                    stream=True
                                )
                            except StopIteration:
                                logger.warning("Gemini stream ended immediately during send_message initialization")
                                return
                        else:
                            # Single message, no history
                            user_content = chat_history[0]["parts"][0] if chat_history and chat_history[0]["parts"] else ""
                            if not user_content:
                                raise ValueError("Empty message content for streaming")
                            
                            # Get the iterator - wrap in try-except to catch StopIteration during initialization
                            try:
                                gemini_iterator = model.generate_content(
                                    user_content,
                                    generation_config=generation_config,
                                    stream=True
                                )
                            except StopIteration:
                                logger.warning("Gemini stream ended immediately during generate_content initialization")
                                return
                        
                        # Now iterate through the stream
                        if gemini_iterator:
                            try:
                                for chunk in gemini_iterator:
                                    yield chunk
                            except StopIteration:
                                # Normal end of stream
                                logger.debug("Gemini stream ended normally")
                                return
                    except StopIteration as e:
                        # Catch any StopIteration that wasn't caught above
                        logger.debug(f"Gemini stream StopIteration: {e}")
                        return
                    except Exception as e:
                        logger.error(f"Gemini streaming error: {e}", exc_info=True)
                        raise
                
                return create_stream_generator()
            else:
                # Start chat session if there's history
                if len(chat_history) > 1:
                    chat = model.start_chat(history=chat_history[:-1])
                    response = chat.send_message(
                        chat_history[-1]["parts"][0],
                        generation_config=generation_config
                    )
                else:
                    # Single message, no history
                    user_content = chat_history[0]["parts"][0] if chat_history else ""
                    response = model.generate_content(
                        user_content,
                        generation_config=generation_config
                    )
                
                # Convert to OpenAI-compatible format
                return GeminiResponse(response)
                
        except Exception as e:
            logger.error(f"Gemini API error: {e}", exc_info=True)
            raise
    
    def test_connection(self):
        """Test Gemini API connection"""
        try:
            response = self.chat(
                messages=[{"role": "user", "content": "Hello"}],
                max_tokens=10
            )
            return True, response.choices[0].message.content
        except Exception as e:
            return False, str(e)


class GeminiResponse:
    """Wrapper to make Gemini response compatible with OpenAI format"""
    def __init__(self, gemini_response):
        self.gemini_response = gemini_response
        self.choices = [GeminiChoice(gemini_response)]


class GeminiChoice:
    """Wrapper to make Gemini choice compatible with OpenAI format"""
    def __init__(self, gemini_response):
        self.gemini_response = gemini_response
        self.message = GeminiMessage(gemini_response)


class GeminiMessage:
    """Wrapper to make Gemini message compatible with OpenAI format"""
    def __init__(self, gemini_response):
        self.gemini_response = gemini_response
        # Handle cases where response doesn't have text (e.g., safety filter, finish_reason = 2)
        try:
            if hasattr(gemini_response, 'text') and gemini_response.text:
                self.content = gemini_response.text
            elif hasattr(gemini_response, 'candidates') and gemini_response.candidates:
                # Check finish_reason
                candidate = gemini_response.candidates[0]
                if hasattr(candidate, 'finish_reason'):
                    if candidate.finish_reason == 2:  # SAFETY
                        self.content = "Xin lỗi, nội dung này không thể được xử lý do chính sách an toàn."
                    elif candidate.finish_reason == 3:  # RECITATION
                        self.content = "Xin lỗi, tôi không thể trả lời câu hỏi này."
                    else:
                        # Try to extract text from parts
                        if hasattr(candidate, 'content') and hasattr(candidate.content, 'parts'):
                            text_parts = [part.text for part in candidate.content.parts if hasattr(part, 'text') and part.text]
                            self.content = ' '.join(text_parts) if text_parts else ""
                        else:
                            self.content = ""
                else:
                    # No finish_reason, try to get text
                    if hasattr(candidate, 'content') and hasattr(candidate.content, 'parts'):
                        text_parts = [part.text for part in candidate.content.parts if hasattr(part, 'text') and part.text]
                        self.content = ' '.join(text_parts) if text_parts else ""
                    else:
                        self.content = ""
            else:
                self.content = ""
        except (ValueError, AttributeError) as e:
            # If accessing .text raises ValueError (no valid Part), return error message
            logger.warning(f"Gemini response has no text content (finish_reason may be SAFETY): {e}")
            self.content = "Xin lỗi, tôi không thể tạo phản hồi cho câu hỏi này. Vui lòng thử lại với câu hỏi khác."


class GeminiStreamChunk:
    """Wrapper for streaming chunks to be compatible with OpenAI format"""
    def __init__(self, chunk):
        self.chunk = chunk
        self.choices = [GeminiStreamDelta(chunk)]


class GeminiStreamDelta:
    """Wrapper for stream delta"""
    def __init__(self, chunk):
        self.chunk = chunk
        self.delta = GeminiDelta(chunk)


class GeminiDelta:
    """Wrapper for delta content"""
    def __init__(self, chunk):
        self.chunk = chunk
        self.content = chunk.text if chunk.text else ""


# Singleton instance (will be initialized by factory)
gemini_client: Optional[GeminiClient] = None

def get_gemini_client() -> GeminiClient:
    """Get or create Gemini client singleton"""
    global gemini_client
    if gemini_client is None:
        gemini_client = GeminiClient()
    return gemini_client

