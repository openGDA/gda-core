"""Functions to manage TopUp and Expression Watchdogs. Consolidated from duplicated 
versions on i08, i13-1 and i14. See DAQ-1347 for more info.
"""


from __future__ import print_function
from uk.ac.diamond.osgi.services import ServiceProvider
from org.eclipse.scanning.api.device import IDeviceWatchdogService

watchdogService = ServiceProvider.getService(IDeviceWatchdogService)
topup_watchdog = watchdogService.getWatchdog("topup_watchdog")
beam_available_watchdog = watchdogService.getWatchdog("beam_available_watchdog")

def enableWatchdogs():
    """Enables all registered watchdogs"""
    for dog in watchdogService.getRegisteredNames():
        watchdogService.getWatchdog(dog).setEnabled(True)

def disableWatchdogs():
    """Disables all registered watchdogs"""
    for dog in watchdogService.getRegisteredNames():
        watchdogService.getWatchdog(dog).setEnabled(False)

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

# Allow access to individual watchdogs
def set_watchdog_enabled(watchdog_name, enabled):
    watchdogService.getWatchdog(watchdog_name).setEnabled(enabled)

def is_watchdog_enabled(watchdog_name):
    return watchdogService.getWatchdog(watchdog_name).isEnabled()

def is_watchdog_pausing(watchdog_name):
    return watchdogService.getWatchdog(watchdog_name).isPausing()
