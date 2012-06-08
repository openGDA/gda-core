# coding=UTF-8
#@PydevCodeAnalysisIgnore

import java 
import gda
from gda.jython import InterfaceProvider
from java.lang import Math, String

from org.slf4j import LoggerFactory
logger = LoggerFactory.getLogger("gdascripts.messages.handle_messages")

import traceback as traceback_mod
		
def getCauseList(exception):
	if isinstance(exception, java.lang.InterruptedException):
		return str(exception)
	elif isinstance(exception, java.lang.Exception):
		return gda.util.exceptionUtils.getFullStackMsg(exception)
	else:
		return str(exception)

def padMsg(msg):
    if msg == None or msg == "":
        return ""
    return msg + " "

# A traceback object t has a frame associated with it, which can be accessed using t.tb_frame. It's possible to go
# further up the call stack (towards where the exception occurred) using t.tb_next. Given a frame f, it's possible to
# go further down the call stack (towards the caller) using f.f_back. Put together, it looks like this (T4 would be
# the traceback object returned from sys.exc_info()):
#
#                T1 -------------→ F1
#                 ↑    tb_frame    |             ↑
#         tb_next |                | f_back      | towards where exception occurred
#                 |                ↓             | newer frames
#                T2 -------------→ F2            |
#                 ↑    tb_frame    |
#         tb_next |                | f_back
#                 |                ↓
#                T3 -------------→ F3            |
#                 ↑    tb_frame    |             | towards caller
#         tb_next |                | f_back      | older frames
#                 |                ↓             ↓
#                T4 -------------→ F4
#                      tb_frame    |
#                                  | f_back
#                                  ↓
#                                  F5
#                                  |
#                                  | f_back
#                                  ↓
#                                  F6
#
# Given a frame f, traceback.format_stack outputs f and all frames below it, in *reverse order* (lowest frame first).
# In the above diagram, if format_stack is called with F4, then F4/5/6 will be displayed. To obtain more complete stack
# traces, we need to go up the chain of traceback objects (from T4 to T1), then across to the corresponding frame (F1),
# and then output that frame and everything below it - i.e. from the very top of the stack right down to the bottom.
#
# More generally:
#
#   * Given a traceback t, format_exception/extract_tb/format_tb return t's frame and all frames *above* it (lowest
#     frame first).
#
#   * Given a frame f, format_stack/extract_stack return f and all frames *below* it (lowest frame first).
#
#   * If a limit is specified, the *lowest* frames are omitted.
#
# For more information, see:
#
#   * sys.exc_info() documentation: http://docs.python.org/library/sys.html#sys.exc_info
#
#   * "Frame objects" and "Traceback objects" in
#     http://docs.python.org/reference/datamodel.html#the-standard-type-hierarchy
#
#   * traceback module documentation: http://docs.python.org/library/traceback.html
#
#   * source code for traceback module: http://svn.python.org/projects/python/trunk/Lib/traceback.py

def constructMessage(msg, exceptionType=None, exception=None, traceback=None ):
    if msg == None:
        msg = ""
    if exceptionType != None:
        msg = padMsg(msg) + str(exceptionType)
    if exception != None:
        msg = padMsg(msg) + getCauseList(exception)
    if traceback != None:
        # Jython 2.2.1 allowed dumpStack
        # msg = msg + ".\n Stack follows: " + traceback.dumpStack()
        traceback_stack = traceback_mod.format_exception(exceptionType, exception, traceback)
        msg = msg + ".\n Stack follows: "
        for t in traceback_stack:
            msg += t
        msg = msg.rstrip("\r\n")
    return msg

def log(controller, msg, exceptionType=None, exception=None, traceback=None, Raise=False, logger=logger):
    msg = str(msg)
    msg = constructMessage(msg, exceptionType, exception, None)
    if controller != None:
        controller.update(None, msg);
    if exception != None:
        msgFull = constructMessage(msg, exceptionType, exception, traceback)
        logger.error(msgFull)
    else:
        logger.info(msg)
    InterfaceProvider.getTerminalPrinter().print(msg)
    if Raise:
        if isinstance(msg, Exception):
            raise msg
        raise Exception(msg)


def getFormattedScannableValue(scannable, value):
	return String.format(scannable.outputFormat[0], [ value ])

def simpleLog(s):
	log(None, s)
