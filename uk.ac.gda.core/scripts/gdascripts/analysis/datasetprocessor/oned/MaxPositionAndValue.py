from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp


class MaxPositionAndValue(XYDataSetFunction):

	def __init__(self, name='maxval', labelList=('maxpos','maxval'),formatString='Maximum value at %f (maxpos) was %f (maxval)'):
		XYDataSetFunction.__init__(self, name, labelList, 'maxpos', formatString)

	def _process(self,xDataSet, yDataSet):
		return ( xDataSet[dnp.argmax(yDataSet)], yDataSet.max() )