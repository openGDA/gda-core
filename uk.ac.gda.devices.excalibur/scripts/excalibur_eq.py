'''
Created on 16 Jan 2012

@author: rsr31645

Script to do the equalization process for the excalibur detector.
'''
'''
def equalization():
    collectData()
    dacOptimization()
    determineOffset()
    performEqualization()
    
def performEqualization();
    find how output varies as THLAdj varies - ought to be fully characterised separately for each pixel -> measuring a threshold 
            scan for each pixel as THLAdj is varied through its full range, resulting in 32 threshold scans per pixel
    analyseEffectOfVaryingTHLOnEachChannel()
    [May be that only 2 or 3 threshold scans are required as the peak position will vary linearly with THLAdj]

def analyseEffectOfVaryingTHLOnEachChannel():
    1. take a full threshold scan at a range of THL settings
    2. Curve fit for THL vs noise edge position(expect linear fit:max+c) on each pixel to obtain the transfer function for each pixel.
    ---The knowledge of the transfer function can then be used to steer the selection of the most appropriate THL when equalizing.
    [Use of pre-calculated value to process data used to determine the transfer function]
    ---Common for some of the threshold scans to not have peaks. routines used for measuring edge of the noise and the linear fits must be able to cope with this.
        
    
def dacOptimization():
    It may be beneficial to minimize the spread of the histogram of the collected data? before doing any pixel by pixel adjustment. 
    Any relevant DAC values should be scanned and the spread determined to find the optimum
    
def determineOffset():
    offset needs to be determined on each pixel
    
    option 1:
        gaussianFitMethod()
    option 2:
        maximumNoiseReponseMethod()
    option 3:
        thresholdMethod()
    option4:
        integralThresholdMethod()
    
    
def collectData():
    set up desired input (no input(noise response), test pulse input, x-ray stimulation)
    scan threshold from <start>:<step>:<finish>     threshold scan?
    record response in each pixel at each point in scan
    
def comparePixelResponse();
    comparePixelNumerically()i
    comparePixelGraphically()
    
def comparePixelNumerically():
    compare and show statistical information illustrating spread for the location features.
    
def comparePixelGraphically():
    showHistogram()
    showDataStatistically()

    
'''