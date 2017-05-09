""" Module providing command line auto-complete functionality

This class is to provide a object which can return information to allow command line auto-complete to work.

It was written to resolve GDA-6130 where the PyDev approach resulted in 'get' methods being called.

@author James Mudd
@author Peter Holloway
"""

import __builtin__
import keyword
import re

from ch.qos.logback.classic import Level
from org.slf4j import LoggerFactory
from java.lang import Object
from org.python.core import PyJavaPackage

logger = LoggerFactory.getLogger(__name__ + '.py')

# Type constants used to pick the icon to display for the option
# The usage is chosen to match PyDev see jyimportsTipper.py
TYPE_IMPORT = 0  # Blue Dot
TYPE_CLASS = 1  # Yellow Diamond
TYPE_FUNCTION = 2  # Blue Triangle
TYPE_ATTR = 3  # Green Circle
TYPE_BUILTIN = 4  # Gray Circle
TYPE_PARAM = 5  # No icon, This doesn't have support in the Java side

CAMEL_REG = re.compile('([A-Z][a-z]*|_[a-z]*)')
# String contains either upper case or underscore - should use camel_match
AUTO_REG = re.compile('.*([A-Z]|_)')


def camel_snake_match(partial):
    """Match words based on camelCase or snake_case

    gP -> getPosition
    r_n -> reset_namespace
    Words with mixed uppercase letters and underscores will only match if all are present
    eg
    gA_m will match getAwkward_mixedCase
    but neither of gAC or g_m will
    """
    keys = CAMEL_REG.findall(partial)
    p = CAMEL_REG.search(partial)
    sub_text = partial if not p else partial[:p.start()]
    if not sub_text and keys:
        # make sure that the match starts with the start of the partial text
        sub_text = keys.pop(0)
    match_regex = '^' + sub_text + '[a-z]*' + '[a-z_]*'.join(keys)
    logger.debug('Matching against regex: {}', match_regex)
    return re.compile(match_regex).match

def basic_match(partial):
    """Match words only if they match exactly (ignoring case)"""
    lower = partial.lower()
    def match(completion):
        logger.debug('Matching if starts with "{}"', lower)
        return completion.lower().startswith(lower)
    return match

def fuzzy_match(partial):
    """Match on any part of the word (case insensitive)"""
    lower = partial.lower()
    def match(completion):
        return lower in completion.lower()
    return match

def auto_match(partial):
    if AUTO_REG.match(partial):
        return camel_snake_match(partial)
    else:
        return basic_match(partial)

# functions that take a partial command and return a function that will match completion options against it.
matchers = {'basic': basic_match,
            'camel': camel_snake_match,
            'fuzzy': fuzzy_match,
            'auto': auto_match}

