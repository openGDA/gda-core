from gdascripts.scannable.metadata import MetadataCollector, MetadataOneOffNote

from mock import Mock
import unittest


def create_mock_scannable(name='scn', ins=[], outs=[], fmts=[], pos=[]):
    scn = Mock()
    scn.name = name
    scn.inputNames = ins
    scn.extraNames = outs
    scn.outputFormat = fmts
    scn.getPosition.return_value = pos
    return scn

KEY = 'SRSWriteAtFileCreation'

class TestMetadataCollector(unittest.TestCase):

    def setUp(self):
        self.d = {'should_remain': 1, KEY: 'abcd\n'}
        self.mc = MetadataCollector('mc', self.d, [])

    def test_createNamePositionPairsZeroInputOutput(self):
        scn = create_mock_scannable()
        self.assertEqual(self.mc._createNamePositionPairs(scn), [])

    def test_createNamePositionPairsOneInput(self):
        scn = create_mock_scannable(ins=['a'], fmts=['%.3f'], pos=[1.12345])
        self.assertEqual(self.mc._createNamePositionPairs(scn), [('scn', '1.123')])

    def test_createNamePositionPairsOneOutput(self):
        scn = create_mock_scannable(outs=['a'], fmts=['%.3f'], pos=[1.12345])
        self.assertEqual(scn.outputFormat, ['%.3f'])
        self.assertEqual(self.mc._createNamePositionPairs(scn), [('scn', '1.123')])

    def test_createNamePositionPairsMultiInputOutput(self):
        scn = create_mock_scannable(ins=['a'], outs=['b'], fmts=['%.3f', '%.4f'],
                         pos=[1.12345, 2.12345])
        self.assertEqual(self.mc._createNamePositionPairs(scn),
            [('scn.a', '1.123'), ('scn.b', '2.1235')])

    def test_createNamePositionPairsMultiInputOutputNoPrepend(self):
        self.mc.prepend_keys_with_scannable_names = False
        scn = create_mock_scannable(ins=['a'], outs=['b'], fmts=['%.3f', '%.4f'],
                         pos=[1.12345, 2.12345])
        self.assertEqual(self.mc._createNamePositionPairs(scn),
            [('a', '1.123'), ('b', '2.1235')])

    def test_createNamePositionPairsMultiInputOutputwithProblem(self):
        scn = create_mock_scannable(ins=['a'], outs=['b'], fmts=['%.3f', '%.4f'],
                         pos=[1.12345, 2.12345])
        scn.getPosition.side_effect = Exception('Boom!')
        self.assertEqual(self.mc._createNamePositionPairs(scn),
            [('scn.a', 'unavailable'), ('scn.b', 'unavailable')])

    def test_createNamePositionPairsSingleInputWithProblem(self):
        scn = create_mock_scannable(ins=['a'], fmts=['%.3f'],
                         pos=[1.12345])
        scn.getPosition.side_effect = Exception('Boom!')
        self.assertEqual(self.mc._createNamePositionPairs(scn), [('scn', 'unavailable')])

    def test_createNamePositionPairsForScannableThetReturnsStringUnexpectdly(self):
        scn = create_mock_scannable(ins=['a'], fmts=['%.3f'],
                         pos=['Unavailable'])
        self.assertEqual(self.mc._createNamePositionPairs(scn), [('scn', 'Unavailable')])

    def test_createNamePositionPairsForScannableThetReturnsStringsUnexpectdly(self):
        scn = create_mock_scannable(ins=['a', 'b'], fmts=['%.3f', '%.3f'],
                         pos=['Unavailable', 'Unavailable'])
        self.assertEqual(self.mc._createNamePositionPairs(scn), [('scn.a', 'Unavailable'), ('scn.b', 'Unavailable')])

    def test__createHeaderStringForScannablesNone(self):
        mc = MetadataCollector('mc', {}, [])
        self.assertEqual(mc._createHeaderStringForScannables(), '')

    def test__createHeaderStringForScannablesTwo(self):
        scn1 = create_mock_scannable(name='scn1', ins=['a'], outs=['b'], fmts=['%.3f', '%.4f'],
                         pos=[1.12345, 2.12345])
        scn2 = create_mock_scannable(name='scn2', ins=['a'], fmts=['%.3f'],
                         pos=[1.12345])
        scn2.getPosition.side_effect = Exception('Boom!')
        scn3 = create_mock_scannable(name='scn3', ins=['b'], outs=['c'], fmts=['%.3f', '%.4f'],
                         pos=[3.12345, 4.12345])

        mc = MetadataCollector('mc', {}, [scn1, scn2, scn3])
        self.assertEqual(mc._createHeaderStringForScannables(),
"""scn1.a=1.123
scn1.b=2.1235
scn2=unavailable
scn3.b=3.123
scn3.c=4.1235
""")

    def test_append_creates_if_needed(self):
        del self.d[KEY]
        self.assertEqual(self.d, {'should_remain': 1})
        self.mc._append('ABCD\n')
        self.assertEqual(self.d, {'should_remain': 1, KEY: 'ABCD\n'})

    def test_append(self):
        self.mc._append('EFGH\n')
        self.assertEqual(self.mc.rootNamespaceDict, {'should_remain': 1, KEY: 'abcd\nEFGH\n'})

    def test_clear(self):
        self.mc._clear()
        self.assertEqual(self.d, {'should_remain': 1, KEY: ''})

    def test_atScanEnd(self):
        self.mc.atScanEnd()
        self.assertEqual(self.mc.rootNamespaceDict, {'should_remain': 1, KEY: 'abcd\n'})

    def test_atCommandFailure(self):
        self.mc.atCommandFailure()
        self.assertEqual(self.d, {'should_remain': 1, KEY: ''})

    def test_atScanStart(self):
        scn = create_mock_scannable(ins=['a'], fmts=['%.3f'], pos=[1.12345])
        self.mc.scannables_to_read = [scn]
        self.mc.atScanStart()
        lines = self.d[KEY].split('\n')
        self.assertEqual(lines[0], 'abcd')
        self.assertEqual(lines[1], '')
        assert lines[2].startswith("date='")
        self.assertEqual(lines[3], 'scn=1.123')
        self.assertEqual(lines[4], '')


class TestMetadataOneOffNote(unittest.TestCase):

    def test_call_creating(self):
        d = {'should_remain': 1}
        note = MetadataOneOffNote(d)
        note('ABCD')
        self.assertEqual(d, {'should_remain': 1, KEY: "\nnote='ABCD'\n"})

    def test_call(self):
        d = {'should_remain': 1, KEY: 'abcd\n'}
        note = MetadataOneOffNote(d)
        note('EFGH')
        self.assertEqual(d, {'should_remain': 1, KEY: "abcd\nnote='EFGH'\n"})


def suite():
    return unittest.TestSuite((
                            unittest.TestLoader().loadTestsFromTestCase(TestMetadataCollector),
                            unittest.TestLoader().loadTestsFromTestCase(TestMetadataOneOffNote)))

if __name__ == '__main__':
    unittest.TextTestRunner(verbosity=2).run(suite())
