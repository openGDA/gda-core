from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper, add, isObjectScannable, sampleScannablesInputPosition
from gdascripts.scan.scanListener import ScanListener
import gdascripts.scan.concurrentScanWrapper #@UnusedImport

from gdascripts.pd.dummy_pds import DummyPD, MultiInputExtraFieldsDummyPD, ZeroInputExtraFieldsDummyPD
from gda.scan import ScanPlotSettings
import unittest
from mock import Mock

def mockpos(args):
	args = list(args)
	while 1:
		try:
			args.pop(0).asynchronousMoveTo(args.pop(0))
		except IndexError:
			break

def mockCreateScanPlotSettings(ob):
	return ScanPlotSettings()

class MockConcurrentScan:
	def __init__(self, args):
		MockConcurrentScan.args = args	#static
		self.scanWasExplicitlyHalted = False
	
	def setScanPlotSettings(self,ob):
		pass
	
	def runScan(self):
		print "Scan started with args: %s" % str(MockConcurrentScan.args)
		for arg in MockConcurrentScan.args:
			if isObjectScannable(arg):
				inputPos =  sampleScannablesInputPosition(arg)
				try: #lists etc.
					diff = [1.]*len(inputPos)
				except TypeError:
					diff = 1
				newPos = add(inputPos, diff)
				print "scan moved %s to %s" % (arg.getName(), str(newPos))
				arg.asynchronousMoveTo(newPos)
				
	def wasScanExplicitlyHalted(self):
		return self.scanWasExplicitlyHalted

def createScannables(posA = 0, posB = 0, posC=0, posMie=[1,2.1]):
	a, b, c = DummyPD('a'), DummyPD('b'), DummyPD('c')
	a.__repr__= a.getName
	b.__repr__= b.getName
	c.__repr__= c.getName
	a.asynchronousMoveTo(posA)
	b.asynchronousMoveTo(posB)
	c.asynchronousMoveTo(posC)
	mie = MultiInputExtraFieldsDummyPD('mie', ['i1','i2'], ['e1','e2'])
	mie.asynchronousMoveTo(posMie)
	mie.__repr__ = mie.getName
	return (a,b,c,mie)


class SimpleScanWrapper(ConcurrentScanWrapper):
	def convertArgStruct(self, argStruct):
		return argStruct


class SimpleScanListener(ScanListener):
	def __init__(self):
		self.scan = None
		self.toReturn=None
			
	def update(self, concurrentScan):
		self.scan = concurrentScan
		print concurrentScan
		return self.toReturn
		

