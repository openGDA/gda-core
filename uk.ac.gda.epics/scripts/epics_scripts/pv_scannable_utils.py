from gda.jython import InterfaceProvider
from gda.device.scannable import EpicsScannable
def createPVScannable( name, pv, addToNameSpace=True, hasUnits=True, getAsString=False):
    """
    utility function to create a scannable from a PV
    arguments:
    name - of scannable
    pv - pv 
    addToNameSpace = if True the scannable is accessible from the commandline after the call
    hasUnits - default True. The value is a number  and support is given for setUserUnits
    getAsString - default False. Useful if the PV is an enum as it returns the string representation. 
                 If true also set hasUnits to False 
    
    e.g.
    createPVScannable("acoll_average_size", "BL13J-OP-ACOLL-01:AVERAGESIZE", True)
    """
    sc = EpicsScannable()
    sc.setName(name)
    sc.setPvName(pv)
    sc.setUseNameAsInputName(True)
    sc.setHasUnits(hasUnits)
    sc.setGetAsString(getAsString)
    sc.afterPropertiesSet()
    sc.configure()
    if addToNameSpace:
        commandServer = InterfaceProvider.getJythonNamespace()    
        commandServer.placeInJythonNamespace(name,sc)
    return sc


def ls_pv_scannables():
    """
    Function to list Scannables associated with EPICs PVs, the PV and the associated DESC field
    Usage:
    ls_pv_scannables()
    """
    from gda.device.scannable import ScannableMotor
    from gda.device.motor import EpicsMotor
    a=InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace()
    l=filter(lambda x: isinstance(x, EpicsScannable) or (isinstance(x, ScannableMotor) and isinstance(x.motor, EpicsMotor)), a.values().toArray())
    for x in l:
        description="unknown"
        pvName ="unknown"
        if isinstance(x, EpicsScannable):
            pvName=x.pvName
        if isinstance(x, ScannableMotor) and isinstance(x.motor, EpicsMotor):
            pvName=x.motor.pvName
            
        if x.configured:
            try:
                description = caget(pvName + ".DESC")
            except:
                description = "Unable to read description"
                pass
        else:
            description = "Not configured!"
        print x.name, pvName, description
        
    
    
from gda.device.scannable import EpicsScannable
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

def caputStringAsWaveform(pv, val):
    """
    Usage: "caputStringAsWaveform BL13J-OP-ACOLL-01:AVERAGESIZE" "This is some text"
    """
    CAClient.putStringAsWaveform(pv, val)