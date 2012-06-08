#@PydevCodeAnalysisIgnore
#!/bin/env python2.4
#
# require a fixed version of serial_sim to be imported
from pkg_resources import require
require("dls.serial_sim==1.5")
from dls.serial_sim import serial_device
# create a class that represents the device
# This device has 3 integer values, a, b, and c
# They can be set by sending "a=100"
# They can be read by sending "a"
# Unrecognised commands reply "ERROR"
# from src import serial_device
import struct
import sys;

def createDataFrameForDouble(itemName, value):
    prefix=struct.pack("ccccccc","(","D","A","T","A",")",":")
    name=struct.pack(str(len(itemName))+"s",itemName) #padded with a single zero
    nameOffset=struct.pack("<l",0)
    nameLength=struct.pack("<l",len(name))
    val = name
    dataOffset=struct.pack("<l",len(val))
    data=struct.pack("<d",value)
    val += data
    dataLength=struct.pack("<l",len(data))
    dataDim1=struct.pack("<l",len(data))
    dataDim2=struct.pack("<l",1)
    dataDstOffset1=struct.pack("<l",len(data))
    dataDstOffset2=struct.pack("<l",1)
    unitsOffset=struct.pack("<l",len(val))
    units=struct.pack("ccccc","u","n","i","t","s")
    val += units
    unitsLength=struct.pack("<l",len(units))
    frameLength=struct.pack("<l",0)
    flags=struct.pack("<l",0)
    return prefix+nameOffset+nameLength+dataOffset+dataLength+dataDim1+dataDim2+dataDstOffset1+dataDstOffset2+unitsOffset+unitsLength+frameLength+flags + val

class my_device(serial_device):
    # set the terminator to control when a string is passed to reply
    OutTerminator=None
    MyOutTerminator="\n\r"
    InTerminator="\n\r"
    debug = True
    # create an internal dict of values
    vals = { "a":5, "b":6, "c":7 }
    loginName="gda"
    binary = False
    connected = False
    shutter="CLOSED"
    # implement a reply function            
    def reply(self, command):
        print "recv: -" + command + "-"
        if "=" in command:
            # set a value in the internal dictionary
            split = command.split("=")
            # if val isn't in the dict, return error
            if not self.vals.has_key(split[0]):
                return "ERROR"
            try:
                # set the dictionary to the right value
                self.vals[split[0]] = int(split[1])
                # returning None means nothing will be sent back
                return None
            except:
                type, exception, traceback = sys.exc_info()
                return str(type) +":"+str(exception)
        else:
            try:
                # report the value as a string
                if command == "binary":
                    self.binary=True
                elif command == "disconnect":
                    self.connected = False
                elif command == "temp":
                    return createDataFrameForDouble("temp",1.0)
                elif command == "water temp":
                    return createDataFrameForDouble("water temp",2.0)
                elif command =="sh o":
                    self.shutter="OPEN"
                elif command =="sh c":
                    self.shutter="CLOSED"
                elif command =="shutter":
                    return "detector:"+self.shutter+self.MyOutTerminator
            except:
                type, exception, traceback = sys.exc_info()
                return str(type) +":"+str(exception)


    def onHandlerSetup(self, handler):
        "Overwrite to do something when a connection is made. e.g. write a WELCOME banner "
        handler.request.send("Welcome client "+ self.loginName +self.MyOutTerminator)
        handler.request.send("Connection restored.")
        self.connected = True


if __name__ == "__main__":
    # little test function that runs only when you run this file
    dev = my_device()
    dev.start_ip(9120)
#    dev.start_debug(9006)
    # do a raw_input() to stop the program exiting immediately
    raw_input()
