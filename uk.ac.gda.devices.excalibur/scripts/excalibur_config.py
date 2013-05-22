'''
Created on 16 Jan 2012

@author: rsr31645

Script to configure the excalibur for different settings as described in the configure.py script
'''
import sys
from gda.factory import Finder

'''Configures the excalibur detector in the basic mode setting the summary image to link with the configuration and enable a few other panels'''
def basicConfiguration():
    masterConfig=Finder.getInstance().find("excalibur_config")
    configSync = masterConfig.get("sync")
    '''self.caPut('SYNC:RESEND.PROC', 1)'''
    configSync.resync()
    
    excalibur_cam =Finder.getInstance().find("excalibur_cam")
    configFem = excalibur_cam.getAdBase()
    '''self.caPut('CONFIG:ACQUIRE:CounterDepth', 2)'''
    configFem.setCounterDepth(2)
    '''self.caPut('CONFIG:ACQUIRE:ArrayCounter', 1)'''
    configFem.setArrayCounter(1)
    '''self.caPut('CONFIG:ACQUIRE:ArrayCounter', 0)'''
    configFem.setArrayCounter(0)
    
    configArr = excalibur_cam.getNdArray()
    configArrBase=configArr.getPluginBase()
    '''self.caPut('CONFIG:ARR:NDArrayPort', 'fem')'''
    configArrBase.setNDArrayPort("fem")
    '''self.caPut('CONFIG:ARR:EnableCallbacks', 1)'''
    configArrBase.enableCallbacks()
    
    configMst=masterConfig.get("mst")
    configMstBase = configMst.getPluginBase()
    '''self.caPut('CONFIG:MASTER:NDArrayPort', 'fem')'''
    configMstBase.setNDArrayPort("fem")
    '''self.caPut('CONFIG:MASTER:FrameDivisor', 1)'''
    configMst.setFrameDivisor(1)
    '''self.caPut('CONFIG:MASTER:EnableCallbacks', 1)'''
    configMstBase.enableCallbacks()
    
    excSum=Finder.getInstance().find("excalibur_summary")
    
    summFem=excSum.get("fem")
    '''self.caPut('MASTER:SLAVES:ArrayCounter', 0)'''
    summFem.setArrayCounter(0)
    
    summProc = excSum.get("proc")
    summProcBase=summProc.getPluginBase()
    '''self.caPut('MASTER:PROC:NDArrayPort', 'master.slaves')'''
    summProcBase.setNDArrayPort("master.slaves")
    '''self.caPut('MASTER:SLAVES:Clear', 1)'''
    summFem.clearCounters()
    '''self.caPut('MASTER:PROC:EnableCallbacks', 1)'''
    summProcBase.enableCallbacks()
    '''self.caPut('MASTER:PROC:EnableOffsetScale', 1)'''
    summProc.setEnableOffsetScale(1)
    '''self.caPut('MASTER:PROC:Scale', 128)'''
    summProc.setScale(128)
    
    summArr = excSum.get("arr")
    summArrBase=summArr.getPluginBase()
    '''self.caPut('MASTER:ARR:NDArrayPort', 'master.slaves')'''
    summArrBase.setNDArrayPort("master.slaves")
    '''self.caPut('MASTER:ARR:EnableCallbacks', 1)'''
    summArrBase.enableCallbacks()
    
    summJpg=excSum.get("mjpg")
    summJpgBase=summJpg.getPluginBase()
    '''self.caPut('MASTER:FFMPEG:NDArrayPort', 'master.proc')'''
    summJpgBase.setNDArrayPort("master.proc")
    '''self.caPut('MASTER:FFMPEG:EnableCallbacks', 1)'''
    summJpgBase.enableCallbacks()
    
'''Configures the gap panel of the configuration and master sets of the excalibur detector'''
def gapConfiguration():
    basicConfiguration()
    excalibur_cam =Finder.getInstance().find("excalibur_cam")
    masterConfig=Finder.getInstance().find("excalibur_config")
    
    configGap=masterConfig.get("gap")
    configGapBase=configGap.getPluginBase()
    configGapBase.setNDArrayPort("fem")
    configGapBase.enableCallbacks()
    configGap.enableGapFilling()
    
    configArr = excalibur_cam.getNdArray()
    configArr.getPluginBase().setNDArrayPort("gap")
    configArr.getPluginBase().enableCallbacks()
    
    configMst=masterConfig.get("mst")
    configMst.getPluginBase().setNDArrayPort("gap")
    configMst.getPluginBase().enableCallbacks()
    configMst.setFrameDivisor(1)

'''Configures the hdf configuration necessary for hdf acquisition'''
def hdfConfiguration(connectTo):
    basicConfiguration()
    masterConfig=Finder.getInstance().find("excalibur_config")
    
    configHdf=masterConfig.get("hdf")
    
    configHdf.setFilePath(".")
    configHdf.setFileName("image")
    configHdf.setFileTemplate("\"\%s\%s\%05d.hdf\"")
    configHdf.setNumCapture(2)
    configHdf.setNumCapture(1)
    configHdf.setFileNumber(1)
    configHdf.setAutoIncrement(1)
    configHdf.getFile().setFileFormat(0)
    configHdf.setAutoSave(1)
    configHdf.getFile().setFileWriteMode(2)
    configHdf.setStoreAttr(1)
    configHdf.setStorePerform(1)
    configHdf.getPluginBase().setNDArrayPort(connectTo)
    configHdf.getPluginBase().enableCallbacks()
    
'''Synchronizes the master configuration with the other readout nodes in the excalibur detector'''
def syncMaster():
    masterConfig=Finder.getInstance().find("excalibur_config")
    configSync = masterConfig.get("sync")
    '''self.caPut('SYNC:RESEND.PROC', 1)'''
    configSync.resync()
    
def getReadout1Fem():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("fem")

def getReadout1Arr():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("arr")

def getReadout1Proc():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("proc")

def getReadout1Roi():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("roi")

def getReadout1Hdf():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("hdf")

def getReadout1Tiff():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("tiff")

def getReadout1Mjpg():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("mjpg")

def getReadout1Mst():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("mst")

def getReadout1Fix():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("fix")

def getReadout1Gap():
    rd1=Finder.getInstance().find("excalibur_readoutNode1")
    return rd1.get("gap")

def getSummaryFem():
    summObs=Finder.getInstance().find("excalibur_summary")
    return rd1.get("fem")

def getSummaryStats():
    summObs=Finder.getInstance().find("excalibur_summary")
    return summObs.get("stats")