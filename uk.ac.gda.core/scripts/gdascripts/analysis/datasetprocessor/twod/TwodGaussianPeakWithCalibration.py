from TwodDataSetProcessor import TwodDataSetProcessor
from TwodGaussianPeak import TwodGaussianPeak

LABELLIST = (
			'background',
			'peakx_raw',
			'peaky_raw',
			'peakx',
			'peaky',
			'topx',
			'topy',
			'fwhmx',
			'fwhmy',
			'fwhmarea',
			'peakx_mm',
			'peaky_mm',
			'fwhmx_mm',
			'fwhmy_mm',
			'fwhmarea_mm2'
			)
class TwodGaussianPeakWithCalibration(TwodGaussianPeak):

	def __init__(self, name='peak',
				labelList=LABELLIST,
				keyxlabel='peakx_mm',
				keyylabel='peaky_mm',
				formatString=', '.join(s + ':%f' for s in LABELLIST)
				):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
		self.maxwidth = None
		self.last_x_peak = 0.0
		self.last_y_peak = 0.0
		self.x_offset = 0.0
		self.y_offset = 0.0

	def _process(self, ds, xoffset=0, yoffset=0):
		background, peakx_raw_pixels, peaky_raw_pixels, topx, topy, fwhmx, fwhmy, fwhmarea = TwodGaussianPeak._process(self, ds, xoffset=xoffset, yoffset=yoffset)
		peakx = peakx_raw_pixels - self.x_offset
		peaky = peaky_raw_pixels - self.y_offset
		peakx_mm = peakx * self.x_scalingFactor
		peaky_mm = peaky * self.y_scalingFactor
		fwhmx_mm = fwhmx * self.x_scalingFactor
		fwhmy_mm = fwhmy * self.y_scalingFactor
		fwhmarea_mm2 = fwhmarea * self.x_scalingFactor * self.y_scalingFactor
		return background, peakx_raw_pixels, peaky_raw_pixels, peakx, peaky, topx, topy, fwhmx, fwhmy, fwhmarea, peakx_mm, peaky_mm, fwhmx_mm, fwhmy_mm, fwhmarea_mm2
	
	def setScalingFactors(self, x_scalingFactor, y_scalingFactor):
		self.x_scalingFactor = x_scalingFactor
		self.y_scalingFactor = y_scalingFactor

	def calibrate(self):
		self.x_offset = self.last_x_peak
		self.y_offset = self.last_y_peak
