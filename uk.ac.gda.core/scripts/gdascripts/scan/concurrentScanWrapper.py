from copy import copy
from gda.device.scannable import ScannableMotionBase, ScannableBase
from gda.device import Scannable
from gda.jython.commands.ScannableCommands import createScanPlotSettings, pos, configureScanPipelineParameters
from gda.scan import ConcurrentScan, ScanBase, ScanPositionProvider
from gdascripts.scan.SecondaryConcurrentScan import SecondaryConcurrentScan
from gdascripts.scan.process.ScannableScan import ScannableScan
import operator
import time
import java.lang.InterruptedException #@UnresolvedImport

import logging

logger = logging.getLogger(__name__)

PRINTTIME = False

ROOT_NAMESPACE_DICT = None
"""If set a variable SRSWriteAtFileCreation will be appended with the scan
command line. This will be added to the metadata section of SRS files."""

def add(a,b):
    """value by value sum of lists, or single value types"""
    try:
        lista = list(a)
    except TypeError:
        lista = [a]
    try:
        listb = list(b)
    except TypeError:
        listb = [b]
    
    result = []
    for aa, bb in zip(lista, listb):
        result.append(aa+bb)
    
    if len(result) == 1:
        return result[0]
    return result

def isObjectScannable(obj):
    return isinstance(obj, (ScannableMotionBase, ScannableBase, Scannable))

def sampleScannablesInputPosition(scannable):
    """
    Returns input fields only
    """
    try:
        # works for lists/tuples/java arrays
        wholePos = list(scannable.getPosition())
    except TypeError:
        # works for single values (float,int etc)
        wholePos = list([scannable.getPosition()])
    wholePos = wholePos[0:len(scannable.getInputNames())]
    if len(wholePos)==0:
        return None
    if type(wholePos[0]) is str:
        raise TypeError
    if len(wholePos)==1:
        return wholePos[0]
    else:
        return wholePos

class ConcurrentScanWrapper(object):
    log = logger.getChild('ConcurrentScanWrapper')
    """
    Overide convertArgsToConcurrentScan and configure returnToStart and relativeScan
    at initialisation to define behaviour.
    
    Trigger the scan via __call__ method.
    
    If the arguments passed to __call__ contain a gdascripts.process.ScannableScan
    instance, then the wrapper will wrap a gdascripts.scan.SecondaryConcurrentScan
    instead of gda.scan.ConcurrentScan. This allow's 'scans of scans' (to some degree).
    """
    
    def convertArgStruct(self, argStruct):
        """
        Override to define a new scan syntax. argStruct is defined in parseArgsIntoArgStruct().
        The argStruct returned should be that expected by the ConcurrentScan object (after
        its been flattened with flattenArgStructToArgs()
        
        Don't worry about converting relative to absolute positions here, only change the syntax
        of the arguments.
        """
        raise RuntimeError("Not implemented")
        #return argStruct

