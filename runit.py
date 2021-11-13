#!/usr/bin/env python3.9
import json
import os
from ctypes import *

import metal.config
metal.config.dll_path = "./metal/libmetal.so"
import metal.core as core

# static linking
venv_dir = os.getcwd()
dll = CDLL("{}/metal/libmetal.so".format(venv_dir))
isolate = c_void_p()
isolatethread = c_void_p()
dll.graal_create_isolate(None, byref(isolate), byref(isolatethread))
dll.return_wrapper.restype = c_char_p
dll.initialize_json.restype = c_char_p
dll.ping_test_BANG_.restype = c_char_p

def display_methods():
    data = json.dumps(
        {"name": "all-methods"})
    encoded = data.encode('utf-8')
    result = dll.return_wrapper(isolatethread, c_char_p(encoded))
    return json.loads(result.decode('utf-8'))

def ping_test():
    data = json.dumps("ping").encode('utf-8')
    return json.loads(dll.ping_test_BANG_(isolatethread, c_char_p(data)))

if __name__ == '__main__':
    print(ping_test())
