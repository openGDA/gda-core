from metadata import Metadata
#from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
#from gda.data.scan.datawriter import NexusFileMetadata
#from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
    
class I18OutputPreparer:
    
    def __init__(self, datawriterconfig):
        self.datawriterconfig = datawriterconfig

    def prepare(self, outputParameters, scanBean):
        initial_energy = scanBean.getInitialEnergy()
        final_energy = scanBean.getFinalEnergy()
        #from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        #NexusExtraMetadataDataWriter.removeAllMetadataEntries();
        metadata = outputParameters.getMetadataList()
        meta=Metadata(self.datawriterconfig)
        if len(metadata)>0:
            meta.add_to_metadata(metadata)
            
        #meta.add_to_nexus_metadata("initial_energy", str(initial_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
        #meta.add_to_nexus_metadata("final_energy", str(final_energy), "additional_scannables", NXinstrumentSubTypes.NXmonochromator)
                
        #meta.addScannableMetadataEntry("d1motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d2motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d3motor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d5amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d5bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d6amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d6bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d7amotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        #meta.addScannableMetadataEntry("d7bmotor", "additional_scannables",NXinstrumentSubTypes.NXattenuator)
        
        #meta.addScannableMetadataEntry("sid_x", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        
        #meta.addScannableMetadataEntry("sc_MicroFocusSampleX", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        #meta.addScannableMetadataEntry("sc_MicroFocusSampleY", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        #meta.addScannableMetadataEntry("sc_sample_z", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        #meta.addScannableMetadataEntry("sc_sample_thetacoarse", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        #meta.addScannableMetadataEntry("sc_sample_thetafine", "additional_scannables",NXinstrumentSubTypes.NXpositioner)
        
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,scanBean):
        return self.datawriterconfig

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,detectorBean,outputBean):
        return None