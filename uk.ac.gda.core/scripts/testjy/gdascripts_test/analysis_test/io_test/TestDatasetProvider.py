from gdascripts.analysis.io import DatasetProvider as DPModule

import mock
import unittest
import time


class TestLazyDatasetProvider(unittest.TestCase):
	
	def setUp(self):
		self.ldp = DPModule.LazyDataSetProvider('/path/to/file')
		DPModule.loadImageIntoSFH = mock.Mock()
		DPModule.loadImageIntoSFH.return_value = [mock.sentinel.dataset1]
		self.ldp = DPModule.LazyDataSetProvider('/path/to/file')
		
	def testIsLazy(self):
		self.assertEquals(None, self.ldp.dataset)
		
	def testConfigure(self):
		self.ldp.configure()
		self.assertEquals(mock.sentinel.dataset1, self.ldp.dataset)
		self.ldp.configure()
		self.assertEquals(DPModule.loadImageIntoSFH.call_args_list,[(('/path/to/file', None), {})]) 

	def tearDown(self):
		import gdascripts.analysis.io.DatasetProvider
		reload(gdascripts.analysis.io.DatasetProvider)

class TestLazyDatasetProviderWithTimeout(TestLazyDatasetProvider):
	
	def setUp(self):
		DPModule.tryToLoadDataset = mock.Mock()
		global tryToLoadResults
		tryToLoadResults=[mock.sentinel.dataset1, None, None]
		DPModule.tryToLoadDataset.side_effect = tryToLoadResults_sideffect
		self.ldp = DPModule.LazyDataSetProvider('/path/to/file', fileLoadTimout=100) #s
	
	def testConfigure(self):
		self.ldp.configure()
		self.assertEquals(mock.sentinel.dataset1, self.ldp.dataset)
		self.ldp.configure()
		self.assertEquals(3, DPModule.tryToLoadDataset.call_count)
		
	def testConfigureWithInadequateTimeout(self):
		self.ldp = DPModule.LazyDataSetProvider('/path/to/file', fileLoadTimout=2) #s
		try:
			self.ldp.configure()
			self.fail("IOError expected")
		except IOError, e:
			self.assertEquals("Could not load file '/path/to/file', within specified timeout of 2.000000 s", e.message)

tryToLoadResults = None
def tryToLoadResults_sideffect(*args, **kwargs):
	time.sleep(1)
	return tryToLoadResults.pop()


def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestLazyDatasetProvider))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestLazyDatasetProviderWithTimeout))

	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	