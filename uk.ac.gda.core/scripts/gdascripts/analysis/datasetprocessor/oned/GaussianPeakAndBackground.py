from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp

class GaussianPeakAndBackground(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','offset','top', 'fwhm', 'residual'),formatString='Gaussian at %f (pos) with offset: %f, top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")

		x, y = toDnpArrays(xDataSet, yDataSet)

		fitResult = dnp.fit.fit([dnp.fit.function.gaussian, dnp.fit.function.offset], x, y, gaussianWithOffsetInitialParameters(x, y), bounds=gaussianWithOffsetBounds(x, y), optimizer='global')

		peak, fwhm, area, offset = fitResult.parameters[:4]
		residual = fitResult.residual
		top = area/fwhm

		if self.plotPanel != None:
			plotGaussian(x, fitResult, self.plotPanel)
		return peak, offset, top, fwhm, residual
	
class GaussianPeak(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','top', 'fwhm','residual'),formatString='Gaussian at %f (pos) with top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")

		x, y = toDnpArrays(xDataSet, yDataSet)
		
		fitResult = dnp.fit.fit([dnp.fit.function.gaussian], x, y, gaussianInitialParameters(x, y), bounds=gaussianBounds(x, y), optimizer='global')
		
		peak, fwhm, area = fitResult.parameters[:3]
		residual = fitResult.residual
		top = area / fwhm
		
		if self.plotPanel != None:
			plotGaussian(x, fitResult, self.plotPanel)
		return peak, top, fwhm, residual

def gaussianInitialParameters(x, y):
	return [x.mean(), x.ptp()*.5, x.ptp()*y.ptp()]

def gaussianWithOffsetInitialParameters(x,y):
	return gaussianInitialParameters(x,y) + [y.mean()]

def gaussianBounds(x, y):
	return [(x.min(), x.max()), (0, x.ptp()), (x.ptp()*y.ptp()*-1, x.ptp()*y.ptp())]

def gaussianWithOffsetBounds(x, y):
	return gaussianBounds(x,y) + [(y.min(), y.max())]

def toDnpArrays(*args):
	l = []
	for arg in args:
		l.append(dnp.array(arg))
	return l

def plotGaussian(x, fitResult, plotPanel):
	dnp.plot.line(x, [	dnp.array(fitResult.makeplotdata()[0]),
								dnp.array(fitResult.makeplotdata()[1]),
								dnp.array(fitResult.makeplotdata()[2]),
								dnp.array(fitResult.makeplotdata()[3])],
							name=plotPanel)