### FINAL ###

    def __init__(self, returnToStart, relativeScan, scanListeners = None):
        self.returnToStart = returnToStart
        self.relativeScan = relativeScan
        self.scanListeners = scanListeners
        self.scanListenerResults = []
        self.yaxis = None
        self.xaxis = None
        self.dataVectorPlotNameForSecondaryScans = None
        self.lastScan=None

    def __repr__(self):
        return self.__doc__
    
    def __str__(self):
        return self.__repr__()

    def __call__(self, *args):
        # Also support arguments passed in a single list or tuple. This is to be compatable
        # with the original ConcurrentScan constructor.
        if len(args) == 1:
            try:
                args = list(args[0])
            except TypeError:
                pass  # The single arg was iterable so leave it be.

        self.log.debug('Starting wrapped concurrent scan with: %s', args)
        self.prepareScanListeners(args)

        # Prepare arguments for ConcurrentScan and possibly record initial positions
        argStruct = self.parseArgsIntoArgStruct(args)
        try:
            argStruct = self.convertArgStruct(copy(argStruct))
        except ValueError: # unpack sequence too short
            raise Exception(self.__doc__)

        self.log.debug('Parsed argStruct: %s', argStruct)
        if self.returnToStart or self.relativeScan:
            initialPositions = self.sampleInitialPositions(argStruct)
        if self.relativeScan:
            argStruct = self.makeAbsolute(argStruct, initialPositions)
        newArgs = self.flattenArgStructToArgs(argStruct)
        self.log.debug('Absolute args to scan: %s', newArgs)
        
        # Create the scan
        self.starttime=time.time()
        if self.checkArgStructForScannableScan(argStruct):
            self.log.debug('checkArgStructForScannableScan is set')
            # (infrequently used)
            scan = SecondaryConcurrentScan(newArgs)
            scan.dataVectorPlot = self.dataVectorPlotNameForSecondaryScans
        else:
            scan = self._createScan(newArgs)
            self.settings = createScanPlotSettings(scan)
            scan.setScanPlotSettings(self.settings)
            if self.yaxis is not None:
                self.settings.setYAxesShown([self.yaxis])
            if self.xaxis is not None:
                self.settings.setXAxisName(self.xaxis)

        self.lastScan = scan # for later debugging
        
        # Add the command to the SRS file header
        if ROOT_NAMESPACE_DICT is not None:
            cmdstring = self._constructUserCommand(args)
            self._appendStringToSRSFileHeader("cmd='%s'"%cmdstring)
        
        # ** Run the scan **
        if PRINTTIME: print "=== Scan started: "+time.ctime()
        try:
            self.log.log(5, 'Starting scan')
            scan.runScan()
            if PRINTTIME: print ("=== Scan ended: " + self._timeStats())
        except java.lang.InterruptedException, e:
            if not scan.wasScanExplicitlyHalted():
                self.log.error('Scan was interrupted', exc_info=True)
                raise 
            else:
                # Keep going if the scan was stopped manually
                print ("=== Scan stopped early by user: " + self._timeStats())
                self.log.debug('User stopped scan early')
                e_explicitely_halting = e        
        finally:
            # Clear the SRS file header string
            if ROOT_NAMESPACE_DICT is not None:
                self._clearStringToSRSFileHeader()
        
        # Possibly return to start positions
        if self.returnToStart:
            self.returnToInitialPositions(initialPositions)
    
        # Inform scan listeners the scan has completed
        self.updateScanListeners(scan)
        if len(self.scanListenerResults)==1:
            print self.scanListenerResults[0].__str__()
            return self.scanListenerResults[0]
        elif len(self.scanListenerResults)>1:
            return self.scanListenerResults
        else:
            return None
        
        # if the scan was stopped manually, re-raise the exception caught earlier
        if scan.wasScanExplicitlyHalted():
            raise e_explicitely_halting
        
    def _timeStats(self):
        return time.ctime() + ". Elapsed time: %.0f seconds" % (time.time()-self.starttime)
    
    def _createScan(self, args):
        scan = ConcurrentScan(args)
        if isinstance(scan, ScanBase): # fails with the mock otherwise TODO: fix test
            configureScanPipelineParameters(scan)
        return scan

    def parseArgsIntoArgStruct(self, args):
        """[[x, 1, 2, 1], [y, 3, 5, 1], [z]] = parseArgsIntoArgStruct(x, 1, 2, 1, y, 3, 5, 1, z)
        """
        if len(args) == 0:
            raise SyntaxError(self.__doc__)
        # start off with the first arg which must be a scannable
        if isObjectScannable(args[0]) == False:
            raise Exception("First argument to scan command must be a scannable")
        currentList = [args[0]]
        
        # start off the list of lists
        listOfLists=[currentList] #NOTE: currentList now point into this list
        
        # loop through args starting new lists for each scannable found
        for arg in args[1:]:
            if isObjectScannable(arg): # start a new list
                currentList = [arg]
                listOfLists.append(currentList)
            elif isinstance(arg, (int, float, list, tuple, ScanPositionProvider)):
                # add to the current list
                currentList.append(arg)
            else:
                raise Exception("Arguments to scan must be a ScannableMotionBase/Scannable, number, or token. Problem with: ",arg)
        return listOfLists
    
    def checkArgStructForScannableScan(self, argStruct):
        for currentList in argStruct:
            if isinstance(currentList[0], ScannableScan):
                return True
        return False
    
    def flattenArgStructToArgs(self, argStruct):
        result = []
        for currentList in argStruct:
            for arg in currentList:
                result.append(arg)
        return result

    def sampleInitialPositions(self, argStruct):
        """
        returns a dictionary of scannable/position pairs for those items that should
        be returned at the end of a scan. Assumes args as expected by concurrent scan
        """
        result = {}
        for currentList in argStruct:
            if len(currentList) in (3,4) or (len(currentList) == 2 and isinstance(currentList[1], tuple)):
                scannable = currentList[0]
                result[scannable] = sampleScannablesInputPosition(scannable)
        return result

    def makeAbsolute(self, argStruct, initialPositions):
        """
        modifies the argStruct to make start and end positions absolute from relative
        requires an initialPositions dictionary of scannable/position pairs
        WARNING: will alter the original list
        """
        for i in range(len(argStruct)):
            currentList = argStruct[i]
            if len(currentList) == 2 and isinstance(currentList[1], tuple):
                initialPos = initialPositions[currentList[0]] # Get initial position of current scannable from dict
                newtuple = []
                for pos in currentList[1]:
                    newtuple.append(add(pos, initialPos))
                currentList[1] = tuple(newtuple)
            elif len(currentList) == 3: # [Scannable, start, step]
                initialPos = initialPositions[currentList[0]] # Get initial position of current scannable from dict
                currentList[1] = add(currentList[1], initialPos) # Make start absolute
            elif len(currentList) == 4: # [Scannable, start, stop, step]
                initialPos = initialPositions[currentList[0]] # Get initial position of current scannable from dict
                currentList[1] = add(currentList[1], initialPos) # Make start absolute
                currentList[2] = add(currentList[2], initialPos) # Make stop absolute
        return argStruct

    def returnToInitialPositions(self, initialPositionsDict):
        for scannable in initialPositionsDict.keys():
            # to ensure nothing is moving even in case the scan is interrupted
            scannable.waitWhileBusy()
        posargs = reduce(operator.add, initialPositionsDict.items())
        pos(posargs)

    def prepareScanListeners(self, args):
        if self.scanListeners != None:
            for listener in self.scanListeners:
                listener.setContext(args)
                listener.prepareForScan()

    def updateScanListeners(self, concurrentScan):
        self.scanListenerResults = []
        if self.scanListeners != None:
            for listener in self.scanListeners:
                result = listener.update(concurrentScan)
                if result is not None:
                    self.scanListenerResults.append(result)
    
    def _constructUserCommand(self, args):
        tokens = [self.__class__.__name__.lower()] # get the scan name
        for arg in args:
            if isObjectScannable(arg):
                tokens.append(arg.name)
            else:
                tokens.append(str(arg))
        return ' '.join(tokens)    
    
    def _appendStringToSRSFileHeader(self, s):
        """Not thread safe"""
        h = ROOT_NAMESPACE_DICT.get('SRSWriteAtFileCreation', '')
        if h in (None,''):
            h='\n'
        ROOT_NAMESPACE_DICT['SRSWriteAtFileCreation'] = h + s

    def _clearStringToSRSFileHeader(self):
        #Creates as a side effect
        ROOT_NAMESPACE_DICT['SRSWriteAtFileCreation'] = ''
