"""Functions to manage TopUp and Expression Watchdogs. Consolidated from duplicated 
versions on i08, i13-1 and i14. See DAQ-1347 for more info.
"""


from __future__ import print_function
from org.eclipse.scanning.sequencer import ServiceHolder

watchdogService = ServiceHolder.getWatchdogService()
topupWatchdog = watchdogService.getWatchdog("TopupWatchdog")
expressionWatchdog = watchdogService.getWatchdog("ExpressionWatchdog")

def enableWatchdogs():
    """Enables TopUp and Expression watchdogs"""
    topupWatchdog.setEnabled(True)
    expressionWatchdog.setEnabled(True)

def disableWatchdogs():
    """Disables TopUp and Expression watchdogs"""
    topupWatchdog.setEnabled(False)
    expressionWatchdog.setEnabled(False)

def listWatchdogs():
    """Function to print a list of watchdogs, highlighting those that are enabled
    """
    registered_names = watchdogService.getRegisteredNames()
    if not registered_names:
        print('No watchdogs are defined')
    else:
        print('The following watchdogs are defined. Watchdogs marked with an asterisk are currently enabled.')
        for name in sorted(registered_names):
            if watchdogService.getWatchdog(name).isEnabled():
                print('  *', name, sep = '')
            else:
                print('   ', name, sep = '')

def enable_watchdogs():
    """Wrapper to facilitate function naming on I08.
    Calls enableWatchdogs()
    """
    enableWatchdogs()

def disable_watchdogs():
    """Wrapper to facilitate function naming on I08.
    Calls disableWatchdogs()
    """
    disableWatchdogs()

def list_watchdogs():
    """Wrapper to facilitate function naming on I08.
    Calls listWatchdogs()
    """
    listWatchdogs()
