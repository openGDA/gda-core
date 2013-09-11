from XYDataSetProcessor import XYDataSetFunction
import scisoftpy as dnp
from peakdetect import peakdet
from bisect import bisect_left, bisect_right

class ExtractPeakParameters(XYDataSetFunction):

    def __init__(self, name='peak', labelList=('peakpos','peakvalue','fwhm'),formatString='Peak value at %f (peakpos) was %f (peakvalue); %f (fwhm)'):
        XYDataSetFunction.__init__(self, name, labelList, 'peakpos', formatString)
        self.delta=None
        self.smoothness=None
    
    def _process(self,xDataSet, yDataSet):
        return self.multiplePeakProcess(xDataSet, yDataSet)

    def setDelta(self, delta):
        self.delta=delta
        
    def setSmoothness(self, smoothness):
        self.smoothness=smoothness
        
    def multiplePeakProcess(self,xDataSet, yDataSet ):
        smoothness=1
        ymax=yDataSet.max()
        ymin=yDataSet.min()
        if self.delta is None:
            self.delta=(ymax-ymin)*1.0/100
        peakpoints=self.findPeaksAndTroughs(yDataSet, self.delta, xDataSet)[0]
        if smoothness is None:
            size=yDataSet.getSize()
            if size<10:
                smoothness=1
            elif size>10 and size<100:
                smoothness=2
            elif size>100 and size<1000:
                smoothness=5
            elif size > 1000 and size < 10000:
                smoothness=10
            else:
                smoothness=50
        basepoints=self.findBasePoints(xDataSet, yDataSet, self.delta, smoothness)
        peakswithbases=self.findbasesforpeaks(basepoints, peakpoints, self.delta)
        peaks=self.calculateFWHMs(xDataSet, yDataSet, peakswithbases)
        #print peaks
        return peaks

        
    def singlePeakProcess(self, xDataSet, yDataSet):
        xarray = dnp.asarray(xDataSet)
        yarray = dnp.asarray(yDataSet)
        ymax=yarray.max()
        ymaxindex=yarray.argmax()
        #print "y max index %d" % ymaxindex
        maxpos=xarray[ymaxindex]
        basey=self.baseline(xarray, yarray, 1)
        halfmax=ymax/2+basey/2
        xcrossingvalues=dnp.crossings(yarray, halfmax, xarray)
        #print xcrossingvalues, maxpos
        if len(xcrossingvalues)>2:
            print "multiple peaks exists in the data set!, only process the highest peak."
        fwhmvalue=find_gt(xcrossingvalues, maxpos)-find_lt(xcrossingvalues,maxpos)
        return [(maxpos,ymax,basey,fwhmvalue)]
    
    def baseline(self,xdataset, ydataset, smoothness):
        '''find the baseline y value for a peak in y dataset'''
        xdataset = dnp.asarray(xdataset)
        ydataset = dnp.asarray(ydataset)
        ymaxindex=ydataset.argmax()
        #TODO
        result=dnp.gradient(ydataset,xdataset)
        #derivative(xdataset, ydataset, smoothness)
        leftresult=result[:ymaxindex]
        rightresult=result[ymaxindex+1:]
        leftminderivativeindex=dnp.abs(leftresult).argmin()
        rightminderivativeindex=dnp.abs(rightresult).argmin()
        leftbasey=ydataset[leftminderivativeindex]
        rightbasey=ydataset[rightminderivativeindex+1+leftresult.shape[0]]
        basey=(leftbasey+rightbasey)/2
        return basey
    
    def findPeaksAndTroughs(self, ydataset, delta, xdataset=None):
        '''returns a list of peaks and troughs in tuple of (peak_position, peak_value). 
        If x data set is not provided, it returns as tuple of (peak_index, peak_value)'''
        
        if xdataset is not None:
            xdataset = dnp.asarray(xdataset)
        return peakdet(dnp.asarray(ydataset), delta, xdataset)
    
    def findBasePoints(self, xdataset, ydataset, delta, smoothness):
        xdataset = dnp.asarray(xdataset)
        ydataset = dnp.asarray(ydataset)
        peaks=self.findPeaksAndTroughs(ydataset, delta)[0]
        #print peaks
        yslices=[]
        xslices=[]
        startindex=0
        for index,value in peaks: #@UnusedVariable
            yslices.append(ydataset[startindex:index])
            xslices.append(xdataset[startindex:index])
            startindex=index+1
        yslices.append(ydataset[startindex:])
        xslices.append(xdataset[startindex:])

        bases=[]
        for xset, yset in zip(xslices, yslices):
            result=dnp.gradient(yset, xset)
            minimumderivativeindex=dnp.abs(result).argmin()
            bases.append((xset[minimumderivativeindex],yset[minimumderivativeindex]))
        #print "Base Points (position, value)   : ", bases
        return bases

    def findbasesforpeaks(self, bases, peaks, delta):
        peaksxyb=[]
        basexs=[x for x,y in bases] #@UnusedVariable
        for peakx,peaky in peaks:
            leftbasey=bases[find_lt_index(basexs,peakx)][1]
            rightbasey=bases[find_gt_index(basexs,peakx)][1]
            if abs(rightbasey-leftbasey)>delta:
                print "\noverlapping peaks at x position %f\n" % peakx
                base=None
            else:
                base=(leftbasey+rightbasey)/2
            peaksxyb.append((peakx,peaky,base))
        return peaksxyb

    def calculateFWHMs(self, xDataSet, yDataSet, peaks):
        peakdatas=[]
        for peakx,peaky,peakbase in peaks:
            if peakbase is None:
                fwhm=None
            else:
                halfmax=peaky/2+peakbase/2
                xcrossingvalues=dnp.crossings(yDataSet, halfmax, xDataSet)
                leftcrossing=find_lt(xcrossingvalues, peakx)
                rightcrossing=find_gt(xcrossingvalues, peakx)
                fwhm=rightcrossing-leftcrossing
            peakdatas.append((peakx,peaky,fwhm))
        print "\nPeak Parameters (position, value, fwhm) : ", peakdatas
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
