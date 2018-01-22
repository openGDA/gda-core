import unittest

from gdascripts.analysis.datasetprocessor.oned.XYDataSetResult import XYDataSetResult
from gdascripts.scan.process.ScanDataProcessorResult import ScanDataProcessorResult
from scisoftpy.dictutils import DataHolder

NAN = float('nan')

class MockScannable(object):

	def __init__(self, name, inputnames, extranames):
		self.name = name
		self.inputnames = inputnames
		self.extranames = extranames
		self.outputformats = ['%.2f']*(len(self.extranames)+len(self.inputnames))
		self.pos = None

	def getName(self):
		return self.name

	def getInputNames(self):
		return self.inputnames

	def getExtraNames(self):
		return self.extranames

	def getOutputFormat(self):
		return self.outputformats

	def __repr__(self):
		return self.name

	def moveTo(self,pos):
		try:
			len(pos)
		except TypeError:
			# presumably just a number
			if len(self.getInputNames()) != 1: raise Exception
			self.pos = pos
			return
		if len(pos) != len(self.getInputNames()): raise Exception
		self.pos = pos


class MockSDPR(ScanDataProcessorResult):
	def __init__(self,keyxscannable, value):
		self.abscissa_scannable = keyxscannable
		self.value = value

	def getScannableValueAtFeature(self, scn):
		if scn != self.abscissa_scannable: raise Exception
		return self.value

def createSimpleScanFileHolderAndScannables():
	w = MockScannable('w', ['wi1','wi2'], ['we'])
	x = MockScannable('x', ['x'], [])
	y = MockScannable('y', ['yi'], ['ypath', 'ye'])
	y.outputformats = ['%.2f', '%s', '%.2f']
	z = MockScannable('z', [], ['ze1','ze2'])

	# Using DataHolder to replace deprecated ScanFileHolder
	testData = DataHolder()
	testData['x'] = [0.,1.,2.,3.,4.,5.,6.,7.,8.,9.]
	testData['yi'] = [0,.1,.2,.3,.4,.5,.3,.2,.1,0.]
	testData['ye'] = [1.0,1.1,1.2,1.3,1.4,1.5,1.3,1.2,1.1,1.0]
	testData['ze1'] = [0., 10., 20. ,30., 40., 50., 60., 70., 80., 90.]
	testData['ze2'] = [1., 11., 21. ,31., 41., 51., 61., 71., 81., 91.]
	testData['wi1'] = [100.,101.,102.,103.,104.,105.,106.,107.,108.,109.]
	testData['wi2'] = [100.,101.,102.,103.,104.,105.,104.,103.,102.,101.,100.]
	testData['we'] = [100.,100.1,100.2,100.3,100.4,100.5,100.3,100.2,100.1,100.0]

	return w, x, y, z, testData

def createSimpleScanFileHolderWithOneValueAndScannables():
	w = MockScannable('w', ['wi1','wi2'], ['we'])
	x = MockScannable('x', ['x'], [])
	y = MockScannable('y', ['yi'], ['ye'])
	z = MockScannable('z', [], ['ze1','ze2'])

	# Using DataHolder to replace deprecated ScanFileHolder
	testData = DataHolder()
	testData['x'] = [0.]
	testData['yi'] = [1.]
	testData['ye'] = [2.]
	testData['ze1'] = [3.]
	testData['ze2'] = [4.]
	testData['wi1'] = [5.]
	testData['wi2'] = [6.]
	testData['we'] = [7.]

	return w, x, y, z, testData

def isnan(a):
	#math.isnan introduced only in python 2.6
	return a != a

def assertArrayWithNansEqual(aarray, barray):
	if len(aarray) != len(barray):
		raise AssertionError("Lengths differ: " + str(aarray) + "!=" + str(barray))
	for a, b in zip(aarray, barray):
		if isnan(a) and isnan(b):
			break
		if a != b:
			raise AssertionError(str(aarray) + "!=" + str(barray))

