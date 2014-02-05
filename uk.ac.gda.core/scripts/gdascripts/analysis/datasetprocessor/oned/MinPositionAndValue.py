from XYDataSetProcessor import XYDataSetFunction

class MinPositionAndValue(XYDataSetFunction):

	def __init__(self, name='minval', labelList=('minpos','minval'),formatString='Minimum value at %f (minpos) was %f (minval)'):
		XYDataSetFunction.__init__(self, name, labelList,'minpos', formatString)
	
	def _process(self,xDataSet, yDataSet):
		return ( xDataSet.get(yDataSet.minPos()[0]), yDataSet.min() )