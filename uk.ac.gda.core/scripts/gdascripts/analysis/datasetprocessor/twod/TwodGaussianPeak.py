from TwodDataSetProcessor import TwodDataSetProcessor

from uk.ac.diamond.scisoft.analysis.dataset.function import Integrate2D
from uk.ac.diamond.scisoft.analysis.fitting.functions import Gaussian, Offset
from uk.ac.diamond.scisoft.analysis.optimize import GeneticAlg
from uk.ac.diamond.scisoft.analysis.fitting import Fitter 

from org.eclipse.dawnsci.analysis.dataset.impl import DatasetFactory

from math import pi
import java.lang.IllegalArgumentException



try:
	from gda.analysis import Fitter # from swingclient plugin not available to PyDev tests
	def fitplot(*args):
		fitted_function = Fitter.fit(*args)
		RCPPlotter.plot("Data Vector", args[0],fitted_function.display(args[0])[0]);
		return fitted_function
except ImportError:
	def fitplot(*args):
		raise

class TwodGaussianPeak(TwodDataSetProcessor):


	def __init__(self, name='peak',
				labelList=('background','peakx','peaky', 'topx', 'topyy','fwhmx','fwhmy', 'fwhmarea' ),
				keyxlabel = 'peakx',
				keyylabel = 'peaky',
				formatString=" background:%f, peakx:%f, peaky:%f, topx:%f, topy:%f, fwhmx:%f, fwhmy:%f, whmarea:%f"
				):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
		self.maxwidth = None
		
	def _process(self, ds, xoffset=0, yoffset=0, Dataset=DoubleDataset):###dsxaxis, dsyaxis):
##		assert(dsyaxis is None)		# STUB
##		assert(dsxaxis is None)		# STUB
		
		integrator = Integrate2D()
		dsy, dsx = integrator.value(ds)
		dsyaxis = DatasetFactory.createRange(dsy.shape[0])
		dsxaxis = DatasetFactory.createRange(dsx.shape[0])
		
		gaussian = Gaussian(dsyaxis.min(), dsyaxis.max(), dsyaxis.max()-dsyaxis.min(), (dsyaxis.max()-dsyaxis.min())*(dsy.max()-dsy.min()) )
		gaussian.getParameter(2).setLowerLimit(0)
		if self.maxwidth is not None:
			gaussian.getParameter(1).setUpperLimit(self.maxwidth)
		ansy = Fitter.fit(dsyaxis, dsy, GeneticAlg(0.001), [ gaussian, Offset( dsy.min(),dsy.max() ) ] )
# 		ansy = DataSetFunctionFitter().fit( dsyaxis, dsy, GeneticAlg(.001), [ gaussian, Offset( dsy.min(),dsy.max() ) ] )
# 		ansy = ansy.functionOutput
		
		gaussian = Gaussian(dsxaxis.min(), dsxaxis.max(), dsxaxis.max()-dsxaxis.min(), (dsxaxis.max()-dsxaxis.min())*(dsx.max()-dsx.min()) )
		gaussian.getParameter(2).setLowerLimit(0)
		if self.maxwidth is not None:
			gaussian.getParameter(1).setUpperLimit(self.maxwidth)
		try:
			ansx = fitplot( dsxaxis, dsx, GeneticAlg(.001), [ gaussian, Offset( dsx.min(),dsx.max() ) ] )
		except java.lang.Exception:
			# Probably cannot find Plot_Manager on the finder
			ansx = Fitter.fit(dsxaxis, dsx, GeneticAlg(0.001), [ gaussian, Offset( dsx.min(),dsx.max() ) ] )
		#dsyaxis = dsyaxis.subSampleMean(dsy.dimensions[0]/2)
		#dsy = dsy.subSampleMean(dsy.dimensions[0]/2)
		#dsxaxis = dsxaxis.subSampleMean(dsx.dimensions[0]/2)
		#dsx = dsx.subSampleMean(dsx.dimensions[0]/2)		
		
		peaky = ansy.getParameters()[0].getValue()
		fwhmy = ansy.getParameters()[1].getValue()
		areay = ansy.getParameters()[2].getValue()
		offsety = ansy.getParameters()[3].getValue() / dsx.shape[0]
		
		peakx = ansx.getParameters()[0].getValue()
		fwhmx = ansx.getParameters()[1].getValue()
		areax = ansx.getParameters()[2].getValue()
		offsetx = ansx.getParameters()[3].getValue() / dsy.shape[0]
		
		background = (offsetx+offsety)/2.
		fwhmarea = fwhmy*fwhmx*pi/4
		topy = areay / fwhmy
		topx = areax / fwhmx
		
		if xoffset==None:
			xoffset=0
		
		if yoffset==None:
			yoffset=0
		
		return background, peakx+xoffset, peaky+yoffset, topx, topy, fwhmx, fwhmy, fwhmarea

