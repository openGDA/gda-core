from __future__ import with_statement
from optparse import OptionParser
from sys import exit, stderr
import math
import os
import shutil
import subprocess
import sys
import unittest
import numpy as np
import re

import h5py
import string
from mklinks import makeLinks, makeLinksToOriginalFiles


from sino_listener import *
import sino_listener

from contextlib import contextmanager

import Image

def getFilenameZidxOffset(inDir, inFilenameFmt="p_%05d.tif"):
	print getFilenameZidxOffset.__name__
	print "inDir =", inDir
	print "inFilenameFmt =", inFilenameFmt
	# need to find the smallest index which is used to label files in input dir with names of input format
	# (output None, or -1, if no matches found)  
	# early out by looking for p_00000.tif and p_00001.tif? 
	# use regex groups?
	
	# early out(s)
	offset = -1
	for idx in [0, 1]:
		pattern_test = inFilenameFmt %idx
		files_test = [f for f in os.listdir(inDir) if re.match(pattern_test,f)]
		if len(files_test)>0:
			offset = idx
			break
	
	if offset>=0:
		msg = "Filename offset from index 0 was automatically detected to be: %i" %offset
		print msg
	else:
		pass
		#pattern_dct = {}
		#pattern_dct['p_%05d.tif'] = r'^p_(\d{5})\.tif$'
		#pattern_dct['%05d.tif'] = r'^(\d{5})\.tif$'
		#pattern = pattern_dct[inFilenameFmt]
		#files = [f for f in os.listdir(inDir) if re.match(pattern,f)]
	offset = 0
	return offset
	
def copySingleFile(src, dst):
	success = False
	if not os.path.exists(src):
		msg = "INFO: File cannot be copied because it does not exist: "+`src`
		raise Exception(msg)
	if os.path.exists(dst):
		msg = "INFO: File already exist (you may wish to delete the existing file and then re-run the command): "+`dst`
		print msg
	else:
		try:
			success = True
			shutil.copy(src, dst)
			msg = "INFO: Copied "+`src`+" to " + `dst`
			print msg
		except:
			msg = "INFO: Failed to copy "+`src`+" to " + `dst`
			print msg
			success = False
	return success

def launchImageAveragingProcess(inDir, inFilenameFmt, outDir, outFilename="flat-avg.tif", imgWidth=4008, imLength=2672, outlierCheckParam=0.01):
	print launchImageAveragingProcess.__name__
	print "inDir =", inDir
	print "inFilenameFmt =", inFilenameFmt
	print "outDir =", outDir 
	print "outFilename =", outFilename
	print "imgWith =", str(imgWidth)
	print "imgLength =", str(imLength)
	print "outlierCheckParam =", str(outlierCheckParam)
	
	#launchX("/dls_sw/i12/software/tomography_scripts/flat_capav")
	#success = launchX
	exePath = "/dls_sw/i12/software/tomography_scripts/flat_capav"
	
	sanity_count = inFilenameFmt.count("%")
	if sanity_count != 1:
		msg = "Unsupported filename format: " + inFilenameFmt + " (only a single template parameter (%) is currently allowed)"
		raise Exception(msg)
	
	args = [exePath]
	args += ["-i", str(inDir)]
	args += ["-f", str(outFilename)]
	args += ["-o", str(outDir)]
	args += ["-I", str(inFilenameFmt)]
	args += ["-w", str(imgWidth)]
	args += ["-l", str(imLength)]
	args += ["-u", str(outlierCheckParam)]
	
	pattern_dct = {}
	pattern_dct['f_000_%05d.tif'] = r'^f_000_(\d{5})\.tif$'
	pattern_dct['d_000_%05d.tif'] = r'^d_000_(\d{5})\.tif$'
	
	#pattern = '^f_000_(\d{5})\.tif$'
	pattern = pattern_dct[inFilenameFmt]
	#print "Filename pattern used in regex =", pattern
	
	#files = [f for f in os.listdir(inDir) if re.match(r'^f_000_(\d{5})\.tif$',f)]
	files = [f for f in os.listdir(inDir) if re.match(pattern,f)]
	
	msg = "Files for averaging (found "+`len(files)`+" matches):"
	print msg
	for f in files:
		print f
	
	len_files = len(files)
	if len_files < 1:
		msg = "No files matching template " + inFilenameFmt + " were found in " + inDir
		raise Exception(msg)
	
	args += ["-a", str(len_files)]
	
	for a in args:
		print a
	
	pid = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	pid.wait()
	(out, err) = pid.communicate()
	print out
	print err
	print "Return value was %s\n" % pid.returncode
	return True if (pid.returncode==0) else False
	

def preModifyAbsolutePaths(inList, prePath):
	inList_len = len(inList)
	outList=np.empty((inList_len,1),dtype='object')
	for i in range(0,inList_len):
		inPath = inList[i][0]
		preModifyAbsolutePath(inPath, prePath)
	return outList

def preModifyAbsolutePathsss(inList, prePath, outList):
	inList_len = len(inList)
	for i in range(0,inList_len):
		inPath = inList[i][0]
		outList[i][0] = preModifyAbsolutePath(inPath, prePath)

def preModifyAbsolutePath(inPath, prePath):		
	if not os.path.isabs(inPath):
		raise Exception("Input inPath is not absolute:"+`inPath`)
	if not os.path.isabs(prePath):
		raise Exception("Input prePath is not absolute:"+`prePath`)
	
	inParts = inPath.split(os.path.sep)
	preParts = prePath.split(os.path.sep)
	inParts_len = len(inParts)
	preParts_len = len(preParts)
	
	if preParts_len <= inParts_len and preParts_len > 0:
		for i in range(1, preParts_len):
			inParts[i] = preParts[i]
	else:
		raise Exception("The input inPath is shorter than the input prePath: " + `inPath` + "vs." +`prePath`)
	return os.path.sep.join(inParts)

def convertFromRelativeToAbsolutePaths(inList, prePath):
	outList=np.empty((len(inList),1),dtype='object')
	for i in range(0,len(inList)):
		s = inList[i][0]
		if s[:1]==".":
			s1 = str(s[1:])
			outList[i][0]=prePath + s1
		else:
			outList[i][0]=s
	return outList

@contextmanager
def opened_w_error(filename, mode="r"):
	try:
		f = open(filename, mode)
	except IOError, err:
		yield None, err
	else:
		try:
			yield f, None
		finally:
			f.close()

@contextmanager
def cd(path):
	saved_dir=os.getcwd()
	try:
		os.chdir(path)
		yield path
	except Exception, ex:
		raise Exception ("ERROR changing directory: "+str(ex))
	finally:
		os.chdir(saved_dir)


def genAncestorPath(path, ngenerationDown):
	outpath=path
	gen_loc=max(ngenerationDown, 0)
	while gen_loc:
		outpath=os.path.dirname(outpath)
		gen_loc-=1
		#print gen_loc
	return outpath


def splitPathIntoDirs(inPath, inDir=['dls', 'beamlineID', 'data', 'yearID', 'visitID', 'scanID', 'detectorID', 'filename'], fallback='unknown_'):
	len_inDir=len(inDir)
	#e.g. /dls/i13/data/2012/mt5811-1/564/pco1/pco1564-00002.tif
	inPath_split=string.split(inPath, "/")
	#Note that src_proj_split contains some empty strings, i.e.''
	outDct={}
	nmismatches=0
	inDir_idx=0
	for i in len(inPath_split):
		if inPath_split[i]!='':
			if inDir_idx<len_inDir:
				outDct[ inDir[inDir_idx] ]=inPath_split[i]
			else:
				outDct[ fallback+`i` ]=inPath_split[i]
				nmismatches+=1
			inDir_idx+=1
	return outDct, nmismatches

def removeTrailingSlash(path):
# remove trailing slash if it exists
	path_out=path
	if path_out[-1:]=="/":
		path_out=path_out[0:-1]
	return path_out


