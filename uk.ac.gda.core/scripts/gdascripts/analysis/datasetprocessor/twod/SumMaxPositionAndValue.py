from TwodDataSetProcessor import TwodDataSetProcessor
from org.slf4j import LoggerFactory

class SumMaxPositionAndValue(TwodDataSetProcessor):
	def __init__(self, name='max',
				 labelList=('maxx','maxy','maxval', 'sum'),
				 keyxlabel='maxx', 
				 keyylabel='maxy', 
				 formatString='Maximum value found to be at %f,%f (maxx,maxy) was %f (maxval). Sum was %f (sum)'
				 ):
		self.logger = LoggerFactory.getLogger("SumMaxPositionAndValue:%s" % name)
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
	
	def _process(self, ds, dsxaxis, dsyaxis):
		self.logger.trace("_process({}, {}, {}) shape={}, getElementsPerItem={} Dtype={}", [
			ds, dsxaxis, dsyaxis, ds.shape, ds.getElementsPerItem(), ds.getDtype()])
		#dsysize, dsxsize = ds.shape
		#assert(dsyaxis==dsysize)		
		#assert(dsxaxis==dsxsize)
		#assert(dsyaxis is None)		# STUB
		#assert(dsxaxis is None)		# STUB
		summation = ds.sum()
		maxval = ds.max()
		yi, xi = ds.maxPos()
		# interpolate
#		sfh = ScanFileHolder()
#		dsxi = DataSet.arange(dsxsize)
#		dsyi = DataSet.arange(dsysize)
#		y = sfh.interpolatedX(dsyaxis, dsyi, yi)
#		y = sfh.interpolatedX(dsxaxis, dsxi, yi)		
#		x = dsyaxis[xi]		
		return ( xi, yi, maxval, summation )