import time;
import os;

from java.io import File;
#import java.io.FileNotFoundException

from gda.device import Detector
from gda.device.detector import DetectorBase
#from gda.device.detector import PseudoDetector

from gda.data import PathConstructor

from gda.analysis.io import JPEGLoader, TIFFImageLoader, PilatusTiffLoader
from gda.analysis import ScanFileHolder



from uk.ac.diamond.scisoft.analysis import SDAPlotter;

from gdascripts.scannable.detector.ZipImageProducer import ZipImageProducerClass;
from org.eclipse.january.dataset import DatasetUtils

FILELOADERS={
			'TIF'  : TIFFImageLoader,
			'TIFF' : TIFFImageLoader,
			'JPG'  : JPEGLoader,
			'JPEG' : JPEGLoader
			}

#class DummyAreaDetectorClass(PseudoDetector):
class DummyAreaDetectorClass(DetectorBase):
	def __init__(self, name, panelName, zippedImageSource, fileImageExtension):
		self.setName(name);
#		self.setInputNames([]);
		self.setLevel(7);
		
		self.panel = panelName;
		self.data = ScanFileHolder();
		self.triggerTime = 0;

		self.filePath = None;
		self.filePrefix = None;
		self.logScale = False;
		self.alive=True;

#		self.imageProducer=ZipImageProducerClass('/scratch/Dev/gdaDev/gda-config/i07/scripts/Diamond/Pilatus/images100K.zip', 'tif');
		self.imageProducer=ZipImageProducerClass(zippedImageSource, fileImageExtension);
		self.fullImageFileName = None;
# DetectorBase Implementation

	def collectData(self):
		self.triggerTime = time.time();
		self.fullImageFileName = self.imageProducer.getNextImage();
		return;


	def readout(self):
		if self.alive:
			self.display();
		return self.fullImageFileName;

	def getStatus(self):
		currenttime = time.time();
		
		if currenttime<self.triggerTime + self.getCollectionTime():
			return Detector.BUSY
		else:
			return Detector.IDLE;
	
	def createsOwnFiles(self):
		return True;
	
	def toString(self):
		self.getPosition();
		return "Latest image file: " + self.getFullFileName();

## Extra Implementation
	def setAlive(self, newAlive=True):
		self.alive = newAlive;
		
	def setLogScale(self, newLogScale=True):
		self.logScale = newLogScale;

	def setPanel(self, newPanelName):
		self.panel = newPanelName;

	def singleShot(self, newExpos):
		"""Shot single image with given exposure time""";
		if self.getStatus() != Detector.IDLE:
			print 'Area Detector not available, please try later';
			return;
		
		self.setCollectionTime(newExpos);
		self.collectData();
		while self.getStatus() != Detector.IDLE:
			time.sleep(self.getCollectionTime()/2.0);
		return self.readout();

	def multiShot(self, numberOfImages, newExpos=None):
		"""Shot multiple images with given exposure time""";
		if self.getStatus() != Detector.IDLE:
			print 'Camera not available, please try later';
			return;
		
		if newExpos is not None: # New exposure time given
			exposureTime=newExpos;
			self.setCollectionTime(exposureTime);
			
		exposureTime=self.getCollectionTime();
		fn=[];
		#self.setNumOfImages(numberOfImages);
		for n in range(numberOfImages):
			self.collectData();
			time.sleep(exposureTime);
			while self.getStatus() != Detector.IDLE:
				time.sleep(exposureTime/10.0);
			fn.append( self.readout() );

		return fn;


	def display(self,file=None):
		if file==None:
			file = self.getFullFileName()
#		self.data.loadPilatusData(file)
		self.data.load(PilatusTiffLoader(file));
		dataset = self.data.getAxis(0);

		if self.panel:
			if self.logScale:
				SDAPlotter.imagePlot(self.panel, DatasetUtils.lognorm(dataset)); #For RCP GUI
			else:
				SDAPlotter.imagePlot(self.panel, dataset); #For RCP GUI
		else:
			print "No panel set to display"
			raise Exception("No panel_name set in %s. Set this or set %s.setAlive(False)" % (self.name,self.name));


## Area Detector Implementation
	def setFile(self, subDir, newFilePrefix):
		"""Set file path and name"""
#		imagePath = PathConstructor.createFromProperty("gda.data.scan.datawriter.datadir");
		imagePath=PathConstructor.createFromDefaultProperty() + File.separator;
		
		fullPath = os.path.join(imagePath, subDir);
		print "Note: Current Pilatus image path: " + fullPath;
		
		self.imageProducer.setFilePath(fullPath);
		self.imageProducer.setFilePrefix(newFilePrefix);

#a.setFilePath("/scratch/temp")
#a.setFilePrefix("p100k")

	def getFilePath(self):
		return self.imageProducer.getFilePath();

	def getFilePrefix(self):
		return self.imageProducer.getFilePrefix();

	def getFileNumber(self):
		"""Get filenumber"""
		self.fileNumber = self.imageProducer.getFileNumber();
		return self.fileNumber

	def getFullFileName(self):
		return self.fullImageFileName;

#Usage 
#from Diamond.Pilatus.DummyAreaDetector import DummyAreaDetectorClass
#dummyCamera = DummyAreaDetectorClass("dummyCamera", "PEEM Image");
#dummyCamera.setFile('dummycam', 'dummyCam');
#dummyCamera.setAlive(True);
