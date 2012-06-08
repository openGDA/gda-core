from testjy.test_util.pathresolver import replacePathSeparater

import __init__
import sys

THISDIR = __init__.__file__
if int(sys.version_info[1]) == 2:
    THISDIR = THISDIR.split('__init__.py')[0] # Jython 2.2
elif int(sys.version_info[1]) == 5:
    THISDIR = THISDIR.split('__init__$py.class')[0] # Jython 2.5

TESTFILE = replacePathSeparater(THISDIR + 'blurred_gauss_with_spots.tif') # made in gimp
IPP_XRAY_EYE_FILE = replacePathSeparater(THISDIR + 'image_pro_plus_tif_from_xray_eye.TIF') # actual data
FOCUSED_BEAM = replacePathSeparater(THISDIR + 'one_micron_focused_beam_from_xrayeye_via_ipp.TIF')