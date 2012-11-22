from uk.ac.gda.beans import BeansFactory

class MapSelect():
    
    def __init__(self, non_raster, raster, datadir):
        self.non_raster=non_raster
        self.raster=raster
        self.datadir=datadir
    
    def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None, scanNumber= -1, validation=True):
    
        xmlFolderName = self.datadir + folderName + "/"
     
        scanBean = BeansFactory.getBeanObject(xmlFolderName, scanFileName)
        if(scanBean.isRaster()):
            self.raster(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)
            return
        
        self.non_raster(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName, scanNumber, validation)