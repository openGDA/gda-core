'''
Created on 25 Feb 2014

@author: zrb13439
'''

from gda.device.detector import DetectorBase
try:
    from gda.device.scannable import TwoDScanSwingPlotter as _TwoScanDPlotter
except ImportError:
    from gda.device.scannable import TwoDScanPlotter as _TwoScanDPlotter
    
from gda.scan import ConstantVelocityRasterScan
from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper
from gdascripts.scan.trajscans import setDefaultScannables
from gdascripts.utils import caput_wait
import random

DEFAULT_SCANNABLES_FOR_RASTERSCANS = []




class DetectorDummy(DetectorBase):
    
    '''Dummy PD Class'''
    def __init__(self, name):
        self.name = name
        
    def isBusy(self):
        return 0

    def readout(self):
#         return self.getCollectionTime()
        return abs(random.gauss(self.getCollectionTime(), 1))


def pieplotter(ystart, ystop, ystep, xstart, xstep, xstop, zname):
    
    _plotter = _TwoScanDPlotter()
    _plotter.setPlotViewname('Plot 2')
    try:
        _plotter.setSwingPlotViewName('Data Vector')
    except AttributeError:
        pass
    _plotter.setX_colName('pieX')
    _plotter.setY_colName('pieY')
    _plotter.setZ_colName(zname)
    _plotter.setName('pieplotter_' + zname)
    _plotter.setXArgs(xstart, xstep, xstop)
    _plotter.setYArgs(ystart, ystop, ystep)
    return _plotter


def xyplotter(ystart, ystop, ystep, xstart, xstep, xstop, zname):
    
    _plotter = _TwoScanDPlotter()
    _plotter.setPlotViewname('Plot 2')
    try:
        _plotter.setSwingPlotViewName('Data Vector')
    except AttributeError:
        pass
    _plotter.setX_colName('x')
    _plotter.setY_colName('y')
    _plotter.setZ_colName(zname)
    _plotter.setName('xylotter_' + zname)
    _plotter.setXArgs(xstart, xstep, xstop)
    _plotter.setYArgs(ystart, ystop, ystep)
    return _plotter




class RasterScan(ConcurrentScanWrapper):
    """USAGE:
    
  rasterscan scnY start stop step scnX start stop step det [time] [det [time]] ... ['column_name']

  e.g.: scan pieY 1 100 1 pieX 1 100 1 rasterpil1 .1 'roi1_total'
        
        Use scan.yaxis = 'axis_name' to determine which axis will be analysed and plotted by default.
        
"""
    def __init__(self, scanListeners = None):
        ConcurrentScanWrapper.__init__(self, returnToStart=False, relativeScan=False, scanListeners=scanListeners)
    
    def convertArgStruct(self, argStruct):
        return argStruct  # ConstantVelocityRasterScan will check args for validiy
    
    def parseArgsIntoArgStruct(self, args):
        args = list(args)
        args = self._tweaks_args(args)
        return ConcurrentScanWrapper.parseArgsIntoArgStruct(self, args)
    
    def _tweaks_args(self, args):
        # TODO: ConstantVelocityRasterScan will check for validity, but should check here too.
        yscn, ystart, ystop, ystep, xscn, xstart, xstep, xstop = args[0:8]
        del yscn  # unused
        
        # ConstantVelocityRasterScan does not properly set the user listed scannables
        # which would otherwise determine the x axis name automatically
        self.xaxis = xscn.name

        
        # Replace last arg with a raster plotter if a string
        if isinstance(args[-1], str):
            zname = args[-1]
            print "'Plot 2' will show: " + zname
            args[-1] = pieplotter(ystart, ystop, ystep, xstart, xstep, xstop, zname)
            self.yaxis = zname
        else:
            print "'Plot 2' will show nothing"
        
        return args
    
    def _createScan(self, args):
        original_default_scannables = setDefaultScannables(DEFAULT_SCANNABLES_FOR_RASTERSCANS)
        try:
            scan = ConstantVelocityRasterScan(args)
        finally:
            setDefaultScannables(original_default_scannables)
        return scan