def mapPhysValTo01(inVal, map01Def, pct=0.1):
	at0=map01Def[0]
	at1=map01Def[1]
	eps=abs(at0-at1)*pct
	outVal=None
	if abs(inVal-at0)<=eps:
		outVal=0
	elif abs(inVal-at1)<=eps:
		outVal=1
	return outVal


def genImageKey(inShutterPos, map01Def_shutter, inStagePos, map01Def_stage, dfpDef):
	shutter_key=mapPhysValTo01(inShutterPos, map01Def_shutter)
	stage_key=mapPhysValTo01(inStagePos, map01Def_stage)
		
	dark_key=None
	flat_key=None
	proj_key=None
		
	#print dfpDef
	img_key={}
	if shutter_key==dfpDef['dark']['shutter'] and stage_key==dfpDef['dark']['stage']:
		dark_key=1
	else: 
		dark_key=0
			
	if shutter_key==dfpDef['flat']['shutter'] and stage_key==dfpDef['flat']['stage']:
		flat_key=1
	else:
		flat_key=0
			
	if shutter_key==dfpDef['proj']['shutter'] and stage_key==dfpDef['proj']['stage']:
		proj_key=1
	else:
		proj_key=0
		
	img_key['dark']=dark_key
	img_key['flat']=flat_key
	img_key['proj']=proj_key
		
	return img_key


def validateFDP(image_key):
	# must be one of the following (1,0,0), (0,1,0), (0,0,1), so the sum of all 3 entries must be 1
	sum=image_key['dark']
	sum+=image_key['flat']
	sum+=image_key['proj']
	return (sum==1)


def createDirs(refFilename, outdir, mandatorydir="processing", verbose=False):

	"""
	In the processing folder create:
		the rawdata folder 
		the sino folder
		the reconstruction folder 
	
	in the rawdata folder create:
		the [scanNumber] folder
		the projections folder
	
	 in the sino folder create:
		the [scanNumber]  folder
		the [scanNumber]/dark folder
		the [scanNumber]/flat folder
	
	in the reconstruction folder create:
		the [scanNumber]  folder
	"""
	
	
	if outdir is None:
		processing_dir=os.path.join(genAncestorPath(refFilename, 3), "processing")
	else:
		outdir_loc=outdir
		if not outdir is None:
			outdir_head, outdir_tail=os.path.split(outdir)
			if outdir_tail!=mandatorydir:
				outdir_loc=outdir_loc+os.sep+mandatorydir
				print "\nINFO: Mandatory dirname appended to outdir_loc=%s"%outdir_loc


		processing_dir=outdir_loc

		#if the required folder doesn't exist, then create it together with any intermediate ones
		if not os.path.exists(processing_dir):
			try:
				os.makedirs(processing_dir)
				#print "Fn createDirs is attempting to create dir:%s"%processing_dir
			except OSError, ex:
				#Raises an error exception if the LEAF directory already exists or cannot be created.
				raise Exception ("ERROR creating outdir "+str(ex))
		elif verbose:
			print "The required directory already exists: %s"%processing_dir


	head=processing_dir
	#from this stage the processing folder is guaranteed to exist (because it was created above or exists by DLS default)
	if not os.path.exists(head):
		msg="The processing dir does NOT exist in :"+`head`
		#print msg
		raise Exception("The processing dir does NOT exist in :"+`head`)
	
	scanNumber_str=os.path.basename(genAncestorPath(refFilename, 2))
	#scanNumber_int=int(scanNumber_str)


	#the paths below are relative to the processing directory
	proj_dir="rawdata"+os.sep+scanNumber_str+os.sep+"projections"
	sino_dir="sino"+os.sep+scanNumber_str
	dark_dir="sino"+os.sep+scanNumber_str+os.sep+"dark"
	flat_dir="sino"+os.sep+scanNumber_str+os.sep+"flat"
	
	darks_dir="rawdata"+os.sep+scanNumber_str+os.sep+"darks"
	flats_dir="rawdata"+os.sep+scanNumber_str+os.sep+"flats"

	dirs=[proj_dir, dark_dir, flat_dir, ("reconstruction"+os.sep+scanNumber_str), darks_dir, flats_dir]

	for dirname in dirs:
		dirname=head+os.sep+dirname
		if not os.path.exists(dirname):
			try:
				os.makedirs(dirname)
				#print "Fn createDirs is attempting to create dir:%s"%dirname
			except OSError, ex:
				#Raises an error exception if the LEAF directory already exists or cannot be created.
				raise Exception ("ERROR creating directory "+str(ex))
		elif verbose:
			print "The required directory already exists: %s"%dirname


	print "\nINFO: Finished creating directories." 
	return scanNumber_str, head, sino_dir, dark_dir, flat_dir, proj_dir, darks_dir, flats_dir


def decimate(inList, decimationRate=1):
	"""
	Selects every N-th element from input list, eg
	
	decimate([12,13,14,15,16,17,18,19,20], 3)= [14,17,20]
	decimate([12,13,14,15,16,17,18,19,20], 9)= [20]
	decimate([12,13,14,15,16,17,18,19,20], 10)= []
	decimate([12,13,14,15,16,17,18,19,20], 11)= []
	decimate([12,13,14,15,16,17,18,19,20], 1)= [12,13,14,15,16,17,18,19,20]
	"""
	decimationRate_loc=int(decimationRate)
	if decimationRate_loc<1:
		decimationRate_loc=1
	
	inLen=len(inList)
	outLen=int(inLen/decimationRate_loc)
	outList=[]
	for i in range((decimationRate_loc-1), (decimationRate_loc*outLen), decimationRate_loc):
		outList.append(inList[i])
	return outList


def populateDirs(scanNumber_str, head, dark_dir, flat_dir, proj_dir, darks_dir, flats_dir, tif_lst, dark_idx, flat_idx, proj_idx, unpicked_idx_dct, filenameOffset=0, decimationRate=1, verbose=False):

	"""
	Create:
	soft links to projection images in the projections folder
	dark.tif in the sino/[scanNumber]/dark folder
	flat.tif in the sino/[scanNumber]/flat folder
	"""
	if len(dark_idx)>0:
		#src_dark=tif_lst[dark_idx[0]][0]
		
		src_dark_list_0 = unpicked_idx_dct['dark'][0]
		#print 'src_dark_list_0=', src_dark_list_0
		len_src_dark_list_0 = len(src_dark_list_0)
		zidx_src_dark = int(len_src_dark_list_0/2)
		src_dark = tif_lst[src_dark_list_0[zidx_src_dark]][0]
		#print "len_src_dark_list_0 = %s" %len_src_dark_list_0
		#print "zidx_src_dark = %s" %zidx_src_dark
		#print "src_dark_x = %s" %src_dark_x
		#print "src_dark=%s"%src_dark

		dst_dark="dark-sgl.tif"
		dst_dark=head+os.sep+dark_dir+os.sep+dst_dark
		createSoftLink(src_dark, dst_dark)
	else:
		msg = "WARNING: As reported earlier, no dark-field images are available to populate the dark sub-dir with the required dark.tif."
		print msg

	if len(flat_idx)>0:
		#src_flat=tif_lst[flat_idx[0]][0]
		
		src_flat_list_0 = unpicked_idx_dct['flat'][0]
		#print 'src_flat_list_0=', src_flat_list_0
		len_src_flat_list_0 = len(src_flat_list_0)
		zidx_src_flat = int(len_src_flat_list_0/2)
		src_flat = tif_lst[src_flat_list_0[zidx_src_flat]][0]
		#print "len_src_flat_list_0 = %s" %len_src_flat_list_0
		#print "zidx_src_flat = %s" %zidx_src_flat
		#print "src_flat_x = %s" %src_flat_x
		#print "src_flat=%s"%src_flat
		
		dst_flat="flat-sgl.tif"
		dst_flat=head+os.sep+flat_dir+os.sep+dst_flat
		createSoftLink(src_flat, dst_flat)
	else:
		msg = "WARNING: As reported earlier, no flat-field images are available to populate the flat sub-dir with the required flat.tif."
		print msg

	#identify arguments to be used when calling makeLinks using the path of the first projection file
	src_proj=tif_lst[proj_idx[0]][0]
	refFilename=src_proj


	src_proj_split=string.split(src_proj, "/")

	#e.g. /dls/i13/data/2012/mt5811-1/564/pco1/pco1564-00002.tif
	#Note that src_proj_split contains some empty strings, i.e.''
	makeLinks_arg={}
	makeLinks_arg['beamlineID']=src_proj_split[2]
	makeLinks_arg['year']=int(src_proj_split[4])
	makeLinks_arg['visit']=src_proj_split[5]
	#makeLinks_arg['scanNumber']=int(src_proj_split[6])
	makeLinks_arg['scanNumber']=src_proj_split[6]
	makeLinks_arg['detector']=src_proj_split[7]
	
	makeLinks_arg['firstImage']=proj_idx[0]
	makeLinks_arg['lastImage']=proj_idx[len(proj_idx)-1]

	detectorName=src_proj_split[7]
	makeLinks_outdir=head+os.sep+proj_dir
	print "makeLinks_outdir=%s"%makeLinks_outdir


	proj_idx_decimated=decimate(proj_idx, decimationRate)
	
	#many=5
	#for i in range(0, len(proj_idx_decimated)):
	#	if i<many or i>len(proj_idx_decimated)-1-many:
	#		print proj_idx_decimated[i]


	#makeLinks(scanNumber, lastImage, firstImage=2, visit="mt5811-1", year="2012", detector="pco1", outdir=None)
