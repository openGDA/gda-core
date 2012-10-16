from gda.factory import Finder
from gdascripts.parameters import beamline_parameters

class B18OutputPreparer:
    def __init__(self):
        pass
    
    def prepare(self, outputParameters):
        metadataActive = outputParameters.isMetadataActive()
        if metadataActive:
            self.add_to_metadata(outputParameters.getMetadataList())
            
    #
    # For any specific plotting requirements based on all the options in this experiment
    #
    def getPlotSettings(self,beanGroup):
        
        return None

    
    def add_to_metadata(self, metadataList):
        for metadata in metadataList:
            from gda.data.scan.datawriter import AsciiMetadataConfig
            asciiConfig = AsciiMetadataConfig()
            
            name=metadata.getScannableName()
            asciiConfig.setLabel(name + ": %4.1f")
            
            scannable=Finder.getInstance().find(name)
            if scannable==None:
                jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
                scannable=jythonNameMap.__getitem__(name)
            
            asciiConfig.setLabelValues([scannable])
            header = Finder.getInstance().find("datawriterconfig").getHeader()
            header.add(asciiConfig)
            print "----- scannable added to metadata -----", name