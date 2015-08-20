from metadata import Metadata
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs import QEXAFSParameters
#from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
#from gda.data.scan.datawriter import NexusFileMetadata
#from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
    
class I18OutputPreparer:
    
    def __init__(self, datawriterconfig):
        self.datawriterconfig = datawriterconfig
        self.scanBean = None
        self.asciiSampleMetadataX = None
        self.asciiSampleMetadataY = None
        self.asciiSampleMetadataZ = None

    def prepare(self, outputParameters, scanBean, sampleParameters):
        initial_energy = scanBean.getInitialEnergy()
        final_energy = scanBean.getFinalEnergy()
        #from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        #NexusExtraMetadataDataWriter.removeAllMetadataEntries();
        metadata = outputParameters.getMetadataList()
        meta=Metadata(self.datawriterconfig)
        self.scanBean = scanBean
        if len(metadata)>0:
            print "adding to file metadata from output parameters"
            meta.add_to_metadata(metadata)
        self.originalAsciiHeader = self.getAsciiDataWriterConfig(scanBean).getHeader()
        if isinstance (scanBean,XasScanParameters) or isinstance (scanBean,XanesScanParameters) or isinstance (scanBean,QEXAFSParameters):
            stage = sampleParameters.getSampleStageParameters()
            self.asciiSampleMetadataX = self.setAsciiSampleStageMetadata(scanBean,stage.getX(), "X stage:")
            self.asciiSampleMetadataY = self.setAsciiSampleStageMetadata(scanBean,stage.getY(), "Y stage:")
            self.asciiSampleMetadataZ = self.setAsciiSampleStageMetadata(scanBean,stage.getZ(), "Z stage:")
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    
    def setAsciiSampleStageMetadata(self, scanBean,position, name):
        from gda.data.scan.datawriter import AsciiMetadataConfig
        asciiConfig = AsciiMetadataConfig()
        asciiConfig.setLabel(name + str(position))
        self.getAsciiDataWriterConfig(scanBean).getHeader().add(asciiConfig)
        return asciiConfig 
            
    def getAsciiDataWriterConfig(self,scanBean):
        return self.datawriterconfig

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,detectorBean,outputBean):
        return None
    
    def _resetNexusStaticMetadataList(self):
        pass
    
    def _resetAsciiStaticMetadataList(self):
        if self.asciiSampleMetadataX!= None:
            self.getAsciiDataWriterConfig(self.scanBean).getHeader().remove(self.asciiSampleMetadataX)
        if self.asciiSampleMetadataY!= None:
            self.getAsciiDataWriterConfig(self.scanBean).getHeader().remove(self.asciiSampleMetadataY)
        if self.asciiSampleMetadataZ!= None:
            self.getAsciiDataWriterConfig(self.scanBean).getHeader().remove(self.asciiSampleMetadataZ)
    