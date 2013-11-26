from XYDataSetProcessor import XYDataSetFunction
from gda.analysis.functions import Gaussian
from gda.analysis.utils import GeneticAlg
import scisoftpy as np

from gda.analysis import DataSetFunctionFitter

class GaussianEdge(XYDataSetFunction):

	def __init__(self, name='edge', labelList=('pos','slope', 'fwhm'),formatString='Edge at %f (pos) with slope proportional to: %f, and fwhm: %f', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'pos', formatString)
		self.smoothwidth = 1
		self.plotPanel = plotPanel
	
	def _process(self,xDataSet, yDataSet):	
		dyDataSet = yDataSet.diff(xDataSet, self.smoothwidth)
		if dyDataSet.max()-dyDataSet.min() == 0:
			raise ValueError("There is no edge")
		
		res={}
		for mul in [-1 , 1]:
			dy = dyDataSet * mul
			gaussian = Gaussian(xDataSet.min(), xDataSet.max(), xDataSet.max()-xDataSet.min(), (xDataSet.max()-xDataSet.min())*(dy.max()-dy.min()) )
			ans = DataSetFunctionFitter().fit( xDataSet, dy, GeneticAlg(.001), [ gaussian ] )
			ans = ans.functionOutput
			cs = ans.getChiSquared()
			res[mul] = (cs, ans, dy)
		
		if res[-1][0] < res[1][0]:
			best = -1
		else:
			best = 1
			
		ans = res[best][1]
		
		pos= ans[0].getValue()
		fwhm = ans[1].getValue()
		area = ans[2].getValue()
		slope = area / fwhm
		if self.plotPanel != None:
			np.plot.line(xDataSet, [res[best][2], np.array(Gaussian([pos, fwhm, area]).makeDataSet([xDataSet]))], name=self.plotPanel)
		return pos, slope, fwhm
