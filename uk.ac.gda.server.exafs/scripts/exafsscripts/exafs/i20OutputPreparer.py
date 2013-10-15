from gda.configuration.properties import LocalProperties
from gda.scan import ScanPlotSettings
from uk.ac.gda.beans import BeansFactory
from uk.ac.gda.beans.exafs import XesScanParameters
from gdascripts.parameters import beamline_parameters
from BeamlineParameters import JythonNameSpaceMapping
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter
from gda.data.scan.datawriter import NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
        
class I20OutputPreparer:
    
    def __init__(self, datawriterconfig, datawriterconfig_xes):
        self.mode = "xas"
        self.jython_mapper = JythonNameSpaceMapping()
        self.datawriterconfig = datawriterconfig
        self.datawriterconfig_xes = datawriterconfig_xes
        
    def prepare(self, outputParameters, scanBean):
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
    def getAsciiDataWriterConfig(self, scanBean):
        if self.mode == "xes" or isinstance(scanBean,XesScanParameters):
            # will return None if not found
            print "Ascii (.dat) files will have XES format header."
            return self.datawriterconfig_xes
        else:
            # will return None if not found
            print "Ascii (.dat) files will have XAS format header."
            return self.datawriterconfig

    #
    # For any specific plotting requirements based on all the options in this experiment
    #
    def getPlotSettings(self,detectorBean,outputBean):
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
                        thisDet =  self.jython_mapper.__getattr__(str(det))
                        extraNames = thisDet.getExtraNames()
                        axes += extraNames
                        
                    extraColumns = outputBean.getSignalList()
                    for column in extraColumns:
                        axes += column.getLabel()

                    visibleAxes = []
                    invisibleAxes = []
                    for axis in axes:
                        if str(axis).startswith("Element"):# and self._containsUnderbar(str(axis)):
                            invisibleAxes += [axis]
                        else:
                            visibleAxes += [axis]
                    sps.setYAxesShown(visibleAxes)
                    sps.setYAxesNotShown(invisibleAxes)
                    # if anythign extra, such as columns added in the output parameters xml should also be plotted
                    sps.setUnlistedColumnBehaviour(2)
                    return sps
        return None
    
#    def _containsUnderbar(self,string):
#        for c in string:
#            if c == "_":
#                return True
#        return False
         
    def redefineNexusMetadata(self):
        
#        if (LocalProperties.get("gda.mode") == 'dummy'):
#            return
        
        # primary slits
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xsize",str(self.jython_mapper.s1_hgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xcentre",str(self.jython_mapper.s1_hoffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ysize",str(self.jython_mapper.s1_vgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ycentre",str(self.jython_mapper.s1_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"primary slits"))
    
        # M1
#        if (LocalProperties.get("gda.mode") != 'dummy'):
#            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.m1m2_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m1_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m1_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Curvature",str(self.jython_mapper.m1_curvature()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ellipticity",str(self.jython_mapper.m1_elip()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sag",str(self.jython_mapper.m1_sag()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Yaw",str(self.jython_mapper.m1_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M1"))
    
        # M2
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m2_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m2_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Curvature",str(self.jython_mapper.m2_curvature()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ellipticity",str(self.jython_mapper.m1_elip()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sag",str(self.jython_mapper.m2_sag()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Yaw",str(self.jython_mapper.m2_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M2"))
    
        # attentuators
        if (LocalProperties.get("gda.mode") != 'dummy'):
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn1()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN1"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn2()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN2"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn3()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN3"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Position",str(self.jython_mapper.atn4()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN4"))

            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 1",str(self.jython_mapper.atn5_filter1_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 1 position",str(self.jython_mapper.atn5_filter1()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 2",str(self.jython_mapper.atn5_filter2_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 2 position",str(self.jython_mapper.atn5_filter2()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 3",str(self.jython_mapper.atn5_filter3_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 3 position",str(self.jython_mapper.atn5_filter3()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 4",str(self.jython_mapper.atn5_filter4_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 4 position",str(self.jython_mapper.atn5_filter4()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 5",str(self.jython_mapper.atn5_filter5_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 5 position",str(self.jython_mapper.atn5_filter5()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 6",str(self.jython_mapper.atn5_filter6_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 6 position",str(self.jython_mapper.atn5_filter6()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 7",str(self.jython_mapper.atn5_filter7_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 7 position",str(self.jython_mapper.atn5_filter7()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 8",str(self.jython_mapper.atn5_filter8_name()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Filter 8 position",str(self.jython_mapper.atn5_filter8()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXattenuator,"ATN5"))
        
        #Y slits  ????
    #    NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("YPlus",str(self.jython_mapper.s1_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"Y slits"))
    
        # Mono
        if (LocalProperties.get("gda.mode") != 'dummy'):
            NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal Cut",str(self.jython_mapper.crystalcut()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 1 pitch",str(self.jython_mapper.crystal1_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Crystal 2 roll",str(self.jython_mapper.crystal2_roll()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pair 2 roll",str(self.jython_mapper.crystal34_roll()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmonochromator,"Mono"))
        
        # Mono slits S2
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xsize",str(self.jython_mapper.s2_hgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xcentre",str(self.jython_mapper.s2_hoffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ysize",str(self.jython_mapper.s2_vgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ycentre",str(self.jython_mapper.s2_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"mono slits"))
        
        # M3
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m3_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.m3_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sag",str(self.jython_mapper.m3_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m3_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M3"))
        
        # M4
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.m4_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.m4_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.m4_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Curvature",str(self.jython_mapper.m4_curvature()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sag",str(self.jython_mapper.m4_yaw()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ellipticity",str(self.jython_mapper.m4_elip()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"M4"))
        
        # HR mirror
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Pitch",str(self.jython_mapper.hr_pitch()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Y",str(self.jython_mapper.hr_height()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Stripe",str(self.jython_mapper.hr_stripe()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"HR mirror"))
        
        # Sample slits S3
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xsize",str(self.jython_mapper.s3_hgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"sample slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Xcentre",str(self.jython_mapper.s3_hoffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"sample slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ysize",str(self.jython_mapper.s3_vgap()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"sample slits"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Ycentre",str(self.jython_mapper.s3_voffset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXaperture,"sample slits"))
        
        # Diagnostics: cannot sdo at the moment as I am not sure what NX type or subtype to use! NXmonitor looks wrong to me.
        
        # Gain and HV for Io, it anf iref
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset current",str(self.jython_mapper.i0_stanford_offset_current()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.i0_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.i0_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.i0_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.i0_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I0 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset current",str(self.jython_mapper.i1_stanford_offset_current()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.i1_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.i1_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.i1_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.i1_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"I1 stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset current",str(self.jython_mapper.it_stanford_offset_current()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.it_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.it_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.it_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.it_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"It stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset current",str(self.jython_mapper.iref_stanford_offset_current()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset",str(self.jython_mapper.iref_stanford_offset()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Offset units",str(self.jython_mapper.iref_stanford_offset_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity",str(self.jython_mapper.iref_stanford_sensitivity()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))
        NexusExtraMetadataDataWriter.addMetadataEntry(NexusFileMetadata("Sensitivity units",str(self.jython_mapper.iref_stanford_sensitivity_units()),EntryTypes.NXinstrument,NXinstrumentSubTypes.NXmirror,"Iref stanford"))