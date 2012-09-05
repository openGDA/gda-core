from math import log10, floor
from optparse import OptionParser
from sys import exit, stderr
import math
import os
import shutil
import subprocess
import sys
import unittest
#from mklinksFromNXSFile import removeTrailingSlash
def removeTrailingSlash(path):
# remove trailing slash if it exists
	path_out=path
	if path_out[-1:]=="/":
		path_out=path_out[0:-1]
	return path_out


def makeLinks(scanNumber, lastImage, firstImage=2, visit="mt5811-1", year="2012", detector="pco1", outdir=None):
	"""
	Command to make soft links for of projections into current folder
	scanNumber - the scan number e.g. 510
	lastImage   - last image number 
	firstImage - first image number. default(2)
	visit-your visit to I13 default(mt5811-1)
	"""
	print makeLinks.__name__
	if not outdir is None:
		if not os.path.exists(outdir):
			os.makedirs(outdir)
			print "Fn makeLinks is attempting to create dir: %s"%outdir
	for i in range(firstImage, lastImage+1):
		#filename=detector+`scanNumber`+("-%05d.tif"%(i-firstImage))
		filename_src=detector+`scanNumber`+("-%05d.tif"%i)
		filename_dst=detector+`scanNumber`+("-%05d.tif"%(i-firstImage))
		fileToLinkTo="/dls/i13/data/"+`year`+"/"+visit+"/"+`scanNumber`+"/"+detector+"/"+filename_src
		if not os.path.exists(fileToLinkTo):
			raise Exception("File cannot be linked to as it does not exist:"+`fileToLinkTo`)
		if not outdir is None:
			filename_dst=outdir+os.sep+filename_dst
		if os.path.exists(filename_dst):
			msg="Soft link already exists:"+`filename_dst`
			#print msg
			raise Exception("Soft link already exists:"+`filename_dst`)
		cmd="ln -s "+fileToLinkTo+" "+filename_dst
		#print cmd
		subprocess.call(cmd, shell=True)


def makeLinksToOriginalFiles(listOfProjIdx, indir="/dls/i13/data/2012/mt5811-1/564/pco1/", inFilenameFmt="p_%05d.tif", outdir=None, outFilenameFmt="p_%05d.tif"):
	"""
	Command to make soft links for of projections into current folder
	scanNumber - the scan number e.g. 510
	lastImage   - last image number 
	firstImage - first image number. default(2)
	visit-your visit to I13 default(mt5811-1)
	"""
	
	#print "Fn: %s"%makeLinksToOriginalFiles.__name__
	#print listOfProjIdx
	#print indir
	#print inFilenameFmt
	#print outdir
	#print outFilenameFmt
	
	indir_loc=removeTrailingSlash(indir)
	
	if not os.path.isdir(indir_loc):
		raise Exception("Input directory does not exist:"+`indir`)
	
	if not outdir is None:
		if not os.path.exists(outdir):
			print "Fn makeLinksToOriginalFiles is attempting to create dir: %s"%outdir
			os.makedirs(outdir)
	j=0		
	for i in listOfProjIdx:
		#print "projection index: i=%s"%i
		#print "loop index: j=%s"%j
		filename_src=inFilenameFmt%i
		#filename_dst=inFilenameFmt%(i-firstImage)
		filename_dst=outFilenameFmt%j
		fileToLinkTo=indir_loc+os.sep+filename_src
		if not os.path.exists(fileToLinkTo):
			raise Exception("File cannot be linked to as it does not exist:"+`fileToLinkTo`)
		if not outdir is None:
			filename_dst=outdir+os.sep+filename_dst
		if os.path.exists(filename_dst):
			msg="Soft link already exists:"+`filename_dst`
			#print msg
			raise Exception("Soft link already exists:"+`filename_dst`)
		cmd="ln -s "+fileToLinkTo+" "+filename_dst
		#print cmd
		j+=1
		subprocess.call(cmd, shell=True)


def ndigits(N):
#lambda N: ((N==0) and 1) or floor(log10(abs(N)))+1    
	ndig=0    
	if N==0:
		ndig=1
	else:
		ndig=floor(log10(abs(N)))+1
	return ndig

def main(argv):
		desc="""This Python script, called %prog,
creates a set of soft links to a sequence of projection images stored in the tiff format.
"""
		usage="%prog -y inputyear -v inputvisit -s inputscan -f inputfirstidx -l inputlastidx -d detector\n"+\
					" or\n%prog --year inputyear --visit inputvisit --scan inputscan --first inputfirstidx --last inputlastidx --detector detector\n"+\
					"Example usage:\n%prog -y 2012 -v mt5811-1 -s 564 -f 2 -l 1201 -d pco1\n"+\
					"or:\n%prog --year 2012 --visit \"mt5811-1\" --scan 564 --firstidx 2 --lastidx 1201 --detector pco1"
		vers="%prog version 1.0"
	
		parser=OptionParser(usage=usage, description=desc, version=vers, prog=argv[0])
	
		parser.add_option("-o", "--outdir", action="store", type="string", dest="outdir", help="path to folder in which links are to be made. Default is current working folder")
		parser.add_option("-y", "--year", action="store", type="int", dest="year", default=2012, help="The year of your visit at DLS (in the 4-digit format), eg 2012; default value is %default.")
		parser.add_option("-v", "--visit", action="store", type="string", dest="visitID", help="The unique identifier of your visit at DLS, eg mt585811-1.")
		parser.add_option("-s", "--scan", action="store", type="int", dest="scanNumber", help="The integer number identifying your scan, eg 564.")
		parser.add_option("-f", "--firstidx", action="store", type="int", dest="firstProjIdx", help="The zero-based index of the first projection image to be reconstructed, eg 2.")
		parser.add_option("-l", "--lastidx", action="store", type="int", dest="lastProjIdx", help="The zero-based index of the last projection image to be reconstructed, eg 1200.")
		parser.add_option("-d", "--detector", action="store", type="string", dest="detector", help="Name of detector. Used as prefix to image file e.g. pco1.")
		parser.add_option("--verbose", action="store_true", dest="verbose", default=False, help="Verbose - useful for diagnosing the script")
	
		(opts, args)=parser.parse_args(args=argv[1:])
		opts_dict=vars(opts)

