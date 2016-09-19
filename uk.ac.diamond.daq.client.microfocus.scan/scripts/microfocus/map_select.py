from uk.ac.gda.beans import BeansFactory
#from gda.data import PathConstructor

#
# This is the general 'map' command in the Jython namespace to run a map based on the given filenames in __call__
#
class MapSelect():
    
    def __init__(self, non_raster, raster, raster_return_write, samplePreparer):
        self.non_raster=non_raster
        self.raster=raster
        self.raster_return_write = raster_return_write
        self.scanBean=None
        self.raster_mode = raster
        self.samplePreparer = samplePreparer # for eaxfs / xanes but swicthes stages in the same way
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
        
        #datadir = PathConstructor.createFromDefaultProperty() + "/xml/"
        xmlFolderName = folderName + "/"
        self.scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        if(self.scanBean.isRaster()):
            self.raster_mode(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
            return
        
        self.non_raster(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
        
    def getMFD(self):
        if self.scanBean!=None:
            if(self.scanBean.isRaster()):
                return self.raster_mode.getMFD()
            else:
                return self.non_raster.getMFD()
            
    def enableFasterRaster(self):
        self.raster_mode = self.raster_return_write
        
    def disableFasterRaster(self):
        self.raster_mode = self.raster
        
    def setStage(self,stageNumber):
        ''' To switch between tables 1 and 3
        '''
        if stageNumber != 1 and stageNumber != 3:
            return "only stages 1 or 3 may be selected"
        
        self.non_raster.setStage(stageNumber)
        self.raster.setStage(stageNumber)
        self.raster_return_write.setStage(stageNumber)
        self.samplePreparer.setStage(stageNumber)
        
    def enableUseIDGap(self):
        ''' Normal running conditions
        '''
        self.non_raster.setUseWithGapEnergy()
        self.raster.setUseWithGapEnergy()
        self.raster_return_write.setUseWithGapEnergy()

    def disableUseIDGap(self):
        ''' For shutdown and machine-dev days when there is no control of the ID gap
        '''
        self.non_raster.setUseNoGapEnergy()
        self.raster.setUseNoGapEnergy()
        self.raster_return_write.setUseNoGapEnergy()
        
    def enableRealPositions(self):
        self.raster.setIncludeRealPositionReader(True)
        self.raster_return_write.setIncludeRealPositionReader(True)

    def disableRealPositions(self):
        self.raster.setIncludeRealPositionReader(False)
        self.raster_return_write.setIncludeRealPositionReader(False)

