from TwodDataSetProcessor import TwodDataSetProcessor
from gda.analysis import ScanFileHolder
from gda.analysis import DataSet



class PixelIntensity(TwodDataSetProcessor):
	def __init__(self, name='intensity',
				 labelList=('mean','stddev'),
				 keyxlabel='NotApplicable', 
				 keyylabel='NotApplicable', 
				 formatString='Intensity mean was %f and stddev was %f'
				 ):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
	
	def _process(self, ds, dsxaxis, dsyaxis):
		dsysize, dsxsize = ds.shape
		return ( ds.mean(), ds.std() )
