'''
Class to configure the Excalibur IOCs ready for use in GDA
usage:

import excalibur_config
configurator=excalibur_config.ExcaliburConfigurator(fix=False, gap=True, arr=False, master=True, hdf5=False, phdf5=True)
configurator.setup()

'''
import sys
from gda.factory import Finder
from gdascripts.messages import handle_messages
from gda.scan import RepeatScan
import recordConfig
from gda.configuration.properties import LocalProperties
from gdascripts.parameters import beamline_parameters

def _getTestPattern(on=True):
    if not on:
        return [0]*65536
    testPulse = []
    filepath = LocalProperties.getVarDir() + "excalibur_test_triangle.mask"
#    handle_messages.log(None,"Loading test mask from " + `filepath`)
    f = open(filepath)
    for line in iter(f):
        row = line.split()
        row = [(1 - int(val)) for val in row]
        testPulse.extend(row)
    f.close()    
    return testPulse
    
def loadTestPattern(on=True):
    if on:
        handle_messages.log(None,"Loading test pattern...")
    else:
        handle_messages.log(None,"Clearing test pattern...")
    testPulse=_getTestPattern(on)
    config= Finder.getInstance().find("excalibur_config")
    nodes = config.get("readoutFems")
    for node in nodes:
        for j in range(8):
            handle_messages.log(None,"...chip "+ `j+1`)
            node.getIndexedMpxiiiChipReg(j).pixel.setTest(testPulse)
            node.getIndexedMpxiiiChipReg(j).loadPixelConfig()
    if on:
        handle_messages.log(None,"Loading test pattern done")
    else:
        handle_messages.log(None,"Clearing test pattern done")
    

import csv
def get_mask_from_csvfile(file_path):
    d=[]
    csvfile= open(file_path)
    rdr=csv.reader(csvfile)
    for row in rdr:
        irow=[]
        for r in row:
            irow.append(int(r))
        d=d+irow
    return d

def loadMask(mask=[0]*3709717): #size of ACQUIRE:PixelMask
    handle_messages.log(None,"Loading mask...")
    config= Finder.getInstance().find("excalibur_config")
    configAdBase = config.get("configAdBase")
    configAdBase.setPixelMask(mask)
    handle_messages.log(None,"Loading mask done")

    
def setOperationModeToNormal(dev):
    handle_messages.log(None,"force operation mode to normal")
    dev.set("1:FEM:OperationMode", 0) #normal
    dev.set("2:FEM:OperationMode", 0) #normal
    dev.set("3:FEM:OperationMode", 0) #normal
    dev.set("4:FEM:OperationMode", 0) #normal
    dev.set("5:FEM:OperationMode", 0) #normal
    dev.set("6:FEM:OperationMode", 0) #normal
    dev.set("CONFIG:ACQUIRE:OperationMode", 0) #normal
    
def runContinuous(exposureTime):
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    setOperationModeToNormal(dev)
    dev.set("CONFIG:ACQUIRE:AcquireTime", exposureTime)
    dev.set("CONFIG:ACQUIRE:AcquirePeriod", exposureTime)
    dev.set("CONFIG:ACQUIRE:ImageMode", 2) #continuous
    dev.set("CONFIG:ACQUIRE:Acquire", 1) #start

import time
def checkFEMInitOK(dev):
    handle_messages.log(None,"Waiting for FEMs to initalise...")
    for i in range(50):
        ok=True
        ok= ok and dev.getInteger("1:HK:FemInitOk")==1
        ok= ok and dev.getInteger("2:HK:FemInitOk")==1
        ok= ok and dev.getInteger("3:HK:FemInitOk")==1
        ok= ok and dev.getInteger("4:HK:FemInitOk")==1
        ok= ok and dev.getInteger("5:HK:FemInitOk")==1
        ok= ok and dev.getInteger("6:HK:FemInitOk")==1
        if ok:
            return True
        time.sleep(1)
    return False

def loadDefaultConfig():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    config_filepath = LocalProperties.getVarDir() + "excalibur_default_calibration.excaliburconfig"
    handle_messages.log(None,"Loading default config from " + `config_filepath`)
    time.sleep(2) #slow things done to avoid problems
    recordConfig.sendConfigInFileToDetector(config_filepath)
    time.sleep(2) #slow things done to avoid problems
    setOperationModeToNormal(dev) #sendConfigInFileToDetector may have changed readout node states

