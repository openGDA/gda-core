from gda.factory import Finder
from gdascripts.parameters import beamline_parameters
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
    
class I18OutputPreparer:
    
    def prepare(self, outputParameters, initial_energy, final_energy):
        from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        NexusExtraMetadataDataWriter.removeAllMetadataEntries();
        metadata = outputParameters.getMetadataList()
        if len(metadata)>0:
            self.add_to_metadata(metadata)
            
        self.add_to_nexus_metadata("initial_energy", str(initial_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
        self.add_to_nexus_metadata("final_energy", str(final_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
                
        self.addScannableMetadataEntry("d1motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d2motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d3motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d5amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d5bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d6amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d6bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d7amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        self.addScannableMetadataEntry("d7bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        
        self.addScannableMetadataEntry("sid_x", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        
        self.addScannableMetadataEntry("sc_MicroFocusSampleX", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        self.addScannableMetadataEntry("sc_MicroFocusSampleY", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        self.addScannableMetadataEntry("sc_sample_z", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        self.addScannableMetadataEntry("sc_sample_thetacoarse", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        self.addScannableMetadataEntry("sc_sample_thetafine", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,beanGroup):
        return None

    # For any specific plotting requirements based on all the options in this experiment
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
            self.add_to_nexus_metadata(name, str(scannable), "additional_scannables", NXinstrumentSubTypes.NXpositioner)
            
    def add_to_nexus_metadata(self, name, value, type, subtype):
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata(name,value,EntryTypes.NXcharacterization,subtype,type))
        
    def addScannableMetadataEntry(self, scannableName, type, subtype):
        jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
        scannable = jythonNameMap.__getitem__(scannableName)
        self.add_to_nexus_metadata(scannableName, str(scannable), "type", subtype)  
