import unittest
import gda_completer
import __builtin__
import keyword
from java.lang import RuntimeException
from gda.device.detector import NXDetector

# Type constants used to pick the icon to display for the option
# The usage is chosen to match PyDev see jyimportsTipper.py
TYPE_IMPORT = 0  # Blue Dot
TYPE_CLASS = 1  # Yellow Diamond
TYPE_FUNCTION = 2  # Blue Triangle
TYPE_ATTR = 3  # Green Circle
TYPE_BUILTIN = 4  # Gray Circle
TYPE_PARAM = 5  # No icon

# The number of keywords and globals expected. -1 because print is in both and is excluded once
keywords_and_globals = len(dir(__builtin__)) + len(keyword.kwlist) - 1

class CompleterTest(unittest.TestCase):

    def setUp(self):
        self.completer = gda_completer.Completer(globals())

    def test_keywords_and_builtins(self):
        # Complete on nothing
        options = self.completer.complete('')
        # Convert to dict
        options_dict = dict((option[0], option) for option in options)

        # Check all the keywords are in the completion options and their icon is right
        for keyword_ in keyword.kwlist:
            self.assertTrue(options_dict.has_key(keyword_), str(keyword_) + " was not in completions options")
            # Check the icon
            name, doc, args, icon = options_dict.get(keyword_) # @UnusedVariable
            self.assertEqual(icon, TYPE_BUILTIN, str(keyword_) + " had wrong icon")

        # Check all the builtins are in the completion options and their icon is right
        for builtin_ in dir(__builtin__):
            self.assertTrue(options_dict.has_key(builtin_), "Builtin was not in completions options")
            # Check the icons
            name, doc, args, icon = options_dict.get(builtin_)  # @UnusedVariable
            try:
                obj = eval(builtin_, globals()) # Get the object to find out what it is
            except SyntaxError:
                # Calling eval on x resulted in a SyntaxError therefore it 'must' be a keyword
                # This is almost a special case for 'print' which was changed in 2.6
                # https://docs.python.org/2/library/functions.html#print
                continue # So don't do anything with it
            if isinstance (obj, type):
                self.assertEqual(icon, TYPE_CLASS, str(builtin_) + " had wrong icon")
            elif callable(obj):
                self.assertEqual(icon, TYPE_FUNCTION, str(builtin_) + " had wrong icon")
            else:
                self.assertEqual(icon, TYPE_ATTR, str(builtin_) + " had wrong icon")

    def test_right_number_of_options_are_found(self):
        options = self.completer.complete('')
        self.assertEqual(len(options), keywords_and_globals + len(globals()), "Wrong number of completion options found")

    def test_adding_global(self):
        # Make a new object and add it to the globals
        global new_object
        new_object = object()

        # Complete on new_object
        options = self.completer.complete('new_object')
        # Convert to dict
        options_dict = dict((option[0], option) for option in options)

        self.assertTrue(options_dict.has_key('new_object'), 'new_object not in completion options')
        # Ensure its the only option
        self.assertEqual(len(options), 1, "Unexpected options found")

    def test_testObject_thows_exception(self):
        obj = testObject()
        self.assertRaises(Exception, obj.getName)

    def test_completing_python_object(self):
        global obj
        obj = testObject()
        # Complete on obj.
        options = self.completer.complete('obj.')
        # Convert to dict
        options_dict = dict((option[0], option) for option in options)

        # There are 3 additional attributes on subclasses '__dict__', '__module__' and '__weakref__'
        # see http://stackoverflow.com/questions/16513029/where-do-classes-get-their-default-dict-attributes-from/16515220
        # There are then an additonal 3 from the testObject class 'name', 'number' and 'getName'
        self.assertEqual(len(options), len(dir(object())) + 6 + 3, 'Number of options was wrong')

        name, doc, args, icon = options_dict.get('getName')  # @UnusedVariable
        self.assertEqual(icon, TYPE_FUNCTION, 'getName had wrong icon')

        name, doc, args, icon = options_dict.get('name')  # @UnusedVariable
        self.assertEqual(icon, TYPE_ATTR, 'name had wrong icon')

        name, doc, args, icon = options_dict.get('number')  # @UnusedVariable
        self.assertEqual(icon, TYPE_ATTR, 'number had wrong icon')

    def test_class(self):
        # Complete on obj.
        options = self.completer.complete('')
        # Convert to dict
        options_dict = dict((option[0], option) for option in options)

        self.assertTrue(options_dict.has_key('testObject'), 'testObject class not found')

        name, doc, args, icon = options_dict.get('testObject') # @UnusedVariable
        self.assertEqual(icon, TYPE_CLASS, "testObject class had wrong icon")


    def test_NXDetector_getPosition_throws(self):
        # make a dummy scannable
        global nd
        nd = NXDetector()
        # Check getPosition throws
        self.assertRaises(RuntimeException, nd.getPosition)

    def test_NXDetector_completion_doesnt_call_methods(self):
        # make a dummy scannable
        global nd
        nd = NXDetector()

        # Complete on nd. If this calls methods it will throw and the test will fail
        options = self.completer.complete('nd.')
        # Convert to dict
        options_dict = dict((option[0], option) for option in options)

        # Check getPosition
        name, doc, args, icon = options_dict.get('getPosition') # @UnusedVariable
        self.assertEqual(name, 'getPosition')
        self.assertEqual(icon, TYPE_FUNCTION, 'getPosition had wrong icon')

        # Check isBusy
        name, doc, args, icon = options_dict.get('isBusy')  # @UnusedVariable
        self.assertEqual(name, 'isBusy')
        self.assertEqual(icon, TYPE_FUNCTION, 'isBusy had wrong icon')

        # Check additionalPluginList
        name, doc, args, icon = options_dict.get('additionalPluginList')  # @UnusedVariable
        self.assertEqual(name, 'additionalPluginList')
        self.assertEqual(icon, TYPE_ATTR, 'additionalPluginList had wrong icon')

    def test_camel_case_complete(self):
        self.completer.match_maker = gda_completer.camel_snake_match
        global obj
        obj = testObject()
        # Complete on obj.
        options = self.completer.complete('obj.cCA')
        self.assertListEqual(options, [('camelCaseAttribute', '', '', TYPE_ATTR)])
        options = self.completer.complete('obj.gN')
        self.assertListEqual(options, [('getName', '', '', TYPE_FUNCTION)])

    def test_snake_case_complete(self):
        self.completer.match_maker = gda_completer.camel_snake_match
        global obj
        obj = testObject()
        # Complete on obj.
        options = self.completer.complete('obj.s_c_a')
        self.assertListEqual(options, [('snake_case_attribute', '', '', TYPE_ATTR)])
        options = self.completer.complete('obj.g_n')
        self.assertListEqual(options, [('get_name', '', '', TYPE_FUNCTION)])

    def test_camel_and_snake_case_globals(self):
        self.completer.match_maker = gda_completer.camel_snake_match
        options = self.completer.complete('FPE')
        self.assertListEqual(options, [('FloatingPointError', '', '', TYPE_CLASS)])
        options = self.completer.complete('r_i')
        self.assertListEqual(options, [('raw_input', '', '', TYPE_FUNCTION)])

    def test_fuzzy_search(self):
        self.completer.match_maker = gda_completer.fuzzy_match
        options = self.completer.complete('ingpoi')
        self.assertListEqual(options, [('FloatingPointError', '', '', TYPE_CLASS)])
        options = self.completer.complete('ingpio')
        self.assertListEqual(options, [])

    def test_basic_completion(self):
        self.completer.match_maker = gda_completer.basic_match
        global obj
        obj = testObject()
        options = self.completer.complete('floati')
        self.assertListEqual(options, [('FloatingPointError', '', '', TYPE_CLASS)])
        options = self.completer.complete('obj.n')
        self.assertListEqual(options, [('name', '', '', TYPE_ATTR), ('number', '', '', TYPE_ATTR)])
        # Don't complete camel case
        options = self.completer.complete('obj.gN')
        self.assertListEqual(options, [])
        options = self.completer.complete('FPE')
        self.assertListEqual(options, [])
        # Don't complete fuzzy
        options = self.completer.complete('obj.attribute')
        self.assertListEqual(options, [])
        options = self.completer.complete('case')
        self.assertListEqual(options, [])

    def test_java_package(self):
        global gda
        import gda
        options = self.completer.complete('gda.device.motor.');
        options_dict = dict((option[0], option) for option in options)

        self.assertTrue(options_dict.has_key('DummyMotor'), 'Completion incorrect for java package')

    def test_invalid_java_package(self):
        global gda
        import gda
        options = self.completer.complete('gda.device.thispackagedoesntexist.');

        self.assertTrue(options == [], "Completion returned completions for package that doesn't exist")

# Test helper class
class testObject(object):

    def __init__(self):
        self.name = "Hi"
        self.number = 34.2
        self.camelCaseAttribute = 'hello'
        self.snake_case_attribute = 'world'

    def getName(self):
        raise Exception('Methods should never be called!')

    def get_name(self):
        raise Exception('Methods should never be called')

def suite():
    suite = unittest.TestSuite()
    suite.addTest(unittest.TestLoader().loadTestsFromTestCase(CompleterTest))
    return suite
