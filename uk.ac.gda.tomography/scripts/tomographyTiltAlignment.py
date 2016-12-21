import scisoftpy as dnp
from gda.factory import Finder
from gda.jython.commands.ScannableCommands import scan
from i12utilities import pwd
from gda.util import Sleep
from org.eclipse.dawnsci.analysis.dataset.impl import Image as javaImage


plotData = True

def analyseData(dd):
    #ss = dd.sum(0)
    #dnp.plot.image(ss)
    ##ss is the flat field
    print "Calculating median for the dataset ..."
    med = dnp.median(dd, 0)
    print "Median calculated"
    if plotData: dnp.plot.image(med)
    
    Sleep.sleep(2000)
    #flatFieldCorrectedSum = ss/med
    #dnp.plot.image(flatFieldCorrectedSum)
    
    xVals = []
    yVals = []
    ##
    print "Calculating centroid ..."
    centroids = []
    for i in range(18):
        #dnp.plot.image(dd[i, :, :] / med)
        im = dd[i, :, :] / med
        im = javaImage.medianFilter(im, [3, 3])
        im = dnp.array(im)
        threshold = 0.85
        if plotData: dnp.plot.image(im)
        if plotData: Sleep.sleep(1000)
        if plotData: dnp.plot.image(im < threshold)
        if plotData: Sleep.sleep(1000)
        cent = dnp.centroid(im < threshold)
        yVals.append(cent[0])
        xVals.append(cent[1])
        centroids.append(cent)
    print "y:" + `yVals`
    print('')
    print "x:" + `xVals`
    #help(dnp.fit.ellipsefit)
    
    if plotData: dnp.plot.line(dnp.array(xVals), dnp.array(yVals))
    ellipseFitValues = dnp.fit.ellipsefit(xVals, yVals)
    print "major: " + `ellipseFitValues[0]`
    print "minor semi-axis: " + `ellipseFitValues[1]`
    print "major axis angle: " + `ellipseFitValues[2]`
    print "centre co-ord 1: " + `ellipseFitValues[3]`
    print "centre co-ord 2: " + `ellipseFitValues[4]`
    print "centroids:" + `centroids`
    xOffset = getXOffset(ellipseFitValues[1], ellipseFitValues[0])
    zOffset = getZOffset(ellipseFitValues[2])
    return xOffset, zOffset, centroids, ellipseFitValues

'''
Returns the values in degrees
'''
def getXOffset(height, width):
    hbyw = height / width
    vals = dnp.arctan(dnp.array(hbyw))
    return vals.getDouble() * 180 / dnp.pi

'''
Returns the values in degrees
'''
def getZOffset(thetaInDeg):
    return thetaInDeg * 180 / dnp.pi