def setBiasOn():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    handle_messages.log(None,"Turning on HV and ramping to desired level")
    dev.set('1:HK:BIAS_ON_OFF',1) #on
    dev.set('1:HK:BIAS_LEVEL',117.5) 
    if dev.getDouble('1:HK:BIAS_ON_OFF') != 1:
        handle_messages.log(None,"HV is OFF", Raise=True)
        
    #check vmon is ok. Loop until it is ok, then check
    for i in range(50):
        ok = dev.getInteger('1:HK:BIAS_VMON.STAT') == 0
        if ok:
            break
        time.sleep(1)
    if dev.getInteger('1:HK:BIAS_VMON.STAT') != 0:
        handle_messages.log(None,"HV VMON is in alarm after 10s", Raise=True)
        
    handle_messages.log(None,"POWER OK")

def setBiasOff():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    dev.set('1:HK:BIAS_ON_OFF',0) #off
    if dev.getInteger('1:HK:BIAS_ON_OFF') != 0:
        handle_messages.log(None,"HV is ON", Raise=True)
    
    
        
def switch_on():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    
    handle_messages.log(None,"Checking interlocks")
    if dev.getInteger("1:HK:COOLANT_TEMP_STATUS") != 1:
        handle_messages.log(None,"Coolant temp is bad",Raise=True)
    if dev.getInteger("1:HK:HUMIDITY_STATUS") != 1:
        handle_messages.log(None,"Humidity is too high",Raise=True)
    if dev.getInteger("1:HK:COOLANT_FLOW_STATUS") != 1:
        handle_messages.log(None,"Coolant flow is too low",Raise=True)
    if dev.getInteger("1:HK:AIR_TEMP_STATUS") != 1:
        handle_messages.log(None,"Air flow is too high",Raise=True)
    if dev.getInteger("1:HK:FAN_FAULT") != 0:
        handle_messages.log(None,"Fan fault",Raise=True)
        
    handle_messages.log(None,"Setting alarm limits")
    excaliburConfigurator=ExcaliburConfigurator(fix=True)
    excaliburConfigurator.do_hklimits()

    if dev.getInteger('1:HK:HUMIDITY_MON.STAT') != 0:
        handle_messages.log(None,"HUMIDITY MON is in alarm", Raise=True)

    handle_messages.log(None,"Turning on LV")
    
    dev.set('CONFIG:ACQUIRE:LvControl',1) #on
    if dev.getDouble('CONFIG:ACQUIRE:LvControl') != 1:
        handle_messages.log(None,"LV is OFF", Raise=True)
        
    if not checkFEMInitOK(dev):
        handle_messages.log(None,"FEMs did not initialise (FEMInitOK). Check ethernet connections", Raise=True)


    setBiasOn()

    handle_messages.log(None,"Setting up areaDetector plugins ")
    time.sleep(2) #slow things done to avoid problems
    excaliburConfigurator.setupPlugins()

    
    handle_messages.log(None,"take image to initialise HDF5 plugin")
    time.sleep(2) #slow things done to avoid problems
    jns=beamline_parameters.JythonNameSpaceMapping()
    det=jns.excalibur_config_normal
    if det is None:
        handle_messages.log(None,"excalibur_config_normal not found", Raise=True)

    setOperationModeToNormal(dev)
    time.sleep(1)
    handle_messages.log(None,"Starting scan")
    det.pluginList[1].enabled=False #turn off file saving as it will fail due to uninitialised PHDF5 plugin
    try:
        scan=RepeatScan.create_repscan([1, det, .01])
        #scan.runScan()
    finally:
        det.pluginList[1].enabled=True #turn on file saving

    #loadDefaultConfig() #kw 30 June 2015
    dev.set('1:FEM:GainMode',0) #Super High #kw 30 June 2015


    #measureTestPattern() #kw 30 June 2015
    
    handle_messages.log(None,"Switch on done")

