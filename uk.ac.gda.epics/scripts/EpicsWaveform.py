
import scisoftpy as dnp
from gda.epics import CAClient


class WaveformPlotterClass(object):
    def __init__(self, name, waveformPV, viewPanelName="Plot 2"):
        self.name = name;
        self.delay=1;
        self.chData=CAClient(waveformPV);
        self.configChannel(self.chData);

        self.width = 1024;
        self.height=768;
        self.panel=viewPanelName
        
    def __del__(self):
        self.cleanChannel(self.chData);

    def configChannel(self, channel):
        if not channel.isConfigured():
            channel.configure();

    def cleanChannel(self, channel):
        if channel.isConfigured():
            channel.clearup();
            
    def setShape(self, width, height):
        self.width = width
        self.height = height
    
    # Data from EPICS is signed 
    def imagePlot0(self, panel=None):
        if panel is None:
            panel=self.panel;
            
        da=self.chData.cagetArrayByte()
        ds = dnp.array(da)
        ds.shape = self.height, self.width

        dnp.plot.image(ds, name=panel)
    
    
    #Fast to get unsigned Byte from EPICS and plot
    def imagePlot(self, panel=None):
        if panel is None:
            panel=self.panel;
            
        da=self.chData.cagetArrayUnsigned();
        ds = dnp.array(da)
        ds.shape = self.height, self.width

        dnp.plot.image(ds, name=panel)
    
    
    #Slow because of the signed to unsigned conversion
    def imagePlot2(self, panel=None):
        if panel is None:
            panel=self.panel;
            
        da=self.chData.cagetArrayByte();
        
        #To convert from singed to unsigned
        #method 2
        da=[x&0xff for x in da]
        
    #method 3
    #    ds1=array.array('B' [x&0xff for x in ds1] )
            
        ds = dnp.array(da)
        ds.shape = self.height, self.width

        dnp.plot.image(ds, name=panel)

#Usage:
#from EpicsWaveform import WaveformPlotterClass;
#wfp = WaveformPlotterClass('wfp', 'BL22I-DI-PHDGN-13:ARR:ArrayData', "Area Detector" )
#wfp.setShape(1390, 1038)
#wfp.imagePlot();
