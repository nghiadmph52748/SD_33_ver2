from pydantic_settings import BaseSettings
from functools import lru_cache

class Settings(BaseSettings):
    # LM Studio
    lm_studio_base_url: str = "http://localhost:3147/v1"
    lm_studio_model: str = "gpt-oss-20bf"
    
    # Database
    db_host: str = "localhost"
    db_port: int = 1433
    db_name: str = "GearUp"
    db_user: str = ""
    db_password: str = ""
    
    # Redis
    redis_host: str = "localhost"
    redis_port: int = 6379
    
    # API
    api_port: int = 8001
    api_host: str = "0.0.0.0"
    
    class Config:
        env_file = ".env"

@lru_cache()
def get_settings():
    return Settings()