#	makeLinks(year=makeLinks_arg['year']\
#						, visit=makeLinks_arg['visit']\
#						, scanNumber=makeLinks_arg['scanNumber']\
#						, detector=makeLinks_arg['detector']\
#						, firstImage=makeLinks_arg['firstImage']\
#						, lastImage=makeLinks_arg['lastImage']\
#						, outdir=(head+os.sep+proj_dir))

	#filenameFmt=detectorName+scanNumber_str+"-"+"%05d.tif"
	filenameFmt="p_%05d.tif"
	makeLinksToOriginalFiles(\
							listOfProjIdx=proj_idx_decimated\
							, indir=genAncestorPath(refFilename, 1)\
							, inFilenameFmt=filenameFmt\
							, outdir=(head+os.sep+proj_dir)\
							, outFilenameFmt=filenameFmt\
							, inFilenameOffset=filenameOffset)
	
	# create links to all flats
	filenameFmt="p_%05d.tif"
	#outFnameFmt="f_%03d_%05d.tif"
	outPrefixFnameFmt="f_%03d_"
	outPostixFnameFmt="%05d.tif"
	outFnameFmt=""
	subseq=0
	for listOfImgIdx in unpicked_idx_dct['flat']:
		outFnameFmt=outPrefixFnameFmt %(subseq)
		outFnameFmt += outPostixFnameFmt
		makeLinksToOriginalFiles(\
								listOfProjIdx=listOfImgIdx\
								, indir=genAncestorPath(refFilename, 1)\
								, inFilenameFmt=filenameFmt\
								, outdir=(head+os.sep+flats_dir)\
								, outFilenameFmt=outFnameFmt\
								, inFilenameOffset=filenameOffset)
		subseq += 1
	
	# create links to all darks
	filenameFmt="p_%05d.tif"
	#outFnameFmt="d_%03d_%05d.tif"
	outPrefixFnameFmt="d_%03d_"
	outPostixFnameFmt="%05d.tif"
	outFnameFmt=""
	subseq=0
	for listOfImgIdx in unpicked_idx_dct['dark']:
		outFnameFmt=outPrefixFnameFmt %(subseq)
		outFnameFmt += outPostixFnameFmt
		makeLinksToOriginalFiles(\
								listOfProjIdx=listOfImgIdx\
								, indir=genAncestorPath(refFilename, 1)\
								, inFilenameFmt=filenameFmt\
								, outdir=(head+os.sep+darks_dir)\
								, outFilenameFmt=outFnameFmt\
								, inFilenameOffset=filenameOffset)
		subseq += 1
	
	print "INFO: Finished populating directories." 
	return len(proj_idx_decimated), detectorName


def createSoftLink(src, dst, throwExceptionIfSourceDoesNotExist=True):
	if os.path.exists(src):
		if os.path.exists(dst):
			src_old = os.path.realpath(dst)
			src_new = os.path.realpath(src)
			#print "src_old = " + src_old
			#print "src_new = " + src_new
			if os.path.realpath(src)!=os.path.realpath(dst):
				msg="Soft link named "+`dst`+" already exists but currently points to "+`src_old`
				msg+="\n which is not the required source: "+`src`+" (you may wish to delete the existing link and then re-run the command)."
				raise Exception(msg)		
		else:
			cmd="ln -s "+src+" "+dst
			subprocess.call(cmd, shell=True)
	else:
		msg = "File cannot be linked to as it does not exist:"+`src`
		if throwExceptionIfSourceDoesNotExist:
			raise Exception(msg)
		else:
			print msg


def launchSinoListener(inDir, inFilenameFmt, nProjs, outDir, inImgWidth=4008, inImgHeight=2672, qsub_proj='i13', verbose=False, testing=True):
	print launchSinoListener.__name__
	args=["sino_listener.py"]
	args+=[ "-i", inDir]
	args+=[ "-I", inFilenameFmt]
	args+=[ "-p", str(nProjs)]
	args+=[ "-o", outDir]
	args+=[ "-w", inImgWidth]
	args+=[ "-l", inImgHeight]
	args+=[ "--qsub_project", qsub_proj]
	if verbose:
		args+=[ "-v"]
	if testing:
		args+=[ "-t"]
	
	print args
	s_success=sino_listener.main(args)
	#sino_listener.main(["sino_listener.py", "-i", "/home/vxu94780/processing/rawdata/564/projections", "-I", "pco1564-%05d.tif", "-p", "1200", "-t"])
	return s_success


