from gdascripts.scannable.detector.epics.EpicsFirewireCamera import EpicsFirewireCamera
from gda.configuration.properties import LocalProperties
from gda.jython.commands.GeneralCommands import run
from gdascripts.scannable.detector.ProcessingDetectorWrapper import ProcessingDetectorWrapper
from gdascripts.scannable.detector.DetectorDataProcessor import DetectorDataProcessorWithRoi
from gdascripts.analysis.datasetprocessor.twod.TwodGaussianPeak import TwodGaussianPeak
from gdascripts.analysis.datasetprocessor.twod.SumMaxPositionAndValue import SumMaxPositionAndValue
from gdascripts.pd.dummy_pds import DummyPD
from gdascripts.scannable.detector.dummy.focused_beam_dataset import CreateImageReadingDummyDetector
from gda.util import VisitPath
from gda.device.scannable import PseudoDevice
from time import sleep
import java.lang.InterruptedException

run("bimorph_mirror_optimising.py") # as it still uses globals to keep things understandable to scientists
from bimorph_mirror_optimising import ScanAborter

print "Creating dummy detector"
xdp = DummyPD("x")
xdp.asynchronousMoveTo(430)
cam1det = CreateImageReadingDummyDetector.create(xdp)
cam1 = ProcessingDetectorWrapper('cam1', cam1det, [], panel_name='ImageProPlus Plot')

# To use a firewire camera:
# from gdascripts.scannable.detector.epics.EpicsFirewireCamera import EpicsFirewireCamera
# cam1det = EpicsFirewireCamera('cam1det', 'BL02I-FW-CAM-01:', datadirstring)   
# cam1 = ProcessingDetectorWrapper('cam1', cam1det, [], panel_name='ImageProPlus Plot')

print "Creating dummy ScanAborter"
mon = DummyPD('mon')
mon.asynchronousMoveTo(1)
scanAborter = ScanAborter("scanAborter",mon, 0.2)

print "Creating peak2d hooked to cam1"
peak2d = DetectorDataProcessorWithRoi('peak2d', cam1, [TwodGaussianPeak()])

print "Creating mirror voltage controller"
# TODO: need a dummy

# to use b16's:
#from gdascripts.bimorph.pd_bimorph import EemBimorph #@UnresolvedImport
#eembimorph = EemBimorph("eembimorph", 0, 8, "EEM_Bimorph:", sleepInS=0)