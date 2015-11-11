from XYDataSetProcessor import XYDataSetFunction
from gdascripts.analysis.datasetprocessor.oned.GaussianPeakAndBackground import  GaussianPeak
from org.eclipse.dawnsci.analysis.dataset.impl import Maths

class GaussianEdge(XYDataSetFunction):

	def __init__(self, name='edge', labelList=('pos','slope', 'fwhm', 'residual'),formatString='Edge at %f (pos) with slope proportional to: %f, fwhm: %f and residual: %f', plotPanel=None, keyxlabel='pos'):
		XYDataSetFunction.__init__(self, name, labelList, keyxlabel, formatString)
		self.smoothwidth = 1
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		dyDataSet = Maths.derivative(xDataSet._jdataset(), yDataSet._jdataset(), self.smoothwidth)
		minVal, maxVal = dyDataSet.min(), dyDataSet.max()
		if maxVal - minVal == 0:
			raise ValueError("There is no edge")

		labels = [label if label != 'slope' else 'top' for label in self.labelList]
		return GaussianPeak(self.name, labels, self.formatString, self.plotPanel)._process(xDataSet, dyDataSet)
