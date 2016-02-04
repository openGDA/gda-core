#
# Series of iterator classes for I20's different sample environment options 
#
from gda.configuration.properties import LocalProperties
from gda.jython import ScriptBase
from uk.ac.gda.beans.exafs.i20 import CryostatParameters, CryostatProperties
from uk.ac.gda.doe import DOEUtils
from org.slf4j import LoggerFactory

class SampleIterator(object):
    '''
    The interface.
    '''
    def setSampleBean(self, sampleBean):
        '''
        Setter to give the I20SampleParameters bean it will iterate over
        '''
    def getNumberOfRepeats(self):
        '''
        Return the total number of scans defined by the bean held by this object
        '''
        raise NotImplementedError("Should have implemented this")
    
    def moveToNext(self):
        '''
        Iterator method. Should be called at each point in the loop to move the sample environment on to the next point in its loop
        '''
        raise NotImplementedError("Should have implemented this")
    
    def resetIterator(self):
        '''
        Do not operate hardware, but move the iteration back to the start. To be used when there is an external loop to the sample environment.
        '''
        raise NotImplementedError("Should have implemented this")
    
    def getNextSampleName(self):
        '''
        Based on the current position of the iterator AFTER a call to moveToNext, what is the name of the next sample to be run
        '''
        raise NotImplementedError("Should have implemented this")
        
    def getNextSampleDescriptions(self):
        '''
        Based on the current position of the iterator AFTER a call to moveToNext, what are the same details (descriptions) of the next sample to be run
        '''
        raise NotImplementedError("Should have implemented this")
    
    def log(self,*msg):
        self.logger = LoggerFactory.getLogger(str(self.__class__))
        message = ""
        for part in msg:
            message += str(part) + " "
        print message
        self.logger.info(message)


class I20_SingleSample_Iterator(SampleIterator):
    
    def __init__(self):
        pass
    
    def setSampleBean(self, sampleBean):
        self.sampleBean = sampleBean

    def getNumberOfRepeats(self):
        return 1
    
    def moveToNext(self):
        pass
    
    def resetIterator(self):
        pass
    
    def getNextSampleName(self):
        return self.sampleBean.getName()
        
    def getNextSampleDescriptions(self):
        return self.sampleBean.getDescriptions()
   

class XASXANES_Roomtemp_Iterator(SampleIterator):
    
    def __init__(self, sample_x,sample_y,sample_z,sample_rot,sample_roll,sample_pitch):
        self.increment = 1
        self.sample_x = sample_x
        self.sample_y = sample_y
        self.sample_z = sample_z
        self.sample_rot = sample_rot
        self.sample_roll = sample_roll
        self.sample_pitch = sample_pitch
    
    def setSampleBean(self, sampleBean):
        self.sampleBean = sampleBean.getRoomTemperatureParameters()
        
    def getNumberOfRepeats(self):
        if self.sampleBean == None:
            raise UnboundLocalError("Sample bean has not been defined!")
        num_repeats = 0
        for i in range(0,len(self.sampleBean)):
            num_repeats += self.sampleBean.get(i).getNumberOfRepetitions()
        return num_repeats
    
    def resetIterator(self):
        self.increment = 0
    
    def getNextSampleName(self):
        return self.samplename
        
    def getNextSampleDescriptions(self):
        return self.descriptions
    
    def moveToNext(self):
        i = self._determineSample()
        sample = self.sampleBean.get(i)
        
        samXEnabled = sample.isSamXEnabled();
        samYEnabled = sample.isSamYEnabled();
        samZEnabled = sample.isSamZEnabled();
        rotEnabled = sample.isRotEnabled();
        rollEnabled = sample.isRollEnabled();
        pitchEnabled = sample.isPitchEnabled();
        fineRotEnabled = sample.isFineRotEnabled();
        
        print "samXEnabled ", samXEnabled
        print "samYEnabled ", samYEnabled
        print "samZEnabled ", samZEnabled
        print "rotEnabled ", rotEnabled
        print "rollEnabled ", rollEnabled
        print "pitchEnabled ", pitchEnabled
        print "fineRotEnabled ", fineRotEnabled
        
        x = sample.getSample_x()
        y = sample.getSample_y()
        z = sample.getSample_z()
        rotation = sample.getSample_rotation()
        roll = sample.getSample_roll()
        pitch = sample.getSample_pitch()
        samplename = sample.getSample_name()
        sampledescription = sample.getSample_description()
        sample_repeats = sample.getNumberOfRepetitions()
        self.log("Running sample:",samplename) 
        if self.sample_x == None or self.sample_y ==None or self.sample_z == None or self.sample_rot == None or self.sample_roll == None or self.sample_pitch == None:
            raise DeviceException("I20 scan script - could not find all sample stage motors!")
        self.log( "Moving sample stage to",x,y,z,rotation,roll,pitch,"...")
        
        if samXEnabled == True:
            self.sample_x.asynchronousMoveTo(x)
            
        if samYEnabled == True:
            self.sample_y.asynchronousMoveTo(y)
        
        if samZEnabled == True:
            self.sample_z.asynchronousMoveTo(z)
        
        if rotEnabled == True:
            self.sample_rot.asynchronousMoveTo(rotation)
        
        self.sample_x.waitWhileBusy()
        self.sample_y.waitWhileBusy()
        self.sample_z.waitWhileBusy()
        self.sample_rot.waitWhileBusy()
        self.log( "Sample stage move complete.")
        ScriptBase.checkForPauses()
        self.samplename = samplename
        self.descriptions = [sampledescription]
        # only increment if all successful
        self.increment += 1
        return

    def _determineSample(self):
        if self.increment == 0:
            return 0
        repeatsSoFar = 0
        for i in range(0,len(self.sampleBean)):
            repeatsSoFar += self.sampleBean.get(i).getNumberOfRepetitions()
            if repeatsSoFar > self.increment:
                return i
        return len(self.sampleBean) - 1
        
        