def makeLinksForNXSFile(\
					filename\
					, shutterOpenPhys=1.0\
					, shutterClosedPhys=0.0\
					, stageInBeamPhys=0.0\
					, stageOutOfBeamPhys=6.0\
					, shutterNXSPath='/entry1/instrument/tomoScanDevice/tomography_shutter'\
					, stagePosNXSPath='/entry1/instrument/tomoScanDevice/ss1_x'\
					, stageRotNXSPath='/entry1/instrument/tomoScanDevice/ss1_theta'\
					, tifNXSPath='/entry1/instrument/pco/data_file/file_name'\
					, imgkeyNXSPath='/entry1/instrument/tomoScanDevice/image_key'\
					, outdir=None\
					, minProjs=129\
					, maxUnclassed=0\
					, inFilenameFmt="p_%05d.tif"\
					, decimationRate=1\
					, sino=False\
					, avgf=False\
					, avgfu=0.01\
					, avgd=False\
					, avgdu=0.01\
					, verbose=False\
					, tiffDirOverride=""\
					, dbg=False):
	"""
	Create directories and links to projection, dark and flat images required for sino_listener to create sinograms.
	
	NXS paths to data:
	SHUTTER PHYSICAL POSITIONS='/entry1/instrument/tomoScanDevice/tomography_shutter'
	SAMPLE STAGE PHYSICAL POSITIONS='/entry1/instrument/tomoScanDevice/ss1_x'
	TIF FILENAMES='/entry1/instrument/pco/data_file/file_name'
	
	STAGE ANGLES='/entry1/instrument/tomoScanDevice/ss1_theta'
	"""
	if verbose:
		print "\nInput arguments for makeLinksForNXSFile:"
		print "filename=%s"%filename
		print "shutterOpenPhys=%s"%shutterOpenPhys
		print "shutterClosedPhys=%s"%shutterClosedPhys
		print "stageInBeamPhys=%s"%stageInBeamPhys
		print "stageOutOfBeamPhys=%s"%stageOutOfBeamPhys
		print "shutterNXSPath=%s"%shutterNXSPath
		print "stagePosNXSPath=%s"%stagePosNXSPath
		print "stageRotNXSPath=%s"%stageRotNXSPath
		print "tifNXSPath=%s"%tifNXSPath
		print "imgkeyNXSPath=%s"%imgkeyNXSPath
		print "outdir=%s"%outdir
		print "minProjs=%s"%minProjs
		print "maxUnclassed=%s"%maxUnclassed
		print "inFilenameFmt=%s"%str(inFilenameFmt)
		print "decimationRate=%s"%decimationRate
		print "sino=%s"%str(sino)
		print "avgf=%s"%str(avgf)
		print "avgfu=%s"%avgfu
		print "avgd=%s"%str(avgd)
		print "avgdu=%s"%avgdu
		print "verbose=%s"%str(verbose)
		print "tiffDirOverride=%s"%tiffDirOverride
		print "dbg=%s"%str(dbg)
		print "\n"
		
	shutOpenPos=shutterOpenPhys
	shutClosedPos=shutterClosedPhys
	inBeamPos=stageInBeamPhys
	outOfBeamPos=stageOutOfBeamPhys
	loProjs_exc=minProjs
	
	# define mapping between logical and physical positions of shutter and sample stage
	shutter_phys={'shutOpenPos': shutOpenPos, 'shutClosedPos': shutClosedPos}
	stage_phys={'inBeamPos': inBeamPos, 'outOfBeamPos': outOfBeamPos}
	
	shutter_log={'shutOpenPos': 1, 'shutClosedPos': 0}
	stage_log={'inBeamPos': 1, 'outOfBeamPos': 0}
	
	map01Def_shutter={shutter_log['shutClosedPos']: shutter_phys['shutClosedPos'], shutter_log['shutOpenPos']: shutter_phys['shutOpenPos']}
	map01Def_stage={stage_log['outOfBeamPos']: stage_phys['outOfBeamPos'], stage_log['inBeamPos']: stage_phys['inBeamPos']}
	
	if verbose:
		print "map01Def_shutter:"
		print map01Def_shutter
		print "map01Def_stage:"
		print map01Def_stage
		print "\n"
	
	#define parameters for differentiating between DARK, FLAT and PROJ images
	# DARK: shutter_open=0, stage_inbeam=1
	darkParam={'shutter': shutter_log['shutClosedPos'], 'stage': stage_log['inBeamPos']}
	
	# FLAT: shutter_open=1, stage_inbeam=0
	flatParam={'shutter': shutter_log['shutOpenPos'], 'stage': stage_log['outOfBeamPos']}
	
	# PROJ: shutter_open=1, stage_inbeam=1
	projParam={'shutter': shutter_log['shutOpenPos'], 'stage': stage_log['inBeamPos']}

	#DARK. FLAT and PROJ definitions bundled together in a handy dictionary
	dfpDef={'dark': darkParam, 'flat': flatParam, 'proj': projParam}
	if verbose:
		print "DARK. FLAT and PROJ definitions bundled together in a dictionary:"
		print dfpDef
	

	# check if the input NeXus file exists
	if not os.path.exists(filename):
		#msg = "The input NeXus file, %s, does NOT exist!"%filename
		raise Exception("The input NeXus file does not exist or insufficient filesystem permissions: "+`filename`)

	dirNXS = os.getcwd()
	# check if the input NeXus is absolute
	if os.path.isabs(filename):
		dirNXS = os.path.dirname(filename)
	print '\tdirNXS =', dirNXS

	#open the input NeXus file for reading 
	nxsFileHandle=h5py.File(filename, 'r')
	
	
	if verbose:
		print "The info from the input NeXus file:"
		for item in nxsFileHandle.attrs.keys():
			print item+":", nxsFileHandle.attrs[item]

	#get data arrays
	try:
		tomography_shutter=nxsFileHandle[shutterNXSPath]
	except Exception, ex:
		raise Exception ("Error on trying to access shutter's data inside the input NeXus file: \n"+str(ex))
	
	try:
		ss1_x=nxsFileHandle[stagePosNXSPath]
	except Exception, ex:
		raise Exception ("Error on trying to access  sample stage's translation data inside the input NeXus file: \n"+str(ex))
	
	try:
		#ss1_rot=nxsFileHandle['/entry1/instrument/tomoScanDevice/ss1_rot']
		ss1_rot=nxsFileHandle[stageRotNXSPath]
	except Exception, ex:
		raise Exception ("Error on trying to access  sample stage's angle data inside the input NeXus file: \n"+str(ex))
	
	try:
		tif_=nxsFileHandle[tifNXSPath]
	except Exception, ex:
		raise Exception ("Error on trying to access paths to TIF images inside the input NeXus file: \n"+str(ex))

	len_all=[]
	imgkeyNXS=True
	len_imgkey=-1
	try:
		imgkeyNXSPath='/entry1/instrument/tomoScanDevice/image_key'
		imgkey=nxsFileHandle[imgkeyNXSPath]
		#print 'type(imgkey)=', type(imgkey)
		len_imgkey=len(imgkey)
		#print 'len(imgkey)=', len_imgkey
		len_all.append(len_imgkey)
	except Exception, ex:
		imgkeyNXS=False
		msg1="Warning on trying to access image-key data inside the input NeXus file: \n"+str(ex)
		print msg1
		msg2="INFO: No image key data found; falling back on generating image keys from available data.\n"
		print msg2
		pass


	#take care of any relative paths 
	tif=np.empty((len(tif_),1),dtype='object')
	#for i in range(0,len(tif_)):
	#	s = tif_[i][0]
	#	if s[:1]==".":
	#		s1 = str(s[1:])
	#		tif[i][0]=dirNXS + s1
	#	else:
	#		tif[i][0]=s
	
	tif = convertFromRelativeToAbsolutePaths(tif_, dirNXS)
	test_filename = tif[0][0]
	does_testfile_exist = True 
	# check if the test file exists
	if not os.path.exists(test_filename):
		does_testfile_exist = False
		msg1 = "INFO: Attempt 0 -> Fail: file does not exist or insufficient filesystem permissions: "+`test_filename`
		print msg1
		
		msg2 = "INFO: Attempting to locate the file relative to the input NeXus file directory: "+`dirNXS`
		print msg2
		
		# modify the location of the test file
		attempt_dir = dirNXS
		test_filename_attempt = preModifyAbsolutePath(test_filename, attempt_dir)
		
		if os.path.exists(test_filename_attempt):
			does_testfile_exist = True
			msg3 = "INFO: Attempt 1 -> Success: located the file in the directory: "+ attempt_dir
			print msg3
		else:
			attempt_dir = None
			msg3 = "INFO: Attempt 1 -> Fail: file does not exist or insufficient filesystem permissions: "+`test_filename_attempt`
			print msg3
		if does_testfile_exist and attempt_dir is not None:
			preModifyAbsolutePathsss(tif, attempt_dir, tif)
		else:
			msg4 = "INFO: Unable to locate the file - proceeding by using the defunct filepath(s) recorded in input NeXus file:"+`test_filename`
			print msg4
	
	if tiffDirOverride != "":
		print "tiffDirOverride = ", tiffDirOverride
		for i in range(0,len(tif)):
			s = tif[i][0]
			print "pre s = ", tif[i][0]
			h, t = os.path.split(s)
			print "h = ", h
			print "t = ", t
			tif[i][0] = os.path.join(tiffDirOverride, t)
			print "post s = ", tif[i][0]
		#print "pre s = ", tif[0][0]
		#preModifyAbsolutePathsss(tif, tiffDirOverride, tif)
		#print "post s = ", tif[0][0]		

	if verbose:
		print 'tif[0][0]=',tif[0][0]
	#return

	len_ss1_x=len(ss1_x)
	len_ss1_rot=len(ss1_rot)
	len_tomography_shutter=len(tomography_shutter)
	len_tif=len(tif)
	
	len_all.append(len_ss1_x)
	len_all.append(len_ss1_rot)
	len_all.append(len_tomography_shutter)
	len_all.append(len_tif)
	print 'len_all=', len_all

	if verbose:
		print "len_ss1_x=%s"%len_ss1_x
		print "len_ss1_rot=%s"%len_ss1_rot
		print "len_tomography_shutter=%s"%len_tomography_shutter
		print "len_tif=%s"%len_tif

	inHead, inTail = os.path.split(test_filename)
	#print "inHead =", inHead
	#print "inTail =", inTail
	filenameZidxOffset = getFilenameZidxOffset(inHead, inFilenameFmt)
	filenameZidxOffset = 0
	
	# lists to store the indices of DARK, FLAT and PROJ images, respectively
	dark_idx=[]
	flat_idx=[]
	proj_idx=[]
	unclassified_idx=[]
	
	# for storing indices of sub-sequences of flats and darks
	unpicked_idx_dct = {}
	unpicked_idx_dct['dark']=[]
	unpicked_idx_dct['flat']=[] 
	unpicked_idx_dct['proj']=[]
	unpicked_idx_dct['uncl']=[]
	
	dfp_encoding={}
	dfp_encoding['dark']=2
	dfp_encoding['flat']=1
	dfp_encoding['proj']=0
	dfp_encoding['uncl']=-1
	dfp_encoding_reverse = {}
	dfp_encoding_reverse[dfp_encoding['dark']]='dark'
	dfp_encoding_reverse[dfp_encoding['flat']]='flat'
	dfp_encoding_reverse[dfp_encoding['proj']]='proj'
	dfp_encoding_reverse[dfp_encoding['uncl']]='uncl'
		
	image_key_idx_0=None
	if not imgkeyNXS:
		if (inBeamPos is None) or (outOfBeamPos is None):
			msg = "INFO: Image-key data are not available in input NeXus file - please re-run this script using \n"
			msg += "the stageInBeamPhys and stageOutOfBeamPhys options, followed by appropriate values \n"
			msg += "(the latter can be found in scan_command recorded in scan's NeXus file)."
			raise Exception(msg)
			
		image_key_idx_0_dct = genImageKey(inShutterPos=tomography_shutter[0], map01Def_shutter=map01Def_shutter, inStagePos=ss1_x[0], map01Def_stage=map01Def_stage, dfpDef=dfpDef)
		image_key_idx_0=dfp_encoding['uncl']
		if image_key_idx_0_dct['dark']==1 and image_key_idx_0_dct['flat']==0 and image_key_idx_0_dct['proj']==0:
			image_key_idx_0=dfp_encoding['dark']
		elif image_key_idx_0_dct['dark']==0 and image_key_idx_0_dct['flat']==1 and image_key_idx_0_dct['proj']==0:
			image_key_idx_0=dfp_encoding['flat']
		elif image_key_idx_0_dct['dark']==0 and image_key_idx_0_dct['flat']==0 and image_key_idx_0_dct['proj']==1:
			image_key_idx_0=dfp_encoding['proj']
	else:
		image_key_idx_0 = int(imgkey[0])
	
	if image_key_idx_0 is None:
		raise Exception("Invalid image key at index 0.")
	# init
	image_key_of_curr_subseq = image_key_idx_0
	image_key_curr_subseq_lst = []
	
	# use min just in case
	N=min(len_ss1_x, len_tomography_shutter, len_tif)
	
	if len_ss1_x!=N or len_tomography_shutter!=N or len_tif!=N:
		raise Exception("The lengths of NeXus data arrays differ between one other! ")

	if dbg and False:
		N=min(10, N)

	if not imgkeyNXS:

		if (inBeamPos is None) or (outOfBeamPos is None):
			msg = "INFO: Image-key data are not available in input NeXus file - please re-run this script using \n"
			msg += "the stageInBeamPhys and stageOutOfBeamPhys options, followed by appropriate values \n"
			msg += "(the latter can be found in scan_command recorded in scan's NeXus file)."
			raise Exception(msg)
			
		image_key_curr_dct={}
		image_key_curr=dfp_encoding['uncl']
		# identify each entry as DARK, FLAT, PROJ or UNCLASSIFIED image
		for i in range(0, N):
			image_key_curr_dct=genImageKey(inShutterPos=tomography_shutter[i], map01Def_shutter=map01Def_shutter, inStagePos=ss1_x[i], map01Def_stage=map01Def_stage, dfpDef=dfpDef) 
			
			if image_key_curr_dct['dark']==1 and image_key_curr_dct['flat']==0 and image_key_curr_dct['proj']==0:
				dark_idx.append(i)
				image_key_curr=dfp_encoding['dark']
			elif image_key_curr_dct['dark']==0 and image_key_curr_dct['flat']==1 and image_key_curr_dct['proj']==0:
				flat_idx.append(i)
				image_key_curr=dfp_encoding['flat']
			elif image_key_curr_dct['dark']==0 and image_key_curr_dct['flat']==0 and image_key_curr_dct['proj']==1:
				proj_idx.append(i)
				image_key_curr=dfp_encoding['proj']
			else:
				unclassified_idx.append(i)
			
			# handle sub-sequences 
			if image_key_of_curr_subseq != image_key_curr:
				# offload old subseq
				unpicked_idx_dct[ dfp_encoding_reverse[image_key_of_curr_subseq] ].append(image_key_curr_subseq_lst)
				# reset all for new subseq
				image_key_of_curr_subseq = image_key_curr
				image_key_curr_subseq_lst = []
				# and add curr idx to new subseq!
				image_key_curr_subseq_lst.append(i)
			else:
				# add curr idx to curr subseq
				image_key_curr_subseq_lst.append(i)
		
		# must offload the last subseq by brute force		
		unpicked_idx_dct[ dfp_encoding_reverse[image_key_of_curr_subseq] ].append(image_key_curr_subseq_lst)
	else:
		msg="INFO: Using image keys found in input NeXus file.\n"
		print msg
		image_key_curr=dfp_encoding['uncl']
		for i in range(0, N):
			image_key_curr=int(imgkey[i])
			if image_key_curr==dfp_encoding['dark']:
				dark_idx.append(i)
			elif image_key_curr==dfp_encoding['flat']:
				flat_idx.append(i)
			elif image_key_curr==dfp_encoding['proj']:
				proj_idx.append(i)
			else:
				unclassified_idx.append(i)
			
			# handle sub-sequences 
			if image_key_of_curr_subseq != image_key_curr:
				# offload old subseq
				unpicked_idx_dct[ dfp_encoding_reverse[image_key_of_curr_subseq] ].append(image_key_curr_subseq_lst)
				# reset all for new subseq
				image_key_of_curr_subseq = image_key_curr
				image_key_curr_subseq_lst = []
				# and add curr idx to new subseq!
				image_key_curr_subseq_lst.append(i)
			else:
				# add curr idx to curr subseq
				image_key_curr_subseq_lst.append(i)
		
		# must offload the last subseq by brute force		
		unpicked_idx_dct[ dfp_encoding_reverse[image_key_of_curr_subseq] ].append(image_key_curr_subseq_lst)
		
	if verbose:
		print "List of DARK indices:"
		print dark_idx
		print "List of FLAT indices:"
		print flat_idx
		print "List of PROJ indices:"
		print proj_idx
		print "List of UNCLASSIFIED indices:"
		print unclassified_idx
		
		print "Sub-sequences of DARK indices:"
		cnt=0
		for lst in unpicked_idx_dct['dark']:
			print cnt, lst
			cnt += 1
		print "Sub-sequences of FLAT indices:"
		cnt = 0
		for lst in unpicked_idx_dct['flat']:
			print cnt, lst
			cnt += 1
	
	# check if we've identified a sufficient number of images as DARK, FLAT and PROJ images to perform a reconstruction 
	len_proj_idx=len(proj_idx)
	if len_proj_idx<loProjs_exc:
		msg = "Warning: Number of the identified PROJECTION images is TOO SMALL for running reconstruction: "+`len_proj_idx`+" < "+`loProjs_exc`+" (the latter is the exclusive min)"
		#raise Exception(msg)
		print msg
	
	#unclassified_idx.append(1300)
	if len(dark_idx)==0:
		msg = "WARNING: Failed to identify ANY DARK-field images!"
		print msg
		#raise Exception(msg)
	
	if len(flat_idx)==0:
		msg = "WARNING: Failed to identify ANY FLAT-field images!"
		print msg
		#raise Exception(msg)
	
	if len(proj_idx)==0:
		msg = "WARNING: Failed to identify ANY PROJECTION images!"
		raise Exception(msg)
	
	hiUnclassifieds_exc=maxUnclassed
	len_unclassified_idx=len(unclassified_idx)
	if len_unclassified_idx>hiUnclassifieds_exc:
		#msg="The number of unclassified images is: %s!"%len_unclassified_idx
		raise Exception("Number of UNCLASSIFIED images is TOO HIGH to proceed: "+`len_unclassified_idx`+" > "+`hiUnclassifieds_exc`+" (the latter is the exclusive max)")
	
	if verbose:
		theta_lo=ss1_rot[ proj_idx[0] ]
		theta_hi=ss1_rot[ proj_idx[len_proj_idx-1] ] 
		print "theta_lo = %s, zidx = %s"%(theta_lo, proj_idx[0])
		print "theta_hi = %s, zidx = %s"%(theta_hi, proj_idx[len_proj_idx-1])
	
	# use the path of the first PROJ image file as a reference file path for identifying the corresponding scanNumber, etc
	srcfile_proj=tif[proj_idx[0]][0]
	
	inWidth = 4008
	inHeight = 2672
	try:
		with opened_w_error(str(srcfile_proj), 'rb') as (f, err):
			#print "Opened/read file: %s"%str(srcfile_proj)
			if err:
				print "IOError:", err
			else:
				try:
					img = Image.open(f)
				except StandardError:
					print "Error on Image.open for file: %s:"%str(srcfile_proj)
					f.close() # force close
				else:
					# process image
					msg = "INFO: Using image width = %s and height = %s, which were automatically detected from: %s"%(img.size[0],img.size[1],str(srcfile_proj))
					print msg
					inWidth = img.size[0]
					inHeight = img.size[1]
					#print '\t\tinWidth= ',inWidth
					#print '\t\tinHeight= ',inHeight
	except IOError:
		print "Could not open/read file: %s"%str(srcfile_proj)
		msg = "INFO: Using default image width = %s and height = %s"%(inWidth,inHeight)
		print msg
	
	#return 
	
	mandatory_parent_foldername="processing"
	
	scanNumber_str, head, sino_dir, dark_dir, flat_dir, proj_dir, darks_dir, flats_dir=createDirs(refFilename=srcfile_proj, outdir=outdir, mandatorydir=mandatory_parent_foldername, verbose=verbose)
	
	len_proj_idx_decimated, detectorName=populateDirs(scanNumber_str, head, dark_dir, flat_dir, proj_dir, darks_dir, flats_dir, tif, dark_idx, flat_idx, proj_idx, unpicked_idx_dct, filenameZidxOffset, decimationRate, verbose=verbose)
	
	# average flats
	avgf_success = False
	if avgf:
		with cd(head+os.sep+sino_dir):
			try:
				avgf_success = launchImageAveragingProcess(\
										inDir=(head+os.sep+flats_dir)\
										, inFilenameFmt="f_000_%05d.tif"\
										, outDir=(head+os.sep+flat_dir)\
										, outFilename="flat-avg.tif"\
										, imgWidth=inWidth\
										, imLength=inHeight\
										, outlierCheckParam=avgfu)
				print "INFO: Finished averaging flat-field images"
			except Exception, ex:
				avgf_success = False
				raise Exception ("ERROR Spawning flat_capav for flat-field images: "+str(ex))
	else:
		msg = "\nINFO: Launching of flat_capav for averaging flat-field images was not requested."
		print msg
	
	#create required link named flat.tif
	dst_flat="flat.tif"
	dst_flat=head+os.sep+flat_dir+os.sep+dst_flat
	if avgf_success:
		src_flat = head+os.sep+flat_dir+os.sep+"flat-avg.tif"
	else:
		src_flat = head+os.sep+flat_dir+os.sep+"flat-sgl.tif"
	createSoftLink(src_flat, dst_flat, throwExceptionIfSourceDoesNotExist=False)
				
	# average darks
	avgd_success = False
	if avgd:
		with cd(head+os.sep+sino_dir):
			try:
				avgd_success = launchImageAveragingProcess(\
										inDir=(head+os.sep+darks_dir)\
										, inFilenameFmt="d_000_%05d.tif"\
										, outDir=(head+os.sep+dark_dir)\
										, outFilename="dark-avg.tif"\
										, imgWidth=inWidth\
										, imLength=inHeight\
										, outlierCheckParam=avgdu)
				print "INFO: Finished averaging dark-field images"
			except Exception, ex:
				avgd_success = False
				raise Exception ("ERROR Spawning flat_capav for dark-field images: "+str(ex))
	else:
		msg = "\nINFO: Launching of flat_capav for averaging dark-field images was not requested."
		print msg
	
	#create required link named dark.tif
	dst_dark="dark.tif"
	dst_dark=head+os.sep+dark_dir+os.sep+dst_dark
	if avgd_success:
		src_dark = head+os.sep+dark_dir+os.sep+"dark-avg.tif"
	else:
		src_dark = head+os.sep+dark_dir+os.sep+"dark-sgl.tif"
	createSoftLink(src_dark, dst_dark, throwExceptionIfSourceDoesNotExist=False)
	
	#copy default settings.xml
	settings_src = "/dls_sw/i12/software/tomography_scripts/settings.xml"
	settings_dst = head+os.sep+sino_dir+os.sep+"settings.xml"
	copySingleFile(settings_src, settings_dst)
	
	if sino:
		with cd(head+os.sep+sino_dir):
			#print "\n\tInside context manager CWD = %s"%os.getcwd()
			try:
				#inProjFmt="pco1564-%05d.tif"
				#inProjFmt=detectorName+scanNumber_str+"-"+"%05d.tif"
				inProjFmt="p_%05d.tif"
				#nprojs=len_proj_idx_decimated
				#nprojs=1200
				if len_proj_idx_decimated<loProjs_exc or len_proj_idx_decimated<2:
					raise Exception("Number of provided projection images is TOO SMALL to run sino_listener: "+`len_proj_idx_decimated`+" < "+`loProjs_exc`+" (the latter is the exclusive min)")
				
				zidx_last=len_proj_idx_decimated-1
				#print 'zidx_last=', zidx_last
				launchSinoListener(head+os.sep+proj_dir, inProjFmt, zidx_last, head+os.sep+sino_dir, inWidth, inHeight, qsub_proj='i12', verbose=True, testing=False)
			except Exception, ex:
				raise Exception ("ERROR Spawning the sino_listener script  "+str(ex))
		
		#print "\nAfter launching the sino_listener script CWD = %s"%os.getcwd()
		
	else:
		print "\nINFO: Launching of the sino_listener script was not requested." 
	
	#print "\nJust before closing NeXus file CWD = %s"%os.getcwd()
	#don't forget to close the input NeXus file
	nxsFileHandle.close()
	
	
