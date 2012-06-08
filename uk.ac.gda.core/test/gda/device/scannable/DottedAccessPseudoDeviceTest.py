from gda.device.scannable import DottedAccessScannableMotionBase
#from gda.device.scannable import PseudoDevice
import unittest

class SimpleDottedAccessPseudoDevice(DottedAccessScannableMotionBase):
	
	def __init__(self):
		self.setName('scn')
		self.setInputNames(['i1','i2'])
		self.setExtraNames(['e1', 'e2'])
		self.setOutputFormat(['%f']*4)
		self.inputPos = [1, 2]
		self.extraPos = [3, 4]
		self.childrenDict = {'abc':1, 'def':2}
		
	def asynchronousMoveTo(self, pos):
		self.inputPos = pos
		
	def getPosition(self):
		return self.inputPos + self.extraPos
	
	def isBusy(self):
		return False

#	def __getattr__(self, name):
#		try:
#			return self.childrenDict[name]
#		except:
#			raise AttributeError
scn = SimpleDottedAccessPseudoDevice()
		
class DottedAccessPseudoDeviceTest(unittest.TestCase):

	def setUp(self):
		self.scn = SimpleDottedAccessPseudoDevice()

	def testAsynchronousMoveToAndGetPosition(self):
		self.scn.asynchronousMoveTo([1.1, 2.2])
		self.assertEquals(self.scn.getPosition(), [1.1, 2.2, 3, 4])

	def test__getattr__(self):
		self.assertEqual(self.scn.i1.getPosition(), 1)
		self.assertEqual(self.scn.e1.getPosition(), 3)
		
def suite():
	return unittest.TestSuite( unittest.TestLoader().loadTestsFromTestCase(DottedAccessPseudoDeviceTest) )

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())