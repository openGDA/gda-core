    """
    Validates the bean which defines the detectors and then configures the vortex or xspress
    based on the xml file whose name is in the bean.
    """
    def configFluoDetector(self, detectorParameters, outputParameters, scriptFolder):
        detType = detectorParameters.getFluorescenceParameters().getDetectorType()
        fullFileName = scriptFolder + detectorParameters.getFluorescenceParameters().getConfigFileName()
        print "configuring", detType, "detector using", fullFileName
        if detType == "Germanium":
            Xspress2DetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()
        else:
            VortexDetectorConfiguration(Finder.getInstance().find("ExafsScriptObserver"), fullFileName, None, outputParameters).configure()