class TestScanDataProcessorResult(unittest.TestCase):

	def setUp(self):
		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('max', {'pos': 5., 'val': .5}, ('pos','val'),'pos',"pos was 5.000000; val was .500000" )
		#dataSetResult, keyxlabel, scanFileHolder, allscannables
		self.sdpr = ScanDataProcessorResult(self.dsr,  self.sfh, [self.x,self.y,self.z], 'x', 'ye')

	def test__init__scannableValuesFound(self):
		self.assertEquals(len(self.sdpr.scannableValues), 3.)
		self.assertEquals(self.sdpr.scannableValues[self.z], [50.0, 51.0])
		self.assertEquals(self.sdpr.scannableValues[self.x], 5.0)
		assertArrayWithNansEqual(self.sdpr.scannableValues[self.y], [0.5, NAN, 1.5])

	def test__init__keyXScannableProperlyInferred(self):
		self.assertEquals(self.sdpr.abscissa_scannable, self.x)

	def testGetScannableValueAtFeature(self):
		get = self.sdpr.getScannableValueAtFeature
		self.assertEquals(get(self.z), [50.0, 51.0])
		self.assertEquals(get(self.x), 5.0)
		assertArrayWithNansEqual(get(self.y), [0.5, NAN, 1.5])

	def test__call__WithRealSDPR(self):
		self.sdpr.go()
		self.assertEquals(self.x.pos, 5.0)

	def testResultiongStructure(self):
		print self.sdpr

		print self.sdpr.scn
		self.assertEquals(self.sdpr.scn.x, 5.0)
		assertArrayWithNansEqual(self.sdpr.scn.y, [0.5, NAN, 1.5])
		self.assertEquals(self.sdpr.scn.z, [50.00, 51.00])

		print self.sdpr.field
		self.assertEquals(self.sdpr.field.x, 5.00)
		self.assertEquals(self.sdpr.field.yi, 0.50)
		self.assertEquals(self.sdpr.field.ye, 1.50)
		self.assertEquals(self.sdpr.field.ze1, 50.00)
		self.assertEquals(self.sdpr.field.ze2, 51.00)

		print self.sdpr.result
		self.assertEquals(self.sdpr.result.pos, 5.0)
		self.assertEquals(self.sdpr.result.val, 0.5)


class TestScanDataProcessorResultWithInterpolatedXValues(TestScanDataProcessorResult):

	def setUp(self):
		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('max', {'pos': 5.4, 'val': .5}, ('pos','val'), 'pos',"pos was 5.400000; val was .500000" )
		#dataSetResult, keyxlabel, scanFileHolder, allscannables
		self.sdpr = ScanDataProcessorResult(self.dsr,  self.sfh, [self.x,self.y,self.z], 'x', 'ye')

	def test__init__scannableValuesFound(self):
		#self.assertEquals(self.sdpr.scannableValues, {self.z: [54.0, 55.0], self.x: 5.4, self.y: [0.41999999999999993, 1.42]})
		self.assertEquals(len(self.sdpr.scannableValues), 3)
		self.assertEquals(self.sdpr.scannableValues[self.z], [54.0, 55.0])
		self.assertEquals(self.sdpr.scannableValues[self.x], 5.4)
		assertArrayWithNansEqual(self.sdpr.scannableValues[self.y], [0.41999999999999993, NAN, 1.42])

	def testGetScannableValueAtFeature(self):
		get = self.sdpr.getScannableValueAtFeature
		self.assertEquals(get(self.z), [54.0, 55.0])
		self.assertEquals(get(self.x), 5.4)
		assertArrayWithNansEqual(get(self.y), [0.41999999999999993, NAN, 1.42])

	def test__call__WithRealSDPR(self):
		self.sdpr.go()
		self.assertEquals(self.x.pos, 5.4)

	def testResultiongStructure(self):
		pass #life too short


