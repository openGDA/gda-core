from gdascripts.parameters import beamline_parameters
import unittest
import inspect
import os

class test_beamline_parameters(unittest.TestCase):
	
	def setUp(self):
		this_file = inspect.getfile(inspect.currentframe())
		this_folder = this_file[:this_file.rfind("/")]
		self.test_files_folder = os.path.join(this_folder, "test_files")
	
	def test_readDictionaryFromFile(self):
		testfile = os.path.join(self.test_files_folder, "mapping.txt")
		mapping = {}
		beamline_parameters.readDictionaryFromFile(testfile, mapping)
		print mapping
		self.assertEqual(15, len(mapping), "mapping does not have the correct number of keys")
		self.assert_contains(mapping, "one", "two")
		self.assert_contains(mapping, "three", "four")
		self.assert_contains(mapping, "five", "six")
		self.assert_contains(mapping, "seven", "eight")
		self.assert_contains(mapping, "test1", "value with spaces")
		self.assert_contains(mapping, "test2", "value with spaces")
		self.assert_contains(mapping, "test3", "value with spaces")
		self.assert_contains(mapping, "test4", "value with spaces")
		self.assert_contains(mapping, "spacetest1a", "value")
		self.assert_contains(mapping, "spacetest1b", "value")
		self.assert_contains(mapping, "spacetest2a", "value")
		self.assert_contains(mapping, "spacetest2b", "value")
		self.assert_contains(mapping, "spacetest3", "value")
		self.assert_contains(mapping, "keyonly", "")
		self.assert_contains(mapping, "commenttest", "multi word value")
	
	def assert_contains(self, mapping, key, value):
		self.assertTrue(key in mapping, "mapping does not contain '" + key + "'")
		self.assertTrue(mapping[key] == value, "value for '" + key + "' in mapping does not equal '" + value + "'")

def suite():
	return unittest.TestLoader().loadTestsFromTestCase(test_beamline_parameters)

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
