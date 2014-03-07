from gda.data import PathConstructor, NumTracker
import scisoftpy as dnp
from uk.ac.gda.server.ncd.optimiser import LadderSampleFinder as LSF

def _getAxes(file, dir=None):
    if dir is None:
        dir = PathConstructor.createFromDefaultProperty()
    
    data = dnp.io.load(dir + file)
    
    default = data.entry1.default
    
    for key in default.keys():
        if hasattr(default[key], 'attrs'):
            att = default[key].attrs
            if 'primary' in att:
                xAxis = default[key]
            else:
                yAxis = default[key]
    xAxis = xAxis._getdata().getSlice(None).data
    yAxis = yAxis._getdata().getSlice(None).data
    return xAxis, yAxis

def findPeaks(file, dir=None):
    xAxis, yAxis = _getAxes(file, dir)
    
    lsf = LSF()
    l = lsf.process(xAxis, yAxis)
    print l

