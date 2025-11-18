from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import customer_chatbot, admin_chatbot
from app.config import get_settings
from app.utils.database import db_client
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
