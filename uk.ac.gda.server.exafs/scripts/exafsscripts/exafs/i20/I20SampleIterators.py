#
# Series of iterator classes for I20's different sample environment options 
#
from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.jython import ScriptBase
from uk.ac.gda.beans.exafs.i20 import CryostatParameters
from uk.ac.gda.doe import DOEUtils
from org.slf4j import LoggerFactory

class SampleIterator(object):
    '''
    The interface.
    '''
    def setBeanGroup(self, beanGroup):
        '''
        Setter to give this object the BeanGroup object which includes the I20SampleParameters bean
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
    
    def log(self,*msg):
        self.logger = LoggerFactory.getLogger(str(self.__class__))
        message = ""
        for part in msg:
            message += str(part) + " "
        print message
        self.logger.info(message)

    
    
class XASXANES_Roomtemp_Iterator(SampleIterator):
    
    def __init__(self):
        self.increment = 1
        self.finder = Finder.getInstance()
    
    def setBeanGroup(self, beanGroup):
        self.group = beanGroup
        self.samples = beanGroup.getSample().getRoomTemperatureParameters()
        
    def getNumberOfRepeats(self):
        if self.group == None:
            raise UnboundLocalError("Sample bean has not been defined!")
        
        num_repeats = 0
        for i in range(0,len(self.samples)):
            num_repeats += self.samples.get(i).getNumberOfRepetitions()
        return num_repeats
    
    def resetIterator(self):
        self.increment = 1
    
    def moveToNext(self):
        
            i = self._determineSample()
        
            x = self.samples.get(i).getSample_x()
            y = self.samples.get(i).getSample_y()
            z = self.samples.get(i).getSample_z()
            rotation = self.samples.get(i).getSample_rotation()
            roll = self.samples.get(i).getSample_roll()
            pitch = self.samples.get(i).getSample_pitch()
            samplename = self.samples.get(i).getSample_name()
            sampledescription = self.samples.get(i).getSample_description()
            sample_repeats = self.samples.get(i).getNumberOfRepetitions()
            
            samx = self.finder.find("sample_x")
            samy = self.finder.find("sample_y")
            samz = self.finder.find("sample_z")
            samrot = self.finder.find("sample_rot")
            samroll = self.finder.find("sample_roll")
            sampitch = self.finder.find("sample_pitch")
            
            if samx == None or samy ==None or samz == None or samrot == None or samroll == None or sampitch == None:
                raise DeviceException("I20 scan script - could not find all sample stage motors!")
            
            self.log( "Moving sample stage to",x,y,z,rotation,roll,pitch,"...")
            samx.asynchronousMoveTo(x)
            samy.asynchronousMoveTo(y)
            samz.asynchronousMoveTo(z)
            samrot.asynchronousMoveTo(rotation)
            samroll.asynchronousMoveTo(roll)
            sampitch.asynchronousMoveTo(pitch)
            samx.waitWhileBusy()
            samy.waitWhileBusy()
            samz.waitWhileBusy()
            samrot.waitWhileBusy()
            samroll.waitWhileBusy()
            sampitch.waitWhileBusy()
            self.log( "Sample stage move complete.\n")
            ScriptBase.checkForPauses()
            
            # change the strings in the filewriter so that the ascii filename changes
            self.group.getSample().setName(samplename)
            self.group.getSample().setDescriptions([sampledescription])
            
            # only increment if all successful
            self.increment += 1
            return
        
    def _determineSample(self):
        
        if self.increment == 1:
            return 0
        
        repeatsSoFar = 0
        for i in range(0,len(self.samples)):
            repeatsSoFar += self.samples.get(i).getNumberOfRepetitions()
            if repeatsSoFar > self.increment:
                return i
        return len(self.samples) - 1
        
        
class XES_Roomtemp_Iterator(XASXANES_Roomtemp_Iterator):

    def moveToNext(self):

            i = self.increment
        
            x = self.samples.get(i).getSample_x()
            y = self.samples.get(i).getSample_y()
            z = self.samples.get(i).getSample_z()
            rotation = self.samples.get(i).getSample_rotation()
            finerotation = samples.get(i).getSample_finerotation()
            samplename = self.samples.get(i).getSample_name()
            sampledescription = self.samples.get(i).getSample_description()
            sample_repeats = self.samples.get(i).getNumberOfRepetitions()
            
            samx = self.finder.find("sample_x")
            samy = self.finder.find("sample_y")
            samz = self.finder.find("sample_z")
            samrot = self.finder.find("sample_rot")
            samfinerot = self.finder.find("sample_fine_rot")
            
            if samx == None or samy ==None or samz == None or samrot == None or samfinerot == None:
                raise DeviceException("I20 scan script - could not find all sample stage motors!")
            
            print "********"
            self.log( "Moving sample stage to",x,y,z,rotation,roll,pitch,"...")
            samx.asynchronousMoveTo(x)
            samy.asynchronousMoveTo(y)
            samz.asynchronousMoveTo(z)
            samrot.asynchronousMoveTo(rotation)
            samfinerot.asynchronousMoveTo(finerotation)
            samx.waitWhileBusy()
            samy.waitWhileBusy()
            samz.waitWhileBusy()
            samrot.waitWhileBusy()
            samfinerot.waitWhileBusy()
            self.log( "Sample stage move complete.\n")
            ScriptBase.checkForPauses()
            
            # change the strings in the filewriter so that the ascii filename changes
            self.group.getSample().setName(samplename)
            self.group.getSample().setDescriptions([sampledescription])
            
            # only increment if all successful
            self.increment += 1
            return

class XASXANES_Cryostat_Iterator(SampleIterator):
    
    def __init__(self):
        self.sample_increment = 0
        self.temp_increment = 0
        self.finder = Finder.getInstance()
    
    def setBeanGroup(self, beanGroup):
        self.group = beanGroup
        self.params = beanGroup.getSample().getCryostatParameters()
        
        self.loopSampleFirst = self.params.getLoopChoice() == CryostatParameters.LOOP_OPTION[0]
        
        self.temperatures_array = [self.params.getTemperature()]
        if DOEUtils.isRange(self.params.getTemperature(),None):
            self.temperatures_array = DOEUtils.expand(self.params.getTemperature(),None)
        self.temperatures_num = len(self.temperatures_array)
        
        self.samples_array = self.params.getSamples()
        self.samples_num = len(self.samples_array)
        
        self.cryostat_scannable = self.finder.find("cryostat")

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
        
        y = self.samples_array.get(self.sample_increment).getPosition()
        finepos = self.samples_array.get(self.sample_increment).getFinePosition()
        name = self.samples_array.get(self.sample_increment).getSample_name()
        desc = self.samples_array.get(self.sample_increment).getSampleDescription()
        sample_repeats = self.samples_array.get(self.sample_increment).getNumberOfRepetitions()
        
        samy = self.finder.find("sample_y")
        sam_fine_pos = self.finder.find("sample_fine_rot")
        
        print "**********"
        self.log( "Setting sample",name,"to next iteration.")
        
        self.log( "Moving sample stage to",y,finepos,"...")
        samy.asynchronousMoveTo(y)
        sam_fine_pos.asynchronousMoveTo(finepos)        
        self.group.getSample().setName(name)
        self.group.getSample().setDescriptions([desc])

        temp = self.temperatures_array[self.temp_increment]
        self.log( "Setting cryostat to",str(temp),"K...")
        self.cryostat_scannable.asynchronousMoveTo(temp)
        
        samy.waitWhileBusy()
        sam_fine_pos.waitWhileBusy()
        self.log("Sample stage move complete.")
        ScriptBase.checkForPauses()
        
        self.cryostat_scannable.waitWhileBusy()
        self.log( "Cryostat temperature change complete.")
        ScriptBase.checkForPauses()
            
        # loop over samples then temperature
        if self.loopSampleFirst:
            ## increment the iterators
            if self.temp_increment < self.temperatures_num -1:
                self.temp_increment += 1
                return
            else:
                self.temp_increment = 0
                self.sample_increment += 1
                return
        
        else:
            if self.sample_increment < self.samples_num -1:
                self.sample_increment+=1
                return
            else:
                self.sample_increment = 0
                self.temp_increment +=1
                return

        

