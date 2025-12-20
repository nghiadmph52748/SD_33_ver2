from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import customer_chatbot, admin_chatbot
from app.config import get_settings
from app.utils.database import db_client
from app.utils.rag_service import rag_service
import logging
import platform

# Logging configuration
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)
settings = get_settings()

# Create FastAPI app
app = FastAPI(
    title="GearUp AI Service",
    description="AI microservice for GearUp e-commerce - Customer Support & Admin Analytics",
    version="2.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers - separated by user type
# Customer chatbot for banHangMain (customer frontend)
app.include_router(customer_chatbot.router, prefix="/api/ai/chatbot")
# Admin chatbot for view (admin frontend)
app.include_router(admin_chatbot.router, prefix="/api/ai/chatbot")

# Startup event for RAG initialization
@app.on_event("startup")
async def startup_event():
    """Initialize RAG service and sync product embeddings"""
    logger.info("=" * 60)
    logger.info("Starting RAG service initialization...")
    logger.info("=" * 60)
    
    try:
        # Initialize RAG components
        await rag_service.initialize()
        
        # Sync products from database to vector store
        if rag_service.is_available():
            await rag_service.sync_products_from_db()
            stats = rag_service.get_stats()
            logger.info("RAG Service Ready:")
            logger.info(f"  - Products indexed: {stats['vector_db_stats']['products_count']}")
            logger.info(f"  - Embedding model: {stats['embedding_model']}")
            logger.info(f"  - Embedding dimension: {stats['embedding_dimension']}")
        else:
            logger.warning("RAG service initialization failed - using fallback keyword search")
    except Exception as e:
        logger.error(f"Error during RAG startup: {e}", exc_info=True)
        logger.warning("Continuing without RAG - will use fallback keyword search")
    
    logger.info("=" * 60)

@app.get("/")
async def root():
    return {
        "service": "GearUp AI Service",
        "version": "2.0.0",
        "status": "running",
        "endpoints": {
            "customer_chat": "/api/ai/chatbot/customer/chat",
            "customer_chat_stream": "/api/ai/chatbot/customer/chat-stream",
            "admin_chat": "/api/ai/chatbot/admin/chat",
            "admin_chat_stream": "/api/ai/chatbot/admin/chat-stream",
            "health": "/health",
            "docs": "/docs"
        }
    }

@app.get("/health")
async def health():
    """Health check endpoint"""
    db_status, db_message = db_client.test_connection()
    
    return {
        "status": "healthy" if db_status else "degraded",
        "database": {
            "connected": db_status,
            "message": db_message,
            "driver": db_client.driver,
            "host": settings.db_host,
            "port": settings.db_port,
            "database": settings.db_name
        },
        "rag": rag_service.get_stats(),
        "system": {
            "platform": platform.system(),
            "platform_version": platform.version()
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.api_host,
        port=settings.api_port,
        reload=True
    )
