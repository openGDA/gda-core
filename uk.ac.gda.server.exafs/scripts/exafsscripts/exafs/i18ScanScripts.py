from xas_scan import XasScan

from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping

from gda.exafs.scan import ExafsScanPointCreator,XanesScanPointCreator
from gda.jython import ScriptBase
from gda.jython.commands import ScannableCommands
from uk.ac.gda.beans.exafs import XanesScanParameters, QEXAFSParameters
from uk.ac.gda.beans.exafs import XasScanParameters

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
        self.log( "setting collection time to" , str(collectionTime))

        if self.topupMonitor!=None:
            self.topupMonitor.setPauseBeforePoint(True)
            self.topupMonitor.setPauseBeforeLine(False)
            self.topupMonitor.setCollectionTime(collectionTime)

        if self.beam!=None:
            self.beam.setPauseBeforePoint(True)
            self.beam.setPauseBeforeLine(True)
            ScannableCommands.add_default([self.beam])

        if self.detectorFillingMonitor!=None and beanGroup.getDetector().getExperimentType() == "Fluorescence" and beanGroup.getDetector().getFluorescenceParameters().getDetectorType() == "Germanium": 
            self.detectorFillingMonitor.setPauseBeforePoint(True)
            self.detectorFillingMonitor.setPauseBeforeLine(False)
            self.detectorFillingMonitor.setCollectionTime(collectionTime)
            ScannableCommands.add_default([self.detectorFillingMonitor])

        if self.trajBeamMonitor!=None:
            self.trajBeamMonitor.setActive(False)
        
    def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, controller, repNum):
        times = []
        self.configureMonitors(beanGroup)
        if isinstance(beanGroup.getScan(),XasScanParameters):
            times = ExafsScanPointCreator.getScanTimeArray(beanGroup.getScan())
        elif isinstance(beanGroup.getScan(),XanesScanParameters):
            times = XanesScanPointCreator.getScanTimeArray(beanGroup.getScan())
        if len(times) > 0:
            self.log( "Setting scan times, using array of length",len(times))
            jython_mapper = JythonNameSpaceMapping()
            jython_mapper.counterTimer01.setTimes(times)
            ScriptBase.checkForPauses()
        return