class Completer(object):

    def __init__(self, globals_, matching='auto'):
        self.globals = globals_

        if matching not in matchers:
            logger.warn('Matching style "{}" not available', matching)
            self.match_maker = matchers['basic']
        else:
            self.match_maker = matchers[matching]

        logger.debug('Using {} completion', self.match_maker)

        # Build and cache the keywords results
        logger.debug('Caching keywords for completion')
        self.keywords = [(x, '', '', TYPE_BUILTIN) for x in keyword.kwlist]  # Use the param icon for keywords
        logger.debug('Key words: {}', str(self.keywords))

        # Build and cache the builtins
        logger.debug('Caching built-ins for completion')
        self.builtins = []
        for x in dir(__builtin__):
            try:
                obj = eval(x, self.globals) # Get the object to find out what it is
            except SyntaxError:
                # Calling eval on x resulted in a SyntaxError therefore it 'must' be a keyword
                # This is almost a special case for 'print' which was changed in 2.6
                # https://docs.python.org/2/library/functions.html#print
                continue # So don't do anything with it
            if isinstance(obj, type):  # If its a type its a class
                self.builtins.append((x, '', '', TYPE_CLASS))
            elif callable(obj):  # If its callable its a method
                self.builtins.append((x, '', '', TYPE_FUNCTION))
            else:
                self.builtins.append((x, '', '', TYPE_ATTR))
        logger.debug('Built-ins: {}', str(self.builtins))

        logger.info("Completer created, command completion should be available")

        # Suppress the debug output by default. Do this at the end of the initialisation so its possible to
        # see the debug output for the keywords and builtin initialisation.
        logger.setLevel(Level.INFO)


    def complete(self, command):
        """
        This method is called to provide command completion on the currently entered command string.

        It should return an list of tuples of 4 elements. Where the elements correspond to:
        1. name
        2. doc string
        3. arguments
        4 type (Where it is a integer as a string)
        e.g. ("complete", "This method is ...", "command", TYPE_FUNCTION)

        The order of the returned list is not important it is sorted by the Java code
        """
        logger.debug('Command to complete: {}', command)

        # If the command includes '.' need to split on it and parse it
        if '.' in command:
            logger.debug('Splitting command')
            # Split on '.'
            parts = command.split('.')

            # Check if the first part is in the globals or builtins, if not then no options are available
            if parts[0] not in (self.globals.keys() + dir(__builtin__)):
                logger.debug('{} is not in globals or builtins, no completion available', parts[0])
                return []

            logger.debug('Walking through command to resolve object to dir()')
            # The first object is available so get it and inspect it
            obj = self.globals.get(parts[0])

            # Check if we are looking at a Java object
            if isinstance(obj, Object):  # It's a Java object
                logger.debug('{} is a Java object', parts[0])
                sub_command = parts[0]

                # Move through the command getting the object after each '.', if it won't result in a method call.
                # This uses the difference between hasattr(motor, 'position') and hasattr(motor.__class__, 'position')
                # The motor instance does have a position because Jython has 'wrapped' the getter, whereas the class
                # doesn't. This is the dangerous case because completing on that would require calling the getter.
                # Start after the initial object which we already have from the globals stop one before the last '.' as
                # thats the object we are interested in.
                for part in parts[1:-1]:
                    # Check it's safe to get the next object
                    if hasattr(obj.__class__, part):
                        obj = getattr(obj, part)  # replace obj with the next object in the command
                        sub_command += '.' + part
                        logger.debug('{} Is next object', sub_command)
                    else:
                        logger.debug('Cannot complete further, would require a method call')
                        return []

                # Get the options for the final object
                options = dir(obj)
                logger.debug('Completions options: {}', str(options))

                match = self.match_maker(parts[-1])

                # Now we have the object figure out the types being careful not to call methods
                results = []
                for opt in options:
                    if not match(opt):
                        continue
                    # Check if the class has the attribute in the same approach as above
                    if hasattr(obj.__class__, opt):
                        if callable(getattr(obj, opt)):  # If its callable its a function
                            results.append((opt, '', '', TYPE_FUNCTION))
                        else:  # Not callable so its a attribute
                            results.append((opt, '', '', TYPE_ATTR))
                    else:  # The attribute is not on the class so it must be a jython wrapped attribute
                        results.append((opt, '', '', TYPE_ATTR))

                return results
            elif isinstance(obj, PyJavaPackage):
                logger.debug('package {}', obj)
                results = []
                for i in parts[1:-1]:
                    a = getattr(obj, i, None)
                    logger.debug('next part: {}, {}', i, a)
                    if a is None:
                        logger.debug('no completions')
                        return []
                    else:
                        obj = a
                for i in dir(obj):
                    if i.startswith(parts[-1]):
                        # needs None as fallback for case where Class appears in dir
                        # but getattr throws exception (eg classes with missing native
                        # dependencies)
                        t = TYPE_CLASS
                        try:
                            if isinstance(getattr(obj, i, None), PyJavaPackage):
                                t = TYPE_IMPORT
                        except:
                            pass
                        results.append((i, '', '', t))
                return results
            else:  # It's a Python object
                logger.debug('{} is a Python object', parts[0])

                sub_command = '.'.join(parts[0:-1])
                logger.debug('Sub command: {}', sub_command)

                # Get the Python object from the global namespace
                # e.g. eval("package.module.object.attr") => <instanceof Attr>
                try:
                    obj = eval(sub_command, self.globals)
                except AttributeError:
                    logger.debug('Could not get {} from globals. No completion available', sub_command)
                    return []

                # Get the options for the final object
                options = dir(obj)
                logger.debug('Completions options: {}', str(options))

                match = self.match_maker(parts[-1])

                # Build the list ignoring doc string and argument lists
                results = []
                for option in options:
                    if not match(option):
                        continue
                    if callable(getattr(obj, option)): # If it's callable it's a function
                        results.append((option, '', '', TYPE_FUNCTION))
                    else: # Else it's a attribute
                        results.append((option, '', '', TYPE_ATTR))
                return results
        else:
            logger.debug('No need to split command, returning globals, keywords and builtins')

            match = self.match_maker(command)

            # Build the lists ignoring doc string and argument lists
            # We might want to cache something here looping over all globals every time is a bit wasteful? It also "feels" slow
            globals_list = []
            for x in self.globals.keys():
                # It prevents looking at objects which don't match the current string up to now.
                if not match(x):
                    continue
                # Get the object
                obj = eval(x, self.globals)
                if isinstance(obj, type):  # If it's a type its a class
                    globals_list.append((x, '', '', TYPE_CLASS))
                elif callable(obj):  # If it's callable its a method
                    globals_list.append((x, '', '', TYPE_FUNCTION))
                # __builtin__.__class__ = module http://stackoverflow.com/questions/865503/how-to-isinstancex-module
                elif isinstance(obj, __builtin__.__class__): # It's a python module
                    globals_list.append((x, '', '', TYPE_IMPORT))
                else: # It's an attribute
                    globals_list.append((x, '', '', TYPE_ATTR))
            logger.debug('Matching globals: {}', str(globals_list))

            # Return the globals, keywords and builtins
            return globals_list + [opt for opt in self.keywords + self.builtins if match(opt[0])]

    def enable_debug(self, enable=True):
        if enable:
            logger.setLevel(Level.DEBUG)
        else:
            logger.setLevel(Level.INFO)
