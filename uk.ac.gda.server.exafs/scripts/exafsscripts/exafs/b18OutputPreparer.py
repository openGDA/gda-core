from metadata import Metadata
from gdascripts.metadata.metadata_commands import meta_clear
    
class B18OutputPreparer:
    
    def __init__(self, datawriterconfig):
        self.datawriterconfig = datawriterconfig
    
    def prepare(self, outputParameters, scanBean):
        metadata = outputParameters.getMetadataList()
        meta=Metadata(self.datawriterconfig)
        if len(metadata)>0:
            meta.add_to_metadata(metadata)
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,scanBean):
        return self.datawriterconfig

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,detectorBean,outputBean):
        return None
    
    def _resetHeader(self):
        self.datawriterconfig.setHeader(self.original_header)
        meta_clear()