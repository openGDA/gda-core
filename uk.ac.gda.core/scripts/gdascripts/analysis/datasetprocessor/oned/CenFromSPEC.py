from XYDataSetProcessor import XYDataSetFunction
from gda.analysis import ScanFileHolder
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
		
		xlist = list(xDataSet.data)
		ylist = list(yDataSet.data)
		
		height = max(ylist)
		index_max_val = ylist.index(height)
		
		counts_left = sum(ylist[:index_max_val]) + height / 2.
		index_left_edge = index_max_val - (counts_left / height)
		pos_left_edge = interp(xlist, index_left_edge)

		counts_right = sum(ylist[index_max_val+1:]) + height / 2.
		index_right_edge = index_max_val + (counts_right / height)		
		pos_right_edge = interp(xlist, index_right_edge)

		cen = (pos_left_edge + pos_right_edge) /2. 

		width = pos_right_edge - pos_left_edge

		return cen, height, width