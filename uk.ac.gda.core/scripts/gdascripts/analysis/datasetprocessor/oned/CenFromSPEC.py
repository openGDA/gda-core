from XYDataSetProcessor import XYDataSetFunction
import math

def interp(xlist, fractional_index):
	if fractional_index > len(xlist) -1 :
		step_size = xlist[-1] - xlist[-2]
		return xlist[-1] + step_size * (fractional_index - len(xlist) + 1) 
	elif fractional_index < 0:
		step_size = xlist[1] - xlist[0]
		return xlist[0] + step_size * fractional_index
	else:
		lower_index = int(math.floor(fractional_index))
		upper_index = int(math.ceil(fractional_index))
		fraction_toward_upper = fractional_index - lower_index
		return xlist[lower_index] + fraction_toward_upper * (xlist[upper_index] - xlist[lower_index]) 


class CenFromSPEC(XYDataSetFunction):
	"""See http://www.certif.com/spec_help/data.html%20for%20cfwhm
	1. choose max value
	2. create box to left and right containing all counts
	3. feature is at the middle of this box, also return height and width
	"""

	def __init__(self, name='cen', labelList=('cen', 'height', 'width'),formatString=''):
		XYDataSetFunction.__init__(self, name, labelList,'cen', formatString)
	
	def _process(self,xDataSet, yDataSet):
		height = yDataSet.max()
		height_idx = yDataSet.argmax()
		left_counts = yDataSet[:height_idx].sum() + height / 2.0
		left_edge_idx = height_idx - (left_counts / height)
		left_edge_pos = interp(xDataSet, left_edge_idx)
		right_counts = yDataSet[height_idx + 1:].sum() + height / 2.0
		right_edge_idx = height_idx + (right_counts / height)
		right_edge_pos = interp(xDataSet, right_edge_idx)

		cen = (left_edge_pos + right_edge_pos) / 2.0
		width = right_edge_pos - left_edge_pos
		return cen, height, width

