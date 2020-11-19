from gdascripts.scannable.beamokay import WaitWhileScannableBelowThreshold,\
	WaitForScannableState
from gda.device import Scannable, DeviceException
from mock import Mock
import unittest


class TestWaitWhileScannableBelowThreshold(unittest.TestCase):
    
    def setUp(self):
        self.scn_to_monitor = Mock(spec=Scannable)
        self.scn_to_monitor.getName.return_value('mon')
        self.scn_to_monitor.name = 'mon'
        
        self.ws = WaitWhileScannableBelowThreshold('ws', self.scn_to_monitor, 1, .1, 1)
        
    def test__init__(self):
        self.assertEqual(self.ws.name, 'ws')
        self.assertEqual(list(self.ws.inputNames), [])
        self.assertEqual(list(self.ws.extraNames), ['beamok'])
        self.assertEqual(list(self.ws.outputFormat), ['%.0f'])
        
    def test_isBusy(self):
        self.ws.isBusy()
        self.scn_to_monitor.isBusy.assert_called_once_with()
        
    def test_getStatus(self):
        self.ws.minimumThreshold = 2
        self.scn_to_monitor.getPosition.return_value = 3
        self.assertEqual(self.ws._getStatus(), True)
        self.scn_to_monitor.getPosition.return_value = 1
        self.assertEqual(self.ws._getStatus(), False)
        self.scn_to_monitor.getPosition.return_value = 2
        self.assertEqual(self.ws._getStatus(), True)
    
    def testHandleStatusChangeGoodGood(self):
        self.ws.handleStatusChange(True)
        self.ws.handleStatusChange(True)
        
    def testHandleStatusChangeGoodBad(self):
        self.ws.handleStatusChange(True)
        self.ws.handleStatusChange(False)
        
    def testHandleStatusChangeBadGood(self):
        self.ws.handleStatusChange(False)
        self.ws.handleStatusChange(True)
        
    def testHandleStatusChangeBadBad(self):
        self.ws.handleStatusChange(False)
        self.ws.handleStatusChange(False)
        
        
class TestWaitForScannableState(unittest.TestCase):
    
    def setUp(self):
        self.scn_to_monitor = Mock(spec=Scannable)
        self.scn_to_monitor.getName.return_value('shutter')
        self.scn_to_monitor.name = 'shutter'
        self.ws = WaitForScannableState('ws', self.scn_to_monitor, .1, None, ['Open'], ['Fault'])
        
    def test__init__(self):
        self.assertEqual(self.ws.name, 'ws')
        self.assertEqual(list(self.ws.inputNames), [])
        self.assertEqual(list(self.ws.extraNames), [])
        self.assertEqual(list(self.ws.outputFormat), [])
        
    def test_getStatus(self):
        self.scn_to_monitor.getPosition.return_value = 'Open'
        self.assertEqual(self.ws._getStatus(), True)
        self.scn_to_monitor.getPosition.return_value = 'Other'
        self.assertEqual(self.ws._getStatus(), False)
        
    def test_getFaultStatus(self):
        self.scn_to_monitor.getPosition.return_value = 'Fault'
        try:
            self.ws._getStatus()
            self.fail("DeviceException expected")
        except DeviceException, e:
            self.assertEqual(e.message, "ws found shutter to be in state: Fault")
    
    def testHandleStatusChangeGoodGood(self):
        self.ws.handleStatusChange(True)
        self.ws.handleStatusChange(True)
        
    def testHandleStatusChangeGoodBad(self):
        self.ws.handleStatusChange(True)
        self.ws.handleStatusChange(False)
        
    def testHandleStatusChangeBadGood(self):
        self.ws.handleStatusChange(False)
        self.ws.handleStatusChange(True)
        
    def testHandleStatusChangeBadBad(self):
        self.ws.handleStatusChange(False)
        self.ws.handleStatusChange(False)    

        
def suite():
    return unittest.TestSuite((
        unittest.TestLoader().loadTestsFromTestCase(TestWaitWhileScannableBelowThreshold),
        unittest.TestLoader().loadTestsFromTestCase(TestWaitForScannableState)
        ))
    
if __name__ == '__main__':
    unittest.TextTestRunner(verbosity=2).run(suite())
