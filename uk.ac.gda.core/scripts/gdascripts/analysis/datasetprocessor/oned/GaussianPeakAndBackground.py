from XYDataSetProcessor import XYDataSetFunction
from gda.analysis.functions import Gaussian, Offset
from gda.analysis import DataSetFunctionFitter
import scisoftpy as dnp

class GaussianPeakAndBackground(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','offset','top', 'fwhm'),formatString='Gaussian at %f (pos) with offset: %f, top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		
		dx = dnp.array(xDataSet)
		dy = dnp.array(yDataSet)
		
		bounds = [(dx.min(), dx.max()), (dx.ptp()*0.05, dx.ptp()), (dx.ptp()*dy.ptp()*0.05, dx.ptp()*dy.ptp()), (dy.min(), dy.max())]
		initialParameters = [dx.mean(), dx.ptp()*.5, dx.ptp()*dy.ptp()*.8, dy.mean()]
		fitResult = dnp.fit.fit([dnp.fit.function.gaussian, dnp.fit.function.offset], dx, dy, initialParameters, bounds=bounds, optimizer='global')
		peak = fitResult[0]
		fwhm= fitResult[1]
		area = fitResult[2]
		offset = fitResult[3]
		
		top = area/fwhm
		if self.plotPanel != None:
			dnp.plot.line(dx, [dy, dnp.array(fitResult.makefuncdata()[0])], name=self.plotPanel)

		return peak, offset, top, fwhm
	
	
class GaussianPeak(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','top', 'fwhm'),formatString='Gaussian at %f (pos) with top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		
		dx = dnp.array(xDataSet)
		dy = dnp.array(yDataSet)
		
		bounds = [(dx.min(), dx.max()), (dx.ptp()*0.05, dx.ptp()), (dx.ptp()*dy.ptp()*0.05, dx.ptp()*dy.ptp())]
		initialParameters = [dx.mean(), (dx.max() - dx.min())*.5, (dx.max() - dx.min())*(dy.max() - dy.min())*.8]
		fitResult = dnp.fit.fit([dnp.fit.function.gaussian], dx, dy, initialParameters, bounds=bounds, optimizer='global')
		peak = fitResult[0]
		fwhm= fitResult[1]
		area = fitResult[2]
		
		top = area / fwhm
		if self.plotPanel != None:
			dnp.plot.line(dx, [dy, dnp.array(fitResult.makefuncdata()[0])], name=self.plotPanel)
		return peak, top, fwhm