from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp


class MinPositionAndValue(XYDataSetFunction):

	def __init__(self, name='minval', labelList=('minpos','minval'),formatString='Minimum value at %f (minpos) was %f (minval)'):
		XYDataSetFunction.__init__(self, name, labelList,'minpos', formatString)

	def _process(self,xDataSet, yDataSet):
		return ( xDataSet[dnp.argmin(yDataSet)], yDataSet.min() )