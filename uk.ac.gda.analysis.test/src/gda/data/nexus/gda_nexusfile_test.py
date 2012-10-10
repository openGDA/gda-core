#@PydevCodeAnalysisIgnore
import unittest
import os.path
import jarray
from gda.data.nexus import GdaNexusFile
from org.nexusformat import NexusFile;
from java.util import Arrays
TestFileFolder = "test-scratch/gda/data/nexus/GdaNexusTestFiles";
class GDANexusFileTest(unittest.TestCase):
    def testSimpleCreation(self):
        abspath = os.path.abspath(TestFileFolder + "/1.nxs")
        parentPath = os.path.split(abspath)[0]
        if not os.path.exists(parentPath):
            os.makedirs(parentPath)
        file = GdaNexusFile(abspath, GdaNexusFile.NXACC_CREATE5);
        file.makegroup("ScanFileHolder", "NXentry");
        file.opengroup("ScanFileHolder", "NXentry");
        file.makegroup("datasets", "NXdata");
        file.opengroup("datasets", "NXdata");        
        file.makedata("heading1", NexusFile.NX_FLOAT64, 2,[10,100000]);
        file.opendata("heading1");
        dataIn = jarray.array(range(1000000),"d")
        file.putdata(dataIn)
        file.closedata();
        file.close();
        file = GdaNexusFile(abspath, GdaNexusFile.NXACC_READ);
        file.opengroup("ScanFileHolder", "NXentry");
        file.opengroup("datasets", "NXdata");        
        file.opendata("heading1");
        dataOut = jarray.array(range(1000000),"d")
        dataOut[999999]=0.
        file.getdata(dataOut)
        file.closedata();
        file.close();
        self.assertEqual(dataIn,dataOut)
        
if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(GDANexusFileTest)
    unittest.TextTestRunner(verbosity=2).run(suite)            