from XYDataSetProcessor import XYDataSetFunction
from scisoftpy.jython.jymaths import crossings
from uk.ac.diamond.scisoft.analysis.dataset import DoubleDataset

class FullWidthHalfMaximum(XYDataSetFunction):

	def __init__(self, name='fwhm', labelList=('maxpos','fwhm'),formatString='FWHM value at %f (maxpos) was %f (fwhm)'):
		XYDataSetFunction.__init__(self, name, labelList, 'maxpos', formatString)
	
	def _process(self,xDataSet, yDataSet):
		ymax=yDataSet.max()
		maxpos=xDataSet.getElementDoubleAbs(int(yDataSet.maxPos()[0]))
		xcrossingvalues=DoubleDataset.createFromList(list(crossings(yDataSet, ymax/2, xDataSet)))
		#print xcrossingvalues, maxpos
		if xcrossingvalues.size>2:
			print "multiple peaks exists in the data set!, only process the highest peak."
		fwhmvalue=findFirstValuesGreaterThanorEqualTo(xcrossingvalues, maxpos)-findFirstValuesLessThanorEqualTo(xcrossingvalues,maxpos)
		return maxpos, fwhmvalue
	

import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils as _utils

def findFirstValuesGreaterThanorEqualTo(y, value):
	'''Finds the first valuein a dataset that is greater than or equal to the given value'''
	return y.getElementDoubleAbs(int(_utils.findIndexGreaterThanorEqualTo(y, value)))

def findFirstValuesLessThanorEqualTo(y, value):
	'''Finds the first valuein a dataset that is greater than or equal to the given value'''
	return y.getElementDoubleAbs(int(_utils.findIndexLessThanorEqualTo(y, value)))
