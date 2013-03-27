from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.scan import ScanPlotSettings

from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XesScanParameters

from gdascripts.parameters import beamline_parameters

from BeamlineParameters import JythonNameSpaceMapping

class I20OutputPreparer:
    
    def __init__(self):
        self.mode = "xas"
        self.jython_mapper = JythonNameSpaceMapping()
        pass
    
    def prepare(self, outputParameters, scanBean):

        from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        NexusExtraMetadataDataWriter.removeAllMetadataEntries();
        self.redefineNexusMetadata()

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
    def getAsciiDataWriterConfig(self,beanGroup):
        scan = beanGroup.getScan()
        if self.mode == "xes" or isinstance(scan,XesScanParameters):
            # will return None if not found
            print "Ascii (.dat) files will have XES header."
            return Finder.getInstance().find("datawriterconfig_xes")
        else:
            # will return None if not found
            print "Ascii (.dat) files will have XAS format header."
            return Finder.getInstance().find("datawriterconfig")

    #
    # For any specific plotting requirements based on all the options in this experiment
    #
    def getPlotSettings(self,beanGroup):
        
        if beanGroup.getDetector().getExperimentType() == "Fluorescence" :
            detType = beanGroup.getDetector().getFluorescenceParameters().getDetectorType()
            if detType == "Germanium" :
                fluoDetBean = BeansFactory.getBeanObject(beanGroup.getScriptFolder(), beanGroup.getDetector().getFluorescenceParameters().getConfigFileName())
                if fluoDetBean.isXspressShowDTRawValues():
                    # create a filter for the DT columns and return it
                    LocalProperties.set("gda.scan.useScanPlotSettings", "true")
                    sps = ScanPlotSettings()
                    sps.setXAxisName("Energy")  # column will be converted to this name
                    
                    fluoDetGroup = None
                    listDetectorGroups = beanGroup.getDetector().getDetectorGroups()
                    for detGroup in listDetectorGroups:
                        if detGroup.getName() == "Germanium":
                            fluoDetGroup = detGroup

                    axes = []
                    for det in fluoDetGroup.getDetector():
                        thisDet =  self.jython_mapper.__getattr__(str(det))
                        extraNames = thisDet.getExtraNames()
                        axes += extraNames
                        
                    extraColumns = beanGroup.getOutput().getSignalList()
                    for column in extraColumns:
                        axes += column.getLabel()

                    visibleAxes = []
                    #invisibleAxes = []
                    for axis in axes:
                        if not str(axis).startswith("Element"):
                             visibleAxes += [axis]
                    sps.setYAxesShown(visibleAxes)
                    sps.setYAxesNotShown(invisibleAxes)
                    # if anythign extra, such as columns added in the output parameters xml should also be plotted
                    sps.setUnlistedColumnBehaviour(2)
                    #print sps
                    return sps
        return None
    
    def _containsUnderbar(self,string):
        for c in string:
            if c == "_":
                return True
        return False
         
    def redefineNexusMetadata(self):

        from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
        from gda.data.scan.datawriter import NexusFileMetadata
        from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
        
#        if (LocalProperties.get("gda.mode") == 'dummy'):
#            return
        
        # primary slits
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xsize",str(self.jython_mapper.s1_hgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xcentre",str(self.jython_mapper.s1_hoffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ysize",str(self.jython_mapper.s1_vgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ycentre",str(self.jython_mapper.s1_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
    
        # M1
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m1_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        if (LocalProperties.get("gda.mode") != 'dummy'):
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.m1m2_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m1_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Curvature",str(self.jython_mapper.m1_bend()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ellipticity",str(self.jython_mapper.m1_elip()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sag",str(self.jython_mapper.m1_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
    
        # M2
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m2_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m2_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Curvature",str(self.jython_mapper.m2_bend()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ellipticity",str(self.jython_mapper.m1_elip()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
    
        # ATNs 1,2&3
        if (LocalProperties.get("gda.mode") != 'dummy'):
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn1()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN1"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn2()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN2"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn3()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN3"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn4()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN4"))
        
        #Y slits  ????
    #    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("YPlus",str(self.jython_mapper.s1_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"Y slits"))
    
        # Mono
        if (LocalProperties.get("gda.mode") != 'dummy'):
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal Cut",str(self.jython_mapper.crystalcut()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 1 pitch",str(self.jython_mapper.crystal1_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 2 roll",str(self.jython_mapper.crystal2_roll()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pair 2 roll",str(self.jython_mapper.pair2_roll()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        
        # Mono slits S2
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xsize",str(self.jython_mapper.s2_hgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xcentre",str(self.jython_mapper.s2_hoffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ysize",str(self.jython_mapper.s2_vgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ycentre",str(self.jython_mapper.s2_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        
        # M3
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 3 Pitch",str(self.jython_mapper.m3_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 3 Stripe",str(self.jython_mapper.m3_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 3 Sag",str(self.jython_mapper.m3_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 3 Y",str(self.jython_mapper.m3_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        
        # M4
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 4 Pitch",str(self.jython_mapper.m4_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 4 Stripe",str(self.jython_mapper.m4_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 4 Y",str(self.jython_mapper.m4_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 4 Curvature",str(self.jython_mapper.m4_bend()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 4 Ellipticity",str(self.jython_mapper.m4_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        
        # HR mirror
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.hr_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.hr_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.hr_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        
        # filter
        
        # mono slits
        
        # ST1?
        
        # Gain and HV for Io, it anf iref
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.i0_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.i0_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.i0_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.i0_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.i1_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.i1_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.i1_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.i1_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.it_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.it_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.it_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.it_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.iref_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.iref_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.iref_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.iref_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        
        # pos of reference wheel
        
        # ring current
        
        # YSlits drian current
        
    #    if isInstance(beanGroup.getScan,XesScanParameters):
            
            # analysertype and crystal cut
            
            # ST2 x and y pos
            
            # xes sample stage positions ??
            
            # xes bragg (deg)
            
            # detector x and y
            
            # analyser hor and rot    
