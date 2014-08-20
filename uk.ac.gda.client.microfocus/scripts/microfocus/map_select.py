from uk.ac.gda.beans import BeansFactory
from gda.data import PathConstructor

#
# This is the general 'map' command in the Jython namespace to run a map based on the given filenames in __call__
#
class MapSelect():
    
    def __init__(self, non_raster, raster, raster_return_write):
        self.non_raster=non_raster
        self.raster=raster
        self.raster_return_write = raster_return_write
        self.scanBean=None
        self.raster_mode = raster
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
        
        datadir = PathConstructor.createFromDefaultProperty() + "/xml/"
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
        
        if stageNumber != 1 and stageNumber != 3:
            return "only stages 1 or 3 may be selected"
        
        self.raster.setStage(stageNumber)
        self.raster_return_write.setStage(stageNumber)
