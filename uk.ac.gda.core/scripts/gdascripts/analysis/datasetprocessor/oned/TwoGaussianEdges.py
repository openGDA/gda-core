import java.lang.NoClassDefFoundError

from XYDataSetProcessor import XYDataSetFunction
from gda.analysis import RCPPlotter
from org.eclipse.dawnsci.analysis.dataset.impl import Maths
import scisoftpy as dnp
from uk.ac.diamond.scisoft.analysis.fitting import Fitter


try:
	def fitplot(*args):
		fitted_function = Fitter.fit(*args)
		RCPPlotter.plot("Data Vector", args[0],fitted_function.display(args[0])[0]);
		return fitted_function
except ImportError:
	def fitplot(*args):
		raise

try:
	from gda.analysis import Plotter  # for Swing
except ImportError:
	Plotter = None


class TwoGaussianEdges(XYDataSetFunction):

	def __init__(self, smoothwidth=1, name='edges', labelList=('upos', 'ufwhm', 'dpos', 'dfwhm', 'area', 'fwhm'), formatString='upos:%f ufwhm:%f dpos:%f dfwhm:%f %f:area %f:fwhm', keyxlabel='upos'):
		XYDataSetFunction.__init__(self, name, labelList, keyxlabel, formatString)
		self.smoothwidth = smoothwidth

	def coarseProcess(self, xDataSet, dyDataSet):

		upos = xDataSet[dnp.argmax(dyDataSet)]
		dpos = xDataSet[dnp.argmin(dyDataSet)]

		# Positive peak (up edge)
		upCrossings = dnp.crossings(dyDataSet, dyDataSet.max() / 3., xDataSet)
		if len(upCrossings) >= 2:
			ufwhm = abs(upCrossings[1] - upCrossings[0])
		else:
			ufwhm = .01 * (xDataSet.max() - xDataSet.min())
		uarea = dyDataSet.max() * ufwhm

		# Negative peak (down edge)
		downCrossings = dnp.crossings(dyDataSet, dyDataSet.min() / 3., xDataSet)
		if len(downCrossings) >= 2:
			dfwhm = abs(downCrossings[1] - downCrossings[0])
		else:
			dfwhm = .01 * (xDataSet.max() - xDataSet.min())
		darea = dyDataSet.min() * dfwhm

		return upos, ufwhm, uarea, dpos, dfwhm , darea

	def _process(self, xDataSet, yDataSet):

		dyDataSet = dnp.array(Maths.derivative(xDataSet._jdataset(), yDataSet._jdataset(), self.smoothwidth))

		uposC, ufwhmC, uareaC, dposC, dfwhmC, dareaC = self.coarseProcess(xDataSet, dyDataSet)

		gaussian = dnp.fit.function.gaussian

		if abs(dareaC) < .2 * uareaC:
			print "only upward edge present"
			r = dnp.fit.fit([gaussian], xDataSet, dyDataSet,
							[uposC, ufwhmC, uareaC],
							ptol=1e-10, optimizer='apache_nm')
			upos, ufwhm, _uarea = r.parameters
			dpos = 0.
			dfwhm = 0.
			area = _uarea
			fwhm = ufwhm

		elif uareaC < .2 * abs(dareaC):
			print "only downward edge present"
			r = dnp.fit.fit([gaussian], xDataSet, dyDataSet,
							[dposC, dfwhmC, dareaC],
							ptol=1e-10, optimizer='apache_nm')
			dpos, dfwhm, _darea = r.parameters
			upos = 0.
			ufwhm = 0.
			area = abs(_darea)
			fwhm = dfwhm

		else:
			print "both edges present"
			r = dnp.fit.fit([gaussian, gaussian], xDataSet, dyDataSet,
							[uposC, ufwhmC, uareaC,dposC, dfwhmC, dareaC],
							ptol=1e-10, optimizer='apache_nm')
			upos, ufwhm, _uarea, dpos, dfwhm, _darea = r.parameters
			area = (_uarea + abs(_darea)) / 2.
			fwhm = (ufwhm + dfwhm) / 2.

		# Plot to RCP
		try:
			r.plot()
		except java.lang.NoClassDefFoundError:
			print "WARNING: TwoGaussianEdges could not plot fit details to RCP client"

		return upos, ufwhm, dpos, dfwhm , area, fwhm
