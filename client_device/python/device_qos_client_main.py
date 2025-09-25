import time

from cpu_total_load_1_4.cpu_total_load_1_4 import start_measuring, get_batch, stop_measuring

start_measuring()
print("start sleep")
time.sleep(5)
print("finish sleep")
print(get_batch())
print("start sleep")
time.sleep(5)
print("finish sleep")
print(get_batch())
print("start sleep")
time.sleep(5)
print("finish sleep")
print(get_batch())
stop_measuring()