import sys
import logging
from os import path

import pytest

from gda.configuration.properties import LocalProperties
from gda.jython import PyTestRunner
from gdaserver import command_server

logger = logging.getLogger("beamline_test")

def run_tests(testdir='tests'):
    test_root = path.join(LocalProperties.getConfigDir(), testdir)
    logger.info('Running tests in %s', test_root)
    with PyTestRunner(command_server):
        pytest.cmdline.main(['-v', '-p', 'no:cacheprovider', test_root])
