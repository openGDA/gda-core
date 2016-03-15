"""Functions for setting and clearing default masks

either run this script or import functions or import them in localstation
"""

from gda.factory import Finder
finder = Finder.getInstance()
dip = finder.find("detectorInfoPath")

def setMask(mask):
    """Set the mask file that will be linked in future scan files
    Mask must be in /entry1/mask/mask"""
    dip.setSaxsDetectorInfoPath(mask)

def clearMask():
    """Clear the saved mask
No mask will be added to future scans"""
    dip.clearSaxsDetectorInfoPath

def currentMask():
    """The current mask file that is linked to scan files"""
    return dip.getSaxsDetectorInfoPath()