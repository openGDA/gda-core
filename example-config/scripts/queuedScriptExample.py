"""
Demonstration of how to inform the CommandQueue of progress.

The important command is:
gda.commandqueue.JythonScriptProgressProvider.sendProgress( percent, msg)
"""
import time
from gda.commandqueue import JythonScriptProgressProvider


def updateProgress( percent, msg):
    JythonScriptProgressProvider.sendProgress( percent, msg)
    print "percentage %d %s" % (percent, msg)

updateProgress(0, "Queued Script Example Started")
for i in range(5):
    time.sleep(1)
    updateProgress(i*20, "Point %d of %d" % (i+1,5))

updateProgress(100, "Queued Script Example Completed")