def main(argv):
	desc="""Description: This Python script, called %prog,
creates directories and links to projection, dark and flat images required for sino_listener to create sinograms.
"""
	usage="%prog -f input_filename --shutterOpenPhys shutOpenPos --shutterClosedPhys shutClosedPos --stageInBeamPhys inBeamPos --stageOutOfBeamPhys outOfBeamPos -o outdir\n"+\
				" or\n%prog --filename input_filename --shutterOpenPhys shutOpenPos --shutterClosedPhys shutClosedPos --stageInBeamPhys inBeamPos --stageOutOfBeamPhys outOfBeamPos --outdir outdir\n"+\
				"\nExample usage:\n%prog -f /dls/i12/data/2012/mt5811-1/564.nxs --shutterOpenPhys 1.0 --shutterClosedPhys 0.0 --stageInBeamPhys 0.0 --stageOutOfBeamPhys 6.0\n"+\
				"or:\n%prog --filename /dls/i12/data/2012/mt5811-1/564.nxs --shutterOpenPhys 1.0 --shutterClosedPhys 0.0 --stageInBeamPhys 0.0 --stageOutOfBeamPhys 6.0"
	vers="%prog version 1.0"

	parser=OptionParser(usage=usage, description=desc, version=vers, prog=argv[0])

	parser.add_option("-f", "--filename", action="store", type="string", dest="filename", help="NeXus filename to be processed.")
	parser.add_option("--shutterOpenPhys", action="store", type="float", dest="shutOpenPos", default=1.0, help="The shutter's PHYSICAL position when it was OPEN during the scan.")
	parser.add_option("--shutterClosedPhys", action="store", type="float", dest="shutClosedPos", default=0.0, help="The shutter's PHYSICAL position when it was CLOSED during the scan. ")
	parser.add_option("--stageInBeamPhys", action="store", type="float", dest="inBeamPos", help="The sample stage's PHYSICAL position when it was IN-BEAM during the scan. ")
	parser.add_option("--stageOutOfBeamPhys", action="store", type="float", dest="outOfBeamPos", help="The sample stage's PHYSICAL position when it was OUT-OF-BEAM during the scan. ")
	parser.add_option("--minProjs", action="store", type="int", dest="minProjs", default=129, help="The absolute minimum number of projections; default value is %default.")
	parser.add_option("--maxUnclassed", action="store", type="int", dest="maxUnclassed", default=0, help="The absolute maximum number of unclassified images; default value is %default.")
	parser.add_option("-e", "--every", action="store", type="int", dest="decimationRate", default=1, help="Indicates that only every n-th projection image will be used for reconstruction; default value is %default.")
	parser.add_option("--shutterNXSPath", action="store", type="string", dest="shutNXSPath", default="/entry1/instrument/tomoScanDevice/tomography_shutter", help="The path to the location of SHUTTER's physical positions inside the input NeXus file.")
	parser.add_option("--stagePosNXSPath", action="store", type="string", dest="stagePosNXSPath", default="/entry1/instrument/tomoScanDevice/ss1_x", help="The path to the location of SAMPLE STAGE's physical positions inside the input NeXus file.")
	parser.add_option("--stageRotNXSPath", action="store", type="string", dest="stageRotNXSPath", default="/entry1/instrument/tomoScanDevice/ss1_theta", help="The path to the location of SAMPLE STAGE's physical rotations inside the input NeXus file.")
	parser.add_option("--tifNXSPath", action="store", type="string", dest="tifNXSPath", default="/entry1/instrument/pco4000_dio_tif/image_data", help="The path to the location of TIFF filenames inside the input NeXus file.")
	parser.add_option("--imgkeyNXSPath", action="store", type="string", dest="imgkeyNXSPath", default="/entry1/instrument/tomoScanDevice/image_key", help="The path to the location of image-key data inside the input NeXus file.")
	parser.add_option("-o", "--outdir", action="store", type="string", dest="outdir", help="Path to folder in which directories and files are to be made. Default is current working folder")
	parser.add_option("--inFilenameFmt", action="store", type="string", dest="inFilenameFmt", default="p_%05d.tif", help="Input filename C-printf format; default: p_%05d.tif")
	parser.add_option("--verbose", action="store_true", dest="verbose", default=False, help="Verbose - useful for diagnosing the script")
	parser.add_option("-s", "--sino", action="store_true", dest="sino", default=False, help="If set to True, the sino_listener script will be launched as well.")
	parser.add_option("--avgf", action="store_true", dest="avgf", default=False, help="If set to True, flat_capav will be launched to average flat-field images.")
	parser.add_option("--avgfu", action="store", type="float", dest="avgfu", default=0.01, help="Outlier check parameter for use with flat_capav on flat-field images.")
	parser.add_option("--avgd", action="store_true", dest="avgd", default=False, help="If set to True, flat_capav will be launched to average dark-field images.")
	parser.add_option("--avgdu", action="store", type="float", dest="avgdu", default=0.01, help="Outlier check parameter for use with flat_capav on dark-field images.")
	parser.add_option("--tiffDirOverride", action="store", type="string", dest="tiffDirOverride", default="", help="The path to a directory containing TIFF images, which overrides that found in input Nexus file.")	
	parser.add_option("--dbg", action="store_true", dest="dbg", default=False, help="Debug option set to True limits the number of processed images to the first 10 (useful for testing, etc.")

	(opts, args)=parser.parse_args(args=argv[1:])
	opts_dict=vars(opts)

