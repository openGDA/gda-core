from exafsscripts.exafs.i20.I20SampleIterators import XASXANES_Roomtemp_Iterator, XES_Roomtemp_Iterator, XASXANES_Cryostat_Iterator, I20_SingleSample_Iterator
from uk.ac.gda.beans.exafs.i20 import I20SampleParameters
from org.slf4j import LoggerFactory

class I20SamplePreparer:
    
    def __init__(self, sample_x,sample_y,sample_z,sample_rot,sample_fine_rot, sample_roll,sample_pitch,filterwheel):
        self.sample_x = sample_x
        self.sample_y = sample_y
        self.sample_z = sample_z
        self.sample_rot = sample_rot
        self.sample_fine_rot = sample_fine_rot
        self.sample_roll = sample_roll
        self.sample_pitch = sample_pitch
        self.filterwheel = filterwheel
        self.logger = LoggerFactory.getLogger("exafsscripts.exafs.scan")
    
    def prepare(self, sampleBean):  
        if sampleBean.getUseSampleWheel():
            self._moveSampleWheel(sampleBean)
        return []
    
    def _moveSampleWheel(self,sampleBean):
        filter_position = sampleBean.getSampleWheelPosition()
        message = "Setting reference filter wheel to "+ str(filter_position)
        self.logger.info(message)
        print message
        self.filterwheel(filter_position)
    
    # XAS / XANES room temperature sample stage  
    def createIterator(self, sampleBean, experiment_type):
        iterator=None
        if experiment_type != 'XES' and sampleBean.getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
            iterator = XASXANES_Roomtemp_Iterator(self.sample_x,self.sample_y,self.sample_z,self.sample_rot,self.sample_roll,self.sample_pitch)
            iterator.setSampleBean(sampleBean)
        # XES room temp sample stage
        elif experiment_type == 'XES' and sampleBean.getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[1] :
            iterator = XES_Roomtemp_Iterator(self.sample_x,self.sample_y,self.sample_z,self.sample_rot,self.sample_fine_rot,self.sample_roll,self.sample_pitch)
            iterator.setSampleBean(sampleBean)
        #XAS/XANES cryostat
        elif experiment_type != 'XES' and sampleBean.getSampleEnvironment() == I20SampleParameters.SAMPLE_ENV[2] :
            iterator = XASXANES_Cryostat_Iterator()
            iterator.setSampleBean(sampleBean)
        else :
            iterator = I20_SingleSample_Iterator()
            iterator.setSampleBean(sampleBean)
        return iterator