from TwodDataSetProcessor import TwodDataSetProcessor

from gda.analysis.functions.dataset import Integrate2D
from gda.analysis import DataSet
from gda.analysis.utils import GeneticAlg
from gda.analysis.functions import Gaussian, Offset

from math import pi
import java.lang.IllegalArgumentException

from gda.analysis import DataSetFunctionFitter

try:
	from gda.analysis import Fitter # from swingclient plugin not available to PyDev tests
	def fitplot(*args):
		return Fitter.plot(*args)
except ImportError:
	def fitplot(*args):
		return DataSetFunctionFitter().fit(*args).functionOutput

class TwodGaussianPeak(TwodDataSetProcessor):


	def __init__(self, name='peak',
				labelList=('background','peakx','peaky', 'topx', 'topyy','fwhmx','fwhmy', 'fwhmarea' ),
				keyxlabel = 'peakx',
				keyylabel = 'peaky',
				formatString=" background:%f, peakx:%f, peaky:%f, topx:%f, topy:%f, fwhmx:%f, fwhmy:%f, whmarea:%f"
				):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
		self.maxwidth = None
		
	def _process(self, ds, xoffset=0, yoffset=0, DataSet=DataSet):###dsxaxis, dsyaxis):
##		assert(dsyaxis is None)		# STUB
##		assert(dsxaxis is None)		# STUB
		
		integrator = Integrate2D()
		dsy, dsx = integrator.execute(ds)
		dsyaxis = DataSet.arange(dsy.shape[0])
		dsxaxis = DataSet.arange(dsx.shape[0])
		
		gaussian = Gaussian(dsyaxis.min(), dsyaxis.max(), dsyaxis.max()-dsyaxis.min(), (dsyaxis.max()-dsyaxis.min())*(dsy.max()-dsy.min()) )
		gaussian.getParameter(2).setLowerLimit(0)
		if self.maxwidth is not None:
			gaussian.getParameter(1).setUpperLimit(self.maxwidth)
		ansy = DataSetFunctionFitter().fit( dsyaxis, dsy, GeneticAlg(.001), [ gaussian, Offset( dsy.min(),dsy.max() ) ] )
		ansy = ansy.functionOutput
		
		gaussian = Gaussian(dsxaxis.min(), dsxaxis.max(), dsxaxis.max()-dsxaxis.min(), (dsxaxis.max()-dsxaxis.min())*(dsx.max()-dsx.min()) )
		gaussian.getParameter(2).setLowerLimit(0)
		if self.maxwidth is not None:
			gaussian.getParameter(1).setUpperLimit(self.maxwidth)
		try:
			ansx = fitplot( dsxaxis, dsx, GeneticAlg(.001), [ gaussian, Offset( dsx.min(),dsx.max() ) ] )
		except java.lang.IllegalArgumentException:
			# Probably cannot find Plot_Manager on the finder
			ansx = DataSetFunctionFitter().fit( dsxaxis, dsx, GeneticAlg(.001), [ gaussian, Offset( dsx.min(),dsx.max() ) ] )
			ansx = ansx.functionOutput
		#dsyaxis = dsyaxis.subSampleMean(dsy.dimensions[0]/2)
		#dsy = dsy.subSampleMean(dsy.dimensions[0]/2)
		#dsxaxis = dsxaxis.subSampleMean(dsx.dimensions[0]/2)
		#dsx = dsx.subSampleMean(dsx.dimensions[0]/2)		
		
		peaky = ansy[0].getValue()
		fwhmy = ansy[1].getValue()
		areay = ansy[2].getValue()
		offsety = ansy[3].getValue() / dsx.shape[0]
		
		peakx = ansx[0].getValue()
		fwhmx = ansx[1].getValue()
		areax = ansx[2].getValue()
		offsetx = ansx[3].getValue() / dsy.shape[0]
		
		background = (offsetx+offsety)/2.
		fwhmarea = fwhmy*fwhmx*pi/4
		topy = areay / fwhmarea
		topx = areax / fwhmarea
		
		if xoffset==None:
			xoffset=0
		
		if yoffset==None:
			yoffset=0
		
		return background, peakx+xoffset, peaky+yoffset, topx, topy, fwhmx, fwhmy, fwhmarea