#print the input args
	if opts.verbose:
		print "Main input args:"
		for key, value in opts_dict.iteritems():
			print key, value
		print "\n"

	#parser.print_help()
	#parser.print_version()

# Make sure all mandatory input variables have some values
	#mandatory_var=['filename', 'shutOpenPos', 'shutClosedPos', 'inBeamPos', 'outOfBeamPos', 'shutNXSPath', 'stagePosNXSPath', 'stageRotNXSPath', 'tifNXSPath']
	mandatory_var=['filename', 'shutOpenPos', 'shutClosedPos', 'shutNXSPath', 'stagePosNXSPath', 'stageRotNXSPath', 'tifNXSPath']
	for m in mandatory_var:
		#print opts_dict[m]
		if opts_dict[m] is None:
			msg="Mandatory input value, %s, is missing!"%m
			raise Exception(msg+"Use -h for help")
	
	outdir_loc=opts.outdir
	if not outdir_loc is None:
		outdir_loc=removeTrailingSlash(outdir_loc)
		
	minProjs_loc=max(opts.minProjs, 1)
	maxUnclassed_loc=max(opts.maxUnclassed, 0)
	makeLinksForNXSFile(\
					filename=opts.filename\
					, shutterOpenPhys=opts.shutOpenPos\
					, shutterClosedPhys=opts.shutClosedPos\
					, stageInBeamPhys=opts.inBeamPos\
					, stageOutOfBeamPhys=opts.outOfBeamPos\
					, outdir=outdir_loc\
					, minProjs=minProjs_loc\
					, maxUnclassed=maxUnclassed_loc\
					, shutterNXSPath=opts.shutNXSPath\
					, stagePosNXSPath=opts.stagePosNXSPath\
					, stageRotNXSPath=opts.stageRotNXSPath\
					, tifNXSPath=opts.tifNXSPath\
					, imgkeyNXSPath=opts.imgkeyNXSPath\
					, inFilenameFmt=opts.inFilenameFmt\
					, decimationRate=opts.decimationRate\
					, verbose=opts.verbose\
					, sino=opts.sino\
					, avgf=opts.avgf\
					, avgfu=opts.avgfu\
					, avgd=opts.avgd\
					, avgdu=opts.avgdu\
					, tiffDirOverride=opts.tiffDirOverride\
					, dbg=opts.dbg)

