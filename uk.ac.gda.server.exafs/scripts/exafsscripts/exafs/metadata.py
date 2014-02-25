from gda.factory import Finder
from java.util import ArrayList

from gdascripts.parameters import beamline_parameters
from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusFileMetadata
from gda.data.scan.datawriter import NexusDataWriter
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
from gdascripts.metadata.metadata_commands import meta_add

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
                scannable=jython_mapper.__getitem__(name)
            asciiConfig.setLabelValues([scannable])
            new_header.add(asciiConfig)
            meta_add(scannable)
           
        self.datawriterconfig.setHeader(new_header)

            
    