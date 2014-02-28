from XYDataSetProcessor import XYDataSetFunction
from gdascripts.analysis.datasetprocessor.oned.GaussianPeakAndBackground import  GaussianPeak

class GaussianEdge(XYDataSetFunction):

	def __init__(self, name='edge', labelList=('pos','slope', 'fwhm', 'residual'),formatString='Edge at %f (pos) with slope proportional to: %f, and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.smoothwidth = 1
		self.plotPanel = plotPanel
	
	def _process(self,xDataSet, yDataSet):	
		dyDataSet = yDataSet.diff(xDataSet, self.smoothwidth)
		if dyDataSet.max()-dyDataSet.min() == 0:
			raise ValueError("There is no edge")
		
		fitResult = GaussianPeak(self.name, self.labelList, self.formatString, self.plotPanel)._process(xDataSet, dyDataSet)

		return fitResult
