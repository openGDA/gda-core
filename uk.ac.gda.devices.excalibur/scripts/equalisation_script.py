import scisoftpy as dnp
from gdascripts.parameters import beamline_parameters
from uk.ac.gda.devices.excalibur.equalization import ExcaliburEqualizationHelper
#from uk.ac.gda.analysis.hdf5 import Hdf5HelperData
#from ncsa.hdf.object.h5 import H5Datatype
#from ncsa.hdf.hdf5lib import HDF5Constants
from uk.ac.gda.analysis.hdf5 import Hdf5Helper
from gda.configuration.properties import LocalProperties

from gda.factory import Finder
finder = Finder.getInstance()
import os
import recordConfig        
from  dac_scan import dacscan
import gdascripts.parameters.beamline_parameters
import scisoftpy as dnp
from epics_scripts.simple_channel_access import caput, caget
"""
The Jython makes use of the Java classes: ExcaliburEqualizationHelper and Hdf5Helper
/home/excalibur/gda_versions/gda_8_26/workspace_git/gda-dls-excalibur.git/uk.ac.gda.devices.excalibur/src/uk/ac/gda/devices/excalibur/equalization/ExcaliburEqualizationHelper.java
/home/excalibur/gda_versions/gda_8_26/workspace_git/gda-core.git/uk.ac.gda.analysis/src/uk/ac/gda/analysis/hdf5/Hdf5Helper.java


import equalisation_script

epg=equalisation_script.ExcaliburEqualiser(logFileName="/dls_sw/i13-1/scripts/equalistaion.dat")
epg.run()

"""

def setUpExcaliburConfig():
    """
    Function to set chunking of phdf writing detectors based on whether gap is enabled or not
    """
    jms = beamline_parameters.JythonNameSpaceMapping()
    excalibur_config_normal=jms.excalibur_config_normal
    prefix = LocalProperties.get("gda.epics.excalibur.pvprefix")
    if caget(prefix +':CONFIG:GAP:EnableCallbacks') == "Enable":
        print "Gap enabled"
        excalibur_config_normal.pluginList[1].chunkSize0=1
        excalibur_config_normal.pluginList[1].chunkSize1 = 259
        excalibur_config_normal.pluginList[1].chunkSize2 = 2069
        excalibur_config_normal.pluginList[1].dsetSize1=1796
        excalibur_config_normal.pluginList[1].dsetSize2=2069
    else:
        print "Gap disabled"
        excalibur_config_normal.pluginList[1].chunkSize1 = 256
        excalibur_config_normal.pluginList[1].chunkSize2 = 2048
        excalibur_config_normal.pluginList[1].chunkSize0 = 4
        excalibur_config_normal.pluginList[1].dsetSize1=1536
        excalibur_config_normal.pluginList[1].dsetSize2=2048        