class XES_Roomtemp_Iterator(XASXANES_Roomtemp_Iterator):

    def __init__(self, sample_x,sample_y,sample_z,sample_rot,sample_fine_rot,sample_roll,sample_pitch):
        self.increment = 1
        self.sample_x = sample_x
        self.sample_y = sample_y
        self.sample_z = sample_z
        self.sample_rot = sample_rot
        self.sample_roll = sample_roll
        self.sample_pitch = sample_pitch
        self.sample_fine_rot = sample_fine_rot
        
    def getNextSampleName(self):
        return self.samplename
        
    def getNextSampleDescriptions(self):
        return self.descriptions
    
    def moveToNext(self):
        i = self._determineSample()
        sample = self.sampleBean.get(i)
        
        samXEnabled = sample.isSamXEnabled();
        samYEnabled = sample.isSamYEnabled();
        samZEnabled = sample.isSamZEnabled();
        rotEnabled = sample.isRotEnabled();
        rollEnabled = sample.isRollEnabled();
        pitchEnabled = sample.isPitchEnabled();
        fineRotEnabled = sample.isFineRotEnabled();
        
        print "samXEnabled ", samXEnabled
        print "samYEnabled ", samYEnabled
        print "samZEnabled ", samZEnabled
        print "rotEnabled ", rotEnabled
        print "rollEnabled ", rollEnabled
        print "pitchEnabled ", pitchEnabled
        print "fineRotEnabled ", fineRotEnabled
        
        
        i = self._determineSample()
        x = sample.getSample_x()
        y = sample.getSample_y()
        z = sample.getSample_z()
        rotation = sample.getSample_rotation()
        finerotation = sample.getSample_finerotation()
        samplename = sample.getSample_name()
        sampledescription = sample.getSample_description()
        sample_repeats = sample.getNumberOfRepetitions()
        self.log("Running sample:",samplename) # +1 as the user will think the first sample is 1 not 0
        if self.sample_x == None or self.sample_y ==None or self.sample_z == None or self.sample_rot == None or self.sample_fine_rot == None:
            raise DeviceException("I20 scan script - could not find all sample stage motors!")
        self.log( "Moving sample stage to",x,y,z,rotation,finerotation,"...")
        self.sample_x.asynchronousMoveTo(x)
        self.sample_y.asynchronousMoveTo(y)
        self.sample_z.asynchronousMoveTo(z)
        self.sample_rot.asynchronousMoveTo(rotation)
        self.sample_x.waitWhileBusy()
        self.sample_y.waitWhileBusy()
        self.sample_z.waitWhileBusy()
        self.sample_rot.waitWhileBusy()
        self.log( "Sample stage move complete.\n")
        ScriptBase.checkForPauses()
        self.samplename = samplename
        self.descriptions = [sampledescription]
        # only increment if all successful
        self.increment += 1
        return


