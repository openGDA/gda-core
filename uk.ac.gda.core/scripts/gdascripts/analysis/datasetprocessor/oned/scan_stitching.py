from XYDataSetProcessor import XYDataSetFunction

class Lcen(XYDataSetFunction):

	def __init__(self, name='lcen', labelList=('lcen',),formatString='Center of next scan to left at %f'):
		XYDataSetFunction.__init__(self, name, labelList, 'lcen', formatString)
	
	def _process(self,xDataSet, yDataSet):
		start = xDataSet[0]
		stop = xDataSet[-1]
		step = xDataSet[1] - xDataSet[0]
		return tuple([start - (stop-start)/2 - step])
	
class Rcen(XYDataSetFunction):

	def __init__(self, name='rcen', labelList=('rcen',),formatString='Center of next scan to right at %f'):
		XYDataSetFunction.__init__(self, name, labelList, 'rcen', formatString)
	
	def _process(self,xDataSet, yDataSet):
		start = xDataSet[0]
		stop = xDataSet[-1]
		step = xDataSet[1] - xDataSet[0]
		return tuple([stop + (stop-start)/2 + step])