class TestConcurrentScanWrapper(unittest.TestCase):

	#	[[x, 1, 2, 1], [y, 3, 5, 1], [z]] = parseArgsIntoArgStruct(x, 1, 2, 1, y, 3, 5, 1, z)
	def setUp(self):
		self.scan = SimpleScanWrapper(returnToStart=False, relativeScan=False)
		gdascripts.scan.concurrentScanWrapper.createScanPlotSettings = mockCreateScanPlotSettings
		
	def testConvertArgStruct(self):
		a, b, c, _ = createScannables()
		argStruct =[[a, 1, 2, 1], [b, 3, 5, 1], [c]]
		self.assertEqual(self.scan.convertArgStruct(argStruct), argStruct)

	def testIsObjectScannable(self):
		a, b, c, _ = createScannables()
		self.assertEquals(isObjectScannable(a), True)
		self.assertEquals(isObjectScannable(b), True)
		self.assertEquals(isObjectScannable(c), True)
		self.assertEquals(isObjectScannable([a]), False)
		self.assertEquals(isObjectScannable('a'), False)
		self.assertEquals(isObjectScannable(1), False)
		self.assertEquals(isObjectScannable(1.), False)
		self.assertEquals(isObjectScannable([1,2]), False)

	def testParseArgsIntoArgStruct(self, *args):
		#[[x, 1, 2, 1], [y, 3, 5, 1], [z]] = parseArgsIntoArgStruct(x, 1, 2, 1, y, 3, 5, 1, z)
		a, b, c, _ = createScannables()
		d, e, f, _ = createScannables(); d.setName('d'), e.setName('e'), f.setName('f')
		
		parse = self.scan.parseArgsIntoArgStruct
		self.assertEquals( parse([a,1,2,3]),			[[a,1,2,3]] )
		self.assertEquals( parse([a,1,2,3,b]),			[[a,1,2,3],[b]] )
		self.assertEquals( parse([a,1,2,3,b,4]),		[[a,1,2,3],[b,4]] )
		self.assertEquals( parse([a,1,2,3,b,4,5]),		[[a,1,2,3],[b,4,5]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,6]),	[[a,1,2,3],[b,4,5,6]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,6,c]),	[[a,1,2,3],[b,4,5,6],[c]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,c,6]),	[[a,1,2,3],[b,4,5],[c,6]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,c,6,d,e,7,f]),[[a,1,2,3],[b,4,5],[c,6],[d],[e,7],[f]] )

		self.assertEquals( parse([a,[1,1.1],[2,2.2],[3,3.3]]),			[[a,[1,1.1],[2,2.2],[3,3.3]]] )
		self.assertEquals( parse([a,[1,1.1],[2,2.2],[3,3.3],c]),		[[a,[1,1.1],[2,2.2],[3,3.3]],[c]] )
		self.assertEquals( parse([a,1,2,3,b,[4,4.1]]),					[[a,1,2,3],[b,[4,4.1]]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,c,[6,6.1],d,e,7,f]),	[[a,1,2,3],[b,4,5],[c,[6,6.1]],[d],[e,7],[f]] )
		self.assertEquals( parse([a,1,2,3,b,4,5,c,[6,6.1],d,e,[7,7.1],f]),[[a,1,2,3],[b,4,5],[c,[6,6.1]],[d],[e,[7,7.1]],[f]] )		
	
		self.assertRaises( SyntaxError , parse, [])
		
	def testFlattenArgStructToArgs(self):
		flatten = self.scan.flattenArgStructToArgs
		a, b, c, _ = createScannables()
		d, e, f, _ = createScannables(); d.setName('d'), e.setName('e'), f.setName('f')
		self.assertEquals( [a,1,2,3],			flatten([[a,1,2,3]]) )
		self.assertEquals( [a,1,2,3,b],			flatten([[a,1,2,3],[b]]) )
		self.assertEquals( [a,1,2,3,b,4],		flatten([[a,1,2,3],[b,4]]) )
		self.assertEquals( [a,1,2,3,b,4,5],		flatten([[a,1,2,3],[b,4,5]]) )
		self.assertEquals( [a,1,2,3,b,4,5,6],	flatten([[a,1,2,3],[b,4,5,6]]) )
		self.assertEquals( [a,1,2,3,b,4,5,6,c],	flatten([[a,1,2,3],[b,4,5,6],[c]]) )
		self.assertEquals( [a,1,2,3,b,4,5,c,6],	flatten([[a,1,2,3],[b,4,5],[c,6]]) )
		self.assertEquals( [a,1,2,3,b,4,5,c,6,d,e,7,f],flatten([[a,1,2,3],[b,4,5],[c,6],[d],[e,7],[f]]) )

		self.assertEquals( [a,[1,1.1],[2,2.2],[3,3.3]],			flatten([[a,[1,1.1],[2,2.2],[3,3.3]]]) )
		self.assertEquals( [a,[1,1.1],[2,2.2],[3,3.3],c],		flatten([[a,[1,1.1],[2,2.2],[3,3.3]],[c]]) )
		self.assertEquals( [a,1,2,3,b,[4,4.1]],					flatten([[a,1,2,3],[b,[4,4.1]]]) )
		self.assertEquals( [a,1,2,3,b,4,5,c,[6,6.1],d,e,7,f],	flatten([[a,1,2,3],[b,4,5],[c,[6,6.1]],[d],[e,7],[f]]) )
		self.assertEquals( [a,1,2,3,b,4,5,c,[6,6.1],d,e,[7,7.1],f],flatten([[a,1,2,3],[b,4,5],[c,[6,6.1]],[d],[e,[7,7.1]],[f]]) )		

	def testSampleScannablesInputPosition(self):
		sample = sampleScannablesInputPosition
		a, b, c,mie = createScannables(0, 1.0, 0, [1,2.1])
		def getPosition():
			return "an unexpected string"
		c.getPosition = getPosition
		zie = ZeroInputExtraFieldsDummyPD('zie')
		
		self.assertEquals(sample(a), 0)
		self.assertEquals(sample(b), 1.0)
		self.assertRaises(TypeError, sample, c)
		self.assertEquals(sample(mie), [1,2.1])
		self.assertEquals(sample(zie), None)
	
	def testSampleInitialPositions(self):
		sample = self.scan.sampleInitialPositions
		a, b, c, mie = createScannables(0, 1.0, 2,[1,2.1] )
		self.assertEquals(sample([[a,1,2,3],[b,4],[c]]), {a:0})
		self.assertEquals(sample([[a,1,2,3],[b,4,5],[c]]), {a:0, b:1})
		self.assertEquals(sample([[a,1,2,3],[b,4,5,6],[c]]), {a:0,b:1.0})
		self.assertEquals(sample([[a,1,2,3],[mie,4,5,6],[c]]), {a:0,mie:[1,2.1]})		

	def testAdd(self):
		self.assertEquals(add(1,2),3)
		self.assertEquals(add(1.,2),3.)
		self.assertEquals(add([1,2],[.1,.2]),[1.1,2.2])

	def testMakeAbsolute(self):
		makeabsolute = self.scan.makeAbsolute
		a, b, c, mie = createScannables()
		self.assertEquals(makeabsolute([[a,1,2,3],[b,4],[c]], {a:0.1, b:0.2}), [[a,1.1,2.1,3],[b,4.],[c]])
		self.assertEquals(makeabsolute([[a,1,2,3],[b,4,5],[c]], {a:0.1, b:0.2}), [[a,1.1,2.1,3],[b,4.2,5.2],[c]])
		self.assertEquals(makeabsolute([[a,1,2,3],[b,4,5,6],[c]], {a:0.1,b:.2}), [[a,1.1,2.1,3],[b,4.2,5.2,6],[c]])
		self.assertEquals(makeabsolute([[a,1,2,3],[mie,[400,410],[500,510],[6,6.1]],[c]], {a:0.1,mie:[1,2]}), [[a,1.1,2.1,3],[mie,[401,412],[501,512],[6,6.1]],[c]])		
		
	def testReturnToInitialPositions(self):
		# Mock the pos command
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		a, b, c, mie = createScannables(0, 1.0, 2, [1,2.1])
		self.scan.returnToInitialPositions({a:100, c:102, mie:[101,102]})
		self.assertEquals(a.getPosition(), 100)
		self.assertEquals(b.getPosition(), 1.0)
		self.assertEquals(c.getPosition(), 102)
		self.assertEquals(mie.getPosition()[0:2], [101,102] )

	def testMockConcurrentScan(self):
		a, b, c, mie = createScannables(1,2,3, [1,2.1])
		MockConcurrentScan((a,1,2,3, c, mie)).runScan() # moves _every* scannable by 1
		self.assertEquals(MockConcurrentScan.args, (a,1,2,3, c, mie) )
		self.assertEquals(a.getPosition(), 2.)
		self.assertEquals(b.getPosition(), 2)
		self.assertEquals(c.getPosition(), 4.)
		self.assertEquals(mie.getPosition()[0:2], [2., 3.1] )		

	def test__call__(self):
		"""
		__call__, not-relataive, no-return
		"""
		self.scan.relativeScan = False
		self.scan.returnToStart = False
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = MockConcurrentScan	
		a, b, c, mie = createScannables(1,2,3, [1,2.1])

		self.scan(a,1,2,3,mie,[400,410],[500,510],[6,6.1],c) #increments *every* scannable field by 1
		self.assertEquals(MockConcurrentScan.args, [a,1,2,3,mie,[400,410],[500,510],[6,6.1],c] )
		self.assertEquals(a.getPosition(), 2.)
		self.assertEquals(b.getPosition(), 2)
		self.assertEquals(c.getPosition(), 4.)
		self.assertEquals(mie.getPosition()[0:2], [2.,3.1] )

	def test__call__with_arg_list(self):
		
		self.scan.relativeScan = False
		self.scan.returnToStart = False
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = MockConcurrentScan	
		a, b, c, mie = createScannables(1,2,3, [1,2.1])

		self.scan([a,1,2,3,mie,[400,410],[500,510],[6,6.1],c]) #increments *every* scannable field by 1
		self.assertEquals(MockConcurrentScan.args, [a,1,2,3,mie,[400,410],[500,510],[6,6.1],c] )
		self.assertEquals(a.getPosition(), 2.)
		self.assertEquals(b.getPosition(), 2)
		self.assertEquals(c.getPosition(), 4.)
		self.assertEquals(mie.getPosition()[0:2], [2.,3.1] )

	def test__call__returnToStart(self):
		self.scan.relativeScan = False
		self.scan.returnToStart = True
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = MockConcurrentScan
		a, b, c, mie = createScannables(1,2,3, [1,2.1])

		self.scan(a,1,2,3,mie,[400,410],[500,510],[6,6.1],c) #increments *every* scannable field by 1
		self.assertEquals(MockConcurrentScan.args, [a,1,2,3,mie,[400,410],[500,510],[6,6.1],c] )
		self.assertEquals(a.getPosition(), 1.) # returned
		self.assertEquals(b.getPosition(), 2)
		self.assertEquals(c.getPosition(), 4.)
		self.assertEquals(mie.getPosition()[0:2], [1.,2.1] ) # returned
		
	def test__call__relative(self):
		self.scan.relativeScan = True
		self.scan.returnToStart = False
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = MockConcurrentScan
		a, _, c, mie = createScannables(1,2,3, [1,2])

		self.scan(a,1,2,3,mie,[400,410],[500,510],[6,6.1],c) #increments *every* scannable field by 1
		self.assertEquals(MockConcurrentScan.args, [a,2.,3.,3,mie,[401,412],[501,512],[6,6.1],c] )


	def testUpdateScanListenersAndPrepareForScan(self):
		a, _, _, _ = createScannables(1,2,3, [1,2.1])
		mock = Mock()
		scan = SimpleScanWrapper(False, False, (mock.scanListener1,mock.scanListener2))
		scan._createScan = Mock(return_value = mock.concurrentScan)

		scan.__call__(a,1,2,3)
		self.assertEqual(mock.method_calls[0], ('scanListener1.prepareForScan', (), {}))
		self.assertEqual(mock.method_calls[1], ('scanListener2.prepareForScan', (), {}))
		# ...
		self.assertEqual(mock.method_calls[-3], ('concurrentScan.runScan', (), {}))
		self.assertEqual(mock.method_calls[-2], ('scanListener1.update', (mock.concurrentScan,), {}))
		self.assertEqual(mock.method_calls[-1], ('scanListener2.update', (mock.concurrentScan,), {}))

		
	def test__call__listenerResultReturnMechanism(self):
		a, _, _, _ = createScannables(1,2,3, [1,2.1])
		gdascripts.scan.concurrentScanWrapper.pos = mockpos
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = MockConcurrentScan
		scanListener1 = SimpleScanListener()
		scanListener2 = SimpleScanListener()
		scan = SimpleScanWrapper(False, False, (scanListener1,scanListener2))

		result = scan.__call__(a,1,2,3)
		self.assertEquals(result, None)

		scanListener2.toReturn = 'from2'
		result = scan.__call__(a,1,2,3)
		self.assertEquals(result, 'from2')
		
		scanListener1.toReturn = 'from1'
		scanListener2.toReturn = 'from2'
		result = scan.__call__(a,1,2,3)
		self.assertEquals(result, ['from1','from2'])
		
	def test_constructUserCommand(self):
		self.assertEquals(self.scan._constructUserCommand((1,2.1,'a')),
						'simplescanwrapper 1 2.1 a')

	def test_constructUserCommandWithScannable(self):
		a, b, c, _ = createScannables(1,2,3, [1,2.1])							
		self.assertEquals(self.scan._constructUserCommand((a, 1, b, 2, c, 3, 'aa')),
						'simplescanwrapper a 1 b 2 c 3 aa')
		
	def test_appendStringToSRSFileHeader_creates_if_needed(self):
		d = {'should_remain':1}
		gdascripts.scan.concurrentScanWrapper.ROOT_NAMESPACE_DICT = d
		self.scan._appendStringToSRSFileHeader('ABCD\n')
		self.assertEquals(d, {'should_remain':1, 'SRSWriteAtFileCreation': '\nABCD\n'})
		
	def test_appendStringToSRSFileHeader(self):
		d = {'should_remain':1, 'SRSWriteAtFileCreation': 'abcd\n'}
		gdascripts.scan.concurrentScanWrapper.ROOT_NAMESPACE_DICT = d
		self.scan._appendStringToSRSFileHeader('EFGH\n')
		self.assertEquals(d, {'should_remain':1, 'SRSWriteAtFileCreation': 'abcd\nEFGH\n'})
	
def suite():
	return unittest.TestLoader().loadTestsFromTestCase(TestConcurrentScanWrapper)

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())