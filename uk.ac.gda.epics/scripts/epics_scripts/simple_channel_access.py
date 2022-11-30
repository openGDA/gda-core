"""
Simple function to read/write to pvs.
Note that these functions create abd destroy connections on each call. It is more effecient 
to create CAClient objects that remain in scope for as long as required.
"""

from gda.epics import CAClient

def caput(pv,val):
    """
    Usage: "caput BL13J-OP-ACOLL-01:AVERAGESIZE" "10"
    """
    CAClient.put(pv, val)

def caget(pv):
    """
    Usage: "caget BL13J-OP-ACOLL-01:AVERAGESIZE"
    """
    return CAClient.get(pv)