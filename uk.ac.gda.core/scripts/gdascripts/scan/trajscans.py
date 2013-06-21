from gda.factory import Finder
from gda.scan import TrajectoryScanLine, ConcurrentScan, \
    ConstantVelocityScanLine

from gdascripts.scan.gdascans import Scan, Rscan, Cscan, Scancn
from gdascripts.scan.specscans import Ascan, Dscan


DEFAULT_SCANNABLES_FOR_TRAJSCANS = []
POSITION_CALLABLE_THREADPOOL_SIZE = 4

def setDefaultScannables(new):
    # TODO: Replace this lot with a call to create a Scan with no defaults.
    cs = Finder.getInstance().find("command_server")
    original = list(cs.getDefaultScannables())
    for scn in original:
        cs.removeDefault(scn)
    for scn in new:
        cs.addDefault(scn)
    return original


class ContinuousMixin(object):
    
    def createTrajAndPossiblyConcurrentScan(self, args):
        argstruct = self.parseArgsIntoArgStruct(args)
        
        # Create TrajectoryScanLine from rightmost arguments
        trajArgstruct = []
        # (work from right until the first group to be scanned over is found)
        while len(argstruct) > 0 and not (len(argstruct[-1]) == 4 or
                (len(argstruct[-1]) == 2 and isinstance(argstruct[-1][1], tuple))):
            trajArgstruct.insert(0,argstruct.pop()) 
        trajArgstruct.insert(0,argstruct.pop()) # pop the group to be scanned over
        
        original_default_scannables = setDefaultScannables(DEFAULT_SCANNABLES_FOR_TRAJSCANS)
        
        try:
            trajscan = self.create_scan(self.flattenArgStructToArgs(trajArgstruct))
            # TODO: Whan available in core, replace with a request for an unbounded queue
            trajscan.setScanDataPointQueueLength(trajscan.getNumberPoints() + 1) # (+1 for luck)
            trajscan.setPositionCallableThreadPoolSize(POSITION_CALLABLE_THREADPOOL_SIZE)
            if len(argstruct) == 0:
                scan = trajscan
            else:
                scan = ConcurrentScan(self.flattenArgStructToArgs(argstruct) + [trajscan])
                scan.setScanDataPointQueueLength(scan.getNumberPoints() + 1) # (+1 for luck)
                scan.setPositionCallableThreadPoolSize(POSITION_CALLABLE_THREADPOOL_SIZE)
        finally:
            setDefaultScannables(original_default_scannables)

        return scan

    def create_scan(self, args):
        raise Exception("ContinuousMixin:create_scan() must be overidden")


class TrajMixin(ContinuousMixin):
    def create_scan(self, args):
        return TrajectoryScanLine(args)


class CvMixin(ContinuousMixin):
    def create_scan(self, args):
        return ConstantVelocityScanLine(args)    


class CvScan(Scan, CvMixin):

    def __init__(self, scanListeners = None):
        Scan.__init__(self, scanListeners)
        self.__doc__ = Scan.__doc__.replace('scan', 'cvscan') #@UndefinedVariable

    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)

## move out!!

    
class TrajScan(Scan, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Scan.__init__(self, scanListeners)
        self.__doc__ = Scan.__doc__.replace('scan', 'trajscan') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)
   
    
class TrajRscan(Rscan, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Rscan.__init__(self, scanListeners)
        self.__doc__ = Rscan.__doc__.replace('rscan', 'trajrscan') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)
 
    
class TrajCscan(Cscan, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Cscan.__init__(self, scanListeners)
        self.__doc__ = Cscan.__doc__.replace('cscan', 'trajcscan') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)

    
class TrajScancn(Scancn, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Scancn.__init__(self, scanListeners)
        self.__doc__ = Scancn.__doc__.replace('scancn', 'trajscancn') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)

    
class TrajAscan(Ascan, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Ascan.__init__(self, scanListeners)
        self.__doc__ = Ascan.__doc__.replace('ascan', 'trajascan') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)

    
class TrajDscan(Dscan, TrajMixin):
    
    def __init__(self, scanListeners = None):
        Dscan.__init__(self, scanListeners)
        self.__doc__ = Dscan.__doc__.replace('dscan', 'trajdscan') #@UndefinedVariable
    
    def _createScan(self, args):
        return self.createTrajAndPossiblyConcurrentScan(args)