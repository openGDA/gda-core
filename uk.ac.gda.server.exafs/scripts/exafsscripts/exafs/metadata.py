from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
from gda.factory import Finder
from java.util import ArrayList

from BeamlineParameters import JythonNameSpaceMapping

class Metadata():
    
    def __init__(self, datawriterconfig):
        self.datawriterconfig = datawriterconfig
    
    def add_to_metadata(self, metadataList):
        header = self.datawriterconfig.getHeader()[:]
        new_header = ArrayList()
        for item in header:
            new_header.add(item)

        for metadata in metadataList:
            from gda.data.scan.datawriter import AsciiMetadataConfig
            asciiConfig = AsciiMetadataConfig()
            name=metadata.getScannableName()
            asciiConfig.setLabel(name + ": %4.1f")
            finder = Finder.getInstance()
            scannable=finder.find(name)
            if scannable==None:
                jython_mapper = JythonNameSpaceMapping()
                scannable=self.jython_mapper.__getitem__(name)
            asciiConfig.setLabelValues([scannable])
            new_header.add(asciiConfig)
            self.add_to_nexus_metadata(name, str(scannable), "additional_scannables", NXinstrumentSubTypes.NXpositioner)

        self.datawriterconfig.setHeader(new_header)

            
    def add_to_nexus_metadata(self, name, value, type, subtype):
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata(name,value,EntryTypes.NXcharacterization,subtype,type))
        
    def addScannableMetadataEntry(self, scannableName, type, subtype):
        jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
        scannable = jythonNameMap.__getitem__(scannableName)
        self.add_to_nexus_metadata(scannableName, str(scannable), "type", subtype)  