def measureTestPattern():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")
    
    loadTestPattern()

    handle_messages.log(None,"Turning on test pulses")
    time.sleep(2) #slow things done to avoid problems
    
    
    dev.set("CONFIG:ACQUIRE:NumTestPulses",4000)
    nodes = config.get("readoutFems")
    for node in nodes:
        for j in range(8):
            node.getIndexedMpxiiiChipReg(j).anper.setTpref(128)
            node.getIndexedMpxiiiChipReg(j).anper.setTprefA(400)
            node.getIndexedMpxiiiChipReg(j).anper.setTprefB(400)
    

    handle_messages.log(None,"Taking a single image - you should see a set of triangles")
    time.sleep(2) #slow things done to avoid problems

    jns=beamline_parameters.JythonNameSpaceMapping()
    det=jns.excalibur_config_normal
    if det is None:
        handle_messages.log(None,"excalibur_config_normal not found", Raise=True)

    setOperationModeToNormal(dev)

    scan=RepeatScan.create_repscan([1, det, 1])
    scan.runScan()
    loadTestPattern(False)
    
def switch_off():
    config= Finder.getInstance().find("excalibur_config")
    dev=config.get("excaliburDev")

    handle_messages.log(None,"Turning power off")
    
    setBiasOff()
    dev.set('CONFIG:ACQUIRE:LvControl',0) #off
    if dev.getInteger('CONFIG:ACQUIRE:LvControl') != 0:
        handle_messages.log(None,"LV is ON", Raise=True)
    handle_messages.log(None,"POWER OFF")

    handle_messages.log(None,"Switch off done")
    

def get_values_from_csvfile(file_path):
    d=[]
    csvfile= open(file_path)
    rdr=csv.reader(csvfile)
    for row in rdr:
        irow=[]
        for r in row:
            irow.append(float(r))
        d.append(irow)
    return d

def set_threshold_from_energy(energy, dryRun=False):
    """ 
    Function to set the threshold0 values for all chips in all readoutNodes
    
    threshold0[readoutNode, chip] = gradients[readoutNode][chip]*energy + offsets[readoutNode][chip]
    
    where offsets and gradients are from from 2 csv files in LocalProperties.getVarDir(), threhold0_energy_offsets.csv and threhold0_energy_gradients.csv
    
    The nth line in a csv file contains the values for the 8 chips in the nth readoutNode
    """
    
    config= Finder.getInstance().find("excalibur_config")
    nodes = config.get("readoutFems")
    offsets= get_values_from_csvfile(LocalProperties.getVarDir() + "threhold0_energy_offsets.csv")
    if dryRun:
        print offsets
    gradients= get_values_from_csvfile(LocalProperties.getVarDir() + "threhold0_energy_gradients.csv")
    if dryRun:
        print gradients
    for readoutNode in range(6):
        for chip in range(8):
            threshold0=int(gradients[readoutNode][chip]*energy + offsets[readoutNode][chip]+.5)
            if dryRun:
                print "%d %d=%d" %(readoutNode, chip, threshold0)
            else:
                nodes[readoutNode].getIndexedMpxiiiChipReg(chip).anper.setThreshold0(threshold0)    