class XASXANES_Cryostat_Iterator(SampleIterator):
    
    def __init__(self, cryostat, cryostick_pos):
        self.sample_increment = 0
        self.temp_increment = 0
        self.cryostat = cryostat
        self.cryostick_pos = cryostick_pos
    
    def setParameters(self, controlMode, heaterRange, waitTime, manualOutput, p, i, d, tolerance, cryostatParameters):
        self.controlMode = controlMode
        self.heaterRange = heaterRange
        self.waitTime = waitTime
        self.manualOutput = manualOutput
        self.p = p
        self.i = i
        self.d = d
        self.tolerance = tolerance
        self.cryostatParameters = cryostatParameters
    
    def determineLoopSampleFirst(self):
        self.loopSampleFirst = self.cryostatParameters.getLoopChoice() == CryostatProperties.LOOP_OPTION[0]
    
    def determineTemperaturesArray(self):
        self.temperatures_array = [self.cryostatParameters.getTemperature()]
        
    def determineTemperatureNum(self):
        self.temperatures_num = len(self.temperatures_array)
    
    def setSampleBean(self, sampleBean):
        if DOEUtils.isRange(self.cryostatParameters.getTemperature(),None):
            self.temperatures_array = DOEUtils.expand(self.cryostatParameters.getTemperature(),None)
        elif DOEUtils.isList(self.cryostatParameters.getTemperature(),None):
            self.temperatures_array = self.cryostatParameters.getTemperature().split(",")
        self._determineSamplesArray()
        self.samples_num = len(self.samples_array)
        
    def _determineSamplesArray(self):
        self.samples_array = []
        for i in range(0,len(self.cryostatParameters.getSamples())):
            for j in range(0,self.cryostatParameters.getSamples().get(i).getNumberOfRepetitions()):
                self.samples_array += [self.cryostatParameters.getSamples().get(i)]

    def getNumberOfRepeats(self):
        if self.cryostatParameters == None:
            raise UnboundLocalError("Sample bean has not been defined!")
        return self.temperatures_num * self.samples_num
    
    def resetIterator(self):
        self.sample_increment = 0
        self.temp_increment = 0
    
    def getNextSampleName(self):
        return self.samplename
        
    def getNextSampleDescriptions(self):
        return self.descriptions

    def moveToNext(self):
        self.cryostat.setup(self.controlMode, self.heaterRange, self.waitTime, self.manualOutput, self.p, self.i, self.d, self.tolerance)
        y = self.samples_array[self.sample_increment].getPosition()
        name = self.samples_array[self.sample_increment].getSample_name()
        desc = self.samples_array[self.sample_increment].getSampleDescription()
        sample_repeats = self.samples_array[self.sample_increment].getNumberOfRepetitions()
        self.log( "Using sample",name,"in next iteration.")
        self.cryostat.setTempSelect(1)
        self.log( "Moving cryostick_pos to ",self.cryostick_pos)
        self.cryostick_pos.asynchronousMoveTo(y)
        self.samplename = name
        self.descriptions = [desc]
        temp = self.temperatures_array[self.temp_increment]
        self.log( "Setting cryostat to",str(temp),"K...")
        self.cryostat.asynchronousMoveTo(temp)
        self.log("Waiting for cryostick_pos to move")
        self.cryostick_pos.waitWhileBusy()
        self.log("cryostick_pos move complete.")
        ScriptBase.checkForPauses()
        self.log("Waiting for Cryostat to set temperature")
        self.cryostat.waitWhileBusy()
        self.log( "Cryostat temperature change complete.")
        ScriptBase.checkForPauses()
        
        # loop over sampleBean then temperature
        if self.loopSampleFirst:
            if self.sample_increment < self.samples_num -1:
                self.sample_increment+=1
                return
            else:
                self.sample_increment = 0
                self.temp_increment +=1
                return
        else:
            if self.temp_increment < self.temperatures_num -1:
                self.temp_increment += 1
                return
            else:
                self.temp_increment = 0
                self.sample_increment += 1
                return