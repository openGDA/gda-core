import os

THISDIR = os.path.dirname(__file__)

TESTFILE = os.path.join(THISDIR, 'blurred_gauss_with_spots.tif') # made in gimp
IPP_XRAY_EYE_FILE = os.path.join(THISDIR, 'image_pro_plus_tif_from_xray_eye.TIF') # actual data
FOCUSED_BEAM = os.path.join(THISDIR, 'one_micron_focused_beam_from_xrayeye_via_ipp.TIF')