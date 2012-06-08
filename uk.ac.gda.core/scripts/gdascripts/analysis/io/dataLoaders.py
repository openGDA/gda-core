
from gda.analysis.io import JPEGLoader, TIFFImageLoader, ScanFileHolderException
from uk.ac.diamond.scisoft.analysis.io import PilatusEdfLoader
try:
	from gda.analysis.io import ConvertedTIFFImageLoader 
except ImportError:
	ConvertedTIFFImageLoader = None
from gda.analysis import ScanFileHolder
import java

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
	if ConvertedTIFFImageLoader is not None and iFileLoader is ConvertedTIFFImageLoader:
		# This requires a special call
		sfh.load(iFileLoader(path, 'uint16', 'none'))  # Bodge to work with xray eye
	else:	
		if iFileLoader is TIFFImageLoader:
			# We have a backup loader for TIFF loading
			try:
				sfh.load(iFileLoader(path))
			except ScanFileHolderException, e:
				# Try the converted tiff loader instead
				if ConvertedTIFFImageLoader is not None:
					sfh.load(ConvertedTIFFImageLoader(path, 'uint16', 'none'))
				else:
					raise e
		else:
			# No backup loader required	
			sfh.load(iFileLoader(path))
	return sfh