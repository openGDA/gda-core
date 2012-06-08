from XYDataSetProcessor import XYDataSetFunction

class MaxPositionAndValue(XYDataSetFunction):

	def __init__(self, name='maxval', labelList=('maxpos','maxval'),formatString='Maximum value at %f (maxpos) was %f (maxval)'):
		XYDataSetFunction.__init__(self, name, labelList, 'maxpos', formatString)
	
	def _process(self,xDataSet, yDataSet):
		return ( xDataSet[int(yDataSet.maxPos()[0])], yDataSet.max() )