if __name__=="__main__":
	#use the line below when running unit-tests or from the command line
	main(sys.argv)
"""
	main(["progname"\
		, "--filename", "/dls/i13/data/2012/mt5811-1/564.nxs"\
		, "--shutterOpenPhys", "1.0"\
		, "--shutterClosedPhys", "0.0"\
		, "--stageInBeamPhys", "0.0"\
		, "--stageOutOfBeamPhys", "6.0"\
		, "--outdir", "/home/vxu94780"\
		#, "--outdir", "/dls/i13/data/2012/mt5811-1"\
		#, "--outdir", "/dls/i13/data/2012/mt5811-1/processing"\
		, "--minProjs", "1"\
		, "--verbose", "True"\
		#, "--every", "3"\
		, "--every", "1"\
		, "--sino", "False"\
		, "--dbg", "True"\
		])
"""
"""
	main(["progname"
		, "--filename", "/dls/i13/data/2012/mt5811-1/564.nxs"\
		, "--shutterOpenPhys", "1.0"\
		, "--shutterClosedPhys", "0.0"\
		, "--stageInBeamPhys", "0.0"\
		, "--stageOutOfBeamPhys", "6.0"\
		, "--outdir", "/home/vxu94780"\
		, "--minProjs", "1"\
		, "--verbose", "True"\
		, "--dbg", "True"\
		])
"""
	#main(["progname", "--filename", "/dls/i13/data/2012/mt5811-1/564.nxs", "--shutterOpenPhys", "1.0", "--shutterClosedPhys", "0.0", "--stageInBeamPhys", "0.0", "--stageOutOfBeamPhys", "6.0", "--verbose", "True"])
