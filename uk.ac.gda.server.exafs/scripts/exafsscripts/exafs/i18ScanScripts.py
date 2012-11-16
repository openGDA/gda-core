from xas_scan import XasScan
from uk.ac.gda.beans.exafs import XanesScanParameters, QEXAFSParameters



class I18XasScan(XasScan):
    
    def addMonitors(self, topupMonitor, beam, detectorFillingMonitor, trajBeamMonitor):
        self.topupMonitor=topupMonitor
        self.beam=beam
        self.detectorFillingMonitor=detectorFillingMonitor
        self.trajBeamMonitor=trajBeamMonitor
        
    def configureMonitors(self, beanGroup):
        scan = beanGroup.getScan()
        collectionTime = 0.0
        if isinstance(scan, XanesScanParameters):
            regions = scan.getRegions()        
            for region in regions:
                if(collectionTime < region.getTime()):
                    collectionTime = region.getTime()
        elif isinstance(scan, QEXAFSParameters):
            pass
        else:
            collectionTime = scan.getExafsTime()
            if(scan.getExafsToTime() > collectionTime):
                collectionTime = scan.getExafsToTime()
        print "setting collection time to" , str(collectionTime)

        if self.topupMonitor!=None:
            self.topupMonitor.setPauseBeforePoint(True)
            self.topupMonitor.setPauseBeforeLine(False)
            self.topupMonitor.setCollectionTime(collectionTime)

        if self.beam!=None:
            self.beam.setPauseBeforePoint(True)
            self.beam.setPauseBeforeLine(True)
            add_default(self.beam)

        if self.detectorFillingMonitor!=None and beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium": 
            self.detectorFillingMonitor.setPauseBeforePoint(True)
            self.detectorFillingMonitor.setPauseBeforeLine(False)
            self.detectorFillingMonitor.setCollectionTime(collectionTime)
            add_default(self.detectorFillingMonitor)

        if self.trajBeamMonitor!=None:
            self.trajBeamMonitor.setActive(False)
        
        
    def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller):
        self.configureMonitors(beanGroup)
        self._doScan(beanGroup,scriptType,scan_unique_id, numRepetitions, xmlFolderName, controller)