#
# Series of iterator classes for I20's different sample environment options 
#
from gda.configuration.properties import LocalProperties
from gda.jython import ScriptBase
from uk.ac.gda.beans.exafs.i20 import CryostatParameters
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

    def getNextSampleName(self):
        return self.samplename
        
    def getNextSampleDescriptions(self):
        return self.descriptions


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
    
    def moveToNext(self):
        i = self._determineSample()
        x = self.sampleBean.get(i).getSample_x()
        y = self.sampleBean.get(i).getSample_y()
        z = self.sampleBean.get(i).getSample_z()
        rotation = self.sampleBean.get(i).getSample_rotation()
        roll = self.sampleBean.get(i).getSample_roll()
        pitch = self.sampleBean.get(i).getSample_pitch()
        samplename = self.sampleBean.get(i).getSample_name()
        sampledescription = self.sampleBean.get(i).getSample_description()
        sample_repeats = self.sampleBean.get(i).getNumberOfRepetitions()
        print "********"
        self.log("Running sample:",samplename) # +1 as the user will think the first sample is 1 not 0
        
        if self.sample_x == None or self.sample_y ==None or self.sample_z == None or self.sample_rot == None or self.sample_roll == None or self.sample_pitch == None:
            raise DeviceException("I20 scan script - could not find all sample stage motors!")
        
        self.log( "Moving sample stage to",x,y,z,rotation,roll,pitch,"...")
        self.sample_x.asynchronousMoveTo(x)
        self.sample_y.asynchronousMoveTo(y)
        self.sample_z.asynchronousMoveTo(z)
        # TODO remove comments when motors are fixed: they were all in an error state during the shutdown
        #self.sample_rot.asynchronousMoveTo(rotation)
        #self.sample_roll.asynchronousMoveTo(roll)
        #self.sample_pitch.asynchronousMoveTo(pitch)
        self.sample_x.waitWhileBusy()
        self.sample_y.waitWhileBusy()
        self.sample_z.waitWhileBusy()
        #self.sample_rot.waitWhileBusy()
        #self.sample_roll.waitWhileBusy()
        #self.sample_pitch.waitWhileBusy()
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
        
    def moveToNext(self):

            i = self._determineSample()
        
            x = self.sampleBean.get(i).getSample_x()
            y = self.sampleBean.get(i).getSample_y()
            z = self.sampleBean.get(i).getSample_z()
            rotation = self.sampleBean.get(i).getSample_rotation()
            finerotation = self.sampleBean.get(i).getSample_finerotation()
            samplename = self.sampleBean.get(i).getSample_name()
            sampledescription = self.sampleBean.get(i).getSample_description()
            sample_repeats = self.sampleBean.get(i).getNumberOfRepetitions()
            
            if self.sample_x == None or self.sample_y ==None or self.sample_z == None or self.sample_rot == None or samfinerot == None:
                raise DeviceException("I20 scan script - could not find all sample stage motors!")
            
            print "********"
            self.log( "Moving sample stage to",x,y,z,rotation,finerotation,"...")
            self.sample_x.asynchronousMoveTo(x)
            self.sample_y.asynchronousMoveTo(y)
            self.sample_z.asynchronousMoveTo(z)
        # TODO remove comments when motors are fixed: they were all in an error state during the shutdown
            #self.sample_rot.asynchronousMoveTo(rotation)
            samfinerot.asynchronousMoveTo(finerotation)
            self.sample_x.waitWhileBusy()
            self.sample_y.waitWhileBusy()
            self.sample_z.waitWhileBusy()
            #self.sample_rot.waitWhileBusy()
            samfinerot.waitWhileBusy()
            self.log( "Sample stage move complete.\n")
            ScriptBase.checkForPauses()
            
            self.samplename = samplename
            self.descriptions = [sampledescription]
            
            # only increment if all successful
            self.increment += 1
            return



class XASXANES_Cryostat_Iterator(SampleIterator):
    
    def __init__(self, cryostat_scannable, sample_y, sample_fine_rot):
        self.sample_increment = 0
        self.temp_increment = 0
        self.cryostat_scannable = cryostat_scannable
        self.sample_y=sample_y
        self.sample_fine_rot=sample_fine_rot
    
    def setSampleBean(self, sampleBean):
        self.params = sampleBean.getCryostatParameters()
        
        self.loopSampleFirst = self.params.getLoopChoice() == CryostatParameters.LOOP_OPTION[0]
        
        self.temperatures_array = [self.params.getTemperature()]
        if DOEUtils.isRange(self.params.getTemperature(),None):
            self.temperatures_array = DOEUtils.expand(self.params.getTemperature(),None)
        elif DOEUtils.isList(self.params.getTemperature(),None):
            self.temperatures_array = self.params.getTemperature().split(",")
        self.temperatures_num = len(self.temperatures_array)
        
        self._determineSamplesArray()
        print self.samples_array
        self.samples_num = len(self.samples_array)
        
    def _determineSamplesArray(self):
        self.samples_array = []
        for i in range(0,len(self.params.getSamples())):
            for j in range(0,self.params.getSamples().get(i).getNumberOfRepetitions()):
                self.samples_array += [self.params.getSamples().get(i)]

    def getNumberOfRepeats(self):
        if self.params == None:
            raise UnboundLocalError("Sample bean has not been defined!")
        
        return self.temperatures_num * self.samples_num
    
    def resetIterator(self):
        self.sample_increment = 0
        self.temp_increment = 0
    
    def _configureCryostat(self, cryoStatParameters):
        if LocalProperties.get("gda.mode") != 'dummy':
            self.cryostat_scannable.setupFromBean(cryoStatParameters)

    def moveToNext(self):
        
        self._configureCryostat(self.params)
        
        y = self.samples_array[self.sample_increment].getPosition()
        finepos = self.samples_array[self.sample_increment].getFinePosition()
        name = self.samples_array[self.sample_increment].getSample_name()
        desc = self.samples_array[self.sample_increment].getSampleDescription()
        sample_repeats = self.samples_array[self.sample_increment].getNumberOfRepetitions()
        
        print "**********"
        self.log( "Using sample",name,"in next iteration.")
        
        self.log( "Moving sample stage to",y,finepos,"...")
        self.sample_y.asynchronousMoveTo(y)
        self.sam_fine_pos.asynchronousMoveTo(finepos) 
        self.samplename = name
        self.descriptions = [desc]

        temp = self.temperatures_array[self.temp_increment]
#        self.log( "Setting cryostat to",str(temp),"K...")
#        self.cryostat_scannable.asynchronousMoveTo(temp)
        self.log( "Would set cryostat to",str(temp),"K but I wont until the Scannable has been tested.")
        
        self.sample_y.waitWhileBusy()
        self.sam_fine_pos.waitWhileBusy()
        self.log("Sample stage move complete.")
        ScriptBase.checkForPauses()
        
        self.cryostat_scannable.waitWhileBusy()
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
