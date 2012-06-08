import os
import sys
import traceback
import unittest
import testhelpers.xmlrunner

def GdaTestRunner(testsuite, desc='', xmlfile=None, report_dir='test-reports/'):
    """Run the given test case or test suite. Uses either XMLTestRunner or TextTestRunner
        testsuite = test suite to run
        desc = description of test suite (used in report)
        xmlfile = name of xml file where results to be written (in junit xml report format)
        report_dir = directory where results to be written (in junit xml report format)
    """

    if xmlfile:
        if not os.path.exists(report_dir):
            os.mkdir(report_dir)
        stream = open(os.path.join(report_dir,xmlfile), "w")
        stream.write('<?xml version="1.0" encoding="utf-8"?>\n')
        testrunner = testhelpers.xmlrunner.XMLTestRunner(stream)
        test_result = testrunner.run(testsuite)
        stream.close()
    else:
        test_result = unittest.TextTestRunner(verbosity=2).run(testsuite)

    # get name of module that called us (trim off unwanted verbage)
    caller_module_name = os.path.join(os.getcwd(), traceback.extract_stack(limit=2) [0] [0]) 
    if '/plugins/' in caller_module_name:
        caller_module_name = caller_module_name.split('/plugins/')[-1]
    elif '/builder/' in caller_module_name:
        caller_module_name = 'builder/' + caller_module_name.split('/builder/')[-1]

    # set return code 0=success, 1 = failure(s), 2=error(s).
    exit_code = (0,1,2,2) [ (len(test_result.failures)>0) + 2*(len(test_result.errors)>0) ]
    exit_status = ('PASSED', 'FAILURES', 'ERRORS') [exit_code]
    # report test results using a distinctive message that can be parsed for. use stderr since that's where unittest reports by default
    sys.stderr.write('[JYTHON_TEST_RESULT:%s] TESTS=%d FAILURES=%d ERRORS=%d %s from file=%s\n' %
                     (exit_status, test_result.testsRun, len(test_result.failures), len(test_result.errors), desc, caller_module_name))
    sys.exit(exit_code)