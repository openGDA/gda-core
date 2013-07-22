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
            header = self.datawriterconfig.getHeader()
            header.add(asciiConfig)
            self.add_to_nexus_metadata(name, str(scannable), "additional_scannables", NXinstrumentSubTypes.NXpositioner)
            
    def add_to_nexus_metadata(self, name, value, type, subtype):
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata(name,value,EntryTypes.NXcharacterization,subtype,type))
        
    def addScannableMetadataEntry(self, scannableName, type, subtype):
        jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
        scannable = jythonNameMap.__getitem__(scannableName)
        self.add_to_nexus_metadata(scannableName, str(scannable), "type", subtype)  