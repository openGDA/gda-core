from gdascripts.scannable.epics.PvManager import PvManager

import gda.epics.LazyPVFactory
from time import sleep
import time
from gda.device.detector import PseudoDetector
from gda.device.Detector import BUSY, IDLE
from gda.analysis import ScanFileHolder
from org.eclipse.january.dataset import DatasetFactory
from gda.analysis.io import PNGSaver
from gda.jython import InterfaceProvider
from gda.data import NumTracker
# test = ScanFileContainer()
# test.loadPilatusData("/dls/i16/data/Pilatus/test1556.tif")
# test.plot()
# matrix = test.getImage().doubleMatrix()

def isScanRunning():
	return InterfaceProvider.getScanStatusHolder().getScanStatus() != 0



def unsign2(x):
	x = float(x)
	if x<0:
		return x+256.
	else:
		return x

class EpicsFirewireCamera(PseudoDetector):

	#TODO: Known bug: only works when epics zoom turned off (read the zoom value)
	def __init__(self, name, pvroot, filepath=None, determine_data_pv_based_on_zoom = False, numtracker_extension='tmp', filename_template='fire%d_%05d.png'):
		# BL07I-DI-PHDGN-06:CAM:DATA
		self.determine_data_pv_based_on_zoom = determine_data_pv_based_on_zoom
		
		self.name = name
		self.extraNames = []
		self.outputFormat = []
		self.level = 9

		self.pvs = PvManager(['SET_SHUTTR', 'WIDTH', 'HEIGHT', 'ZOOM'], pvroot)
		self.pv_data = []
		self.pv_data.append(gda.epics.LazyPVFactory.newReadOnlyIntegerArrayPV(pvroot + 'DATA'))
		self.pv_data.append(gda.epics.LazyPVFactory.newReadOnlyIntegerArrayPV(pvroot + 'DATA1'))
		self.pv_data.append(gda.epics.LazyPVFactory.newReadOnlyIntegerArrayPV(pvroot + 'DATA2'))
		self.pv_data.append(gda.epics.LazyPVFactory.newReadOnlyIntegerArrayPV(pvroot + 'DATA3'))
		self.pv_data.append(gda.epics.LazyPVFactory.newReadOnlyIntegerArrayPV(pvroot + 'DATA4'))
		self.pvs.configure()
		
		self.filepath = filepath
		self.ds = None
		self.last_image_path = None
		self.last_filename = None
		self.last_image_number = 0
		self.numtracker_extension = numtracker_extension
		self.filename_template = filename_template

	def createsOwnFiles(self):
		return False

	def setCollectionTime(self, t):
		self.pvs['SET_SHUTTR'].caput(int(t))

	def getCollectionTime(self):
		return float(self.pvs['SET_SHUTTR'].caget())
	
	def prepareForCollection(self):
		self.last_image_number = -1
	
	def collectData(self):
		#rawdata = self.pvs['DATA'].cagetArray()
		if self.determine_data_pv_based_on_zoom:
			zoom_ordinal = int(float(self.pvs['ZOOM'].caget()))
			rawdata = self.pv_data[zoom_ordinal].get()
		else:
			rawdata = self.pv_data[0].get()
		data = map(unsign2, rawdata )
		self.ds = DatasetFactory.zeros(int(float(self.pvs['HEIGHT'].caget())), int(float(self.pvs['WIDTH'].caget())), data)
		self.last_image_number += 1
		self.last_filename = self._generateCurrentFilename()
		if self.filepath is not None:
			self.saveImage(self.last_filename);
	
	def _getCurrentScanNumer(self):
		return NumTracker(self.numtracker_extension).getCurrentFileNumber();
	
	def _generateCurrentFilename(self):
		if isScanRunning():
			return self.filename_template % (self._getCurrentScanNumer(), self.last_image_number)
		else:
			return time.strftime("%Y%m%d%H%M%S", time.localtime()) + '%03d' % ((time.time()%1)*1000) + '.png'
	
	def getStatus(self):
		return IDLE
	
	def readout(self):
		if self.ds is None:
			raise Exception("Epics Firewire Camera %s has not acquired an image" % self.name)
		else:
			return self.ds
		
	def setFilepath(self, filepath):
		self.filepath=filepath
		
	def getFilepath(self):
		return self.filepath
	
	def getLastImagePath(self):
		return self.last_image_path
	
	def saveImage(self, filename):
		path = self.filepath + filename
		if self.ds is None:
			raise Exception("Epics Firewire Camera %s has not acquired an image" % self.name)
		else:
			sfh = ScanFileHolder()
			sfh.addDataSet("Image",self.ds)
			sfh.save(PNGSaver(path))
			self.last_image_path = path