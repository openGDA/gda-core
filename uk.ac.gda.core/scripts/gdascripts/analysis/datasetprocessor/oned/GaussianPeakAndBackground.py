from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp


class _GaussianPeak(XYDataSetFunction):
	def __init__(self, name, labelList, formatString, plotPanel, offset, keyxlabel):
		XYDataSetFunction.__init__(self, name, labelList, keyxlabel, formatString)
		self.plotPanel = plotPanel
		self.offset = offset

	def _process(self, xDataset, yDataset):
		if yDataset.max()-yDataset.min() == 0:
			raise ValueError("There is no peak")

		x, y = toDnpArrays(xDataset, yDataset)
		fitResult = self.getFitResult(x,y)
		if self.plotPanel != None:
			plotGaussian(x, fitResult, self.plotPanel)
		results = self.getResults(fitResult)
		return [results.get(label, float('NaN')) for label in self.labelList]

	def getFitResult(self, x, y):
		funcs = getFitFunctions(self.offset)
		initial = gaussianInitialParameters(x, y, offset=self.offset)
		fitResult_p = dnp.fit.fit(funcs, x, y, initial, bounds=gaussianBounds(x, y, offset=self.offset), optimizer='global')
		fitResult_n = dnp.fit.fit(funcs, x, y, initial, bounds=gaussianBounds(x, y, negative_peak=True, offset=self.offset), optimizer='global')
		return fitResult_p if fitResult_p.residual < fitResult_n.residual else fitResult_n


class GaussianPeakAndBackground(_GaussianPeak):

	def __init__(self, name='peak', labelList=('pos','offset','top', 'fwhm', 'residual'),formatString='Gaussian at %f (pos) with offset: %f, top: %f, fwhm: %f and residual: %f', plotPanel=None, keyxlabel='pos'):
		_GaussianPeak.__init__(self, name, labelList, formatString, plotPanel, offset=True, keyxlabel=keyxlabel)

	def getResults(self, fitResult):
		peak, fwhm, area, offset = fitResult.parameters[:4]
		residual = fitResult.residual
		top = area / fwhm
		return {'pos': peak, 'offset': offset, 'top': top, 'fwhm': fwhm,'residual': residual}


class GaussianPeak(_GaussianPeak):

	def __init__(self, name='peak', labelList=('pos','top', 'fwhm','residual'), formatString='Gaussian at %f (pos) with top: %f, fwhm: %f and residual: %f', plotPanel=None, keyxlabel='pos'):
		_GaussianPeak.__init__(self, name, labelList, formatString, plotPanel, offset=False, keyxlabel=keyxlabel)

	def getResults(self, fitResult):
		peak, fwhm, area = fitResult.parameters[:3]
		residual = fitResult.residual
		top = area / fwhm
		return {'pos': peak, 'top': top, 'fwhm': fwhm, 'residual': residual}


def gaussianInitialParameters(x, y, offset=False):
	initialParameters = [x.mean(), x.ptp()*.5, x.ptp()*y.ptp()]
	if offset:
		initialParameters += [y.mean()]
	return initialParameters

def getFitFunctions(offset):
	funcs = [dnp.fit.function.gaussian]
	if offset:
		funcs += [dnp.fit.function.offset]
	return funcs

def gaussianBounds(x, y, negative_peak=False, offset=False):
	bounds = [(x.min(), x.max()), (0, x.ptp())]
	bounds += [(x.ptp()*y.ptp()*-1, 0, x.ptp()*y.ptp())[negative_peak:negative_peak+2]]
	if offset:
		bounds += [(y.min(), y.max())]
	return bounds

def toDnpArrays(*args):
	return [dnp.array(arg) for arg in args]

def plotGaussian(x, fitResult, plotPanel):
	plotData = fitResult.makeplotdata()
	dnp.plot.line(x, toDnpArrays(plotData[0], plotData[1], plotData[2], plotData[3]),
							name=plotPanel)