#print the input args
		if opts.verbose:
			for key, value in opts_dict.iteritems():
				print key, value    


# Make sure all mandatory variables are initialised
		mandatories=['year', 'visitID', 'scanNumber', 'firstProjIdx', 'lastProjIdx', 'detector']
		for m in mandatories:
#        print opts_dict[m]
			if opts_dict[m] is None:
				raise Exception("Mandatory input value is missing! Use -h for help")

# Validate the year's format
		yearlen=ndigits(opts.year)
#    print yearlen
		if yearlen!=4:
			raise Exception("The input year must have 4 digits, eg 2012! Use -h for help")
		makeLinks(scanNumber=opts.scanNumber, lastImage=opts.lastProjIdx, firstImage=opts.firstProjIdx, visit=opts.visitID, year=opts.year, detector=opts.detector, outdir=opts.outdir)

if __name__=="__main__":
	main(sys.argv)

class Test1(unittest.TestCase):

	def setUp(self):
		self.cwd=os.getcwd()

	def tearDown(self):
		os.chdir(self.cwd)

	def testHelp(self):
		main(["program", "-h"])
		
	def test_noArgs(self):
		try:
			main(["program"])
		except  Exception as ex:
			self.assertEquals('Mandatory input value is missing! Use -h for help', str(ex))
			
		
	def test_y_arg(self):
		try:
			main(["program", "-y", "12"])
		except  Exception as ex:
			self.assertEquals('Mandatory input value is missing! Use -h for help', str(ex))
		
	def test_makeLinksInvalidYearFormat(self):
		try:
			main(["program", "-y", "12", "-v", "mt5811-1", "-s", "564", "-f", "2", "-l", "10", "-d", "pco1"])
		except  Exception as ex:
			self.assertEquals('The input year must have 4 digits, eg 2012! Use -h for help', str(ex))

	def test_makeLinksFileToLinkInexistent(self):
		try:
			main(["program", "-y", "2012", "-v", "mt5811-1", "-s", "564", "-f", "2", "-l", "10", "-d", "pco2"])
		except  Exception as ex:
			self.assertEquals("File cannot be linked to as it does not exist:'/dls/i13/data/2012/mt5811-1/564/pco2/pco2564-00000.tif'", str(ex))

	def test_makeLinksSoftLinkAlreadyExists(self):
		try:
			outputDir="test_makeLinksSoftLinkAlreadyExists_output"
			if os.path.exists(outputDir):
				shutil.rmtree(outputDir)
			os.mkdir(outputDir)
			os.chdir(outputDir)
			f=open("pco1564-00000.tif", "w");
			f.write("Dummy")
			f.flush()
			f.close()			
			main(["program", "-y", "2012", "-v", "mt5811-1", "-s", "564", "-f", "2", "-l", "10", "-d", "pco1"])
		except  Exception as ex:
			self.assertEquals("Soft link already exists:'pco1564-00000.tif'", str(ex))


	def test_makeLinksShort(self):
		outputDir="test_makeLinks_output"
		if os.path.exists(outputDir):
			shutil.rmtree(outputDir)
		os.mkdir(outputDir)
		os.chdir(outputDir)

		main(["program", "-y", "2012", "-v", "mt5811-1", "-s", "564", "-f", "2", "-l", "10", "-d", "pco1"])
		fileNameToCheck="pco1564-00008.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)
		fileNameToCheck="pco1564-00000.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)

	def test_makeLinksShortWithOutDir(self):
		outputDir="test_makeLinksShortWithOutDir"
		if os.path.exists(outputDir):
			shutil.rmtree(outputDir)

		main(["program", "-y", "2012", "-v", "mt5811-1", "-s", "564", "-f", "2", "-l", "10", "-d", "pco1", "-o", outputDir])
		os.chdir(outputDir)
		fileNameToCheck="pco1564-00008.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)
		fileNameToCheck="pco1564-00000.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)

	def test_makeLinksLong(self):
		outputDir="test_makeLinksLong_output"
		if os.path.exists(outputDir):
			shutil.rmtree(outputDir)
		os.mkdir(outputDir)
		os.chdir(outputDir)

		main(["program", "--year", "2012", "--visit", "mt5811-1", "--scan", "564", "--first", "2", "--last", "10", "--detector", "pco1"])
		fileNameToCheck="pco1564-00008.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)
		fileNameToCheck="pco1564-00000.tif"
		if not os.path.exists(fileNameToCheck):
			self.fail("Link not made for file:"+fileNameToCheck)
