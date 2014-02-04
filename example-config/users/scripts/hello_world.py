import time
from gda.commandqueue import JythonScriptProgressProvider


def updateProgress( percent, msg):
    JythonScriptProgressProvider.sendProgress( percent, msg)
    print "percentage %d %s" % (percent, msg)

for i in range(5):
    time.sleep(1)
    updateProgress(i*20, "Hello world!")

updateProgress(100, "Done")
