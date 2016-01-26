from org.eclipse.dawnsci.analysis.dataset.impl import DoubleDataset, Maths
class DatasetShapeRenderer(object):
	
	def __init__(self):
		self.shapesToPaint = {}

	def addShape(self, detectorDataProcessor, shapeid, shape):	
		if not self.shapesToPaint.has_key(detectorDataProcessor):
			self.shapesToPaint[detectorDataProcessor] = {shapeid:shape}
		else:
			self.shapesToPaint[detectorDataProcessor][shapeid] = shape
			
	def removeShape(self, detectorDataProcessor, shapeid):
		del self.shapesToPaint[detectorDataProcessor][shapeid]
		if len(self.shapesToPaint[detectorDataProcessor]) == 0:
			del self.shapesToPaint[detectorDataProcessor]
		
	def renderShapes(self, targetDataset):
		# Make a blank data set
		image = DoubleDataset(targetDataset.getShape())
		for shapeDict in self.shapesToPaint.values():
			for shape in shapeDict.values():
				image = shape.paint(image)
		return image
	
	def renderShapesOntoDataset(self, targetDataset):
		if self.shapesToPaint == {}:
			return targetDataset.clone()
		a = targetDataset.max()
		b = targetDataset.min()
		image = self.renderShapes(targetDataset)
		image.imultiply(b - (a - b))
		return Maths.add(targetDataset, image)


class ShapePainter(object):
	"""positions all start at zero not one"""
	def paint(self, dataset):
		raise Exception("Override")


class LinePainter(ShapePainter):
	"""Only paints horizontal or vertical lines!"""
	
	def __init__(self, y1, x1, y2, x2):
		assert x1 == x2 or y1 == y2 # vertical or horizontal
		if y1 > y2:
			y1orig, x1orig = y1, x1
			y1, x1 = y2, x2
			y2, x2 = y1orig, x1orig
		if x1 > x2:
			y1orig, x1orig = y1, x1
			y1, x1 = y2, x2
			y2, x2 = y1orig, x1orig	
		self.x1 = x1
		self.y1 = y1
		self.x2 = x2
		self.y2 = y2
				
	def paint(self, dataset):
		rows, cols = dataset.getShape()
#		print "line painting:", self.y1, self.x1, self.y2, self.x2
		for x in range(self.x1, self.x2 + 1):
			for y in range(self.y1, self.y2 + 1):
#				print (y,x)
				if y > rows - 1: raise ValueError("y exceeds rows-1: %i > %i" % (y, rows - 1))
				if x > cols - 1: raise ValueError("x exceeds cols-1: %i > %i" % (x, cols - 1))
				dataset.set(1, (y, x))
		return dataset


class RectPainter(ShapePainter):
	def __init__(self, y1, x1, y2, x2):
		self.lines = [
				LinePainter(y1, x1, y2, x1),
				LinePainter(y2, x1, y2, x2),
				LinePainter(y2, x2, y1, x2),
				LinePainter(y1, x2, y1, x1)
				]
	def paint(self, dataset):
		for line in self.lines:
			line.paint(dataset)
		return dataset
