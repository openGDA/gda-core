from XYDataSetProcessor import XYDataSetFunction


class CentreOfMass(XYDataSetFunction):

	def __init__(self, name='com', labelList=('com', 'stddev'), formatString='The COM or centroid was at %f (com) and had a std. dev. of %f (stddev)'):
		XYDataSetFunction.__init__(self, name, labelList,'com', formatString)

	def _process(self, xDataset, yDataset):
		# Calculate COM
		comTotal = (xDataset * yDataset).sum()
		total = yDataset.sum()
		com = comTotal / total
		
		# Calculate second moment
		diff = xDataset - com
		second_moment = (diff * diff * yDataset).sum()
		
		# Normalise second moment
		normalised_second_moment = second_moment / total
		
		return com, normalised_second_moment