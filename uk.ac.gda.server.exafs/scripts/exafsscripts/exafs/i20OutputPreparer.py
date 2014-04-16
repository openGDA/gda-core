from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping
from metadata import Metadata
from gda.configuration.properties import LocalProperties
from gda.scan import ScanPlotSettings
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XesScanParameters,XasScanParameters,XanesScanParameters
from gdascripts.metadata.metadata_commands import meta_clear_alldynamical, meta_rm, meta_add
from gda.factory import Finder
from gda.data.scan.datawriter import NexusDataWriter
from java.util import HashSet 
# from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
# from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
# from gda.data.scan.datawriter import NexusFileMetadata
# from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
        
class I20OutputPreparer:
    
    def __init__(self, datawriterconfig, datawriterconfig_xes):
        self.jython_mapper = JythonNameSpaceMapping()
        self.datawriterconfig = datawriterconfig
        self.datawriterconfig_xes = datawriterconfig_xes
        self.meta=Metadata(self.datawriterconfig)
        
    def prepare(self, outputParameters, scanBean):
        self.redefineNexusMetadata(scanBean)
        self.jython_mapper.ionchambers.setOutputLogValues(True) 
        # Custom for I20, which is why it is here instead of the shared DetectorConfiguration.java classes.
        # Set the output options for the fluo detectors. Hope that this output preparer has been called AFTER the
        # detector preparer or these settings will be overwritten.
        self.jython_mapper.xspress2system.setOnlyDisplayFF(outputParameters.isXspressOnlyShowFF());
        self.jython_mapper.xspress2system.setAddDTScalerValuesToAscii(outputParameters.isXspressShowDTRawValues());
        self.jython_mapper.xspress2system.setSaveRawSpectrum(outputParameters.isXspressSaveRawSpectrum());
        self.jython_mapper.xmapMca.setSaveRawSpectrum(outputParameters.isVortexSaveRawSpectrum());
        return []
    #
    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    #
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    #
    def getAsciiDataWriterConfig(self, scanBean):
        if (isinstance(scanBean, XesScanParameters)):
            # will return None if not found
            print "Ascii (.dat) files will have XES format header."
            return self.datawriterconfig_xes
        else:
            # will return None if not found
            print "Ascii (.dat) files will have XAS format header."
            return self.datawriterconfig

    def _resetNexusStaticMetadataList(self):
        self.meta.removeNexusMetadataList(self.getXasNexusMetadataList() + self.getXesNexusMetadataList())   
    
    def redefineNexusMetadata(self, scanBean):
        # XES mode just need information of I1 Stanford amplifiers and four filters for attenuator 5 
        # clear all metadata linked to stanford_amplifiers and attenuator 5 (filter5 ... filter8) in the two list of 
        # NexusDataWriter (LocationMap and MetaScannableList) 
        addListXes = self.getXesNexusMetadataList()
        addListXas = self.getXasNexusMetadataList()
        
        self.meta.removeNexusMetadataList(addListXes + addListXas)               
        # add metadata specific to xes or xas 
        addList=[]
        if (isinstance(scanBean, XesScanParameters)):
            addList = addListXes
        elif isinstance(scanBean,XasScanParameters) or isinstance(scanBean,XanesScanParameters):
            addList = addListXas      
        self.meta.addNexusMetadataList(addList)
                    
                    
    def getXasNexusMetadataList(self):
        addListXas = ["atn5_filter5_name", "atn5_filter5", "atn5_filter6_name", "atn5_filter6", "atn5_filter7_name", "atn5_filter7",
                       "atn5_filter8_name", "atn5_filter8", "i0_stanford_offset_current", "i0_stanford_offset", "i0_stanford_offset_units",
                       "i0_stanford_sensitivity", "i0_stanford_sensitivity_units", "iref_stanford_offset_current", "iref_stanford_offset",
                       "iref_stanford_offset_units", "iref_stanford_sensitivity", "iref_stanford_sensitivity_units", "it_stanford_offset_current",
                       "it_stanford_offset", "it_stanford_offset_units", "it_stanford_sensitivity", "it_stanford_sensitivity_units"]
        return addListXas

    def getXesNexusMetadataList(self):       
        addListXes = ["i1_stanford_offset_current", "i1_stanford_offset", "i1_stanford_offset_units", "i1_stanford_sensitivity",
                      "i1_stanford_sensitivity_units"]
        return addListXes         
    
    #
    # For any specific plotting requirements based on all the options in this experiment
    #
    def getPlotSettings(self, detectorBean, outputBean):
        if detectorBean.getExperimentType() == "Fluorescence" :
            detType = detectorBean.getFluorescenceParameters().getDetectorType()
            if detType == "Germanium" :
                if outputBean.isXspressShowDTRawValues() or not outputBean.isXspressOnlyShowFF():
                    # create a filter for the DT columns and return it
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                    sps = ScanPlotSettings()
                    sps.setXAxisName("Energy")  # column will have be converted to this name
                    
                    fluoDetGroup = None
                    listDetectorGroups = detectorBean.getDetectorGroups()
                    for detGroup in listDetectorGroups:
                        if detGroup.getName() == "Germanium":
                            fluoDetGroup = detGroup

                    axes = []
                    for det in fluoDetGroup.getDetector():
                        thisDet = self.jython_mapper.__getattr__(str(det))
                        extraNames = thisDet.getExtraNames()
                        axes += extraNames
                        
                    extraColumns = outputBean.getSignalList()
                    for column in extraColumns:
                        axes += column.getLabel()

                    visibleAxes = []
                    invisibleAxes = []
                    for axis in axes:
                        if str(axis).startswith("Element"):  # and self._containsUnderbar(str(axis)):
                            invisibleAxes += [axis]
                        else:
                            visibleAxes += [axis]
                    sps.setYAxesShown(visibleAxes)
                    sps.setYAxesNotShown(invisibleAxes)
                    # if anythign extra, such as columns added in the output parameters xml should also be plotted
                    sps.setUnlistedColumnBehaviour(2)
                    return sps
        return None
    