import asyncio
import threading

def run_async(coro):
    # Start a new thread that runs the async function in its own loop
    def run():
        asyncio.run(coro)
    threading.Thread(target=run).start()