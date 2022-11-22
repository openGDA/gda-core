'''
function return the synchrotron machine operation mode.
It returns the index position of from EPICS PV of Enum type.

Created on Nov 22, 2022

@author: fy65
'''
# To see the mode names:
#   $ caget -d 31 CS-CS-MSTAT-01:MODE
# [ 0] Shutdown
# [ 1] Injection
# [ 2] No Beam
# [ 3] Mach. Dev.
# [ 4] User
# [ 5] Special
# [ 6] BL Startup
# [ 7] Unknown
# 
from gdascripts.utils import caget

SHUTDOWN, INJECTION, NO_BEAM, MACH_DEV, USER, SPECIAL, BL_STARTUP, UNKNOWN = range(8)

MACHINE_STATE_PV = "CS-CS-MSTAT-01:MODE"

def get_machine_state():
    return int(caget(MACHINE_STATE_PV))