#@PydevCodeAnalysisIgnore
import unittest
import os.path
import jarray
from java.util import Arrays
from org.eclipse.dawnsci.hdf5.nexus import NexusFileHDF5
from org.eclipse.january.dataset import DatasetFactory, DoubleDataset
from org.eclipse.january.dataset import SliceND

TestFileFolder = "test-scratch/gda/data/nexus/GdaNexusTestFiles";
class NexusFileTest(unittest.TestCase):
    def testSimpleCreation(self):
        abspath = os.path.abspath(TestFileFolder + "/1.nxs")
        parentPath = os.path.split(abspath)[0]
        if not os.path.exists(parentPath):
            os.makedirs(parentPath)
        file = NexusFileHDF5.createNexusFile(abspath)
        g = file.getGroup("/ScanFileHolder:NXentry/datasets:NXdata", True)
        lazy = NexusFileHDF5.createLazyWriteableDataset("heading1", DoubleDataset, [10, 100000], None, None)
        file.createData(g, lazy)
        dataIn = DatasetFactory.createRange(lazy.getSize())
        dataIn.shape = lazy.getShape()
        lazy.setSlice(None, dataIn, SliceND.createSlice(lazy, None, None))
        file.close()
        file = NexusFileHDF5.openNexusFileReadOnly(abspath)
        g = file.getGroup("/ScanFileHolder:NXentry/datasets:NXdata", False)
        dataOut = file.getData(g, "heading1").getDataset().getSlice()
        file.close()
        self.assertEqual(dataIn,dataOut)
        
if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(NexusFileTest)
    unittest.TextTestRunner(verbosity=2).run(suite)
