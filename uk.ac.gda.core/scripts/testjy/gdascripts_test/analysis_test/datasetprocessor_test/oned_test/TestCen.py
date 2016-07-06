from gdascripts.analysis.datasetprocessor.oned.CenFromSPEC import CenFromSPEC, interp

from org.eclipse.january.dataset import DatasetFactory


import unittest


def close(l1, l2):
	for v1, v2 in zip(l1, l2):
		if abs(v1-v2) > .01:
			return False
	return True

def closer(l1, l2, tolerance=0.01):
	for v1, v2 in zip(l1, l2):
		if abs(v1-v2) > tolerance:
			return False
	return True


class TestCOM(unittest.TestCase):
	
	def setUp(self):
		self.p = CenFromSPEC() # -> cen, height, width
		self.x = DatasetFactory.createFromObject([10.,11.,12.,13.,14.,15.,16.,17.,18.,19.])
		self.l = [0, 10, 20, 30, 40]
		
	def test_process_ones(self):
		y = DatasetFactory.createFromObject([0, 0, 1, 1.000000001, 1, 1, 1, 0, 0, 0])
		cen, height, width = self.p._process(self.x, y)
		self.assertAlmostEqual(cen, 14)
		self.assertAlmostEqual(height, 1.000000001)
		self.assertAlmostEqual(width, 5)

	def test_process_ones_max_at_left_edge(self):
		y = DatasetFactory.createFromObject([1.000000001, 1, 1, 0, 0, 0, 0, 0, 0, 0])
		cen, height, width = self.p._process(self.x, y)
		self.assertAlmostEqual(cen, 11)
		self.assertAlmostEqual(height, 1.000000001)
		self.assertAlmostEqual(width, 3)

	def test_process_ones_max_at_right_edge(self):
		y = DatasetFactory.createFromObject([0, 0, 0, 0, 0, 0, 0, 1, 1, 1.000000001])
		cen, height, width = self.p._process(self.x, y)
		self.assertAlmostEqual(cen, 18)
		self.assertAlmostEqual(height, 1.000000001)
		self.assertAlmostEqual(width, 3)

	def test_process(self):
		y = DatasetFactory.createFromObject([0, 0, 1, 1.000000001, 1, 1, 0, 1, 0, 0])
		cen, height, width = self.p._process(self.x, y)
		self.assertAlmostEqual(cen, 14)
		self.assertAlmostEqual(height, 1.000000001)
		self.assertAlmostEqual(width, 5)
		
	def test_interp_minus1(self):
		self.assertEqual(interp(self.l, -1), -10)

	def test_interp_minus05(self):
		self.assertEqual(interp(self.l, -.5), -5)

	def test_interp_0(self):
		self.assertEqual(interp(self.l, 0), 0)
		
	def test_interp_2(self):
		self.assertEqual(interp(self.l, 2), 20)
		
	def test_interp_4(self):
		self.assertEqual(interp(self.l, 4), 40)
		
	def test_interp_2_1(self):
		self.assertEqual(interp(self.l, 2.1), 21)
		
	def test_interp_2_5(self):
		self.assertEqual(interp(self.l, 2.5), 25)


def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestCOM))
	return suite 


if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
