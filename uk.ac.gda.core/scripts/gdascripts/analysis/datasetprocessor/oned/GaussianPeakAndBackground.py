from XYDataSetProcessor import XYDataSetFunction
from gda.analysis.functions import Gaussian, Offset
from gda.analysis import DataSetFunctionFitter

from gda.analysis.utils import GeneticAlg
import scisoftpy as np

class GaussianPeakAndBackground(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','offset','top', 'fwhm'),formatString='Gaussian at %f (pos) with offset: %f, top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		gaussian = Gaussian(xDataSet.min(), xDataSet.max(), xDataSet.max()-xDataSet.min(), (xDataSet.max()-xDataSet.min())*(yDataSet.max()-yDataSet.min()) )
		ans = DataSetFunctionFitter().fit( xDataSet, yDataSet, GeneticAlg(.001), [ gaussian, Offset( yDataSet.min(),yDataSet.max() ) ] )	
		ans = ans.functionOutput
		peak= ans[0].getValue()
		fwhm = ans[1].getValue()
		area = ans[2].getValue()
		offset = ans[3].getValue()
		top = area / fwhm
		if self.plotPanel != None:
			np.plot.line(xDataSet, [yDataSet, offset + np.array(Gaussian([peak, fwhm, area]).makeDataSet([xDataSet]))], name=self.plotPanel)
		return peak, offset, top, fwhm
	
	
class GaussianPeak(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('pos','top', 'fwhm'),formatString='Gaussian at %f (pos) with top: %f and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.plotPanel = plotPanel

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		gaussian = Gaussian(xDataSet.min(), xDataSet.max(), xDataSet.max()-xDataSet.min(), (xDataSet.max()-xDataSet.min())*(yDataSet.max()-yDataSet.min()) )
		ans = DataSetFunctionFitter().fit( xDataSet, yDataSet, GeneticAlg(.001), [ gaussian ] )	
		
		peak= ans[0].getValue()
		fwhm = ans[1].getValue()
		area = ans[2].getValue()
		top = area / fwhm
		if self.plotPanel != None:
			np.plot.line(xDataSet, [yDataSet, np.array(Gaussian([peak, fwhm, area]).makeDataSet([xDataSet]))], name=self.plotPanel)
		return peak, top, fwhm