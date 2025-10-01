import psutil
import time

from typing import List
from collections import deque
from utils.data_model.measurement import Measurement
from utils.utils import new_thread
from utils.global_constants import FREQUENCY, WINDOW_SIZE

# MEMBERS
QUEUE_A: deque[Measurement] = deque(maxlen=WINDOW_SIZE)
QUEUE_B: deque[Measurement] = deque(maxlen=WINDOW_SIZE)
current_batch_id = "a"
do_measuring = False
waiting = False

# METHODS

def start_measuring_ram():
    global do_measuring
    do_measuring = True
    new_thread(_start_new_batch(current_batch_id, QUEUE_A))

def stop_measuring_ram():
    global do_measuring
    do_measuring = False

    time.sleep(FREQUENCY)

def get_batch_ram():
    global current_batch_id

    reading_queue = None
    working_queue = None
    if current_batch_id == "a":
        reading_queue = QUEUE_A
        working_queue = QUEUE_B
        current_batch_id = "b"       
    else:
        reading_queue = QUEUE_B
        working_queue = QUEUE_A
        current_batch_id = "a"
    
    while waiting:
        time.sleep(0.5)

    new_thread(_start_new_batch(current_batch_id, working_queue))
    return _read_batch(reading_queue)
    
    
# ASSISTANT METHODS

async def _start_new_batch(id: str, queue: deque[Measurement]):
    global waiting

    doWork = True
    while doWork:
        # UTC epoch time in seconds and free RAM in percentage
        waiting = True
        queue.append(Measurement(_timestamp(), _get_free_ram()))
        time.sleep(FREQUENCY)
        waiting = False
        doWork = do_measuring and current_batch_id == id

def _get_free_ram() -> float:
    mem = psutil.virtual_memory()
    return round((mem.available / mem.total) * 100, 2)

def _timestamp() -> int:
    # UTC epoch time in seconds
    return int(time.time())

def _read_batch(queue: deque[Measurement]) -> List[Measurement]:
    data = list(queue)
    queue.clear()
    return [m.__dict__ for m in data]