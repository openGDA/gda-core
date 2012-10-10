#@PydevCodeAnalysisIgnore
import unittest
import os.path
import os
import jarray
from gda.analysis import ScanFileHolder #@UnresolvedImport
from gda.analysis.io import RawNexusLoader, AsciiScanFileHolderSaver, SimpleNexusSaver, SRSLoader #@UnresolvedImport
from gda.data.nexus import GdaNexusFile #@UnresolvedImport
from org.nexusformat import NexusFile; #@UnresolvedImport
from java.util import Arrays
from gda.jython import InterfaceProvider #@UnresolvedImport
from gda.jython import MockJythonServerFacade #@UnresolvedImport

TestFileFolder = "test/gda/analysis/io/TestFiles";
class RawNexusLoaderTest(unittest.TestCase):
    def setUp(self):
        InterfaceProvider.setTerminalPrinterForTesting(MockJythonServerFacade())
        self.abspath = os.path.abspath(TestFileFolder + "/1.nxs")
        parentPath = os.path.split(self.abspath)[0]
        if not os.path.isdir(parentPath):
            os.makedirs(parentPath)
        if os.path.isfile(self.abspath):
            os.remove(self.abspath)

    def testSimpleCreation(self):
        file = GdaNexusFile(self.abspath, GdaNexusFile.NXACC_CREATE5);
        file.makegroup("ScanFileHolder", "NXentry");
        file.opengroup("ScanFileHolder", "NXentry");
        file.makegroup("datasets", "NXdata");
        file.opengroup("datasets", "NXdata");        
        file.makedata("heading1", NexusFile.NX_FLOAT64, 2,[10,100000]);#@UndefinedVariable
        file.opendata("heading1");
        dataIn = jarray.array(range(1000000),"d")
        file.putdata(dataIn)
        iDim = jarray.array(range(20),"i")
        iStart = jarray.array(range(2),"i")
        file.getinfo(iDim, iStart)
        file.closedata();
#        print iDim
#        print iStart
        file.close();
        
        rnl = RawNexusLoader(self.abspath)
        sfh = ScanFileHolder()
        sfh.load(rnl);
        sfh.info()
        ds = sfh.getDataSet("heading1")

        os.remove(self.abspath)
# This cannot work as the saved file is _NOT_ a valid SRS format
#        sfh.save(AsciiScanFileHolderSaver(self.abspath+"_srs"));
#
#        sfh = ScanFileHolder()
#        sfh.load(SRSLoader(self.abspath+"_srs"));#@UndefinedVariable
#        os.remove(self.abspath)
        sfh.save(SimpleNexusSaver(self.abspath));
        
        file = GdaNexusFile(self.abspath, GdaNexusFile.NXACC_READ);
        file.opengroup("ScanFileHolder", "NXentry");
        file.opengroup("datasets", "NXdata");        
        file.opendata("heading1");
        dataOut = jarray.array(range(1000000),"d")
        dataOut[999999]=0.
        file.getdata(dataOut)
        file.closedata();
        file.close();
        if dataIn!=dataOut:
            self.fail("dataIn != dataOut")
        
        
if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(RawNexusLoaderTest)
    unittest.TextTestRunner(verbosity=2).run(suite)            