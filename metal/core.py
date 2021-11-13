#!/usr/bin/env python3.9
import functools
import json
import os
import sys
from ctypes import *

from metal import config


class SharedLibraryNotFoundError(Exception):
    pass


def init_dll(dll_path=config.dll_path):
    global dll, isolate, isolatethread
    dll = CDLL(dll_path)
    isolate = c_void_p()
    isolatethread = c_void_p()
    dll.graal_create_isolate(None, byref(isolate), byref(isolatethread))
    dll.return_wrapper.restype = c_char_p
    dll.initialize_json.restype = c_char_p
    dll.ping_test_BANG_.restype = c_char_p


def ping_test():
    data = json.dumps("ping").encode('utf-8')
    return json.loads(dll.ping_test_BANG_(isolatethread, c_char_p(data)))

def safe_execute(f, *, initialized=[False]):
    def _(*args, **kwargs):
        if initialized[0] is False:
            res = f(*args, **kwargs)
            initialized[0]=True
            return res
    return _

@safe_execute
def initialize(dll_path=None):
    if dll_path is None:
        abspath = os.path.abspath(config.dll_path)
        if os.path.exists(abspath):
            dll_path = abspath
        dll_path1 = os.path.join(os.getcwd(), *os.path.split(config.dll_path))
        dll_path1 = os.path.abspath(dll_path1)
        if os.path.exists(dll_path1):
            dll_path = dll_path1

        else:
            raise SharedLibraryNotFoundError(
                "Cannot find shared library! "
                "Please ensure libmetal.so is available.")
    init_dll(dll_path)
    data = json.dumps({"initialization": "data"}).encode(
        'utf8')
    res = json.loads(dll.initialize_json(isolatethread, c_char_p(data)))
    return True


def safe_initialize(initialized=[False]):
    if not initialized[0]:
        initialize()
        initialized[0] = True
        return True
    else:
        return False


def raw_exec_c_func(fname, *args):
    safe_initialize()
    data = json.dumps({"name": fname, "data": list(args)}).encode('utf-8')
    res = json.loads(
        dll.return_wrapper(isolatethread, c_char_p(data)).decode('utf-8'))
    return res


def exec_c_func(fname, *args):
    def _():
        return raw_exec_c_func(fname, *args)
    data = _()
    return {"data": data["res"],
            "stdout": data["stdout"],
            "stderr": data["stderr"]}

def c_func(fname):
    def _c_func(f):
        @functools.wraps(f)
        def _(*args):
            return exec_c_func(fname, *args)

        return _

    return _c_func

