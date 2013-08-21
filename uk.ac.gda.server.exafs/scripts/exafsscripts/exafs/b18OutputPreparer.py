from gda.data.scan.datawriter.NexusFileMetadata import NXinstrumentSubTypes
from metadata import Metadata
    
class B18OutputPreparer:
    
    def __init__(self, datawriterconfig):
        self.datawriterconfig = datawriterconfig
    
    def prepare(self, outputParameters, scanBean):
        initial_energy = scanBean.getInitialEnergy()
        final_energy = scanBean.getFinalEnergy()
        from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        NexusExtraMetadataDataWriter.removeAllMetadataEntries();
        metadata = outputParameters.getMetadataList()
        meta=Metadata(self.datawriterconfig)
        if len(metadata)>0:
            meta.add_to_metadata(metadata)
        meta.add_to_nexus_metadata("initial_energy", str(initial_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
        meta.add_to_nexus_metadata("final_energy", str(final_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,scanBean):
        return None

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,detectorBean,outputBean):
        return None