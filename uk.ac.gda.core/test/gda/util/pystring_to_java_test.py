#@PydevCodeAnalysisIgnore
import unittest
from gda.util import PyStringToJava
import java
class PyStringToJavaTest(unittest.TestCase):
    def dummy(self):
        """
        Similar to 'scan' in CLAM (but order of arguments not the same). This moves several scannable objects simultaneously,
        the same number of steps. After each movement data is collected from items in the allDetectors vector.
        Expect arguments in the following format:
        scannable1 start stop step scannable2 [start] [[stop] step] scannable3 [start] [[stop] step]
        The number of steps is calculated from scannable1.
        For subsequent scannables: if only 'start' then they are moved only to that position. If no start value given then
        the current position will be used (so this scannable will not be moved, but will be included in any output from the
        scan.
        If a step value given then the scannable will be moved each time
        If a stop value is also given then this is treated as a nested scan containing one scannable. This scan will be run
        in full at each node of the main scan. If there are multiple nested scans then they are nested inside each other to
        create a multidimensional scan space (rasta scan).
        """
        pass
    def testListOfTuplesToJava(self):
        expected = [("noquote\ntest", "singlequote'''ffasfs", "doublequote"+ "\"" +"fdsfsff"),
                ("singlequote'''beforedouble"+ "\""+ "ffasfs","doublequote"+ "\"" +"beforesingle''ff", self.dummy.__doc__) ]
        actual = PyStringToJava.ListOfTuplesToJava(`expected`)
        print "output"
        s = java.lang.String("noquote\ntest");
        print `s`
        print expected
        print `expected[1][2]`    
        print `actual[1][2]`    
        for  i in range(2) :
            for  j in range(3) :
                self.assertEqual(expected[i][j], actual[i][j]);
if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(PyStringToJavaTest)
    unittest.TextTestRunner(verbosity=2).run(suite)            