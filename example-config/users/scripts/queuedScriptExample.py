""" 
Example of a script that is to run on the Queue. 
Once a second it calls JythonScriptProgressProvider.sendProgress to send
and update on the percentage complete and provide a status message.
If the user presses Pause the script will pause during the call to 
JythonScriptProgressProvider.sendProgress
"""

import time
from gda.commandqueue import JythonScriptProgressProvider

def updateProgress( percent, msg):
    JythonScriptProgressProvider.sendProgress( percent, msg)
    print "percentage %d %s" % (percent, msg)

updateProgress(0, "Queued Script Example started")
for i in range(5):
    time.sleep(1)
    updateProgress((i+1)*20, "Point %d of %d" %(i+1,5))

updateProgress(100, "Queued Script Example complete")
