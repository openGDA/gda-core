from XYDataSetProcessor import XYDataSetFunction
from gda.analysis import ScanFileHolder

class CentreOfMass(XYDataSetFunction):

	def __init__(self, name='com', labelList=('com', 'stddev'),formatString='The COM or centroid was at %f (com) and had a std. dev. of %f (stddev)'):
		XYDataSetFunction.__init__(self, name, labelList,'com', formatString)
	
	def _process(self,xDataSet, yDataSet):
		com = ScanFileHolder().centroid(xDataSet, yDataSet)
		xlist = list(xDataSet.getBuffer())
		ylist = list(yDataSet.getBuffer())		
		
#		sum = 0
#		weighted_sum = 0
#		for x, y in zip(xlist,ylist):
#			sum += y
#			weighted_sum += x*y
#		mycom = weighted_sum/sum
		
		second_moment = 0
		sum = 0
		for x, y in zip(xlist,ylist):
			sum += y
			second_moment += pow(x-com, 2) * y
		
		normalised_second_moment = second_moment/sum
		
		return com, normalised_second_moment