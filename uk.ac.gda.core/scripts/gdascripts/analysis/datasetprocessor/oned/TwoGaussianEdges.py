from XYDataSetProcessor import XYDataSetFunction
from org.eclipse.dawnsci.analysis.dataset.impl import Maths
import scisoftpy as dnp


try:
	from gda.analysis import Plotter # for Swing
except ImportError:
	Plotter = None


class TwoGaussianEdges(XYDataSetFunction):

	def __init__(self, smoothwidth=1, name='edges', labelList=('upos', 'ufwhm', 'dpos', 'dfwhm', 'area', 'fwhm'), formatString='upos:%f ufwhm:%f dpos:%f dfwhm:%f %f:area %f:fwhm', keyxlabel='upos', plotPanel=None, optimizer='genetic'):
		XYDataSetFunction.__init__(self, name, labelList, keyxlabel, formatString)
		self.smoothwidth = smoothwidth
		self.plotPanel = plotPanel
		self.optimizer = optimizer

	def coarseProcess(self, xDataSet, dyDataSet):
		upos = xDataSet[dnp.argmax(dyDataSet)]
		dpos = xDataSet[dnp.argmin(dyDataSet)]

		# Positive peak (up edge)
		upCrossings = dnp.crossings(dyDataSet, dyDataSet.max() / 3.0, xDataSet)
		if len(upCrossings) >= 2:
			ufwhm = abs(upCrossings[1] - upCrossings[0])
		else:
			ufwhm = 0.01 * (xDataSet.max() - xDataSet.min())
		uarea = dyDataSet.max() * ufwhm

		# Negative peak (down edge)
		downCrossings = dnp.crossings(dyDataSet, dyDataSet.min() / 3.0, xDataSet)
		if len(downCrossings) >= 2:
			dfwhm = abs(downCrossings[1] - downCrossings[0])
		else:
			dfwhm = 0.01 * (xDataSet.max() - xDataSet.min())
		darea = dyDataSet.min() * dfwhm

		return upos, ufwhm, uarea, dpos, dfwhm , darea

	def _process(self, xDataSet, yDataSet):
		dyDataSet = dnp.array(Maths.derivative(xDataSet._jdataset(), yDataSet._jdataset(), self.smoothwidth))
		uposC, ufwhmC, uareaC, dposC, dfwhmC, dareaC = self.coarseProcess(xDataSet, dyDataSet)
		gaussian = dnp.fit.function.gaussian

		if abs(dareaC) < 0.2 * uareaC:
			r = dnp.fit.fit([gaussian], xDataSet, dyDataSet,
							[uposC, ufwhmC, uareaC],
							bounds=[
								(uposC - 2 * ufwhmC, uposC + 2 * ufwhmC),
								(0, 2 * ufwhmC),
								(0, 2 * uareaC)],
							ptol=1e-10, optimizer=self.optimizer)
			upos, ufwhm, _uarea = r.parameters
			results = {'upos': upos, 'ufwhm': ufwhm, 'area': _uarea, 'uarea': _uarea, 'fwhm': ufwhm}

		elif uareaC < 0.2 * abs(dareaC):
			r = dnp.fit.fit([gaussian], xDataSet, dyDataSet,
							[dposC, dfwhmC, dareaC],
							bounds=[
								(dposC - 2 * dfwhmC, dposC + 2 * dfwhmC),
								(0, 2 * dfwhmC),
								(2 * dareaC, 0)],
							ptol=1e-10, optimizer=self.optimizer)
			dpos, dfwhm, _darea = r.parameters
			results = {'dpos': dpos, 'dfwhm': dfwhm, 'area': abs(_darea), 'darea': _darea, 'fwhm': dfwhm}

		else:
			r = dnp.fit.fit([gaussian, gaussian], xDataSet, dyDataSet,
							[uposC, ufwhmC, uareaC,dposC, dfwhmC, dareaC],
							bounds=[
								(uposC - 2 * ufwhmC, uposC + 2 * ufwhmC),
								(0, 2 * ufwhmC),
								(0, 2 * uareaC),
								(dposC - 2 * dfwhmC, dposC + 2 * dfwhmC),
								(0, 2 * dfwhmC),
								(2 * dareaC, 0)],
							ptol=1e-10, optimizer=self.optimizer)
			upos, ufwhm, _uarea, dpos, dfwhm, _darea = r.parameters
			results = {'upos': upos,
					'dpos': dpos,
					'ufwhm': ufwhm,
					'dfwhm': dfwhm,
					'uarea': _uarea,
					'darea': _darea,
					'centre': (upos + dpos) / 2.0,
					'width': abs(upos - dpos),
					'area': (_uarea + abs(_darea)) / 2.0,
					'fwhm': (ufwhm + dfwhm) / 2.0}

		self.plotResult(r)
		results['residual'] = r.residual
		return [results.get(label, float('NaN')) for label in self.labelList]

	def plotResult(self, result):
		if self.plotPanel is not None:
			try:
				if Plotter is not None:
					#swing
					Plotter.plot(self.plotPanel, *result.makeplotdata)
				else:
					#rcp
					dnp.plot.clear(self.plotPanel)
					result.plot(name=self.plotPanel, title=self.plotPanel)
			except Exception, e:
				print "Could not plot two gaussian fit"
				print e.message