"""
	main(["progname"\
		, "--filename", "/dls/i13/data/2012/mt5811-1/564.nxs"\
		, "--shutterOpenPhys", "1.0"\
		, "--shutterClosedPhys", "0.0"\
		, "--stageInBeamPhys", "0.0"\
		, "--stageOutOfBeamPhys", "6.0"\
		, "--outdir", "test_makeLinksNXS_SoftLinkAlreadyExists_output/processing"\
		, "--minProjs", "1"\
		, "--verbose", "True"\
		])
"""

class Test1(unittest.TestCase):

	def setUp(self):
		#/scratch/i13trunk2_git/gda-dls-beamlines-i13x.git/i13i/scripts
		self.cwd=os.getcwd()

	def tearDown(self):
		os.chdir(self.cwd)

	def testHelp(self):
		main(["progname", "-h"])

	def test_noArgs(self):
		try:
			main(["progname"])
		except  Exception,  ex:
			self.assertEquals('Mandatory input value is missing! Use -h for help', str(ex))

	def test_makeLinksNXS_InputFileInexistent(self):
		nonExistentNXSFile="/dls/i13/data/2012/mt5811-1/X564X.nxs"
		try:
			main(\
				["progname"\
				 , "--filename", nonExistentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 ])
		except  Exception , ex:
			self.assertEquals("The input NeXus file does not exist: "+nonExistentNXSFile, str(ex))

	def test_makeLinksNXS_InsufficientProjections(self):
		existentNXSFile="/dls/i13/data/2012/mt5811-1/564.nxs"
		try:
			main(\
				["progname"\
				 , "--filename", existentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 ])
		except  Exception, ex:
			self.assertEquals("The number of projections is too small!", str(ex))

	def test_makeLinksNXS_SoftLinkToProjAlreadyExists(self):
		existentNXSFile="/dls/i13/data/2012/mt5811-1/564.nxs"
		try:
			outputDir="test_makeLinksNXS_SoftLinkAlreadyExists_output/processing"
			dummyProjFileDir=outputDir+os.sep+"rawdata/564/projections"
			if os.path.exists(dummyProjFileDir):
				shutil.rmtree(dummyProjFileDir)
			#os.mkdir(outputDir)
			os.makedirs(dummyProjFileDir)
			#os.chdir(outputDir)
			
			#dummyProjFile="rawdata/564/projections"+os.sep+"pco1564-00000.tif"
			dummyProjFile=dummyProjFileDir+os.sep+"pco1564-00000.tif"
			f=open(dummyProjFile, "w");
			f.write("Some dummy PROJ content")
			f.flush()
			f.close()
			main(\
				["progname"\
				 , "--filename", existentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 , "--minProjs", "1"\
				 , "--outdir", outputDir\
				 ])
		except  Exception, ex:
			self.assertEquals("Soft link to PROJECTION image already exists: "+dummyProjFile, str(ex))

	def test_makeLinksNXS_SoftLinkToDarkAlreadyExists(self):
		existentNXSFile="/dls/i13/data/2012/mt5811-1/564.nxs"
		try:
			outputDir="test_makeLinksNXS_SoftLinkAlreadyExists_output/processing"
			dummyDarkFileDir=outputDir+os.sep+"sino/564/dark"
			if os.path.exists(dummyDarkFileDir):
				shutil.rmtree(dummyDarkFileDir)
			#os.mkdir(outputDir)
			os.makedirs(dummyDarkFileDir)
			#os.chdir(outputDir)
			
			dummyDarkFile=dummyDarkFileDir+os.sep+"dark.tif"
			f=open(dummyDarkFile, "w");
			f.write("Some dummyDARK content")
			f.flush()
			f.close()
			main(\
				["progname"\
				 , "--filename", existentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 , "--minProjs", "1"\
				 , "--outdir", outputDir\
				 ])
		except  Exception, ex:
			self.assertEquals("Soft link to DARK image already exists: "+dummyDarkFile, str(ex))

	def test_makeLinksNXS_SoftLinkToFlatAlreadyExists(self):
		existentNXSFile="/dls/i13/data/2012/mt5811-1/564.nxs"
		try:
			outputDir="test_makeLinksNXS_SoftLinkAlreadyExists_output/processing"
			dummyFlatFileDir=outputDir+os.sep+"sino/564/flat"
			if os.path.exists(dummyFlatFileDir):
				shutil.rmtree(dummyFlatFileDir)
			#os.mkdir(outputDir)
			os.makedirs(dummyFlatFileDir)
			#os.chdir(outputDir)
			
			dummyFlatFile=dummyFlatFileDir+os.sep+"flat.tif"
			f=open(dummyFlatFile, "w");
			f.write("Some dummy FLAT content")
			f.flush()
			f.close()
			main(\
				["progname"\
				 , "--filename", existentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 , "--minProjs", "1"\
				 , "--outdir", outputDir\
				 ])
		except  Exception, ex:
			self.assertEquals("Soft link to FLAT image already exists: "+dummyFlatFile, str(ex))

	def test_makeLinksNXS_OutdirGiven(self):
		existentNXSFile="/dls/i13/data/2012/mt5811-1/564.nxs"
		outputDir="test_makeLinksNXS_SoftLinkAlreadyExists_output/processing"
		if os.path.exists(outputDir):
			shutil.rmtree(outputDir)
		os.makedirs(outputDir)
		
		main(\
				["progname"\
				 , "--filename", existentNXSFile\
				 , "--shutterOpenPhys", "1.0"\
				 , "--shutterClosedPhys", "0.0"\
				 , "--stageInBeamPhys", "0.0"\
				 , "--stageOutOfBeamPhys", "6.0"\
				 , "--minProjs", "1"\
				 , "--outdir", outputDir\
				 , "--dbg", True\
				 ])

		#the actual test of the outcome for the above part
		os.chdir(outputDir)
		fileToTest="sino/564/dark/dark.tif"
		if not os.path.exists(fileToTest):
			self.fail("Link to DARK image failed to be created for file:"+fileToTest)

		fileToTest="sino/564/flat/flat.tif"
		if not os.path.exists(fileToTest):
			self.fail("Link to FLAT image failed to be created for file:"+fileToTest)

		fileToTest="rawdata/564/projections/pco1564-00000.tif"
		if not os.path.exists(fileToTest):
			self.fail("Link to PROJ image failed to be created for file:"+fileToTest)

