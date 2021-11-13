import sys
import metal.core as core

def callfn(name, *args):
    res = core.exec_c_func(name, *args)
    if res["stdout"]:
        print(res["stdout"])
    if res["stderr"]:
        print(res["stderr"], file=sys.stderr)
    return res["data"]

