from b18ScanScripts import XasScan

class I18XasScan(XasScan):
    def _configureScannable(self,beanGroup):
        useFrames = LocalProperties.check("gda.microfocus.scans.useFrames")
        if(detectorBean.getExperimentType() == "Fluorescence" and detectorBean.getFluorescenceParameters().getDetectorType() == "Germanium" and useFrames):
            xas_scannable = XasScannableWithDetectorFramesSetup()
            xas_scannable.setHarmonicConverterName("auto_mDeg_idGap_mm_converter")
        else:
            xas_scannable = XasScannable()
            useFrames = 0
        xas_scannable.setName("xas_scannable")
        xas_scannable.setEnergyScannable(Finder.getInstance().find(scanBean.getScannableName()))
        
        if isinstance(beanGroup.getScan(), XanesScanParameters):
            if useFrames:
                xas_scannable.setExafsScanRegionTimes(XanesScanPointCreator.getScanTimes(beanGroup.getScan()))
        else:
            if useFrames:
                xas_scannable.setExafsScanRegionTimes(ExafsScanPointCreator.getScanTimes(beanGroup.getScan()))

