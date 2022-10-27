import sys
import logging
import java
from os import path
from functools import wraps

import pytest

from gda.configuration.properties import LocalProperties
from gda.jython import PyTestRunner
from gdaserver import command_server

from beamline_test.fixtures import DefaultGdaFixtures

logger = logging.getLogger("beamline_test")

def run_tests(testdir='tests', marks=None):
    test_root = path.join(LocalProperties.getConfigDir(), testdir)
    logger.info('Running tests in %s', test_root)
    cmd = ['-v', '-p', 'no:cacheprovider', '--show-capture=no', test_root]
    if marks:
        cmd.append('-m')
        cmd.append(marks)
    logger.info('Running PyTest command: %s', cmd)
    with PyTestRunner(command_server):
        pytest.cmdline.main(cmd, plugins=[JavaExceptionHandling(), DefaultGdaFixtures(), ModeFilter()])

class PyTestFailure(BaseException):
    """Custom exception to wrap any Java exceptions raised in tests"""
    pass

class ModeFilter(object):
    """
    Pytest plugin to allow tests to be skipped based on GDA mode

    If a test relies on beamline hardware, it can be marked @pytest.mark.mode('live')
    so that it is skipped when running in dummy modes.

    If there are multiple modes where a test should be run, they can be specified
    by either passing multiple modes to a single mark, or by adding additional marks.

        @pytest.mark.mode('live', 'dummy')
        def test_in_dummy_and_live():
            pass

        @pytest.mark.mode('live')
        @pytest.mark.mode('dummy')
        def test_in_live_and_dummy():
            pass
    """
    def __init__(self):
        self.mode = LocalProperties.get(LocalProperties.GDA_MODE)
    def pytest_configure(self, config):
        config.addinivalue_line("markers", "mode(<mode>): only run test when in specified mode")
    def pytest_runtest_setup(self, item):
        # Combine all names passed to mode markers
        modes = {mode for mark in item.iter_markers() if mark.name == "mode" for mode in mark.args}
        logger.debug("Mode filter: %s", modes)
        if modes and self.mode not in modes:
            pytest.skip("Not running in mode {}".format(self.mode))

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
