"""A function to enable/disable calling reset on a MalcomDevice at the end of a scan.
Calling reset ensures that malcolm closes the nexus files it is writing to at the same
time as GDA. If this is not done it can cause issues with archiving. However, calling
reset on the malcolm device clears any error state, making it more difficult to debug.
This function provides a way to do this. The flag set by this method is True
by default, this is recommended for normal operation. It should be set to False only
when debugging.
"""

from org.eclipse.scanning.malcolm.core import MalcolmDevice

def reset_malcolm_after_scan(resetMalcolm):
    MalcolmDevice.setResetAfterScan(resetMalcolm)