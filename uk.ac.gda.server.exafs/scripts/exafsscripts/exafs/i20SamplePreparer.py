from gda.configuration.properties import LocalProperties
from gda.exafs.scan import BeanGroup
from gda.exafs.scan import BeanGroups
from gda.factory import Finder
from uk.ac.gda.beans.exafs import XasScanParameters
from uk.ac.gda.beans.exafs import XesScanParameters
from uk.ac.gda.beans.exafs import XanesScanParameters
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters

from BeamlineParameters import JythonNameSpaceMapping

from exafsscripts.exafs.configFluoDetector import configFluoDetector

class I20SamplePreparer:
    def __init__(self):
        pass
    
    def prepare(self, sampleParameters):
        # if microreactor, then add this to the list of detectors
        if sampleParameters.getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[4] :
            return [Finder.getInstance().find("cirrus")]
        
        # for testing ONLY
        #jython_mapper = JythonNameSpaceMapping()
        #return [jython_mapper.inctime]
        return []