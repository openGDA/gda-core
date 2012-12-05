from uk.ac.gda.beans import BeansFactory

class MapSelect():
    
    def __init__(self, non_raster, raster, datadir):
        self.non_raster=non_raster
        self.raster=raster
        self.datadir=datadir
        self.scanBean=None
        
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    
        xmlFolderName = self.datadir + folderName + "/"
     
        self.scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        if(self.scanBean.isRaster()):
            self.raster(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
            return
        
        self.non_raster(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
        
    def getMFD(self):
        if(self.scanBean.isRaster()):
            return self.raster.getMFD()
        else:
            return self.non_raster.getMFD()