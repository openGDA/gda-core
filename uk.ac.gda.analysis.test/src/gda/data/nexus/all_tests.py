import unittest
import gda.data.nexus.gda_nexusfile_test
def suite():
    return unittest.makeSuite(gda.data.nexus.gda_nexusfile_test.GDANexusFileTest,'test')

if __name__ == '__main__':
    unittest.TextTestRunner(verbosity=2).run(suite())            