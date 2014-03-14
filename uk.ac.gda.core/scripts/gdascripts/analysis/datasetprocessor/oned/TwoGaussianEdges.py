from XYDataSetProcessor import XYDataSetFunction
from GaussianPeakAndBackground import GaussianPeak
from gda.analysis.functions import Gaussian, Offset
from gda.analysis import ScanFileHolder, DataSet
from gda.analysis.utils import GeneticAlg, NelderMead

import scisoftpy as dnp

from gda.analysis import DataSetFunctionFitter
import java.lang.IllegalArgumentException

try:
	from gda.analysis import Fitter # from swingclient plugin not available to PyDev tests
	def fitplot(*args):
		return Fitter.plot(*args)
except ImportError:
	def fitplot(*args):
		return DataSetFunctionFitter.fit(*args)

try:
	from gda.analysis import Plotter  # for Swing
except ImportError:
	Plotter = None


class TwoGaussianEdges(XYDataSetFunction):

	def __init__(self, smoothwidth=1, name='edges', labelList=('upos', 'ufwhm', 'dpos', 'dfwhm', 'area', 'fwhm'), formatString='upos:%f ufwhm:%f dpos:%f dfwhm:%f %f:area %f:fwhm'):
		XYDataSetFunction.__init__(self, name, labelList, 'upos', formatString)
		self.smoothwidth = smoothwidth
	
	def coarseProcess(self, xDataSet, dyDataSet):
		
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
			
		return upos, ufwhm, uarea, dpos, dfwhm , darea
	
	def _process(self, xDataSet, yDataSet):	
		dyDataSet = yDataSet.diff(xDataSet, self.smoothwidth)
		
		uposC, ufwhmC, uareaC, dposC, dfwhmC, dareaC = self.coarseProcess(xDataSet, dyDataSet)
# 		
		gaussian = dnp.fit.function.gaussian
		x = dnp.array(xDataSet)
		dy = dnp.array(dyDataSet)

		if abs(dareaC) < .2 * uareaC:
			print "only upward edge present"
			r = dnp.fit.fit([gaussian], x, dy, 
							[uposC, ufwhmC, uareaC],
							ptol=1e-10, optimizer='apache_nm')
			upos, ufwhm, _uarea = r.parameters
			dpos = 0.
			dfwhm = 0.
			area = _uarea
			fwhm = ufwhm
		
		elif uareaC < .2 * abs(dareaC):
			print "only downward edge present"
			r = dnp.fit.fit([gaussian], x, dy, 
							[dposC, dfwhmC, dareaC],
							ptol=1e-10, optimizer='apache_nm')
			dpos, dfwhm, _darea = r.parameters
			upos = 0.
			ufwhm = 0.
			area = abs(_darea)
			fwhm = dfwhm
		
		else:
			print "both edges present"
			r = dnp.fit.fit([gaussian, gaussian], x, dy, 
							[uposC, ufwhmC, uareaC,dposC, dfwhmC, dareaC],
							ptol=1e-10, optimizer='apache_nm')
			upos, ufwhm, _uarea, dpos, dfwhm, _darea = r.parameters
			area = (_uarea + abs(_darea)) / 2.
			fwhm = (ufwhm + dfwhm) / 2.

		# Plot to swing	
		if Plotter:  # Not always on path
			yaxes = [dyDataSet]
			for funcset in r.makefuncdata():
				yaxes.append(DataSet(list(funcset)))
		try:
			Plotter.plot('Data Vector', xDataSet, yaxes)
		except java.lang.IllegalArgumentException:
			# Probably cannot find Plot_Manager on the finder
			print "WARNING: TwoGaussianEdges could not plot fit details as there is no Plot_Manager"
		
		# Plot to RCP
		try:
			r.plot()
		except java.lang.NoClassDefFoundError:
			print "WARNING: TwoGaussianEdges could not plot fit details to RCP client"
		
	
		return upos, ufwhm, dpos, dfwhm , area, fwhm
