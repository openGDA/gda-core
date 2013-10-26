'''
Class to configure the Excalibur IOCs ready for use in GDA
usage:

import excalibur_config
configurator=excalibur_config.ExcaliburConfigurator(fix=False, gap=True, arr=False, master=True, hdf5=False, phdf5=True)
configurator.setup()

'''
import sys
from gda.factory import Finder


class ExcaliburConfigurator():
    def __init__(self, fix=False, gap=True, arr=False, master=True, hdf5=False, phdf5=True ):
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
        self.configHdfBase = self.configHdf.getPluginBase()
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
    
    def setup(self):
        self.do_configFem()
        self.do_hklimits()
        self.do_fix()
        self.do_gap()
        self.do_configArr()
        self.do_summary()
        self.do_hdf5()
        self.do_phdf5()
            
    '''Configures the excalibur detector in the basic mode setting the summ image to link with the configuration and enable a few other panels'''
    def do_configFem(self):
        self.configSync.resync()
        self.configFem.setCounterDepth(2)
        self.configFem.setNumExposures(1)
        self.configFem.setArrayCounter(1)
        self.configFem.setArrayCounter(0)
        self.configFem.setChipEnable([1 for x in range(48)])
        
    def do_hklimits(self): 
        self.dev.set('1:HK:P5V_A_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_B_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5V_FEMO0_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P5V_FEMO1_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P5V_FEMO2_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P5V_FEMO3_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P5V_FEMO4_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P5V_FEMO5_IMON.HIGH', 6.0)
        self.dev.set('1:HK:P48V_VMON.HIGH', 52.0)
        self.dev.set('1:HK:P48V_IMON.HIGH', 10.0)
        self.dev.set('1:HK:P5VSUP_VMON.HIGH', 5.2)
        self.dev.set('1:HK:P5VSUP_IMON.HIGH', 3.0)
        self.dev.set('1:HK:P5V_A_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5V_B_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5V_FEMO0_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5V_FEMO1_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5V_FEMO2_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5V_FEMO3_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5V_FEMO4_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5V_FEMO5_IMON.LOW', 0.0)
        self.dev.set('1:HK:P48V_VMON.LOW', 44.0)
        self.dev.set('1:HK:P48V_IMON.LOW', 0.0)
        self.dev.set('1:HK:P5VSUP_VMON.LOW', 4.8)
        self.dev.set('1:HK:P5VSUP_IMON.LOW', 0.0)
        self.dev.set('1:HK:HUMIDITY_MON.HIGH', 50.0)
        self.dev.set('1:HK:AIR_TEMP_MON.HIGH', 40.0)
        self.dev.set('1:HK:COOLANT_TEMP_MON.HIGH', 30.0)
        self.dev.set('1:HK:COOLANT_FLOW_MON.HIGH', 10000)
        self.dev.set('1:HK:P3V3_IMON.HIGH', 3.0)
        self.dev.set('1:HK:P1V8_IMON_A.HIGH', 20.0)
        self.dev.set('1:HK:BIAS_IMON.HIGH', 10.0)
        self.dev.set('1:HK:P3V3_VMON.HIGH', 3.5)
        self.dev.set('1:HK:P1V8_VMON_A.HIGH', 1.9)
        self.dev.set('1:HK:P1V8_IMON_B.HIGH', 20.0)
        self.dev.set('1:HK:P1V8_VMON_B.HIGH', 1.9)
        self.dev.set('1:HK:HUMIDITY_MON.LOW', 0.0)
        self.dev.set('1:HK:AIR_TEMP_MON.LOW', 0.0)
        self.dev.set('1:HK:COOLANT_TEMP_MON.LOW', 0.0)
        self.dev.set('1:HK:COOLANT_FLOW_MON.LOW', 100)
        self.dev.set('1:HK:P3V3_IMON.LOW', 0.0)
        self.dev.set('1:HK:P1V8_IMON_A.LOW', 0.0)
        self.dev.set('1:HK:BIAS_IMON.LOW', 0.0)
        self.dev.set('1:HK:P3V3_VMON.LOW', 3.1)
        self.dev.set('1:HK:P1V8_VMON_A.LOW', 1.7)
        self.dev.set('1:HK:P1V8_IMON_B.LOW', 0.0)
        self.dev.set('1:HK:P1V8_VMON_B.LOW', 1.7)
        self.dev.set('1:HK:MOLY_TEMPERATURE.HIGH', 35.0)
        self.dev.set('1:HK:MOLY_TEMPERATURE.LOW', 0.0)
        self.dev.set('1:HK:LOCAL_TEMP.HIGH', 45.0)
        self.dev.set('1:HK:LOCAL_TEMP.LOW', 0.0)
        self.dev.set('1:HK:REMOTE_DIODE_TEMP.HIGH', 55.0)
        self.dev.set('1:HK:REMOTE_DIODE_TEMP.LOW', 0.0)
        self.dev.set('1:HK:MOLY_HUMIDITY.HIGH', 70.0)
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