class ExcaliburConfigurator():
    def __init__(self, fix=True, gap=True, arr=False, master=True, hdf5=False, phdf5=True ):
        self.config= Finder.getInstance().find("excalibur_config")
        self.dev=self.config.get("excaliburDev")
        self.configSync=self.config.get("sync")
        self.configFem=self.config.get("fem")
        self.configArr = self.config.get("arr")
        self.configArrBase=self.configArr.getPluginBase()
        self.configFix=self.config.get("fix")
        self.configFixBase=self.configFix.getPluginBase()
        self.configGap=self.config.get("gap")
        self.configGapBase=self.configGap.getPluginBase()
        self.configMst=self.config.get("mst")
        self.configMstBase = self.configMst.getPluginBase()
        self.configHdf=self.config.get("hdf")
        self.configHdfBase = self.configHdf.getFile().getPluginBase()
        self.configPhdf=self.config.get("phdf")
        self.configPhdfBase = self.configPhdf.getPluginBase()

        
        self.summ=Finder.getInstance().find("excalibur_summary")
        self.summFem=self.summ.get("fem")
        self.summProc=self.summ.get("proc")
        self.summProcBase = self.summProc.getPluginBase()
        self.summArr = self.summ.get("arr")
        self.summArrBase = self.summArr.getPluginBase()
        self.summJpg=self.summ.get("mjpg")
        self.summJpgBase=self.summJpg.getPluginBase()
        

        
        self.configFix = fix
        self.gap = gap
        self.configArr = arr
        self.master=master
        self.hdf5=hdf5
        self.phdf5=phdf5
    
    def setupPlugins(self):
        self.do_configFem()
        self.do_fix()
        self.do_gap()
        self.do_configArr()
        self.do_summary()
        self.do_hdf5()
        self.do_phdf5()

    def setup(self):
        self.setupPlugins()
        self.do_hklimits()
            
    '''Configures the excalibur detector in the basic mode setting the summ image to link with the configuration and enable a few other panels'''
    def do_configFem(self):
        self.configSync.resync()
        self.configFem.setCounterDepth(2)
        self.configFem.setNumExposures(1)
        self.configFem.setArrayCounter(1)
        self.configFem.setArrayCounter(0)
        self.configFem.setChipEnable([1 for x in range(48)])
        
    def do_hklimits(self): 
        print "New hklimits"
        self.dev.set('1:HK:P5V_A_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_A_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5V_B_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_B_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5V_FEMO0_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO0_IMON.LOW', 4.2)
        self.dev.set('1:HK:P5V_FEMO1_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO1_IMON.LOW', 4.2)
        self.dev.set('1:HK:P5V_FEMO2_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO2_IMON.LOW', 4.2)
        self.dev.set('1:HK:P5V_FEMO3_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO3_IMON.LOW', 4.2)
        self.dev.set('1:HK:P5V_FEMO4_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO4_IMON.LOW', 4.2)
        self.dev.set('1:HK:P5V_FEMO5_IMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO5_IMON.LOW', 4.2)
        self.dev.set('1:HK:P48V_VMON.HIGH', 52.0)
        self.dev.set('1:HK:P48V_VMON.LOW', 44.0)
        self.dev.set('1:HK:P48V_IMON.HIGH', 5.5)    #11.0) #kw 30 June 2015
        self.dev.set('1:HK:P48V_IMON.LOW', 4.0)     #9.0) #kw 30 June 2015
        self.dev.set('1:HK:P5VSUP_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5VSUP_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5VSUP_IMON.HIGH', 3.0)
        self.dev.set('1:HK:P5VSUP_IMON.LOW', 1.5)
        self.dev.set('1:HK:HUMIDITY_MON.HIGH', 50.0) #45.0) #kw 14 Aug 2015 #40.0) #kw 30 June 2015
        self.dev.set('1:HK:HUMIDITY_MON.LOW', 0.0)
        self.dev.set('1:HK:AIR_TEMP_MON.HIGH', 30.0)
        self.dev.set('1:HK:AIR_TEMP_MON.LOW', 0.0)
        self.dev.set('1:HK:COOLANT_TEMP_MON.HIGH', 30.0)
        self.dev.set('1:HK:COOLANT_TEMP_MON.LOW', 0.0)
        self.dev.set('1:HK:COOLANT_FLOW_MON.HIGH', 2000)
        self.dev.set('1:HK:COOLANT_FLOW_MON.LOW', 1200)
        self.dev.set('1:HK:P3V3_IMON.HIGH', 3.0)
        self.dev.set('1:HK:P3V3_IMON.LOW', 2.5)
        self.dev.set('1:HK:P1V8_IMON_A.HIGH', 22.0)     #15.0) #kw 30 June 2015
        self.dev.set('1:HK:P1V8_IMON_A.LOW', 10.0)
        self.dev.set('1:HK:P1V8_IMON_B.HIGH', 22.0)     #15.0) #kw 30 June 2015
        self.dev.set('1:HK:P1V8_IMON_B.LOW', 10.0)
        self.dev.set('1:HK:BIAS_IMON.HIGH', .0006)
        self.dev.set('1:HK:BIAS_IMON.LOW', .0001)
        self.dev.set('1:HK:P3V3_VMON.HIGH', 3.5)
        self.dev.set('1:HK:P3V3_VMON.LOW', 3.15)
        self.dev.set('1:HK:P1V8_VMON_A.HIGH', 2.0)      #1.9) #kw 30 June 2015
        self.dev.set('1:HK:P1V8_VMON_A.LOW', 1.7)
        self.dev.set('1:HK:P1V8_VMON_B.HIGH', 2.0)      #1.9) #kw 30 June 2015
        self.dev.set('1:HK:P1V8_VMON_B.LOW', 1.7)
        self.dev.set('1:HK:MOLY_TEMPERATURE.HIGH', 39.0) #37.0) #kw 14 Aug 2015 #35.0) #kw 30 June 2015
        self.dev.set('1:HK:MOLY_TEMPERATURE.LOW', 0.0)
        self.dev.set('1:HK:LOCAL_TEMP.HIGH', 45.0)
        self.dev.set('1:HK:LOCAL_TEMP.LOW', 0.0)
        self.dev.set('1:HK:REMOTE_DIODE_TEMP.HIGH', 55.0)
        self.dev.set('1:HK:REMOTE_DIODE_TEMP.LOW', 0.0)
        self.dev.set('1:HK:MOLY_HUMIDITY.HIGH', 50) #40.0) #kw 14 Aug 2015
        self.dev.set('1:HK:MOLY_HUMIDITY.LOW', 0.0)

    def do_configArr(self):
        if self.configArr:
            self.configArrBase.setNDArrayPort(self.get_output_port())
            self.configArrBase.enableCallbacks()
        else:
            self.configArrBase.disableCallbacks()
        
    def do_fix(self):
        if self.configFix:
            self.configFixBase.setNDArrayPort("fem")
            self.configFixBase.enableCallbacks()
        else:
            self.configFixBase.disableCallbacks()


    def do_gap(self):
        if self.gap:
            if self.configFix:
                self.configGapBase.setNDArrayPort("fix")
            else:
                self.configGapBase.setNDArrayPort("fem")
            self.configGap.enableGapFilling()
            self.configGapBase.enableCallbacks()
        else:
            self.configGapBase.disableCallbacks()

    def get_output_port(self):
        if self.gap:
            return "gap"
        elif self.fix:
            return "fix"
        else:
            return "fem"
        
    def do_summary(self):
        if not self.master:
            self.configMstBase.disableCallbacks()
            return
        self.configMstBase.enableCallbacks()
        self.configMstBase.setNDArrayPort(self.get_output_port())
        self.configMst.setFrameDivisor(1)
        self.summFem.setArrayCounter(0)
        self.summProcBase.setNDArrayPort("master.slaves")
        self.summFem.clearCounters()
        self.summProcBase.enableCallbacks()
        self.summProc.setEnableOffsetScale(0)
        self.summProc.setScale(1)
        self.summArrBase.setNDArrayPort("master.slaves")
        self.summArrBase.enableCallbacks()
        self.summJpgBase.setNDArrayPort("master.proc")
        self.summJpgBase.enableCallbacks()

    def do_hdf5(self):
        if not self.hdf5:
            self.configHdfBase.disableCallbacks()
            return
        self.configHdf.setNumCapture(2) #send 2 then 1 to fore a change in the slaves
        self.configHdf.setNumCapture(1)
        self.configHdf.setFileNumber(1)
        self.configHdf.setAutoIncrement(1)
        self.configHdf.getFile().setFileFormat(0)
        self.configHdf.setAutoSave(1)
        self.configHdf.getFile().setFileWriteMode(2)
        self.configHdf.setStoreAttr(0)
        self.configHdf.setStorePerform(0)
        self.configHdfBase.setNDArrayPort(self.get_output_port())
        self.configHdfBase.enableCallbacks()
    
    def do_phdf5(self):
        if not self.phdf5:
            self.configPhdfBase.disableCallbacks()
            return
        self.configPhdf.setNumCapture(2) #send 2 then 1 to fore a change in the slaves
        self.configPhdf.setNumCapture(1)
        self.configPhdf.setFileNumber(1)
        self.configPhdf.setAutoIncrement(1)
        self.configPhdf.setFileFormat(0)
        self.configPhdf.setAutoSave(1)
        self.configPhdf.setFileWriteMode(2)
#        self.configPhdf.setStoreAttr(0)
#        self.configPhdf.setStorePerform(0)
        self.configPhdfBase.setNDArrayPort(self.get_output_port())
        self.configPhdfBase.enableCallbacks()
