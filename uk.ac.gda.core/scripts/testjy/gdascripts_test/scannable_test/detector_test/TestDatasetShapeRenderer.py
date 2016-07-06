from TestProcessingDetectorWrapper import MockDetectorDataProcessor
from org.eclipse.january.dataset import DatasetUtils, DatasetFactory
from gdascripts.scannable.detector.DatasetShapeRenderer import DatasetShapeRenderer, LinePainter, RectPainter
import unittest

								
class TestLinePainter(unittest.TestCase):
	def test__init__(self):
		self.assertRaises(AssertionError, LinePainter, 1, 1, 2, 3)
		LinePainter(1, 1, 1, 3)
		LinePainter(1, 1, 2, 1)
		
	def testPainter(self):
		line = LinePainter(0, 0 , 0, 1)
		ds = line.paint(DatasetFactory.zeros(2, 3))
		expected = [
				[1., 1., 0.],
				[0., 0., 0.]]
		self.assertEquals(ds2lofl(ds), expected)
		
		
class TestRectPainter(unittest.TestCase):
	def test__init__(self):
		RectPainter(1, 1, 3, 4)
		
	def testPainter(self):
		rect = RectPainter(1, 1, 3, 4)
		ds = rect.paint(DatasetFactory.zeros(4, 5))
		expected = [
				[0., 0., 0., 0., 0.],
				[0., 1., 1., 1., 1.],
				[0., 1., 0., 0., 1.],
				[0., 1., 1., 1., 1.]]											
		self.assertEquals(ds2lofl(ds), expected)		


class TestDatasetShapeRenderer(unittest.TestCase):
	
	def setUp(self):
		self.renderer = DatasetShapeRenderer()
		self.p1 = MockDetectorDataProcessor('p1', ['a'], [1.])
		self.p2 = MockDetectorDataProcessor('p2', ['a'], [1.])
		self.line = LinePainter(0, 0 , 0, 1)
		self.rect = RectPainter(1, 1, 3, 4)
	
	def testAddShape(self):
		self.renderer.addShape(self.p1, 'p1line', self.line)
		self.assertEqual(self.renderer.shapesToPaint, {self.p1:{'p1line':self.line}})
		self.renderer.addShape(self.p1, 'p1line', self.line)
		self.assertEqual(self.renderer.shapesToPaint, {self.p1:{'p1line':self.line}})		
		self.renderer.addShape(self.p1, 'p1rect', self.rect)
		self.assertEqual(self.renderer.shapesToPaint, {self.p1:{'p1line':self.line, 'p1rect':self.rect}})
		self.renderer.addShape(self.p2, 'p2line', self.line)
		self.assertEqual(self.renderer.shapesToPaint, {self.p1:{'p1line':self.line, 'p1rect':self.rect}, self.p2:{'p2line':self.line}})
	
	def testRemoveShape(self):
		self.testAddShape()
		self.renderer.removeShape(self.p1, 'p1line')
		self.assertEqual(self.renderer.shapesToPaint, {self.p1:{'p1rect':self.rect}, self.p2:{'p2line':self.line}})
		self.renderer.removeShape(self.p1, 'p1rect')
		self.renderer.removeShape(self.p2, 'p2line')
		self.assertEqual(self.renderer.shapesToPaint, {})
	
	def testRenderShapes(self):
		self.testAddShape()
		ds = DatasetFactory.zeros(4, 5)
		result = self.renderer.renderShapes(ds)
		expected = [
				[1., 1., 0., 0., 0.],
				[0., 1., 1., 1., 1.],
				[0., 1., 0., 0., 1.],
				[0., 1., 1., 1., 1.]]											
		self.assertEquals(ds2lofl(result), expected)
	
	def testRenderShapesOntoDataset(self):
		self.testAddShape()
		ds = DatasetFactory.zeros(4, 5)
		ds.set(10, (2, 2))
		result = self.renderer.renderShapesOntoDataset(ds)
		expected = [
				[-10.0, -10.0, 0.0, 0.0, 0.0],
				[0.0, -10.0, -10.0, -10.0, -10.0],
				[0.0, -10.0, 10.0, 0.0, -10.0],
				[0.0, -10.0, -10.0, -10.0, -10.0]]
		self.assertEquals(ds2lofl(result), expected)

# I think the original developer of this meant this to mean Data set to list of lists, but a better name would have been helpful!....
def ds2lofl(ds):
	
	array_from_ds = DatasetUtils.createJavaArray(ds)
	
	return [list(l) for l in list(array_from_ds)]

def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestLinePainter))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestRectPainter))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestDatasetShapeRenderer))	

	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	
	
