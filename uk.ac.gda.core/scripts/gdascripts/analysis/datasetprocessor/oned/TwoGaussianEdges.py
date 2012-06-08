from XYDataSetProcessor import XYDataSetFunction
from GaussianPeakAndBackground import GaussianPeak
from gda.analysis.functions import Gaussian, Offset
from gda.analysis import ScanFileHolder
from gda.analysis.utils import GeneticAlg, NelderMead

from gda.analysis import DataSetFunctionFitter

try:
	from gda.analysis import Fitter # from swingclient plugin not available to PyDev tests
	def fitplot(*args):
		return Fitter.plot(*args)
except ImportError:
	def fitplot(*args):
		return DataSetFunctionFitter.fit(*args)


class TwoGaussianEdges(XYDataSetFunction):

	def __init__(self, smoothwidth=1, name='edges', labelList=('upos', 'ufwhm', 'dpos', 'dfwhm', 'area', 'fwhm'), formatString='upos:%f ufwhm:%f dpos:%f dfwhm:%f %f:area %f:fwhm'):
		XYDataSetFunction.__init__(self, name, labelList, 'upos', formatString)
		self.smoothwidth = smoothwidth
	
	def coarseProcess(self, xDataSet, dyDataSet):
		# For fallback only:
		
		
		upos = xDataSet[dyDataSet.maxPos()[0]]
		dpos = xDataSet[dyDataSet.minPos()[0]]
		sfh = ScanFileHolder()
		
		# Positive peak (up edge)
		upCrossings = sfh.getInterpolatedX(xDataSet, dyDataSet, dyDataSet.max() / 3.)
		if len(upCrossings) >= 2:
			ufwhm = abs(upCrossings[1] - upCrossings[0])
		else:
			ufwhm = .01 * (xDataSet.max() - xDataSet.min())
		uarea = dyDataSet.max() * ufwhm
		
		# Negative peak (down edge)
		downCrossings = sfh.getInterpolatedX(xDataSet, dyDataSet, dyDataSet.min() / 3.)
		if len(downCrossings) >= 2:
			dfwhm = abs(downCrossings[1] - downCrossings[0])
		else:
			dfwhm = .01 * (xDataSet.max() - xDataSet.min())
		darea = dyDataSet.min() * dfwhm
			
			
		#print 'upos', upos
		#print 'ufwhm', ufwhm
		#print 'uarea', uarea
		#print 'dpos', dpos
		#print 'dfwhm', dfwhm
		#print 'darea', darea		
		return upos, ufwhm, uarea, dpos, dfwhm , darea
	
	def _process(self, xDataSet, yDataSet):	
		dyDataSet = yDataSet.diff(xDataSet, self.smoothwidth)
		
		uposC, ufwhmC, uareaC, dposC, dfwhmC, dareaC = self.coarseProcess(xDataSet, dyDataSet)
		
		gu = Gaussian([uposC, ufwhmC, uareaC])
		gd = Gaussian([dposC, dfwhmC, dareaC])		
		try:	
			r = fitplot(xDataSet, dyDataSet, GeneticAlg(1e-10), [gu, gd])
		except: #java.lang.IllegalArgumentException: cannot find the Plot_Manager object of type PlotManager
			r = DataSetFunctionFitter().fit(xDataSet, dyDataSet, GeneticAlg(1e-10), [gu, gd])
			r = r.functionOutput
		#print r.disp()
	
		upos = r[0].getValue()
		ufwhm = r[1].getValue()
		uarea = r[2].getValue()
		dpos = r[3].getValue()
		dfwhm = r[4].getValue()
		darea = r[5].getValue()
		# TODO: Check for Nones here
		if darea>=0:
			print "Area under slope of downward edge should be negative!"
		if uarea <= 0:
			print "Area under slope of upward edge should be positive!"	
		darea = abs(darea)
		
		if 	uarea > darea:
			if darea > .2 * uarea:
				area = (uarea + darea) / 2.
				fwhm = (ufwhm + dfwhm) / 2.
			else:
				dpos = 0
				dfwhm = 0
				area = uarea
				fwhm = ufwhm
				
		else: #	darea > uarea:
			if uarea > .2 * darea:
				area = (uarea + darea) / 2.
				fwhm = (ufwhm + dfwhm) / 2.
			else:
				upos = 0
				ufwhm = 0
				area = darea
				fwhm = dfwhm				
	
		return upos, ufwhm, dpos, dfwhm , area, fwhm
