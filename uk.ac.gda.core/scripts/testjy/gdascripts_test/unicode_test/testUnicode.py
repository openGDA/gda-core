import unittest

class TestUnicode(unittest.TestCase):
	def testUnicode(self):
		try:
			unicode("100 μm")
		except:
			self.fail("The unicode command should not have caused an exception if utf-8 encoding has been set correctly")

def suite():
	return unittest.TestSuite((
							unittest.TestLoader().loadTestsFromTestCase(TestUnicode)))

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())