from gdascripts.analysis.datasetprocessor.oned.XYDataSetProcessor import XYDataSetFunction
import scisoftpy as np
from uk.ac.gda.server.ncd.optimiser import LadderSampleFinder

class LadderFinder(XYDataSetFunction):

	def __init__(self, name='ladder', labelList=('samples', 'positions'),formatString='if you see this string report to it support', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'samples', formatString)
		self.plotPanel = plotPanel
		self.javafinder = LadderSampleFinder()

	def _getbaseline(self, xDataSet, yDataSet):
		return 0
	
	def _process(self, xDataSet, yDataSet):
	
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		xDataSet = np.Sciwrap(xDataSet) 
		yDataSet = np.Sciwrap(yDataSet)

		self.javafinder.setPlotPanel(self.plotPanel)
		
		positions = self.javafinder.process(xDataSet.data, yDataSet.data)
			
		return len(positions), positions