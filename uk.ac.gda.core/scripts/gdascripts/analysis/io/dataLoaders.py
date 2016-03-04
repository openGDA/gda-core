
from gda.analysis.io import JPEGLoader, TIFFImageLoader
from uk.ac.diamond.scisoft.analysis.io import PilatusEdfLoader

from gda.analysis import ScanFileHolder

FILELOADERS={
			'TIF':TIFFImageLoader,
			'TIFF':TIFFImageLoader,
			'JPG':JPEGLoader,
			'JPEG':JPEGLoader,
			"EDF":PilatusEdfLoader
			}

def loadImageIntoSFH(path, iFileLoader=None):
	if iFileLoader is None:
		iFileLoader = FILELOADERS[path.split('.')[-1].upper()]
#	print "loadIntoSfh loading: %s using %s" % (path, str(iFileLoader))
	sfh = ScanFileHolder()
	sfh.load(iFileLoader(path))
	return sfh
