from gda.factory import Finder
from gdascripts.parameters import beamline_parameters
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
from gda.data.scan.datawriter import AsciiMetadataConfig
    
class I18OutputPreparer:
    def __init__(self):
        pass
    
    def prepare(self, outputParameters):
        metadata = outputParameters.getMetadataList()
        if len(metadata)>0:
            self.add_to_metadata(metadata)
        self.add_default_nexus_metadata()
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,beanGroup):
        return None

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,beanGroup):
        return None

    def add_to_metadata(self, metadataList):
        for metadata in metadataList:
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
            # NXpositioner is not right and there is no generic subcategory.
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata(name,str(scannable),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXpositioner,name))            
            print "----- scannable added to metadata -----", name
            
    def addMetadataEntry(self, scannableName, type, subtype):
        jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
        scannable = jythonNameMap.__getitem__(scannableName)
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata(scannableName,str(scannable),EntryTypes.NXinstrument,subtype,type))            
    
    def add_default_nexus_metadata(self):
        self.addMetadataEntry("d1motor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d2motor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d3motor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d5amotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d5bmotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d6amotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d6bmotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d7amotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        self.addMetadataEntry("d7bmotor", "diodes",NXinstrumentSubTypes.NXattenuator)
        