import unittest
from mock import Mock, call, patch

with patch('gda.jython.commands.GeneralCommands.alias') as alias:
    # alias requires a working JythonServerFacade
    import bimorph_mirror_optimising as bmo

mock_caclient = Mock()
mock_sleep = Mock()
mock_scan = Mock()
mock_scan_aborter = Mock()

bmo.sleep = mock_sleep
bmo.CAClient = mock_caclient
bmo.defScanAborter = mock_scan_aborter
bmo.ConcurrentScan = mock_scan


class TestBimorphOptimisationFunctions(unittest.TestCase):

    def setUp(self):
        mock_caclient.reset_mock()
        mock_caclient.return_value = Mock()
        mock_sleep.reset_mock()

    def test_generate_positions(self):
        init_positions = [1,2,3,4,5]
        generated_positions = bmo.generatePositions(init_positions, 1)
        expected_positions = [[1,2,3,4,5],
                              [2,2,3,4,5],
                              [2,3,3,4,5],
                              [2,3,4,4,5],
                              [2,3,4,5,5],
                              [2,3,4,5,6]]
        self.assertEqual(generated_positions, expected_positions)

    def test_generate_grouped_positions(self):
        init_poisitions = [1,2,3,4,5]
        generated_positions = bmo.generateGroupedPositions(init_poisitions, 1, '1-2,3,4-5')
        expected_positions = [[1,2,3,4,5],
                              [2,3,3,4,5],
                              [2,3,4,4,5],
                              [2,3,4,5,6]]
        self.assertEqual(generated_positions, expected_positions)

    def test_topup_countdown_outside_topup(self):
        countdown = bmo.TopupCountdown('countdown')
        mock_caclient().caget.return_value = 23.3
        countdown.atPointStart()
        mock_caclient().caget.assert_called_once_with()

    def test_topup_countdown_during_topup(self):
        countdown = bmo.TopupCountdown('countdown')
        mock_caclient().caget.side_effect = (5, 0, 15)
        countdown.atPointStart()
        self.assertEqual(3, len(mock_caclient().caget.mock_calls))
        mock_sleep.assert_has_calls([call(5), call(5)])
        

class TestSlitScanner(unittest.TestCase):
    def setUp(self):
        mock_sleep.reset_mock()
        mock_scan.reset_mock()
        mock_scan_aborter.reset_mock()

    def test_run_with_no_groups(self):
        namespace = get_mock_globals()
        mock_mirror = namespace.get('mirror')
        mock_mirror.inputNames = list(map(str, range(3)))
        mock_mirror.getPosition.return_value = [1,2,3]
        ss = bmo.SlitScanner()
        ss.run(namespace,
               mirrorName='mirror',
               increment=1,
               slitToScanSizeName='slitToScanSize',
               slitToScanPosName='slitToScanPos',
               slitSize=12.2,
               otherSlitSizeName='otherSlitSize',
               otherSlitPosName='otherSlitPos',
               slitStart=1,
               slitEnd=10,
               slitStep=2,
               detectorName='detector',
               exposure=4.3,
               settleTime=17.2,
               otherSlitSizeValue=14.2,
               otherSlitPosValue=18.1,
               doOptimization=Mock(),
               grouped=None,
               groups_string='')
        namespace.get('slitToScanSize').assert_called_once_with(12.2)
        namespace.get('otherSlitSize').assert_called_once_with(14.2)
        namespace.get('otherSlitPos').assert_called_once_with(18.1)
        self.assertEqual(mock_scan_aborter.isOK.call_count, 4)
        mock_sleep.assert_has_calls([call(17.2)]*4)
        mock_mirror.assert_has_calls([call([1, 2, 3]), call([2,2,3]), call([2,3,3]), call([2,3,4])], any_order=True)
        mock_scan.assert_has_calls([call([namespace.get('slitToScanPos'), 1, 10, 2, namespace.get('detector'), 4.3, namespace.get('peak2d'), bmo.defScanAborter, bmo.bm_topup]), call().runScan()]*4)

    def test_run_with_groups(self):
        namespace = get_mock_globals()
        mock_mirror = namespace.get('mirror')
        mock_mirror.inputNames = list(map(str, range(5)))
        mock_mirror.getPosition.return_value = [1, 2, 3, 4, 5]
        ss = bmo.SlitScanner()
        ss.run(namespace,
               mirrorName='mirror',
               increment=1,
               slitToScanSizeName='slitToScanSize',
               slitToScanPosName='slitToScanPos',
               slitSize=12.2,
               otherSlitSizeName='otherSlitSize',
               otherSlitPosName='otherSlitPos',
               slitStart=1,
               slitEnd=10,
               slitStep=2,
               detectorName='detector',
               exposure=4.3,
               settleTime=17.2,
               otherSlitSizeValue=14.2,
               otherSlitPosValue=18.1,
               doOptimization=Mock(),
               grouped=True,
               groups_string='1-2, 3, 4-5')
        namespace.get('slitToScanSize').assert_called_once_with(12.2)
        namespace.get('otherSlitSize').assert_called_once_with(14.2)
        namespace.get('otherSlitPos').assert_called_once_with(18.1)
        self.assertEqual(mock_scan_aborter.isOK.call_count, 4)
        mock_sleep.assert_has_calls([call(17.2)]*4)
        mock_mirror.assert_has_calls([call([1, 2, 3, 4, 5]), call([2, 3, 3, 4, 5]), call([2, 3, 4, 4, 5]), call([2, 3, 4, 5, 6])], any_order=True)
        mock_scan.assert_has_calls([call([namespace.get('slitToScanPos'), 1, 10, 2, namespace.get('detector'), 4.3, namespace.get('peak2d'), bmo.defScanAborter, bmo.bm_topup]), call().runScan()]*4)



def get_mock_globals():
    namespace = Mock()
    mock_namespace = {}
    namespace.get.side_effect = lambda x: mock_namespace.setdefault(x, Mock())
    return namespace