class TestScanDataProcessorResultWithFeatureOutsideCollection(TestScanDataProcessorResult):

	def setUp(self):
		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('lcen', {'lcen': -5.4}, ('lcen',), 'lcen',"..." )
		#dataSetResult, keyxlabel, scanFileHolder, allscannables
		self.sdpr = ScanDataProcessorResult(self.dsr,  self.sfh, [self.x,self.y,self.z], 'x.x', 'y.ye')

	def test__init__scannableValuesFound(self):

		#self.assertEquals(self.sdpr.scannableValues, {self.z: [None, None], self.x: -5.4, self.y: [None, None]})
		self.assertEquals(len(self.sdpr.scannableValues), 3.)
		self.assertEquals(self.sdpr.scannableValues[self.z], [0., 1.])
		self.assertEquals(self.sdpr.scannableValues[self.x], -5.4)
		assertArrayWithNansEqual(self.sdpr.scannableValues[self.y], [0., NAN, 1.])

	def testGetScannableValueAtFeature(self):
		get = self.sdpr.getScannableValueAtFeature
		self.assertEquals(get(self.z), [0., 1.])
		self.assertEquals(get(self.x), -5.4)
		assertArrayWithNansEqual(get(self.y), [0., NAN, 1.])

	def test__call__WithRealSDPR(self):
		self.sdpr.go()
		self.assertEquals(self.x.pos, -5.4)

	def testResultiongStructure(self):
		pass #life too short

class MockScanDataPointCache():

	def __init__(self, sfh):
		self.sfh = sfh

	def getPositionsFor(self, scannableName):
		return self.sfh[scannableName]

class TestScanDataProcessorResultWithSDPC(TestScanDataProcessorResult):

	def setUp(self):
		unittest.TestCase.setUp(self)

		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('max', {'pos': 5., 'val': .5}, ('pos', 'val'), 'pos', "pos was 5.000000; val was .500000" )

		sdpc = MockScanDataPointCache(self.sfh)

		# Pass in None for the lastScanFile to ensure its not used as the SDPC should be used instead
		self.sdpr = ScanDataProcessorResult(self.dsr, None, [self.x,self.y,self.z], 'x', 'ye', scanDataPointCache=sdpc)


class TestScanDataProcessorResultWithInterpolatedXValuesWithSDPC(TestScanDataProcessorResultWithInterpolatedXValues):

	def setUp(self):
		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('max', {'pos': 5.4, 'val': .5}, ('pos','val'), 'pos',"pos was 5.400000; val was .500000" )

		sdpc = MockScanDataPointCache(self.sfh)

		# Pass in None for the lastScanFile to ensure its not used as the SDPC should be used instead
		self.sdpr = ScanDataProcessorResult(self.dsr, None, [self.x,self.y,self.z], 'x', 'ye', sdpc)


class TestScanDataProcessorResultWithFeatureOutsideCollectionWithSDPC(TestScanDataProcessorResultWithFeatureOutsideCollection):

	def setUp(self):
		self.w, self.x,self.y,self.z,self.sfh = createSimpleScanFileHolderAndScannables()
		# assume that we have found the max value of ye as x was varied
		self.dsr = XYDataSetResult('lcen', {'lcen': -5.4}, ('lcen',), 'lcen',"..." )

		sdpc = MockScanDataPointCache(self.sfh)

		# Pass in None for the lastScanFile to ensure its not used as the SDPC should be used instead
		self.sdpr = ScanDataProcessorResult(self.dsr, None, [self.x,self.y,self.z], 'x.x', 'y.ye', sdpc)


def suite():
	return unittest.TestSuite((
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResult),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResultWithInterpolatedXValues),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResultWithFeatureOutsideCollection),
		# With ScanDataPointCache
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResultWithSDPC),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResultWithInterpolatedXValuesWithSDPC),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorResultWithFeatureOutsideCollectionWithSDPC)
	))

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())