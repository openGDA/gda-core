from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp
from peakdetect import peakdet
from bisect import bisect_left, bisect_right

class FullWidthHalfMaximum(XYDataSetFunction):

	def __init__(self, name='peak', labelList=('peakpos','peakvalue','peakbase','fwhm'),formatString='Peak value at %f (peakpos) was %f (peakvalue); %f (fwhm)'):
		XYDataSetFunction.__init__(self, name, labelList, 'peakpos', formatString)
	
	def _process(self,xDataSet, yDataSet):
		return self.singlePeakProcess(xDataSet, yDataSet)

	def multiplePeakProcess(self,xDataSet, yDataSet ):
		smoothness=1
		ymax=yDataSet.max()
		ymin=yDataSet.min()
		delta=(ymax-ymin)*10/100
		peakpoints=self.findPeaksAndTroughs(yDataSet, delta, xDataSet)[0]
		size=yDataSet.getSize()
		if size<10:
			smoothness=1
		elif size>10 and size<100:
			smoothness=2
		elif size>100 and size<1000:
			smoothness=5
		else:
			smoothness=10
		basepoints=self.findBases(xDataSet, yDataSet, delta, smoothness)
		peakswithbases=self.findbasesforpeaks(basepoints, peakpoints, delta)
		peaks=self.calculateFWHMs(xDataSet, yDataSet, peakswithbases)
		return peaks

		
	def singlePeakProcess(self,xDataSet, yDataSet ):
		ymax=yDataSet.max()
		ymaxindex=yDataSet.argMax()
		#print "y max index %d" % ymaxindex
		maxpos=xDataSet.getElementDoubleAbs(ymaxindex)
		basey=self.baseline(xDataSet, yDataSet, 1)
		halfmax=ymax/2+basey/2
		xcrossingvalues=dnp.crossings(yDataSet, halfmax, xDataSet)
		#print xcrossingvalues, maxpos
		if len(xcrossingvalues)>2:
			print "multiple peaks exists in the data set!, only process the highest peak."
		fwhmvalue=find_gt(xcrossingvalues, maxpos)-find_lt(xcrossingvalues,maxpos)
		return maxpos,ymax,basey,fwhmvalue
	
	def baseline(self,xdataset, ydataset, smoothness):
		'''find the baseline y value for a peak in y dataset'''
		ymaxindex=ydataset.argMax()
		result=dnp.derivative(xdataset, ydataset, smoothness)
		leftresult=result[:ymaxindex]
		rightresult=result[ymaxindex+1:]
		leftminderivativeindex=dnp.abs(leftresult).argMin()
		rightminderivativeindex=dnp.abs(rightresult).argMin()
		leftbasey=ydataset.getElementDoubleAbs(leftminderivativeindex)
		rightbasey=ydataset.getElementDoubleAbs(rightminderivativeindex+1+leftresult.shape[0])
		basey=(leftbasey+rightbasey)/2
		return basey
	
	def findPeaksAndTroughs(self, ydataset, delta, xdataset=None):
		'''returns a list of peaks and troughs in tuple of (peak_position, peak_value). 
		If x data set is not provided, it returns as tuple of (peak_index, peak_value)'''
		if xdataset != None:
			peaks,troughs=peakdet(ydataset[:], delta, xdataset[:])
		else:
			peaks,troughs=peakdet(ydataset[:], delta)
		return peaks, troughs
	
	def findBases(self, xdataset, ydataset, delta, smoothness):
		bases=[]
		peaks=self.findPeaksAndTroughs(ydataset, delta)[0]
		yslices=[]
		xslices=[]
		startindex=0
		for index,value in peaks: #@UnusedVariable
			yslices.append(ydataset[startindex:index])
			xslices.append(xdataset[startindex:index])
			startindex=index+1
		for xset, yset in xslices, yslices:
			result=dnp.derivative(xset, yset, smoothness)
			minimumderivativeindex=dnp.abs(result).argMin()
			bases.append((xset[minimumderivativeindex],yset[minimumderivativeindex]))
		return bases

	def findbasesforpeaks(self, bases, peaks, delta):
		peaksxyb=[]
		basexs=[x for x,y in bases]
		for peakx,peaky in peaks:
			leftbasey=bases[find_lt_index(basexs,peakx)][1]
			rightbasey=bases[find_gt_index(basexs,peakx)][1]
			if abs(rightbasey-leftbasey)>delta:
				print "overlapping peaks at x position %f" % peakx
				base=None
			else:
				base=(leftbasey+rightbasey)/2
			peaksxyb.append((peakx,peaky,base))
		return peaksxyb

	def calculateFWHMs(self, xDataSet, yDataSet, peaks):
		peakdatas=[]
		for peakx,peaky,peakbase in peaks:
			if peakbase is None:
				halfmax=None
				fwhm=None
			else:
				halfmax=peaky/2+peakbase/2
				xcrossingvalues=dnp.crossings(yDataSet, halfmax, xDataSet)
				leftcrossing=find_lt(xcrossingvalues, peakx)
				rightcrossing=find_gt(xcrossingvalues, peakx)
				fwhm=rightcrossing-leftcrossing
			peakdatas.append((peakx,peaky,peakbase,fwhm))
		return peakdatas

	
def find_lt_index(a,x):
	'Find the index of the rightmost value less than x'
	i = bisect_left(a, x)
	if i:
		return i-1
	raise ValueError

def find_le_index(a, x):
	'Find the index of the rightmost value less than or equal to x'
	i = bisect_right(a, x)
	if i:
		return i-1
	raise ValueError

def find_gt_index(a, x):
	'Find the index of the leftmost value greater than x'
	i = bisect_right(a, x)
	if i != len(a):
		return i
	raise ValueError

def find_ge_index(a, x):
	'Find index of the leftmost item greater than or equal to x'
	i = bisect_left(a, x)
	if i != len(a):
		return i
	raise ValueError

def index(a, x):
	'Locate the leftmost value exactly equal to x'
	i = bisect_left(a, x)
	if i != len(a) and a[i] == x:
		return i
	raise ValueError

def find_lt(a, x):
	'Find rightmost value less than x'
	i = bisect_left(a, x)
	if i:
		return a[i-1]
	raise ValueError

def find_le(a, x):
	'Find rightmost value less than or equal to x'
	i = bisect_right(a, x)
	if i:
		return a[i-1]
	raise ValueError

def find_gt(a, x):
	'Find leftmost value greater than x'
	i = bisect_right(a, x)
	if i != len(a):
		return a[i]
	raise ValueError

def find_ge(a, x):
	'Find leftmost item greater than or equal to x'
	i = bisect_left(a, x)
	if i != len(a):
		return a[i]
	raise ValueError
