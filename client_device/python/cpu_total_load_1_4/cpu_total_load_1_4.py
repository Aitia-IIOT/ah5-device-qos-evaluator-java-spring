import psutil
import time
import os

from utils.utils import run_async

# MEMBERS

DATA_FOLDER = "cpu_total_load_1_4/data"
FREQUENCY = 1 # sec
do_measuring = False
writing_batch_file = ""

# METHODS

def start_measuring():
    global do_measuring
    do_measuring = True
    _crete_data_folder()
    run_async(_start_new_batch())

def stop_measuring():
    global do_measuring
    do_measuring = False

def get_batch():
    batch_to_read = writing_batch_file
    run_async(_start_new_batch())
    time.sleep(1)
    return _read_batch_file(batch_to_read)
    
    
# ASSISTANT METHODS

async def _start_new_batch():
    global writing_batch_file

    file_path = _get_new_batch_file()
    writing_batch_file = file_path
    do_work = True    
    with open(file_path, "a") as file:  # open file in append mode
        while do_work:
            # UTC epoch time in seconds and total cpu load in percentage
            line = f"{_timestamp()},{_get_cpu_load()}\n"
            file.write(line)
            file.flush()
            do_work = do_measuring and writing_batch_file == file_path
            print(writing_batch_file == file_path)

def _crete_data_folder():
    if not os.path.exists(DATA_FOLDER):
        os.makedirs(DATA_FOLDER)

def _get_new_batch_file():
    return f"{DATA_FOLDER}/data_{_timestamp()}"

def _get_cpu_load():
    return psutil.cpu_percent(interval = FREQUENCY)

def _timestamp():
    # UTC epoch time in seconds
    return int(time.time())

def _read_batch_file(file_path):
    print(f"reading: {file_path}")
    data = []
    with open(file_path, "r") as file:
        for line in file:
            timestamp, value = line.strip().split(",", 1)
            data.append({"t": timestamp, "v": float(value)})
    return data