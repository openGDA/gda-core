import sys
from gda.data.scan.datawriter import DataWriterExtenderBase
from gda.device.scannable.scannablegroup import ScannableGroup
from gda.data.scan.datawriter import DataWriterFactory
from gda.factory import Finder
import gda.jython.commands.ScannableCommands.scan
from gda.analysis import DataSet
from uk.ac.diamond.scisoft.analysis import SDAPlotter as RCPPlotter
from uk.ac.diamond.scisoft.analysis.roi import GridROI
from uk.ac.diamond.scisoft.analysis.plotserver import GuiParameters
from gdascripts.messages import handle_messages
from gdascripts.scan import gdascans
from uk.ac.gda.server.ncd.subdetector import LastImageProvider
import scisoftpy as dnp
from uk.ac.diamond.scisoft.analysis.io import Metadata
from uk.ac.diamond.scisoft.analysis.roi import GridPreferences

class Grid(DataWriterExtenderBase):
	
	def __init__(self, cameraPanel, gridPanel, camera, positioner, ncddetectors):
		self.camera=camera
		self.positioner=positioner
		self.ncddetectors = ncddetectors
		self.iAmAGridThingy=True
		self.scanrunning=0
		self.cameraPanel = cameraPanel
		self.gridPanel = gridPanel
		dwfs=Finder.getInstance().getFindablesOfType(DataWriterFactory)
		for fac in dwfs.values():
			dwf=fac
			break
		extenders=dwf.getDataWriterExtenders()
		extenders=[ex for ex in extenders if not "iAmAGridThingy" in dir(ex)]
		extenders.append(self)
		dwf.setDataWriterExtenders(extenders)
		self.gridpreferences=GridPreferences()
	
	def snap(self):
		try:
			image =  self.camera.readLastImage()
			if not self.gridpreferences == None:
				image.setMetadata(Metadata({"GDA_GRID_METADATA" : self.gridpreferences}))
				xs = image.getShape()[1]
				ys = image.getShape()[0]
				xbs = self.getBeamCentreX()
				ybs = self.getBeamCentreY()
				xres = self.getResolutionX()
				yres = self.getResolutionY()
				xa = dnp.array([(x-xbs)/xres for x in range(xs)])
				ya = dnp.array([(y-ybs)/yres for y in range(ys)])
				xa.setName("mm")
				ya.setName("mm")
				RCPPlotter.imagePlot(self.cameraPanel, xa, ya, image)
			else:
				RCPPlotter.imagePlot(self.cameraPanel, image)
		except:
			print "  gridscan: error getting camera image"
		
	def scan(self):
		beanbag=RCPPlotter.getGuiBean(self.cameraPanel)
		if beanbag == None:
			print "No Bean found on "+self.camerPanel+" (that is strange)"
			return
		roi=beanbag[GuiParameters.ROIDATA]
		if not isinstance(roi, GridROI):
			print "no Grid ROI selected"
			return
		if self.scanrunning:
			print "Already Running"
			return
		self.gridpreferences = roi.getGridPreferences()
		print "Beam centre: %d, %d  Resolution px/mm: %5.5f %5.5f" % (self.getBeamCentreX(), self.getBeamCentreY(), self.getResolutionX(), self.getResolutionY())
		self.scanrunning=True
		try:
			points=roi.getPhysicalGridPoints()
			self.dimensions=roi.getDimensions()
			self.dimensions=[self.dimensions[1],self.dimensions[0]]
			try:
				bc = roi.getBeamCentre()
				self.camera.setAttribute("beam_center_x", bc[0])
				self.camera.setAttribute("beam_center_y", bc[1])
				ps = roi.getPixelSizeM()
				self.camera.setAttribute("x_pixel_size", ps[0])
				self.camera.setAttribute("y_pixel_size", ps[1])
			except:
				pass
			print "Scanning a %d by %d grid" % tuple(self.dimensions)
			RCPPlotter.setupNewImageGrid(self.gridPanel,self.dimensions[0],self.dimensions[1])
			spoints=tuple([x.tolist() for x in points.tolist()])
			gdascans.Rscan()(self.positioner, spoints, self.ncddetectors, self.camera)
		except:
			type, exception, traceback = sys.exc_info()
			self.scanrunning = False
			handle_messages.log(None, "Error in grid_scan.scan", type, exception, traceback, False)		

	def getSaxsDetector(self):
		for det in self.ncddetectors.getDetectors():
			if det.getDetectorType() == "SAXS":
				return det
		raise Exception("Unable to find SAXS detector in ncddetectors")
	
	def getSaxsDetectorName(self):
		return self.getSaxsDetector().getName()

	def addData(self, parent, dataPoint):
		try:
			if self.scanrunning:
				pno=dataPoint.getCurrentPointNumber()
				index=dataPoint.getDetectorNames().indexOf(self.ncddetectors.getName())
				tree=dataPoint.getDetectorData().get(index).getNexusTree()
				try:
					data=tree.findNode("detector").findNode("data").getData()
					ds = dnp.array(data.getBuffer().tolist())
					ds.shape = data.dimensions.tolist()[1],data.dimensions.tolist()[2]
				except:
					if isinstance(self.getSaxsDetector(), LastImageProvider):
						ds=self.getSaxsDetector().readLastImage()
				if not ds == None:
					ds.setName("saxs at grid point (%d, %d)" % (pno/self.dimensions[0]+1,pno%self.dimensions[0]+1))
					RCPPlotter.plotImageToGrid(self.gridPanel,ds,pno/self.dimensions[0],pno%self.dimensions[0],True)
		except:
			type, exception, traceback = sys.exc_info()
			handle_messages.log(None, "Error in grid_scan.addData", type, exception, traceback, False)		

	def completeCollection(self, parent):
		if self.scanrunning:
			self.scanrunning=False
			print "Grid scan complete"
			
	def getBeamCentreX(self):
		return self.gridpreferences.getBeamlinePosX()
	def getBeamCentreY(self):
		return self.gridpreferences.getBeamlinePosY()
	def setBeamCentreX(self, x):
		self.gridpreferences.setBeamlinePosX(x)
	def setBeamCentreY(self, y):
		self.gridpreferences.setBeamlinePosY(y)
		
	def getResolutionX(self):
		return self.gridpreferences.getResolutionX()
	def getResolutionY(self):
		return self.gridpreferences.getResolutionY()
	def setResolutionX(self, x):
		self.gridpreferences.setResolutionX(x)
	def setResolutionY(self, y):
		self.gridpreferences.setResolutionY(y)