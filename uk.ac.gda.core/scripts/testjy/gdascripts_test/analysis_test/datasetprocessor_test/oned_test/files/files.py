import os

THISDIR = os.path.dirname(__file__)

WIRESCANFILE  = os.path.join(THISDIR, "wire_scan_to_determine_focus_10040.dat") #tbdiagZcoarse	tbdiagY	rc	pips2
# where tbdiagZcoarse is outer loop, tbdiagY is inner loop and pips2 is data

WIRESCANFILE2 = os.path.join(THISDIR, "dls_b16_data_2009_nt498-1_10998.dat")	#'tbdiagY'/'ch16'
WIRESCANFILE_FAILING_NEGATIVE_STEP = os.path.join(THISDIR, "dls_b16_data_2014_cm4969-1_78698.dat")	# kbwireY / ai2

DAT_31473 = os.path.join(THISDIR, "31473.dat")
DAT_31474 = os.path.join(THISDIR, "31474.dat")
DAT_31484 = os.path.join(THISDIR, "31484.dat")
SILICON_DIFFRACTION=os.path.join(THISDIR, "silicon_diffraction.dat")