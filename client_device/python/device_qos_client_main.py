from fastapi import FastAPI

import uvicorn
import logging

from utils.global_constants import BASE_PATH
from cpu_total_load_1_4.cpu_total_load_1_4 import start_measuring, get_batch, stop_measuring

logger = logging.getLogger("uvicorn")

# SERVER
    
async def on_startup():
    logger.info("Application is initializing...")
    start_measuring()
    logger.info("Application has been initialized.")

async def on_shutdown():
    logger.info("Application is cleaning up...")
    stop_measuring()
    logger.info("Application has been cleaned up.")

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None, on_startup=[on_startup], on_shutdown=[on_shutdown])

@app.get(f"{BASE_PATH}/cpu")
async def get_time():
    # query paramerter: utolsó x
    return get_batch()

# Free memory 2.1

def main():
    logger.info("Arrowhead DeviceQoSClient")
    config = uvicorn.Config(app, host="0.0.0.0", port=9473)
    server = uvicorn.Server(config)
    server.run()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Server stopped by user.")