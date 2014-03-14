from testjy.test_util.pathresolver import replacePathSeparater
THISDIR = __file__.split('/files/')[0] + "/files/"

WIRESCANFILE  = replacePathSeparater(THISDIR + "wire_scan_to_determine_focus_10040.dat") #tbdiagZcoarse	tbdiagY	rc	pips2
# where tbdiagZcoarse is outer loop, tbdiagY is inner loop and pips2 is data

WIRESCANFILE2 = replacePathSeparater(THISDIR + "dls_b16_data_2009_nt498-1_10998.dat")	#'tbdiagY'/'ch16'
WIRESCANFILE_FAILING_NEGATIVE_STEP = replacePathSeparater(THISDIR + "dls_b16_data_2014_cm4969-1_78698.dat")	# kbwireY / ai2

DAT_31473 = replacePathSeparater(THISDIR + "31473.dat")
DAT_31474 = replacePathSeparater(THISDIR + "31474.dat")
DAT_31484 = replacePathSeparater(THISDIR + "31484.dat")
SILICON_DIFFRACTION=replacePathSeparater(THISDIR+"silicon_diffraction.dat")