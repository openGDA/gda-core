import sys
from gda.data.scan.datawriter import DataWriterExtenderBase
from gda.device.scannable.scannablegroup import ScannableGroup
from gda.data.scan.datawriter import DataWriterFactory
from gda.factory import Finder
import gda.jython.commands.ScannableCommands.scan
from gda.analysis import DataSet, RCPPlotter
from uk.ac.diamond.scisoft.analysis.roi import GridROI
from uk.ac.diamond.scisoft.analysis.plotserver import GuiParameters
from gdascripts.messages import handle_messages
from gdascripts.scan import gdascans
from uk.ac.gda.server.ncd.subdetector import LastImageProvider
import scisoftpy as dnp

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
	
	def snap(self):
		try:
			image =  self.camera.readLastImage()
			RCPPlotter.imagePlot("Camera View", image)
		except:
			print "  gridscan: error getting camera image"
		
	def scan(self):
		beanbag=RCPPlotter.getGuiBean(self.cameraPanel)
		roi=beanbag[GuiParameters.ROIDATA]
		if not isinstance(roi, GridROI):
			print "no Grid ROI selected"
			return
		if self.scanrunning:
			print "Already Running"
			return
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
					data=tree.findNode(self.getSaxsDetectorName()).findNode("data").getData()
					ds=DataSet(data.dimensions.tolist()[1],data.dimensions.tolist()[2],data.getBuffer().tolist())
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
