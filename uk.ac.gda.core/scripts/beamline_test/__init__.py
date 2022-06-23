import sys
import logging
import java
from os import path
from functools import wraps

import pytest

from gda.configuration.properties import LocalProperties
from gda.jython import PyTestRunner
from gdaserver import command_server

logger = logging.getLogger("beamline_test")

def run_tests(testdir='tests'):
    test_root = path.join(LocalProperties.getConfigDir(), testdir)
    logger.info('Running tests in %s', test_root)
    with PyTestRunner(command_server):
        pytest.cmdline.main(['-v', '-p', 'no:cacheprovider', test_root], plugins=[JavaExceptionHandling()])

class PyTestFailure(BaseException):
    """Custom exception to wrap any Java exceptions raised in tests"""
    pass

class JavaExceptionHandling(object):
    """
    PyTest plugin to handle Java Exceptions raised by tests

    By default, PyTest ignores any Java exceptions raised in a test and marks
    that test as passing.

    This wraps each test function so that any Java exceptions are raised as
    Python exceptions. The message and the traceback are maintained but further
    information is not kept. eg, if an exception has additional fields, these
    are lost. As exceptions are only used to determine if a test has failed,
    this should not be an issue. If individual tests want to handle exceptions
    they can still use the normal try/catch mechanisms.
    """

    def pytest_collection_modifyitems(self, items):
        """PyTest hook to manipulate the test functions before they are run"""
        for it in items:
            it.obj = self.wrap(it.obj)

    def wrap(self, fn):
        @wraps(fn)
        def inner(*a, **kw):
            try:
                fn(*a, **kw)
            except java.lang.Throwable, e:
                tb = sys.exc_info()[2]
                # drop the first frame of the traceback so this wrapper function isn't
                # listed as the source of the exception
                tb = tb.tb_next
                # Raise new exception but use the traceback of the Java exception
                raise PyTestFailure, "{}: {}".format(e.class.simpleName, e.message), tb
        return inner
