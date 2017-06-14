'''
Created on April 28th, 2017

@author: nathan
'''
from uk.ac.gda.devices.hplc.beans import HplcSessionBean
from gda.commandqueue import JythonScriptProgressProvider
from gdascripts.pd.epics_pds import DisplayEpicsPVClass, SingleEpicsPositionerClass
from gda.data.metadata import GDAMetadataProvider
from gda.scan import StaticScan
from tfgsetup import fs
from time import sleep
import logging
import gda.factory.Finder
import sys
from cStringIO import StringIO
from math import ceil
from datetime import datetime


class HPLC(object):
    """Handles collection of HPLC SAXS data in GDA/Jython

    The class is called via the 'Queue Experiment' button on the
    <filename.hplc> xml file in the HPLC setup perspective in GDA.
    The class has various functions to check shutters etc on the 
    beamline as well as measuring the data.
    """
    
    def __init__(self, filename):
        self.__version__ = '1.00'
        self.hplcFile = filename
        self.bean = HplcSessionBean.createFromXML(filename)
        finder = gda.factory.Finder.getInstance()
        find = finder.find
        self.shutter = find('shutter')
        self.sample_type = find('sample_type')
        self.sampleName = find("samplename")
        self.environment = find("sample_environment")
        self.tfg = finder.listAllLocalObjects("gda.device.Timer")[0]
        self.ncddetectors = finder.listAllLocalObjects("uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem")[0]
        #CREATE A LOGGER
        self.logger = logging.getLogger('HPLC')
        self.logger.setLevel(logging.INFO)
        formatter = logging.Formatter('%(asctime)s: %(levelname)s: %(module)s: %(message)s',"[%Y-%m-%d %H:%M:%S]")
        streamhandler = logging.StreamHandler()
        streamhandler.setFormatter(formatter)
        if len(self.logger.handlers) == 0:
            self.logger.addHandler(streamhandler)
        self.logger.info('HPLC class version '+self.__version__+' was instantiated')
        
    def setupTfg(self, frames, readout_time, tpf):
        self.tfg.clearFrameSets()
        self.tfg.addFrameSet(frames, readout_time * 1000, tpf * 1000, int('00000100', 2), int('11111111', 2), 0, 0)#note, byte order is reversed!
        self.tfg.loadFrameSets()
        return frames * (tpf + readout_time)
    
    def setTitle(self, title):
        GDAMetadataProvider.getInstance().setMetadataValue("title", title)
        
    def setSampleType(self, type='sample'):
        if type in ['sample', 'sample+buffer', 'buffer']:
            self.sample_type(type)
        else:
            self.logger.error('Sample type should be one of: sample, sample+buffer, buffer.')
            
    def setEnvironment(self, type='HPLC'):
        if type in ['BSSC', 'HPLC', 'Manual']:
            self.environment(type)
        else:
            self.logger.error('Environment type should be one of: HPLC, BSSC, Manual.')

    def armFastValve(self):
        try:
            fv1.getPosition()
        except:
            fv1 = SingleEpicsPositionerClass('fv1', 'BL21B-VA-FVALV-01:CON', 'BL21B-VA-FVALV-01:STA', 'BL21B-VA-FVALV-01:STA', 'BL21B-VA-FVALV-01:CON', 'mm', '%d')
        if not fv1.getPosition() == 3.0:
            fv1(3.0)


    def getSearchStatus(self):
        try:
            eh_search_status.getPosition()
        except:
            eh_search_status=DisplayEpicsPVClass('eh_search_status', 'BL21B-PS-IOC-01:M11:LOP', 'units', '%d')

        if eh_search_status.getPosition() != 0.0:
            return False
        else:
            return True

    def getSafetyShutter(self):
        if self.shutter.getPosition() == 'Open':
            return True
        else:
            return False

    def setSafetyShutter(self, command='Open'):
        levels = ['Open', 'Close']
        if command in levels:
            self.shutter(command)
            self.logger.info('Setting safety shutter to '+command)
        else:
            self.logger.error("setSafetyShutter function requires either 'Open' or 'Close' as input")

            
    def getHplcValve(self):
        try:
            hplcvalve_status.getPosition()
        except:
            hplcvalve_status=DisplayEpicsPVClass('hplcvalve_status', 'BL21B-EA-HPLC-01:MOD1:VALVE:CTRL', 'units', '%d')
        if hplcvalve_status.getPosition() == 1.0:
            return True
        else:
            return False
                                  
    def getFastShutter(self):
        old_stdout = sys.stdout
        sys.stdout = mystdout = StringIO()
        fs()
        sys.stdout = old_stdout
        if mystdout.getvalue() == 'fs: Open\n':
            self.logger.info('Fast shutter is open')
            return True
        else:
            self.logger.info('Fast shutter is closed')
            return False

    def setFastShutter(self, command='Open'):
        if command in ['Open', 'Close']:
            self.logger.info('Setting fast shutter to '+command)
            fs(command)
            
        else:
            self.logger.error('setFastValve function requires either Close or Open as input')

    def getInjectSignal(self):
        cutoff = 3.0
        signal = 3.05
        try:
            signal = inject_signal.getPosition()
        except:
            inject_signal=DisplayEpicsPVClass('inject_signal', 'BL21B-EA-ENV-01:HPLC:TRIG', 'units', '%.3e')
            signal = inject_signal.getPosition()
            
        if signal < cutoff:
            return True
        else:
            return False
            
        

    def run(self, processing=True):
        try:
            if not self.getSafetyShutter():
                if self.getSearchStatus():
                    #self.armFastValve()
                    self.setSafetyShutter('Open')
                else:
                    self.logger.error('Search the hutch you crazy fool!')
                    raise EnvironmentError('The script terminated early because the hutch is not searched.')
                
            if not self.getHplcValve():
                self.logger.error('The HPLC static/flow valve is in static position, this may be caused by a loss of vacuum in the sample section.')
                raise EnvironmentError('The script terminated early because the HPLC static/flow valve is in the static position')
            
            if self.getFastShutter():
                self.logger.info('Fast shutter was open, closing it so the tfg can take care of it during collection')
                self.setFastShutter('Close')
                
            self.setEnvironment('HPLC')
            self.setSampleType('sample+buffer')

            for i, b in enumerate(self.bean.measurements):
                self.logger.info('---- STARTING RUN '+str(i)+' of '+str(len(self.bean.measurements))+': SAMPLE: '+b.getSampleName()+' ----')
                readout_time = 0.1
                exposure_time = b.getTimePerFrame()
                pre_run_delay = 120
                
                #GET THE RUN TIME
                try:
                    runtime = int(b.getComment())
                except:
                    self.logger.error('The run time of the experiment is set in the comment box, must be an integer and is in minutes, using 30 mins as a default')
                    runtime = 30
                number_of_images = int(ceil(runtime * 60.0 / ( readout_time + exposure_time)))
                
                #Set up run parameters
                self.setupTfg(number_of_images, readout_time, exposure_time)
                self.setTitle(b.getSampleName())
                
                #Get the inject signal
                self.logger.info('Waiting for injection signal from HPLC')
                starttime = datetime.now()
                elapsed_time = 0
                found_signal = False
                while elapsed_time < pre_run_delay:
                    if self.getInjectSignal():
                        self.logger.info('Found injection signal')
                        found_signal = True
                        break
                    else:
                        elapsed_time = (datetime.now() - starttime).seconds
                    sleep(0.1)
                if not found_signal:
                    self.logger.info('Did not find the inject signal, will proceed anyway.')
                    
                #Start the data collection
                self.logger.info('Starting collection of '+str(number_of_images)+'x'+str(exposure_time)+' second exposures')
                StaticScan([self.ncddetectors]).run()
                self.logger.info('Finished collecting '+b.getSampleName())
            self.setSafetyShutter('Close')
            self.logger.info('SCRIPT FINISHED NORMALLY')
            #namespace:
            #b.getLocation().getRow()
            #b.getLocation().getColumn()
            #b.getSampleName()
            #b.getConcentration()
            #b.getMolecularWeight()
            #b.getTimePerFrame()
            #b.getBuffers()
            #b.getComment()
            #b.getVisit()
            #b.getUsername()
            #JythonScriptProgressProvider.sendProgress(100*i/float(len(self.bean.measurements)))
        except:
            self.logger.error('SCRIPT TERMINATED IN ERROR')