class Threshold0ScanData:
    def __init__(self, name, parameters, thresholdAVal=None, thresholdNVal=None, dacPixelVal=None, edgeFilename=None):
        self.name = name
        self.parameters = parameters
        self.thresholdAVal = thresholdAVal
        self.thresholdNVal = thresholdNVal
        self.dacPixelVal = dacPixelVal
        self.excaliburEH = ExcaliburEqualizationHelper.getInstance()
        
    def log(self, msg):
        print self.name, msg

    def getTh0EdgeFilename(self, equalizer):
        filename = self.parameters.getValueOrNone(self.name + ".edgeFilename")
        if  filename == None:
            scanFileName = self.getTh0DACScanFilename(equalizer)
            self.log("Creating edge file from " + scanFileName)
            filename = equalizer.createThresholdFileFromScanData(scanFileName, "")
            self.parameters.parameters[self.name + ".edgeFilename"] = filename
        return filename

    def getTh0DACScanFilename(self, equalizer):
        filename = self.parameters.getValueOrNone(self.name + ".dacScanFilename")
        if filename == None:
            self.log("performing dacscan - sending thresholdA, thresholdN and dacPixel if required")
            if self.thresholdAVal != None:
                equalizer.thresholdA.moveTo(self.thresholdAVal)
            if self.thresholdNVal != None:
                equalizer.thresholdN.moveTo(self.thresholdNVal)
            if self.dacPixelVal != None:
                equalizer.dacPixel.moveTo(self.dacPixelVal)
            self.log("performing dacscan - starting scan")
            dacscan([ equalizer.threshold0, 0, equalizer.threshold0Max, equalizer.threshold0StepSize, equalizer.detector, equalizer.exposureTime])
            filename = equalizer.lastScanDataPoint().currentFilename
            self.parameters.parameters[self.name + ".dacScanFilename"] = filename
            equalizer.save()
        else:
            self.log(self.name + ".dacScanFilename : " + filename + " already exists")
        return filename

    def getThresholdNMaskFilename(self, equalizer):
        """ 
        Create an image of pixels with dimensions that match the dimensions of the image. The value for each
        is 1 if no edge is found in the edge scan
        """
        filename = self.parameters.getValueOrNone(self.name + ".thresholdNMaskFilename")
        if filename == None:
            edgeThresholdFile = self.getTh0EdgeFilename(self)
            filename = edgeThresholdFile.replace(".nxs", "_thresholdNMask.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createMaskFromEdgePositionValidData(edgeThresholdFile, equalizer.chipRows, equalizer.chipCols, equalizer.eqTarget, filename)
            self.parameters.parameters[self.name + ".thresholdNMaskFilename"] = filename
        return filename

    def getTmaxFilename(self, equaliser):
        filename = self.parameters.getValueOrNone(self.name + ".tmaxFilename")
        if filename == None:
            filein = self.getTh0EdgeFilename(self)
            filename = filein.replace(".nxs", "_tmax.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createThresholdTargetFromChipPopulations(filein,
                equaliser.chipRows, equaliser.chipCols, equaliser.chipPresentVals, 100, filename)
            self.parameters.parameters[self.name + ".tmaxFilename"] = filename
        return filename

import os

class ExcaliburEqualiser:
    def __init__(self, logFileName="/scratch/Equalisation/1.dat"):
        self.threshold = 10
        self.threshold0Max = 511
        self.threshold0StepSize = 2
        self.exposureTime = .01
        self.eqTarget = 10
        jms = beamline_parameters.JythonNameSpaceMapping()
        self.excaliburEH = ExcaliburEqualizationHelper.getInstance()
        self.detector = jms.excalibur_config_dacscan
        self.detectorName = self.detector.getName()
        self.thresholdA = jms.thresholdA
        self.scan = jms.scan
        self.lastScanDataPoint = jms.lastScanDataPoint
        self.threshold0 = jms.threshold0
        self.thresholdN = jms.thresholdN
        self.dacPixel = jms.dacPixel
        excalibur_config = finder.find("excalibur_config")
        self.readoutFems = excalibur_config.get("readoutFems")
        self.chipRows = 6
        self.chipCols = 8
#        self.chipPresentVals=[ True for x in range(2) for y in range(8)] + [ False for x in range(4) for y in range(8)]
        self.chipPresentVals = [ True for x in range(self.chipRows) for y in range(self.chipCols)]
#        self.chipPresentVals[5 * 8 + 1] = False
        if  not os.path.exists(logFileName):
            open(logFileName, 'w').close()
        self.logFileName = logFileName
        self.parameters = gdascripts.parameters.beamline_parameters.Parameters(logFileName)
        """
        set thresholdA to 0 to turn use of thresholdN and DAC_PIXEL OFF
        """
        self.th0InitialScanData = Threshold0ScanData("th0InitialScanData", self.parameters, 0, 0)
#        self.th0InitialScanData.dacScanFilename = "/scratch/Equalisation/65.nxs"
#        self.th0InitialScanData.edgeFilename = "/scratch/Equalisation/65_10.nxs"
#        self.th0InitialScanData.thresholdNMaskFilename = "/scratch/Equalisation/65_10_thresholdNMask.nxs"


        """
        set thresholdA to 16 to turn use of thresholdN ON but use DAC_PIXEL OFF
        set thresholdN to 40
        dac scan of threshold0
        """
        self.th0ThresholdNLowerScanData = Threshold0ScanData("th0ThresholdNLowerScanData", self.parameters, 16, 25)


        """
        set thresholdA to 16 to turn use of thresholdN ON but use DAC_PIXEL OFF
        set thresholdN to 50
        dac scan of threshold0
        """
        self.th0ThresholdNUpperScanData = Threshold0ScanData("th0ThresholdNUpperScanData", self.parameters, 16, 45)
        
        
        self.th0ThresholdNChkScanData = Threshold0ScanData("th0ThresholdNChkScanData", self.parameters)
        
        self.th0DacPixel0t = Threshold0ScanData("th0DacPixel0t", self.parameters, dacPixelVal=0)

        self.th0DacPixel10 = Threshold0ScanData("th0DacPixel10", self.parameters, dacPixelVal=40)

        self.th0DacPixel40 = Threshold0ScanData("th0DacPixel40", self.parameters, dacPixelVal=65)

        self.dacPixelCheckScanData = Threshold0ScanData("dacPixelCheckScanData", self.parameters)
        
        self.rawEqCheckScanData = Threshold0ScanData("rawEqCheckScanData", self.parameters)

        
        self.tweakThresholdAdjFilename = self.parameters.getValueOrNone("tweakThresholdAdjFilename")
        self.thresholdNResponseFile = self.parameters.getValueOrNone("thresholdNResponseFile")
        self.thresholdNOptFilename = self.parameters.getValueOrNone("thresholdNOptFilename")
        self.checkScanData = []

        
    def run(self, numberOfTweaks=3):
        print "Starting equalisation"
        self.threshold0Max = 511
        self.threshold0StepSize = 2
                
        self.th0InitialScanData.getTh0EdgeFilename(self)
        self.save()
        self.getThresholdNResponseFilename()
        self.save()
        self.getThresholdNOptFilename()
        self.save()
        self.getThresholdNChkTh0EdgeFilename()
        self.save()
        #load thresholdN mask and thresholdN values
        self.th0ThresholdNChkScanData.getTmaxFilename(self)
        self.save()
        self.getDACPixelControlBitsFilename()
        self.save()

        self.getDacPixel0tTh0EdgeFilename()
        self.th0DacPixel0t.getTmaxFilename(self)
        self.save()

        self.th0DacPixel10.getTh0EdgeFilename(self)
        self.save()

        self.th0DacPixel40.getTh0EdgeFilename(self)
        self.save()
        
        #create changes in edge positions for 0 to 40 and 0 to 10
        #save as new edgePostions
        #fit gaussians to those
        #get response to these shifts
        self.getDACPixel10ShiftFilename()
        self.save()
        
        self.getDACPixel40ShiftFilename()
        self.save()
        
        self.getDACPixelResponseFilename()
#%% choose optimal DACPixel values for each chip
        self.getDACPixelOptFilename()
        self.save()

        self.threshold0Max = 100
        self.threshold0StepSize = 2
        self.getDacPixelCheckScanData()
        self.save()

        self.threshold0Max = 100
        self.threshold0StepSize = 1

        self.getRawEqCheckFilename()
        self.save()

#Fine tuning steps
        self.threshold0Max = 50
        self.threshold0StepSize = 1
        
        for i in range(numberOfTweaks):
            self.checkScanData.append(Threshold0ScanData("tweakedCheckScanData" + `i`, self.parameters))
            self.checkScanData.append(Threshold0ScanData("finalCheckScanData" + `i`, self.parameters))
        
        
        for i in range(numberOfTweaks):
            self.get_tweakThresholdAdjFilename(i)
            self.save()
            
            self.get_tweakThresholdAdjCheckFilename(i)
            self.save()
            
            self.get_finalThresholdAdjFilename(i)
            self.save()
            
            self.get_finalThresholdAdjCheckFilename(i)
            self.save()

        print "Equalisation done"

    
    def save(self):
        f = open(self.logFileName, "w")
#        for x, y in sorted(self.parameters.parameters.items()):
#            f.write(x + "=" + y + "\n")
            
        keys = []

        keys.append("th0InitialScanData.dacScanFilename")
        keys.append("th0InitialScanData.edgeFilename")
        keys.append("th0InitialScanData.thresholdNMaskFilename")
        
        keys.append("th0ThresholdNLowerScanData.dacScanFilename")
        keys.append("th0ThresholdNLowerScanData.edgeFilename")
        keys.append("th0ThresholdNUpperScanData.dacScanFilename")
        keys.append("th0ThresholdNUpperScanData.edgeFilename")
        keys.append("thresholdNResponseFile")
        keys.append("thresholdNOptFilename")
        keys.append("th0ThresholdNChkScanData.dacScanFilename")
        keys.append("th0ThresholdNChkScanData.edgeFilename")
        keys.append("th0ThresholdNChkScanData.tmaxFilename")
        
        keys.append("dacPixelControlBitsFilename")

        
        keys.append("th0DacPixel0t.dacScanFilename")
        keys.append("th0DacPixel0t.edgeFilename")
        keys.append("th0DacPixel0t.tmaxFilename")
        
        keys.append("th0DacPixel10.dacScanFilename")
        keys.append("th0DacPixel10.edgeFilename")
        keys.append("th0DacPixel40.dacScanFilename")
        keys.append("th0DacPixel40.edgeFilename")
        
        keys.append("dacPixel10ShiftFilename")
        keys.append("dacPixel40ShiftFilename")
        keys.append("dacPixelResponseFile")
        keys.append("dacPixelOptFilename")

        keys.append("dacPixelCheckScanData.dacScanFilename")
        keys.append("dacPixelCheckScanData.edgeFilename")
        keys.append("thresholdAdjFilename")

        keys.append("rawEqCheckScanData.dacScanFilename")
        keys.append("rawEqCheckScanData.edgeFilename")
        
        for tweak in range(len(self.checkScanData)/2):
            keys.append("tweakThresholdAdjFilename" + `tweak`)
            keys.append(self.checkScanData[tweak*2].name +".dacScanFilename")
            keys.append(self.checkScanData[tweak*2].name +".edgeFilename")
            keys.append("finalThresholdAdjFilename" + `tweak`)
            keys.append(self.checkScanData[tweak*2+1].name +".dacScanFilename")
            keys.append(self.checkScanData[tweak*2+1].name +".edgeFilename")
            
        
        for key in keys:
            val = self.parameters.parameters.get(key)
            if val != None:
                f.write(key + "=" + val + "\n")
        f.close()        
        
    def log(self, msg):
        print msg

    def getDacPixelCheckScanData(self):
        filename = self.parameters.getValueOrNone("dacPixelCheckScanData.dacScanFilename")
        if filename == None:
            self.log("Getting Check DACPixel data")
            #set thresholdN and dacPixel to opt 
            self.excaliburEH.setThresholdNFromFile(self.getThresholdNOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            self.excaliburEH.setDACPixelFromFile(self.getDACPixelOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            #set thresholdAdj so thresholdN bit is set as calc but dacpixel bits set to 1 for all pixels
            self.excaliburEH.setThresholdAdjFromFile(self.th0InitialScanData.getThresholdNMaskFilename(self), None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 15)
        return self.dacPixelCheckScanData.getTh0EdgeFilename(self)
        
    def get_thresholdAdjFilename(self):
        filename = self.parameters.getValueOrNone("thresholdAdjFilename" )
        if filename == None:
            self.getDACPixelControlBitsFilename(), 
            filein = self.getDACPixelControlBitsFilename()
            filename = filein.replace(".nxs", "thresholdAjd"  + ".nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            
            self.excaliburEH.createThresholdAdj(self.getDACPixelControlBitsFilename(), self.th0InitialScanData.getThresholdNMaskFilename(self), filename)
            self.parameters.parameters["thresholdAdjFilename"] = filename
        return filename
        
    def getRawEqCheckFilename(self):
        filename = self.parameters.getValueOrNone("rawEqCheckScanData.dacScanFilename")
        if filename == None:     
            self.log("Setting ThresholdA, ThresholdN and DACPixel before creating resultsEq2RawScanData.dacScanFilename")
            self.excaliburEH.setThresholdAdjFromFile(self.get_thresholdAdjFilename(),None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 0)
            self.excaliburEH.setThresholdNFromFile(self.getThresholdNOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            self.excaliburEH.setDACPixelFromFile(self.getDACPixelOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            recordConfig.createModelAndSaveToFile(self.logFileName + "_rawEq.excaliburconfig")
        return self.rawEqCheckScanData.getTh0EdgeFilename(self)
    
    
    def get_tweakThresholdAdjFilename(self, tweak):
        filename = self.parameters.getValueOrNone("tweakThresholdAdjFilename" + `tweak`)
        if filename == None:
            filein = self.getDACPixelControlBitsFilename()
            filename = filein.replace(".nxs", "tweak" + `tweak` + ".nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            if tweak == 0:
                checkScanData = self.rawEqCheckScanData.getTh0EdgeFilename(self)
                thresholdAdj = self.get_thresholdAdjFilename()
            else:
                checkScanData = self.get_finalThresholdAdjCheckFilename(tweak-1)
                thresholdAdj = self.get_finalThresholdAdjFilename(tweak-1)
            
            self.excaliburEH.tweakThresholdAdj(checkScanData, thresholdAdj, self.eqTarget, filename)
            self.parameters.parameters["tweakThresholdAdjFilename"+ `tweak`] = filename
        return filename

    def get_tweakThresholdAdjCheckFilename(self, tweak):
        filename = self.parameters.getValueOrNone(self.checkScanData[tweak*2].name + ".dacScanFilename")
        if filename == None:     
            self.log("Setting ThresholdA, ThresholdN and DACPixel before creating tweakedCheckScanData" + `tweak` + ".dacScanFilename")           
            self.excaliburEH.setThresholdAdjFromFile(self.get_tweakThresholdAdjFilename(tweak), None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 0)
            self.excaliburEH.setThresholdNFromFile(self.getThresholdNOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            self.excaliburEH.setDACPixelFromFile(self.getDACPixelOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            recordConfig.createModelAndSaveToFile(self.logFileName + "_tweakedEq"+`tweak`+".excaliburconfig")
        return self.checkScanData[tweak*2].getTh0EdgeFilename(self)
    

    def get_finalThresholdAdjFilename(self,tweak):
        filename = self.parameters.getValueOrNone("finalThresholdAdjFilename"+ `tweak`)
        if filename == None:
            filein = self.getDACPixelControlBitsFilename()
            filename = filein.replace(".nxs", "final" + `tweak` + ".nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)

            if tweak == 0:
                beforeCheckScanData = self.rawEqCheckScanData.getTh0EdgeFilename(self)
                before_thresholdAdj = self.get_thresholdAdjFilename()
            else:
#                beforeCheckScanData = self.getFinalEqCheckFilename(tweak-1)
                beforeCheckScanData = self.get_finalThresholdAdjCheckFilename(tweak-1)
                before_thresholdAdj = self.get_finalThresholdAdjFilename(tweak-1)
            
            self.excaliburEH.selectClosestThresholdAdj(beforeCheckScanData, self.get_tweakThresholdAdjCheckFilename(tweak), before_thresholdAdj, self.get_tweakThresholdAdjFilename(tweak), self.eqTarget, filename)
            self.parameters.parameters["finalThresholdAdjFilename"+ `tweak`] = filename
        return filename


    def get_finalThresholdAdjCheckFilename(self,tweak):
        filename = self.parameters.getValueOrNone(self.checkScanData[tweak*2+1].name + ".dacScanFilename")
        if filename == None:     
            self.log("Setting ThresholdA, ThresholdN and DACPixel before creating finalCheckScanData" + `tweak` + ".dacScanFilename")           
            self.excaliburEH.setThresholdAdjFromFile(self.get_finalThresholdAdjFilename(tweak), None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 0)
            self.excaliburEH.setThresholdNFromFile(self.getThresholdNOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            self.excaliburEH.setDACPixelFromFile(self.getDACPixelOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
            recordConfig.createModelAndSaveToFile(self.logFileName + "_finalEq"+`tweak`+".excaliburconfig")
        return self.checkScanData[tweak*2+1].getTh0EdgeFilename(self)





    def getDACPixel10ShiftFilename(self):
        filename = self.parameters.getValueOrNone("dacPixel10ShiftFilename")
        if filename == None:                
            filein = self.th0DacPixel10.getTh0EdgeFilename(self)
            filename = filein.replace(".nxs", "_dacPixel10Shift.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createDACPixelShift(filein, self.th0DacPixel0t.getTh0EdgeFilename(self), self.chipRows, self.chipCols, self.chipPresentVals, filename)
            self.parameters.parameters["dacPixel10ShiftFilename"] = filename
        return filename

    def getDACPixel40ShiftFilename(self):
        filename = self.parameters.getValueOrNone("dacPixel40ShiftFilename")
        if filename == None:                
            filein = self.th0DacPixel40.getTh0EdgeFilename(self)
            filename = filein.replace(".nxs", "_dacPixel40Shift.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createDACPixelShift(filein, self.th0DacPixel0t.getTh0EdgeFilename(self), self.chipRows, self.chipCols, self.chipPresentVals, filename)
            self.parameters.parameters["dacPixel40ShiftFilename"] = filename
        return filename

    def getDACPixelOptFilename(self):
        filename = self.parameters.getValueOrNone("dacPixelOptFilename")
        if filename == None:        
            filein = self.getDACPixelResponseFilename()
            filename = filein.replace("_dacPixelResponse.nxs", "_dacPixelOpt.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createDACPixelOpt(self.getDACPixelResponseFilename(), self.th0DacPixel0t.getTmaxFilename(self), self.eqTarget, filename)
            self.parameters.parameters["dacPixelOptFilename"] = filename
        return filename

    def getDACPixelControlBitsFilename(self):
        filename = self.parameters.getValueOrNone("dacPixelControlBitsFilename")
        if filename == None:        
            filein = self.th0ThresholdNChkScanData.getTmaxFilename(self)
            filename = filein.replace("_tmax.nxs", "_dacPixelControlBits.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.calcDACPixelControlBits(self.th0ThresholdNChkScanData.getTh0EdgeFilename(self), self.eqTarget,
                    self.th0ThresholdNChkScanData.getTmaxFilename(self), self.chipRows, self.chipCols , self.chipPresentVals, filename)
            self.parameters.parameters["dacPixelControlBitsFilename"] = filename
        return filename

    def getDacPixel0tTh0EdgeFilename(self):
        filename = self.parameters.getValueOrNone("th0DacPixel0t.dacScanFilename")
        if filename == None:
            self.log("Setting ThresholdAdj before creating th0DacPixel0t.dacScanFilename")           
            self.excaliburEH.setThresholdAdjFromFile(self.th0InitialScanData.getThresholdNMaskFilename(self), None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 15)
        return self.th0DacPixel0t.getTh0EdgeFilename(self)

    
    def getThresholdNChkTh0EdgeFilename(self):
        filename = self.parameters.getValueOrNone("th0ThresholdNChkScanData.dacScanFilename")
        if filename == None:
            self.log("Setting ThresholdAdj and ThresholdN before creating th0ThresholdNChkScanData.dacScanFilename")           
            self.excaliburEH.setThresholdAdjFromFile(self.th0InitialScanData.getThresholdNMaskFilename(self), None, self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals, 0)
            self.excaliburEH.setThresholdNFromFile(self.getThresholdNOptFilename(), self.readoutFems, self.chipRows, self.chipCols , self.chipPresentVals)
        return self.th0ThresholdNChkScanData.getTh0EdgeFilename(self)
            
      
    def getThresholdNOptFilename(self):
        filename = self.parameters.getValueOrNone("thresholdNOptFilename")
        if filename == None:        
            thresholdNReponseFilename = self.getThresholdNResponseFilename()
            filename = thresholdNReponseFilename.replace("_thresholdNResponse.nxs", "_thresholdNOpt.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            self.excaliburEH.createThresholdNOpt(thresholdNReponseFilename,
                self.th0ThresholdNUpperScanData.getTh0EdgeFilename(self), self.eqTarget, filename)
            self.parameters.parameters["thresholdNOptFilename"] = filename
        return filename
    

    def getThresholdNResponseFilename(self):
        filename = self.parameters.getValueOrNone("thresholdNResponseFile")
        if filename == None:      
            filename = self.th0ThresholdNLowerScanData.getTh0DACScanFilename(self)
            filename = filename.replace(".nxs", "_thresholdNResponse.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            optThresholdNThresholdFiles = [self.th0ThresholdNLowerScanData.getTh0EdgeFilename(self),
                                           self.th0ThresholdNUpperScanData.getTh0EdgeFilename(self)]
            axisValues = [25, 45]
            self.excaliburEH.createDACResponseFromThresholdFiles(optThresholdNThresholdFiles, axisValues, filename)
            self.parameters.parameters["thresholdNResponseFile"] = filename
        return filename
        
    def getDACPixelResponseFilename(self):
        filename = self.parameters.getValueOrNone("dacPixelResponseFile")
        if filename == None:      
            filename = self.getDACPixel40ShiftFilename()
            filename = filename.replace(".nxs", "_dacPixelResponse.nxs")
            self.log("Creating " + filename)
            if os.path.exists(filename):
                os.remove(filename)
            Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename)
            optThresholdNThresholdFiles = [self.getDACPixel10ShiftFilename(),
                                           self.getDACPixel40ShiftFilename()]
            axisValues = [40, 65]
            self.excaliburEH.createDACResponseFromThresholdFiles(optThresholdNThresholdFiles, axisValues, filename)
            self.parameters.parameters["dacPixelResponseFile"] = filename
        return filename
        
    def logger(self, msg):
        print msg

    def createThresholdFileFromScanData(self, scanFilename, thresholdABNName):
        """
        Utility function to get edge data from a threshold scan and write to a file
        """
        excalibur = ExcaliburEqualizationHelper.getInstance()
        edgefilename = scanFilename.replace(".nxs", "_" + `self.threshold` + ".nxs")
        if os.path.exists(edgefilename):
            os.remove(edgefilename)
        Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(edgefilename)
        sizeOfSlice = 100
        excalibur.createThresholdFileFromScanData(scanFilename, self.detectorName, "threshold0", thresholdABNName, self.threshold, sizeOfSlice,
                                                   self.chipRows, self.chipCols, self.chipPresentVals, edgefilename);
        return edgefilename

    """
    Function to output analysis of a threshold file e.g. 
    
    
    analyse_thresholdfile(self, filePath="/data/excalibur/data/296_10.nxs" , pointsInTail=100, printOutResults=True)
    
    """
    def analyse_thresholdfile(self, filePath="/data/excalibur/data/296_10.nxs" , pointsInTail=100, printOutResults=True):
        chipRows = []
        for irow in range(self.chipRows):
            chips = []
            chipRows.append(chips)
            for icol in range(self.chipCols):
                chip = ChipAnalysis(irow, icol)
                chip.present = self.chipPresentVals[irow * self.chipCols + icol]
                chips.append(chip)
                if chip.present:
                    t = dnp.io.load(filePath)
#                    edgeThreshold = t['entry1/equalisation/edgeThresholds_col' + `icol` + '_row' + `irow`][...]
                    edgeThreshold = t['entry1/equalisation/edgeThresholds_row' + `irow` + '_column' + `icol`][...]
                    equalToMinus10 = edgeThreshold == -10
                    chip.numUnequalisedPixels = int(equalToMinus10.sum())
                    xvals = t['entry1/equalisation/Population_row' + `irow` + '_col' + `icol` + '_xvals'][...]
                    yvals = t['entry1/equalisation/Population_row' + `irow` + '_col' + `icol` + '_yvals'][...]
                    sum = 0;
                    len = yvals.shape[0]
                    for i in range(len):
                        sum += yvals[len - 1 - i]
                        if sum >= pointsInTail:
                            chip.tailThreshold = int(xvals[len - 1 - i])
                            break
        if printOutResults:
            print "Analysis of " + filePath
            print "Number of unequalised pixels on each chip"
            s = "Chip\t"
            for col in chipRows[0]:
                s += "%6d\t" % (col.col + 1)
            print s
            for row in chipRows:
                s = "Row\t%2d\t" % (row[0].row + 1)
                for col in row:
                    s += "%6d\t" % (col.numUnequalisedPixels)
                print s
            print ""
            
            print "Threshold above which there are %s equalised pixels" % (`pointsInTail`)
            s = "Chip\t"
            for col in chipRows[0]:
                s += "%6d\t" % (col.col + 1)
            print s
            for row in chipRows:
                s = "Row\t%2d\t" % (row[0].row + 1)
                for col in row:
                    s += "%6d\t" % (col.tailThreshold)
                print s
            print ""
            return
        else:
            return chipRows
        
    def sentThreshold0ToTailThreshold(self, chipRows):
        for row in chipRows:
            for col in row:
                if col.present:
                    chipReg = self.readoutFems[col.row].getIndexedMpxiiiChipReg(col.col)
                    chipReg.getAnper().setThreshold0(col.tailThreshold)
                    chipReg.loadPixelConfig()
        
class ChipAnalysis:
    def __init__(self, row, col):
        self.row = row
        self.col = col
        self.present = True
        self.numUnequalisedPixels = int(0)
        self.tailThreshold = int(0)
