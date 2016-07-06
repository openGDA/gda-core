#@PydevCodeAnalysisIgnore
import unittest
import os.path
import os
import jarray
from gda.analysis import ScanFileHolder #@UnresolvedImport
from gda.analysis.io import AsciiScanFileHolderSaver, SimpleNexusSaver, SRSLoader #@UnresolvedImport
from gda.data.nexus import NexusUtils
from org.eclipse.january.dataset import Dataset, DatasetFactory
from org.eclipse.january.dataset import SliceND
from java.util import Arrays
from gda.jython import InterfaceProvider #@UnresolvedImport
from gda.jython import MockJythonServerFacade #@UnresolvedImport

TestFileFolder = "test-scratch/gda/analysis/io/TestFiles";
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
#         file = NexusUtils.createNexusFile(self.abspath)
#         g = file.getGroup("/ScanFileHolder:NXentry/datasets:NXdata", True)
#         lazy = NexusUtils.createLazyWriteableDataset("heading1", Dataset.FLOAT64, [10, 100000], None, None)
#         file.createData(g, lazy)
#         dataIn = DatasetFactory.createRange(lazy.getSize(), Dataset.FLOAT64)
#         dataIn.shape = lazy.getShape()
#         lazy.setSlice(None, dataIn, SliceND.createSlice(lazy, None, None))
#         file.close()
#         os.remove(self.abspath)

# This cannot work as the saved file is _NOT_ a valid SRS format
#        sfh.save(AsciiScanFileHolderSaver(self.abspath+"_srs"));
#
        dataIn = DatasetFactory.createRange(1000000, Dataset.FLOAT64)
        dataIn.shape = [10,100000]
        sfh = ScanFileHolder()
        sfh.addDataSet("heading1", dataIn)
#        sfh.load(SRSLoader(self.abspath+"_srs"));#@UndefinedVariable
#        os.remove(self.abspath)
        sfh.save(SimpleNexusSaver(self.abspath));
        
        file = NexusUtils.openNexusFileReadOnly(self.abspath)
        g = file.getGroup("/ScanFileHolder:NXentry/datasets:NXdata", False)
        dataOut = file.getData(g, "heading1").getDataset().getSlice()
        file.close()
        if dataIn!=dataOut:
            self.fail("dataIn != dataOut")
        
        
if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(RawNexusLoaderTest)
    unittest.TextTestRunner(verbosity=2).run(suite)            