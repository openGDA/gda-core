from XYDataSetProcessor import XYDataSetFunction
from gda.analysis.functions import Gaussian
import scisoftpy as np
from uk.ac.diamond.scisoft.analysis.fitting import Generic1DFitter
from uk.ac.diamond.scisoft.analysis.optimize import NelderMead, ApacheNelderMead, GeneticAlg

def holefunc(centre, size, innervalue, outervalue, x, *arg):
#	print "height %5.5g" % outervalue
	rlist = []
	for val in x[0]:
		if abs(val-centre) <= abs(size/2.0):
			rlist.append(innervalue)
		else:
			rlist.append(outervalue)
	#print "i have been evaluated %f %f %f %f" %(centre, size, innervalue, outervalue)
	return np.array(rlist)

class GaussianPeaksInHole(XYDataSetFunction):

	def __init__(self, name='peaksinhole', labelList=('peaksinhole', 'positions'),formatString='if you see this string report to it support', plotPanel=None):
		XYDataSetFunction.__init__(self, name, labelList, 'peaksinhole', formatString)
		self.plotPanel = plotPanel
		self.scale = -1.0
		self.gaussians = 13
		self.maxfwhm = 0.3
		self.minarea = 0.001
		self.threshold = 0.05
		self.smoothing = 2
		self.accuracy = 0.01

	def _process(self,xDataSet, yDataSet):
		if yDataSet.max()-yDataSet.min() == 0:
			raise ValueError("There is no peak")
		xDataSet = np.Sciwrap(xDataSet) 
		yDataSet = np.Sciwrap(yDataSet) * self.scale
		funcs = [ holefunc ]
		initparams = [ xDataSet.mean(), (xDataSet.max() - xDataSet.min())* 0.90, yDataSet.min(), yDataSet.max()]
		
		fr = np.fit.fit(funcs, xDataSet, yDataSet, initparams, ptol=1e-4, optimizer='local')
		if self.plotPanel != None:
			fr.plot(self.plotPanel)

		initparams = [ p for p in fr.func.getParameterValues() ]
		
		start = initparams[0] - 0.5 * initparams[1]
		length = initparams[1]
	
		truncx = []
		truncy = []

		for i in range(xDataSet.size):
			val = xDataSet[i]
			if val >= start and val <= start+length:
				truncx.append(val)
				truncy.append(yDataSet[i])
				
		lala = Generic1DFitter.fitPeaks(np.toDS(np.array(truncx)), np.toDS(np.array(truncy)), Gaussian(1,1,1,1), NelderMead(self.accuracy), 2, self.gaussians, 0.05, True, False)
		print lala
		if lala == None or len(lala) == 0:
			print "no peaks found"
			return [0, []]
		positions = []
		cff = np.fit.fitcore.cfitfunc()
		for f in lala:
			print f.getParameter(1), f.getParameter(2)
			if f.getParameter(1).getValue() <= self.maxfwhm and f.getParameter(2).getValue() >= self.minarea:
				positions.append(f.getParameter(0).getValue())
				cff.addFunction(f)
			else:
				print "removed one"
		if len(positions) == 0:
			print "too bad nothing found within threshold"
		else:
			print "plotting remaining found positions"
			if self.plotPanel != None:
				fire = np.fit.fitcore.fitresult(cff, [np.toDS(np.array(truncx))], np.array(truncy))
				fire.plot(self.plotPanel)

		return [len(positions), positions]