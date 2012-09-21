from __future__ import with_statement
from optparse import OptionParser
from sys import exit, stderr
import math
import os
import shutil
import subprocess
import sys
import unittest

import h5py
import string
from mklinks import makeLinks, makeLinksToOriginalFiles


from sino_listener import *
import sino_listener

from recon_arrayxml import *
import recon_arrayxml

from contextlib import contextmanager

from numpy import *
import numpy as np
#import PIL.Image as Image
import Image

def checkDirContentForFilesWithPrefix(inDir, filenameTemplate='sino_'):
	
	tot = 0
	if not os.path.isdir(inDir):
		raise Exception("checkDirContentForFilesWithPrefix: input directory does not exist or is not a directory:"+`inDir`)
	
	for n in os.listdir(inDir):
		if os.path.isfile(os.path.join(inDir,n)) and n.startswith(filenameTemplate):
			tot+=1
	return tot
	
	
def getBeamlineTomoConfig(inLocalTomoXML="localTomo.xml", verbose=True):
	# check if input file exists
	if not os.path.exists(inLocalTomoXML):
		msg = "Input beamline-specific localTomo file, %s, does not exist!"%inLocalTomoXML
		raise Exception(msg)

	mydoc=parse(inLocalTomoXML)
	
	# beamline
	beamline_tag=mydoc.getElementsByTagName("beamline")

	# beamline>ixx
	val_tag=beamline_tag[0].getElementsByTagName("ixx")
	ixx = val_tag[0].childNodes[0].data

	#tomodo
	tomodo_tag=mydoc.getElementsByTagName("tomodo")
	
	#tomodo>shutter
	mid_tag=tomodo_tag[0].getElementsByTagName("shutter")
	val_tag=mid_tag[0].getElementsByTagName("shutterOpenPhys")
	shutterOpenPhys = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("shutterClosedPhys")
	shutterClosedPhys = val_tag[0].childNodes[0].data

	#tomodo>tifimage
	mid_tag=tomodo_tag[0].getElementsByTagName("tifimage")
	
	val_tag=mid_tag[0].getElementsByTagName("filenameFmt")
	filenameFmt = val_tag[0].childNodes[0].data

	#tomodo>nexusfile
	mid_tag=tomodo_tag[0].getElementsByTagName("nexusfile")
	
	val_tag=mid_tag[0].getElementsByTagName("shutterNXSPath")
	shutterNXSPath = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("stagePosNXSPath")
	stagePosNXSPath = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("stageRotNXSPath")
	stageRotNXSPath = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("tifNXSPath")
	tifNXSPath = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("imgkeyNXSPath")
	imgkeyNXSPath = val_tag[0].childNodes[0].data

	#tomodo>settingsfile
	mid_tag=tomodo_tag[0].getElementsByTagName("settingsfile")
	
	val_tag=mid_tag[0].getElementsByTagName("blueprint")
	settings_blueprint = val_tag[0].childNodes[0].data

	#tomodo>imagekeyencoding
	mid_tag=tomodo_tag[0].getElementsByTagName("imagekeyencoding")
	
	val_tag=mid_tag[0].getElementsByTagName("darkfield")
	imgkey_darkfield = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("flatfield")
	imgkey_flatfield = val_tag[0].childNodes[0].data

	val_tag=mid_tag[0].getElementsByTagName("projection")
	imgkey_projection = val_tag[0].childNodes[0].data

	#tomodo>cluster
	cluster_tag=tomodo_tag[0].getElementsByTagName("cluster")

	#tomodo>cluster>qsub
	qsub_tag=cluster_tag[0].getElementsByTagName("qsub")

	projectname_tag=qsub_tag[0].getElementsByTagName("projectname")
	projectname = projectname_tag[0].childNodes[0].data

	args_tag=qsub_tag[0].getElementsByTagName("args")
	args = args_tag[0].childNodes[0].data
	
	sinoqueue_tag=qsub_tag[0].getElementsByTagName("sinoqueue")
	sinoqueue = sinoqueue_tag[0].childNodes[0].data

	reconqueue_tag=qsub_tag[0].getElementsByTagName("reconqueue")
	reconqueue = reconqueue_tag[0].childNodes[0].data
	
	if verbose:
		print '\t ixx=', ixx
		print '\t shutterOpenPhys=', shutterOpenPhys
		print '\t shutterClosedPhys=', shutterClosedPhys
		print '\t filenameFmt=', filenameFmt
		print '\t shutterNXSPath=', shutterNXSPath
		print '\t stagePosNXSPath=', stagePosNXSPath
		print '\t stageRotNXSPath=', stageRotNXSPath
		print '\t tifNXSPath=', tifNXSPath
		print '\t imgkeyNXSPath=', imgkeyNXSPath
		print '\t settings_blueprint=', settings_blueprint
		print '\t imgkey_darkfield=', imgkey_darkfield
		print '\t imgkey_flatfield=', imgkey_flatfield
		print '\t imgkey_projection=', imgkey_projection
		print '\t projectname=', projectname
		print '\t args=', args
		print '\t sinoqueue=', sinoqueue
		print '\t reconqueue=', reconqueue
		
	return filenameFmt, stagePosNXSPath, stageRotNXSPath, tifNXSPath, projectname, args
	
def reindexLinks(inListOfIdx, outListOfIdx, indir="/dls/i13/data/2012/mt5811-1/564/pco1/", outdir="/dls/i13/data/2012/mt5811-1/564/pco1/", inFilenameFmt="p_%05d.tif", outFilenameFmt="p_%05d.tif"):
	
	#print "Fn: %s"%reindexLinks.__name__
	#print '\tinListOfIdx=', inListOfIdx
	#print '\toutListOfIdx=', outListOfIdx
	#print '\tindir=', indir
	#print '\toutdir=', outdir
	#print '\tinFilenameFmt=', inFilenameFmt
	#print '\toutFilenameFmt=', outFilenameFmt
	
	len_in =len(inListOfIdx)
	len_out =len(outListOfIdx)
	if len_in != len_out:
		msg="The number of input and output indices are different: %s and %s:"%(len_in, len_out)
		raise Exception(msg)
	
	indir_loc=removeTrailingSlash(indir)
	
	if not os.path.isdir(indir_loc):
		raise Exception("Input directory does not exist:"+`indir`)
	
	j = 0
	for i in inListOfIdx:
		k = outListOfIdx[j]
		#print "loop j=%s"%j
		#print "src i=%s"%i
		#print "dst k=%s"%k
		filename_src=inFilenameFmt%i
		filename_dst=outFilenameFmt%k
		fileToReindex=indir_loc+os.sep+filename_src
		if not os.path.exists(fileToReindex):
			raise Exception("File cannot be re-indexed as it does not exist:"+`fileToReindex`)
		if not outdir is None:
			filename_dst=outdir+os.sep+filename_dst
		#if os.path.exists(filename_dst) and False:
		#	if os.path.realpath(filename_dst)!=os.path.realpath(fileToReindex):
		#		msg="Soft link already exists:"+`filename_dst`+" but not to the required destination "+`fileToReindex` 
		#		raise Exception(msg)
		#else:
		cmd="mv"+" "+fileToReindex+" "+filename_dst
		#print 'cmd=', cmd
		subprocess.call(cmd, shell=True)
		j+=1

@contextmanager
def cd(path):
	saved_dir=os.getcwd()
	try:
		os.chdir(path)
		yield path
	except Exception, ex:
		raise Exception ("cd: ERROR changing directory: "+str(ex))
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

def areApproxEqual(inVal0, inVal1, eps=0.001):
	if abs(inVal0-inVal1)<eps:
		return True
	else:
		return False
		
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


def createDirs(refFilename, outdir, mandatorydir="processing", quick=False, verbose=False):

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
				print "\nMandatory dirname appended to outdir_loc=%s"%outdir_loc


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
	recon_dir="reconstruction"+os.sep+scanNumber_str

	dirs=[proj_dir, dark_dir, flat_dir, recon_dir]
	
	if quick:
		dirnameToDelete = head+os.sep+recon_dir+'_quick'
		if os.path.exists(dirnameToDelete):
			print "Removing a reconstruction directory used in the quick mode: %s"%dirnameToDelete
			shutil.rmtree(dirnameToDelete)

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


	print "\nFinished creating directories." 
	return scanNumber_str, head, sino_dir, dark_dir, flat_dir, proj_dir, recon_dir


def decimate(inList, decimationRate=1):
	"""
	Selects every N-th element from sequential input list of integers, eg
	
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

def stepThrough(inList, stepSize=1):
	"""
	Selects every N-th element from sequential input list of integers, eg
	
	step([12,13,14,15,16,17,18,19,20], 3)= [12,15,18]
	step([12,13,14,15,16,17,18,19,20], 9)= [12]
	step([12,13,14,15,16,17,18,19,20], 10)= [12]
	step([12,13,14,15,16,17,18,19,20], 11)= [12]
	step([12,13,14,15,16,17,18,19,20], 1)= [12,13,14,15,16,17,18,19,20]
	"""
	step_loc=int(stepSize)
	if step_loc<1:
		step_loc=1
	
	inLen=len(inList)
	outLen=int(inLen/step_loc)

	i_from=0
	i_to=step_loc*outLen
	i_step=step_loc
	if i_to == 0:
		i_to=1
	outList=[]
	for i in range(i_from, i_to, i_step):
		outList.append(inList[i])
	return outList

def populateDirs(scanNumber_str, head, dark_dir, flat_dir, proj_dir, tif_lst, dark_idx, flat_idx, proj_idx, filenameFmt, decimationRate=1, verbose=False):

	"""
	Create:
	soft links to projection images in the projections folder
	dark.tif in the sino/[scanNumber]/dark folder
	flat.tif in the sino/[scanNumber]/flat folder
	"""

	src_dark=tif_lst[dark_idx[0]][0]
	#print "src_dark=%s"%src_dark

	dst_dark="dark.tif"
	dst_dark=head+os.sep+dark_dir+os.sep+dst_dark
	createSoftLink(src_dark, dst_dark)

	src_flat=tif_lst[flat_idx[0]][0]
	dst_flat="flat.tif"
	dst_flat=head+os.sep+flat_dir+os.sep+dst_flat
	createSoftLink(src_flat, dst_flat)

	#identify arguments to be used when calling makeLinks using the path of the first projection file
	src_proj=tif_lst[proj_idx[0]][0]
	refFilename=src_proj


	src_proj_split=string.split(src_proj, "/")

	#e.g. /dls/i13/data/2012/mt5811-1/564/pco1/pco1564-00002.tif
	#Note that src_proj_split contains some empty strings, i.e.''
	#makeLinks_arg={}
	#makeLinks_arg['beamlineID']=src_proj_split[2]
	#makeLinks_arg['year']=int(src_proj_split[4])
	#makeLinks_arg['visit']=src_proj_split[5]
	#makeLinks_arg['scanNumber']=int(src_proj_split[6])
	#makeLinks_arg['scanNumber']=src_proj_split[6]
	#makeLinks_arg['detector']=src_proj_split[7]
	
	#makeLinks_arg['firstImage']=proj_idx[0]
	#makeLinks_arg['lastImage']=proj_idx[len(proj_idx)-1]

	detectorName=src_proj_split[7]
	makeLinks_outdir=head+os.sep+proj_dir
	print "makeLinks_outdir=%s"%makeLinks_outdir
	#print "makeLinks_beamlineID=%s"%makeLinks_arg['beamlineID']


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
	#inFnameFmt="%05d.tif"
	#if makeLinks_arg['beamlineID'] == "i12":
		#inFnameFmt=inImgFilenameFmt
	#	inFnameFmt="p_%05d.tif"
	inFnameFmt=filenameFmt
	outFnameFmt="p_%05d.tif"
	makeLinksToOriginalFiles(\
							listOfProjIdx=proj_idx_decimated\
							, indir=genAncestorPath(refFilename, 1)\
							, inFilenameFmt=inFnameFmt\
							, outdir=(head+os.sep+proj_dir)\
							, outFilenameFmt=outFnameFmt)

	print "\nFinished populating directories." 
	return len(proj_idx_decimated), detectorName


def createSoftLink(src, dst):
	if not os.path.exists(src):
		raise Exception("File cannot be linked to as it does not exist:"+`src`)
	if os.path.exists(dst):
		if os.path.realpath(src)!=os.path.realpath(dst):
				msg="Soft link already exists:"+`src`+" but not to the required destination "+`dst`
				raise Exception(msg)		
	else:
		cmd="ln -s "+src+" "+dst
		subprocess.call(cmd, shell=True)


def launchSinoListener(inDir, inFilenameFmt, nProjs, outDir, qsub_proj='i13', verbose=False, testing=True):
	print launchSinoListener.__name__
	args=["sino_listener.py"]
	args+=[ "-i", inDir]
	args+=[ "-I", inFilenameFmt]
	args+=[ "-p", str(nProjs)]
	args+=[ "-o", outDir]
	args+=[ "--qsub_project", qsub_proj]
	if verbose:
		args+=[ "-v"]
	if testing:
		args+=[ "-t"]
	
	print args
	s_success=sino_listener.main(args)
	#sino_listener.main(["sino_listener.py", "-i", "/home/vxu94780/processing/rawdata/564/projections", "-I", "pco1564-%05d.tif", "-p", "1200", "-t"])
	return s_success

def readTIFF_u16(path):
	"""Read 16bit TIFF"""
	print '\treadTIFF_u16 START'
	print 'filepath=', path
	im=Image.open(path)
	print "im.format", im.format
	print "im.mode", im.mode
	print 'im.size', im.size
	
	#out=np.fromstring(im.tostring(), np.uint8).reshape(tuple(list(im.size)+[2]))
	arr=np.fromstring(im.tostring(), np.uint8).reshape(im.size[1], im.size[0], 2)
	print 'arr.shape', arr.shape
	print 'im.size[0]*im.size[1] =', im.size[0]*im.size[1]
	print '\treadTIFF_u16 END'
	return im, (np.array(arr[:, :, 0], np.uint16)<<8)+arr[:, :, 1]

def histogram_full(arr):
	print '\thistogram_full START'
	maxIntensity=arr.max()
	maxIntensity_=np.amax(arr)
	print 'maxIntensity=', maxIntensity
	print 'maxIntensity_=', maxIntensity_
	
	bins=xrange(0, maxIntensity+1+1)
	print 'bins=', bins
	#hist=np.histogram(arr, maxIntensity+1)
	hist=np.histogram(arr, bins)
	print 'hist: ', hist
	print 'len(hist)=', len(hist)
	print 'len(hist[0])=', len(hist[0])
	print 'len(hist[1])=', len(hist[1])
	print 'type(hist[0])=', type(hist[0])
	print 'hist[0].size=', hist[0].size
	print 'hist[0][0]=', hist[0][0]
	print 'hist[0][maxIntensity+1]=', hist[0][maxIntensity]
	
	print '\thistogram_full END'
	return hist[0]
		
def find_threshold_otsu(arr, ignore_zeros=False):
	print '\tfind_threshold_otsu START'
	hist=histogram_full(arr)
	hist=hist.astype(np.double)
	
	if ignore_zeros:
		hist[0]=0
		
	Ng=len(hist)
	nB=np.cumsum(hist)
	nO=nB[-1]-nB
	mu_B=0
	mu_O=(np.arange(1, Ng)*hist[1:]).sum()/hist[1:].sum()
	best=nB[0]*nO[0]*(mu_B-mu_O)*(mu_B-mu_O)
	bestT=0

	for T in xrange(1, Ng):
		if nB[T]==0: continue
		if nO[T]==0: break
		mu_B=(mu_B*nB[T-1]+T*hist[T])/nB[T]
		mu_O=(mu_O*nO[T-1]-T*hist[T])/nO[T]
		sigma_between=nB[T]*nO[T]*(mu_B-mu_O)*(mu_B-mu_O)
		if sigma_between>best:
			best=sigma_between
			bestT=T
	
	print '\tfind_threshold_otsu END'
	return bestT

def calc_centroid_arr(arr):
	
	sx=sy=n=0
	for x in range(0, arr.shape[0]):
		for y in range(0, arr.shape[1]):
			if arr[x, y]:
				n+=1
				
	if n>0:
		print 'calc_centroid_arr: n=', n
		n=1.0/n
			
	for x in range(0, arr.shape[0]):
		for y in range(0, arr.shape[1]):
			if arr[x, y]:
				sx+=y*n
				sy+=x*n
	
	return float(sx)+0.5, float(sy)+0.5

def overwrite_timestamp(filename, dy=16+1, intensity=0):
	fname=filename+'.tif'
	im0, a0=readTIFF_u16(fname)
	
	w, h=a0.shape[1], a0.shape[0]
	#w=a0.size[1]
	#h=a0.size[0]
	arr_ovr=np.zeros((w, h), np.uint16)
	arr_ovr.shape=h, w
	
	#copy
	for x in range(0, a0.shape[0]):
		for y in range(0, a0.shape[1]):
			arr_ovr[x, y]=a0[x, y]
			
	#overwrite
	for x in range(0, min(a0.shape[0], dy)):
		for y in range(0, a0.shape[1]):
			arr_ovr[x, y]=intensity
			
	im_ovr=Image.frombuffer("I;16", (w, h) , arr_ovr, "raw", "I;16", 0, 1)
	filename_ovr=filename+'_ovr'+`dy`
	fname_ovr=filename_ovr+'.tif'
	file=open(fname_ovr, 'w')
	output_type="TIFF"
	im_ovr.save(file, output_type)
	file.close()
	return filename_ovr

def findLeastUpperBound( refVal, lstVal ):
	len_lstVal = len( lstVal )
	lstVal_sorted = sorted( lstVal )
	#print lstVal_sorted
	leastUpperBoundIdx = 0
	leastUpperBoundVal = lstVal_sorted[leastUpperBoundIdx]
	i = 0
	while ( i<len_lstVal and lstVal_sorted[i]<refVal ):
		#print 'i=', i
		if lstVal_sorted[i]>leastUpperBoundVal:
			leastUpperBoundVal = lstVal_sorted[i]
			leastUpperBoundIdx = i
		i+=1
	return leastUpperBoundVal, leastUpperBoundIdx

def findGreatestLowerBound( refVal, lstVal ):
	len_lstVal = len( lstVal )
	lstVal_sorted = sorted( lstVal )
	#print lstVal_sorted
	greatestLowerBoundIdx = len_lstVal-1
	greatestLowerBoundVal = lstVal_sorted[greatestLowerBoundIdx]
	i = len_lstVal
	while ( (i-1)>0 and lstVal_sorted[i-1]>refVal ):
		#print 'i-1=', (i-1)
		if lstVal_sorted[i-1]<greatestLowerBoundVal:
			greatestLowerBoundVal = lstVal_sorted[i-1]
			greatestLowerBoundIdx = i-1
		i-=1
	return greatestLowerBoundVal, greatestLowerBoundIdx

def estimateCOR(projFilename_0deg, flatFilename_0deg, darkFilename_0deg, projFilename_180deg, flatFilename_180deg, darkFilename_180deg):
	print estimateCOR.__name__
	print "projFilename_0deg=", projFilename_0deg
	print "flatFilename_0deg=", flatFilename_0deg
	print "darkFilename_0deg=", darkFilename_0deg
	print "projFilename_180deg=", projFilename_180deg
	print "flatFilename_180deg=", flatFilename_180deg
	print "darkFilename_180deg=", darkFilename_180deg

	#filename with ext
	fname0=projFilename_0deg
	fname180=projFilename_180deg

	#filename without extension
	filename0, filenameExt0 = os.path.splitext(fname0)
	filename180, filenameExt180 = os.path.splitext(fname180)

	#filename head and tail
	head0, tail0 = os.path.split(filename0)
	head180, tail180 = os.path.split(filename180)

	filename0='/dls/tmp/vxu94780'+os.sep+tail0
	filename180='/dls/tmp/vxu94780'+os.sep+tail180

	print 'filename0=', filename0
	print 'filename180=', filename180

	print 'fname0=', fname0
	print 'fname180=', fname180

	#return 0, 0
	
	dbg_save = False

	im0, a0=readTIFF_u16(fname0)
	im180, a180=readTIFF_u16(fname180)
	print 'im180.size', im180.size
	print 'a180.shape', a180.shape
	
	w, h=a180.shape[1], a180.shape[0]
	
	useSubtraction=False
	if useSubtraction:
		filename0_flat=flatFilename_0deg
		filename180_flat=flatFilename_180deg
		
		fname0_flat=filename0_flat+'.tif'
		fname180_flat=filename180_flat+'.tif'
		
		im0_flat, a0_flat=readTIFF_u16(fname0_flat)
		im180_flat, a180_flat=readTIFF_u16(fname180_flat)
		print 'im180_flat.size=', im180_flat.size
		print 'a180_flat.shape=', a180_flat.shape
		
		s=0
		for x in range(0, a0.shape[0]):
			for y in range(0, a0.shape[1]):
				f=a0[x, y]
				a0[x, y]-=a0_flat[x, y]
				#a0[x, y]=a0_flat[x, y]-a0[x, y]
				if a0[x, y]<0: 
					a0[x, y]=0
				#a0[x, y]=65535-a0[x, y]
				if a0[x, y]>f:
					
					if s<10:
						print "\n"
						print "f=", f
						print 'a0_flat[x, y]=', a0_flat[x, y]
						print 'a0[x, y]=', a0[x, y]
						print 'f-a0_flat[x, y]=', f-a0_flat[x, y]
						s+=1
				
				a180[x, y]-=a180_flat[x, y]
				#a180[x, y]=a180_flat[x, y]-a180[x, y]
				if a180[x, y]<0: 
					a180[x, y]=65535
				#a180[x, y]=65535-a180[x, y]
			
		#im0_sub=im0.filter(ImageFilter.BLUR)
		im0_sub=Image.frombuffer("I;16B", (w, h) , a0, "raw", "I;16B", 0, 1)
		filename0_sub=filename0+'_sub'
		fname0_sub=filename0+'_sub.tif'
		file=open(fname0_sub, 'w')
		output_type="TIFF"
		im0_sub.save(file, output_type)
		file.close()
		print '>>im0: flat-subtracted'
	
		im180_sub=Image.frombuffer("I;16B", (w, h) , a180, "raw", "I;16B", 0, 1)
		filename180_sub=filename180+'_sub'
		fname180_sub=filename180+'_sub.tif'
		file=open(fname180_sub, 'w')
		output_type="TIFF"
		im180_sub.save(file, output_type)
		file.close()
		print '>>im180: flat-subtracted'
	
	# im180 flipped LR 
	a180_flp=np.zeros((w, h), np.uint16)
	a180_flp.shape=h, w
	
	for x in range(0, a180.shape[0]):
		for y in range(0, a180.shape[1]):
			a180_flp[x, y]=a180[x, a180.shape[1]-y-1]
	#		a180_flp[x, y]=a180[x, y]
			
	im180_flp=Image.frombuffer("I;16B", (w, h) , a180_flp, "raw", "I;16B", 0, 1)
	filename180_flp=filename180+'_flp'
	fname180_flp=filename180+'_flp.tif'
	if dbg_save:
		file=open(fname180_flp, 'w')
		output_type="TIFF"
		im180_flp.save(file, output_type)
		file.close()
	print 'File saving pt: ',fname180_flp
	print '>>im180: LR-flipped'
	
	# im180 flipped LR, inverted
	a180_flp_inv=np.zeros((w, h), np.uint16)
	a180_flp_inv.shape=h, w
	
	for x in range(0, a180_flp.shape[0]):
		for y in range(0, a180_flp.shape[1]):
			if not useSubtraction:
				a180_flp_inv[x, y]=65535-a180_flp[x, y]
			else:
				a180_flp_inv[x, y]=a180_flp[x, y]
			
	im180_flp_inv=Image.frombuffer("I;16B", (w, h) , a180_flp_inv, "raw", "I;16B", 0, 1)
	filename180_flp_inv=filename180+'_flp_inv'
	fname180_flp_inv=filename180+'_flp_inv.tif'
	if dbg_save:
		file=open(fname180_flp_inv, 'w')
		output_type="TIFF"
		im180_flp_inv.save(file, output_type)
		file.close()
	print 'File saving pt: ',fname180_flp_inv
	print '>>im180: LR-flipped + inverted'
	
	# im0 inverted
	a0_inv=np.zeros((w, h), np.uint16)
	a0_inv.shape=h, w
	
	for x in range(0, a0.shape[0]):
		for y in range(0, a0.shape[1]):
			if not useSubtraction:
				a0_inv[x, y]=65535-a0[x, y]
			else:
				a0_inv[x, y]=a0[x, y]
			
	im0_inv=Image.frombuffer("I;16B", (w, h) , a0_inv, "raw", "I;16B", 0, 1)
	filename0_inv=filename0+'_inv'
	fname0_inv=filename0+'_inv.tif'
	if dbg_save:
		file=open(fname0_inv, 'w')
		output_type="TIFF"
		im0_inv.save(file, output_type)
		file.close()
	print 'File saving pt: ',fname0_inv
	print '>>im0: inverted'
	
	
	arr_tmp=np.fromstring(im0_inv.tostring(), np.uint8).reshape(im0_inv.size[1], im0_inv.size[0], 2)
	a0_inv_=(np.array(arr_tmp[:, :, 0], np.uint16)<<8)+arr_tmp[:, :, 1]

	thr0=find_threshold_otsu(a0_inv_)
	thr0_=find_threshold_otsu(a0_inv_, True)
	print 'x thr0=', thr0
	print 'x thr0_=', thr0_
	
	# apply threshold to im0
	a0_inv_thr_=np.zeros((w, h), np.uint16)
	a0_inv_thr_.shape=h, w
	threshold0=thr0
	lo=0
	hi=65535
	for x in range(0, a0_inv_.shape[0]):
		for y in range(0, a0_inv_.shape[1]):
			if a0_inv_[x, y]<threshold0:
				a0_inv_thr_[x, y]=lo
			else:
				a0_inv_thr_[x, y]=hi
			
	im0_inv_thr=Image.frombuffer("I;16", (w, h) , a0_inv_thr_, "raw", "I;16", 0, 1)
	filename0_inv_thr=filename0+'_inv_thr'+`threshold0`
	fname0_inv_thr=filename0_inv_thr+'.tif'
	if dbg_save: 
		file=open(fname0_inv_thr, 'w')
		output_type="TIFF"
		im0_inv_thr.save(file, output_type)
		file.close()
	filename0_thr=filename0_inv_thr
	
	"""
	im0_inv, a0_inv=readTIFF_u16(fname0_inv)
	thr0=find_threshold_otsu(a0_inv)
	thr0_=find_threshold_otsu(a0_inv, True)
	print 'thr0=', thr0
	print 'thr0_=', thr0_
		
	filename0_thr=apply_threshold(filename0_inv, thr0)
	"""
	print 'File saving pt: ',fname0_inv_thr
	print 'filename0_thr=', filename0_thr
	print '>>im0: inverted + thresholded'
	
	
	arr_tmp=np.fromstring(im180_flp_inv.tostring(), np.uint8).reshape(im180_flp_inv.size[1], im180_flp_inv.size[0], 2)
	a180_flp_inv_=(np.array(arr_tmp[:, :, 0], np.uint16)<<8)+arr_tmp[:, :, 1]
	thr180=find_threshold_otsu(a180_flp_inv_)
	thr180_=find_threshold_otsu(a180_flp_inv_, True)
	print 'x thr180=', thr180
	print 'x thr180_=', thr180_

	# apply threshold to im180
	a180_flp_inv_thr_=np.zeros((w, h), np.uint16)
	a180_flp_inv_thr_.shape=h, w
	threshold180=thr180
	lo=0
	hi=65535
	for x in range(0, a180_flp_inv_.shape[0]):
		for y in range(0, a180_flp_inv_.shape[1]):
			if a180_flp_inv_[x, y]<threshold180:
				a180_flp_inv_thr_[x, y]=lo
			else:
				a180_flp_inv_thr_[x, y]=hi
			
	im180_flp_inv_thr=Image.frombuffer("I;16", (w, h) , a180_flp_inv_thr_, "raw", "I;16", 0, 1)
	filename180_flp_inv_thr=filename180+'_flp_inv_thr'+`threshold180`
	fname180_flp_inv_thr=filename180_flp_inv_thr+'.tif'
	if dbg_save:
		file=open(fname180_flp_inv_thr, 'w')
		output_type="TIFF"
		im180_flp_inv_thr.save(file, output_type)
		file.close()
	filename180_thr=filename180_flp_inv_thr
	
	"""
	im180_flp_inv, a180_flp_inv=readTIFF_u16(fname180_flp_inv)
	thr180=find_threshold_otsu(a180_flp_inv)
	thr180_=find_threshold_otsu(a180_flp_inv, True)
	print 'thr180=', thr180
	print 'thr180_=', thr180_
	
	filename180_thr=apply_threshold(filename180_flp_inv, thr180)
	"""
	print 'File saving pt: ',fname180_flp_inv_thr
	print 'filename180_thr=', filename180_thr
	print '>>im180: LR-flipped + inverted + thresholded'
	
	
	# apply timestamp-overwrite to im0
	a0_inv_thr_ovr_=np.zeros((w, h), np.uint16)
	a0_inv_thr_ovr_.shape=h, w
	intensity=0
	dy=256
	
	# first copy the entire source content
	for x in range(0, a0_inv_thr_.shape[0]):
		for y in range(0, a0_inv_thr_.shape[1]):
			a0_inv_thr_ovr_[x, y]=a0_inv_thr_[x, y]
			
	# then overwrite what's required 
	for x in range(0, min(a0_inv_thr_.shape[0], dy)):
		for y in range(0, a0_inv_thr_.shape[1]):
			a0_inv_thr_ovr_[x, y]=intensity
			
	#for x in range(0, a0_inv_thr_.shape[0]):
	#	for y in range(a0_inv_thr_.shape[1]-256*3, a0_inv_thr_.shape[1]):
	#		a0_inv_thr_ovr_[x, y]=intensity
			
	im0_inv_thr_ovr=Image.frombuffer("I;16", (w, h) , a0_inv_thr_ovr_, "raw", "I;16", 0, 1)
	filename0_inv_thr_ovr=filename0+'_inv_thr'+`threshold0`+'_ovr'+`dy`
	fname0_inv_thr_ovr=filename0_inv_thr_ovr+'.tif'
	if dbg_save:
		file=open(fname0_inv_thr_ovr, 'w')
		output_type="TIFF"
		im0_inv_thr_ovr.save(file, output_type)
		file.close()
	filename0_ovr=filename0_inv_thr_ovr

	#filename0_ovr=overwrite_timestamp(filename0_thr, dy=256)
	print 'File saving pt: ',fname0_inv_thr_ovr
	print 'filename0_ovr=', filename0_ovr
	print '>>im0: inverted + thresholded + timestamp-overwritten'
	
	
	
	# apply timestamp-overwrite to im180
	a180_flp_inv_thr_ovr_=np.zeros((w, h), np.uint16)
	a180_flp_inv_thr_ovr_.shape=h, w
	intensity=0
	dy=256
	
	# first copy the entire source content 
	for x in range(0, a180_flp_inv_thr_.shape[0]):
		for y in range(0, a180_flp_inv_thr_.shape[1]):
			a180_flp_inv_thr_ovr_[x, y]=a180_flp_inv_thr_[x, y]
			
	# then overwrite what's required 
	for x in range(0, min(a180_flp_inv_thr_.shape[0], dy)):
		for y in range(0, a180_flp_inv_thr_.shape[1]):
			a180_flp_inv_thr_ovr_[x, y]=intensity
			
	#for x in range(0, a180_flp_inv_thr_.shape[0]):
	#	for y in range(0, 256*3):
	#		a180_flp_inv_thr_ovr_[x, y]=intensity
			
	im180_flp_inv_thr_ovr=Image.frombuffer("I;16", (w, h) , a180_flp_inv_thr_ovr_, "raw", "I;16", 0, 1)
	filename180_flp_inv_thr_ovr=filename180+'_flp_inv_thr'+`threshold180`+'_ovr'+`dy`
	fname180_flp_inv_thr_ovr=filename180_flp_inv_thr_ovr+'.tif'
	if dbg_save:
		file=open(fname180_flp_inv_thr_ovr, 'w')
		output_type="TIFF"
		im180_flp_inv_thr_ovr.save(file, output_type)
		file.close()
	filename180_ovr=filename180_flp_inv_thr_ovr

	#filename180_ovr=overwrite_timestamp(filename180_thr, dy=256)
	print 'File saving pt: ',fname180_flp_inv_thr_ovr
	print 'filename180_ovr=', filename180_ovr
	print '>>im180: LR-flipped + inverted + thresholded + timestamp-overwritten'
	
	
	
	
	# calculate centroid for im0
	#im0_centroid_x_, im0_centroid_y_=calc_centroid(filename0_ovr)
	im0_centroid_x, im0_centroid_y=calc_centroid_arr(a0_inv_thr_ovr_)
	print '\t(im0_centroid_x, im0_centroid_y)=', im0_centroid_x, im0_centroid_y
	#print '\t(im0_centroid_x_, im0_centroid_y_)=', im0_centroid_x_, im0_centroid_y_
	print '>>im0: centroid calculated'
	
	# calculate centroid for im180
	#im180_centroid_x_, im180_centroid_y_=calc_centroid(filename180_ovr)
	im180_centroid_x, im180_centroid_y=calc_centroid_arr(a180_flp_inv_thr_ovr_)
	print '\t(im180_centroid_x, im180_centroid_y)=', im180_centroid_x, im180_centroid_y
	#print '\t(im180_centroid_x_, im180_centroid_y_)=', im180_centroid_x_, im180_centroid_y_
	print '>>im180: centroid calculated'
	
	# calc the relative displacement of the centroids
	centroid_t_x=im180_centroid_x-im0_centroid_x
	centroid_t_y=im180_centroid_y-im0_centroid_y
	print '\t(centroid_t_x, centroid_t_y)=', centroid_t_x, centroid_t_y
	
	
	# calc tomoaxis
	tomoaxis_x=0.5*(im180.size[0]-centroid_t_x)
	tomoaxis_y=centroid_t_y
	print '>>tomoaxis=', (tomoaxis_x, tomoaxis_y)
	
	print '\testmateRotationCentre END'
	return tomoaxis_x, tomoaxis_y


def launchReconArray(outDir, ctrCoord, inSinoFolder='sinograms', settingsfile='/dls_sw/i12/software/tomography_scripts/settings.xml', qsub_proj='i13', verbose=True, testing=False):
	print launchReconArray.__name__
	args=["recon_arrayxml.py"]
	#args+=[ "-h"]
	args+=[ "-I", settingsfile]
	args+=[ "-o", outDir]
	args+=[ "-d", inSinoFolder]
	args+=[ "-C", str(ctrCoord)]
	args+=[ "--qsub_project", qsub_proj]
	if verbose:
		args+=[ "-v"]
	if testing:
		args+=[ "-t"]
	
	print args
	r_success, r_imfolder=recon_arrayxml.main(args)
	return r_success, r_imfolder


def makeLinksForNXSFile(\
					filename\
					, settingsfile='/dls_sw/i12/software/tomography_scripts/settings.xml'
					, shutterOpenPhys=1.0\
					, shutterClosedPhys=0.0\
					, stageInBeamPhys=0.0\
					, stageOutOfBeamPhys=6.0\
					, shutterNXSPath='/entry1/instrument/tomoScanDevice/tomography_shutter'\
					, stagePosNXSPath='/entry1/instrument/tomoScanDevice/ss1_X'\
					, stageRotNXSPath='/entry1/instrument/tomoScanDevice/ss1_rot'\
					, tifNXSPath='/entry1/instrument/pco1_hw_tif/image_data'\
					, imgkeyNXSPath='/entry1/instrument/tomoScanDevice/image_key'\
					, inImgFilenameFmt='%05d.tif'\
					, qsub_project='i13'\
					, outdir=None\
					, minProjs=129\
					, quickstep=100\
					, maxUnclassed=0\
					, decimationRate=1\
					, sino=False\
					, recon=False\
					, quick=False\
					, verbose=False\
					, dbg=False):
	"""
	Create directories and links to projection, dark and flat images required for sino_listener to create sinograms.
	
	NXS paths to the relevant data arrays:
	*primary:
	SHUTTER PHYSICAL POSITIONS='/entry1/instrument/tomoScanDevice/tomography_shutter'
	SAMPLE-STAGE PHYSICAL POSITIONS	='/entry1/instrument/tomoScanDevice/ss1_X'
	TIF FILENAMES='/entry1/instrument/pco1_hw_tif/image_data'
	IMAGE KEY='/entry1/instrument/tomoScanDevice/image_key'
	
	*secondary:
	SAMPLE-STAGE ANGLES='/entry1/instrument/tomoScanDevice/ss1_rot'
	
	*additional
	SCAN COMMAND='/entry1/scan_command'
	"""
	if verbose:
		print "\nInput arguments for makeLinksForNXSFile:"
		print "filename=%s"%filename
		print "settingsfile=%s"%settingsfile
		print "shutterOpenPhys=%s"%shutterOpenPhys
		print "shutterClosedPhys=%s"%shutterClosedPhys
		print "stageInBeamPhys=%s"%stageInBeamPhys
		print "stageOutOfBeamPhys=%s"%stageOutOfBeamPhys
		print "shutterNXSPath=%s"%shutterNXSPath
		print "stagePosNXSPath=%s"%stagePosNXSPath
		print "stageRotNXSPath=%s"%stageRotNXSPath
		print "tifNXSPath=%s"%tifNXSPath
		print "imgkeyNXSPath=%s"%imgkeyNXSPath
		print "inImgFilenameFmt=%s"%inImgFilenameFmt
		print "qsub_project=%s"%qsub_project
		print "outdir=%s"%outdir
		print "minProjs=%s"%minProjs
		print "maxUnclassed=%s"%maxUnclassed
		print "decimationRate=%s"%decimationRate
		print "sino=%s"%str(sino)
		print "recon=%s"%str(recon)
		print "quick=%s"%str(quick)
		print "verbose=%s"%str(verbose)
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
	
	
	# check if the input template file exists
	if not os.path.exists(settingsfile):
		#msg = "The input NeXus file, %s, does NOT exist!"%filename
		raise Exception("The input settings file does not exist or insufficient filesystem permissions: "+`filename`)

	inSettings = settingsfile
	quickStep = quickstep
	
	# check if the input NeXus file exists
	if not os.path.exists(filename):
		#msg = "The input NeXus file, %s, does NOT exist!"%filename
		raise Exception("The input NeXus file does not exist or insufficient filesystem permissions: "+`filename`)

	dirNXS = os.getcwd()
	# check if the input NeXus filepath is absolute
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
		raise Exception ("Error on trying to access sample stage's translation data inside the input NeXus file: \n"+str(ex))
	
	try:
		#ss1_rot=nxsFileHandle['/entry1/instrument/tomoScanDevice/ss1_rot']
		ss1_rot=nxsFileHandle[stageRotNXSPath]
	except Exception, ex:
		raise Exception ("Error on trying to access sample stage's angle data inside the input NeXus file: \n"+str(ex))
	
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

	#take care of any relative paths to TIF images
	tif=np.empty((len(tif_),1),dtype='object')
	for i in range(0,len(tif_)):
		s = tif_[i][0]
		if s[:1]==".":
			s1 = str(s[1:])
			tif[i][0]=dirNXS + s1
		else:
			tif[i][0]=s
	
	if verbose:
		print '\t Absolute path in tif[0][0]=',tif[0][0]
	
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

	
	# lists to store the indices of DARK, FLAT and PROJ images, respectively
	dark_idx=[]
	flat_idx=[]
	proj_idx=[]
	unclassified_idx=[]
	
	# use min just in case
	N=min(len_ss1_x, len_tomography_shutter, len_tif)
	
	if len_ss1_x!=N or len_tomography_shutter!=N or len_tif!=N:
		raise Exception("The lengths of NeXus data arrays differ between one other! ")

	if dbg and False:
		N=min(10, N)

	if not imgkeyNXS:
		if (inBeamPos is None) or (outOfBeamPos is None):
			msg="INFO: Image-key data are not available in input NeXus file - please re-run this script using the stageInBeamPhys and stageOutOfBeamPhys options accompanied by appropriate values (the latter can be found in scan_command recorded in scan's NeXus file)."
			raise Exception(msg)
		
		image_key_curr={}
	# identify each entry as DARK, FLAT, PROJ or UNCLASSIFIED image
		for i in range(0, N):
			image_key_curr=genImageKey(inShutterPos=tomography_shutter[i], map01Def_shutter=map01Def_shutter, inStagePos=ss1_x[i], map01Def_stage=map01Def_stage, dfpDef=dfpDef) 
			
			if image_key_curr['dark']==1 and image_key_curr['flat']==0 and image_key_curr['proj']==0:
				dark_idx.append(i)
			elif image_key_curr['dark']==0 and image_key_curr['flat']==1 and image_key_curr['proj']==0:
				flat_idx.append(i)
			elif image_key_curr['dark']==0 and image_key_curr['flat']==0 and image_key_curr['proj']==1:
				proj_idx.append(i)
			else:
				unclassified_idx.append(i)
	else:
		msg="INFO: Using image keys found in input NeXus file.\n"
		print msg
		dfp={}
		dfp['dark']=2
		dfp['flat']=1
		dfp['proj']=0
		image_key_curr=-1
		for i in range(0, N):
			image_key_curr=int(imgkey[i])
			if image_key_curr==dfp['dark']:
				dark_idx.append(i)
			elif image_key_curr==dfp['flat']:
				flat_idx.append(i)
			elif image_key_curr==dfp['proj']:
				proj_idx.append(i)
			else:
				unclassified_idx.append(i)
	
	if verbose:
		print "List of DARK indices:"
		print dark_idx
		print "List of FLAT indices:"
		print flat_idx
		print "List of PROJ indices:"
		print proj_idx
		print "List of UNCLASSIFIED indices:"
		print unclassified_idx
	
	# check if we've identified a sufficient number of images as DARK, FLAT and PROJ images to perform a reconstruction 
	len_proj_idx=len(proj_idx)
	if len_proj_idx<loProjs_exc:
		raise Exception("Number of the identified PROJECTION images is TOO SMALL to proceed: "+`len_proj_idx`+" < "+`loProjs_exc`+" (the latter is the exclusive min)")
	
	#unclassified_idx.append(1300)
	if len(dark_idx)==0:
		raise Exception("Failed to identify ANY DARK field images!")
	
	if len(flat_idx)==0:
		raise Exception("Failed to identify ANY FLAT field images!")
	
	if len(proj_idx)==0:
		raise Exception("Failed to identify ANY PROJECTION images!")
	
	hiUnclassifieds_exc=maxUnclassed
	len_unclassified_idx=len(unclassified_idx)
	if len_unclassified_idx>hiUnclassifieds_exc:
		#msg="The number of unclassified images is: %s!"%len_unclassified_idx
		raise Exception("Number of UNCLASSIFIED images is TOO HIGH to proceed: "+`len_unclassified_idx`+" > "+`hiUnclassifieds_exc`+" (the latter is the exclusive max)")
	
	theta_lo=ss1_rot[ proj_idx[0] ]
	theta_hi=ss1_rot[ proj_idx[len_proj_idx-1] ]

	#if verbose:
		#theta_lo=ss1_rot[ proj_idx[0] ]
		#theta_hi=ss1_rot[ proj_idx[len_proj_idx-1] ] 
		#print "theta_lo = %s, zidx = %s"%(theta_lo, proj_idx[0])
		#print "theta_hi = %s, zidx = %s"%(theta_hi, proj_idx[len_proj_idx-1])

	path_proj_0deg=''
	img_idx_proj_0deg=-1
	if areApproxEqual(theta_lo, 0.0):
		print 'Found projection at angle 0 with image index=', proj_idx[0]
		path_proj_0deg = tif[ proj_idx[0] ][0]
		img_idx_proj_0deg = proj_idx[0]
	else:
		msg = 'Failed to find projection at angle 0!' 
		print "theta_lo = %s, zidx = %s"%(theta_lo, proj_idx[0])
		raise Exception(msg)
		
	path_proj_180deg=''
	img_idx_proj_180deg=-1
	if areApproxEqual(theta_hi, 180.0):
		print 'Found projection at angle 180 with image index=', proj_idx[len_proj_idx-1]
		path_proj_180deg = tif[ proj_idx[len_proj_idx-1] ][0]
		img_idx_proj_180deg = proj_idx[len_proj_idx-1]
	else:
		msg = 'Failed to find projection at angle 180!' 
		print "theta_hi = %s, zidx = %s"%(theta_hi, proj_idx[len_proj_idx-1])
		raise Exception(msg)
	
	# use the path of the first PROJ image file as a reference file path for identifying the corresponding scanNumber, etc
	srcfile_proj=tif[ proj_idx[0] ][0]
	mandatory_parent_foldername="processing"
	
	scanNumber_str, head, sino_dir, dark_dir, flat_dir, proj_dir, recon_dir=createDirs(refFilename=srcfile_proj, outdir=outdir, mandatorydir=mandatory_parent_foldername, quick=quick, verbose=verbose)
	
	len_proj_idx_decimated, detectorName=populateDirs(scanNumber_str, head, dark_dir, flat_dir, proj_dir, tif, dark_idx, flat_idx, proj_idx, inImgFilenameFmt, decimationRate, verbose=verbose)
	
	if sino:
		#print "\n\tAbout to launch the sino_listener script from CWD = %s"%os.getcwd()
		#sino=SinoListener(argv, out=sys.stdout, err=sys.stderr)
		#sino_listener.py -i projections -I pco1564-%05d.tif -P 1201
		#sino=SinoListener(["prog", "-h"], out=sys.stdout, err=sys.stderr, testing=True)
		#sino=SinoListener(["prog", "-h"], out=sys.stdout, err=sys.stderr, testing=True)
		#/dls/i13/data/2012/mt5811-1/processing/rawdata/564/projections
		sino_success=False
		with cd(head+os.sep+sino_dir):
			#print "\n\tInside sino context manager CWD = %s"%os.getcwd()
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
				sinoTot= checkDirContentForFilesWithPrefix(inDir=head+os.sep+sino_dir, filenameTemplate='sino_')
				print '\t sinoTot=', sinoTot
				if sinoTot > 0:
					sino_success = True
					msg="INFO: Using %s sinograms previously created in %s"%(sinoTot, head+os.sep+sino_dir)
					print msg
				else:
					sino_success=launchSinoListener(\
											inDir=head+os.sep+proj_dir\
											, inFilenameFmt=inProjFmt\
											, nProjs=zidx_last\
											, outDir=head+os.sep+sino_dir\
											, qsub_proj=qsub_project\
											, verbose=True\
											, testing=False)
			except Exception, ex:
				sino_success=False
				raise Exception ("ERROR Spawning the sino_listener script  "+str(ex))
		
		print "\nAfter launching the sino_listener script CWD = %s"%os.getcwd()
		
		if sino_success:
			print 'sino_success=TRUE'
		else:
			print 'sino_success=FALSE'

		if sino_success:
			sino_idx=range(0,2672)
			sino_idx_stepped=[]
			sino_idx_stepped=stepThrough(sino_idx, stepSize=quickStep)
			inFnameFmt="sino_%05d.tiff"
			outFnameFmt="sino_%05d.tiff"
			inDir=head+os.sep+sino_dir+os.sep+"sinograms"
			outDir=head+os.sep+sino_dir+os.sep+"sino_quick"
			print "quick sino inDir=",inDir
			print "quick sino outDir=",outDir
			if quick:
				makeLinksToOriginalFiles(\
								sino_idx_stepped\
								, indir=inDir\
								, inFilenameFmt=inFnameFmt\
								, outdir=outDir\
								, outFilenameFmt=outFnameFmt)
		
		#recon = True	
		if sino_success and recon:
			recon_success=False
			recon_imfolder=''
			#with cd(head+os.sep+"reconstruction/564"):
			with cd(head+os.sep+sino_dir):
				try:
					print "\n\tInside recon context manager CWD = %s"%os.getcwd()
					#reconDir="../../reconstruction/564"
					reconDir=head+os.sep+recon_dir
					print "reconDir=",reconDir

					print '\tpath_proj_0deg=', path_proj_0deg
					print '\tpath_proj_180deg=', path_proj_180deg
					
					lubVal_flat_0deg, lubIdx_flat_0deg = findLeastUpperBound(img_idx_proj_0deg,flat_idx)
					path_flat_0deg = tif[ lubVal_flat_0deg ][0]
					lubVal_dark_0deg, lubIdx_dark_0deg = findLeastUpperBound(img_idx_proj_0deg, dark_idx)
					path_dark_0deg = tif[ lubVal_dark_0deg ][0]
					
					glbVal_flat_180deg, glbIdx_flat_180deg = findGreatestLowerBound(img_idx_proj_180deg,flat_idx)
					path_flat_180deg = tif[ glbVal_flat_180deg ][0]
					glbVal_dark_180deg, glbIdx_dark_180deg = findGreatestLowerBound(img_idx_proj_180deg,dark_idx)
					path_dark_180deg = tif[ glbVal_dark_180deg ][0]
					
					print '\tpath_flat_0deg=', path_flat_0deg
					print '\tpath_dark_0deg=', path_dark_0deg

					print '\tpath_flat_180deg=', path_flat_180deg
					print '\tpath_dark_180deg=', path_dark_180deg
															
					projFilename_0deg = tif[ proj_idx[0] ][0]
					flatFilename_0deg = tif[ flat_idx[0] ][0]
					darkFilename_0deg = tif[ dark_idx[0] ][0]

					#projFilename_180deg = tif[ proj_idx[len(proj_idx)-1] ][0]
					projFilename_180deg = tif[ proj_idx[len_proj_idx_decimated-1] ][0]

					flatFilename_180deg = tif[ flat_idx[0] ][0]
					darkFilename_180deg = tif[ dark_idx[0] ][0]

					#CORx, CORy=estimateCOR(projFilename_0deg, flatFilename_0deg, darkFilename_0deg, projFilename_180deg, flatFilename_180deg, darkFilename_180deg)
					#CORx=1557.8
					CORx=1320
					if quick:
						#reconDir=outDir=head+os.sep+recon_dir+'_quick'
						inSinoFoldername='sino_quick'
						
						#
						mydoc=parse(inSettings)
						backprojtag=mydoc.getElementsByTagName("Backprojection")
						valtag=backprojtag[0].getElementsByTagName("ImageCentre")
						CORx = valtag[0].childNodes[0].data
					else:
						inSinoFoldername='sinograms'
		
					recon_success, recon_imfolder=launchReconArray(\
																outDir=reconDir\
																, ctrCoord=CORx\
																, inSinoFolder=inSinoFoldername\
																, settingsfile=inSettings\
																, qsub_proj=qsub_project)
				except Exception, ex:
					recon_success=False
					raise Exception ("ERROR Spawning the recon_arrayxml script  "+str(ex))
			
			if recon_success:
				print 'recon_success=TRUE'
			else:
				print 'recon_success=FALSE'

			if recon_success:
				recon_idx=range(0,2672)
				recon_idx_stepped=[]
				recon_idx_stepped=stepThrough(recon_idx, stepSize=quickStep)
				recon_idx_stepped=range(0, len(sino_idx_stepped))
				inFnameFmt="image_%05d.tif"
				outFnameFmt="image_%05d.tif"
				inDir=head+os.sep+recon_dir+os.sep+recon_imfolder
				outDir=head+os.sep+recon_dir+'_quick'
				print "quick recon inDir=",inDir
				print "quick recon outDir=",outDir
				if quick:
					makeLinksToOriginalFiles(\
									recon_idx_stepped\
									, indir=inDir\
									, inFilenameFmt=inFnameFmt\
									, outdir=outDir\
									, outFilenameFmt=outFnameFmt)
		if quick:
			inListOfIdx=range(0, len(sino_idx_stepped))
			outListOfIdx=[]
			ftor = quickStep
			for i in inListOfIdx:
				outListOfIdx.append(i*ftor)
		
			reindexLinks(inListOfIdx, outListOfIdx, indir=head+os.sep+sino_dir+os.sep+"sino_quick", outdir=head+os.sep+sino_dir+os.sep+"sino_quick", inFilenameFmt="sino_%05d.tiff", outFilenameFmt="sino_%05d.tiff")
			
			reindexLinks(inListOfIdx, outListOfIdx, indir=head+os.sep+recon_dir+'_quick', outdir=head+os.sep+recon_dir+'_quick', inFilenameFmt="image_%05d.tif", outFilenameFmt="image_%05d.tif")
	else:
		print "\nLaunch of the sino_listener script was not requested at the end of makeLinksForNXSFile." 
	
	#print "\nJust before closing NeXus file CWD = %s"%os.getcwd()
	#don't forget to close the input NeXus file
	nxsFileHandle.close()
	
	
def main(argv):
	desc="""Description: This Python script, called %prog,
creates directories and links to projection, dark and flat images required for sino_listener to create sinograms.
"""
	usage="%prog -f input_filename --shutterOpenPhys shutOpenPos --shutterClosedPhys shutClosedPos --stageInBeamPhys inBeamPos --stageOutOfBeamPhys outOfBeamPos -o outdir\n"+\
				" or\n%prog --filename input_filename --shutterOpenPhys shutOpenPos --shutterClosedPhys shutClosedPos --stageInBeamPhys inBeamPos --stageOutOfBeamPhys outOfBeamPos --outdir outdir\n"+\
				"\nExample usage:\n%prog -f /dls/i13/data/2012/mt5811-1/564.nxs --shutterOpenPhys 1.0 --shutterClosedPhys 0.0 --stageInBeamPhys 0.0 --stageOutOfBeamPhys 6.0\n"+\
				"or:\n%prog --filename /dls/i13/data/2012/mt5811-1/564.nxs --shutterOpenPhys 1.0 --shutterClosedPhys 0.0 --stageInBeamPhys 0.0 --stageOutOfBeamPhys 6.0 --template settingsfile"
	vers="%prog version 1.0"

	parser=OptionParser(usage=usage, description=desc, version=vers, prog=argv[0])

	parser.add_option("-f", "--filename", action="store", type="string", dest="filename", help="Path to NeXus filename to be processed.")
	parser.add_option("--template", action="store", type="string", dest="template", default="/dls_sw/i12/software/tomography_scripts/settings.xml", help="Settings filename to be used for reconstruction.")
	parser.add_option("--shutterOpenPhys", action="store", type="float", dest="shutOpenPos", default=1.0, help="The shutter's PHYSICAL position when it was OPEN during the scan.")
	parser.add_option("--shutterClosedPhys", action="store", type="float", dest="shutClosedPos", default=0.0, help="The shutter's PHYSICAL position when it was CLOSED during the scan. ")
	parser.add_option("--stageInBeamPhys", action="store", type="float", dest="inBeamPos", help="The sample stage's PHYSICAL position when it was IN-BEAM during the scan. ")
	parser.add_option("--stageOutOfBeamPhys", action="store", type="float", dest="outOfBeamPos", help="The sample stage's PHYSICAL position when it was OUT-OF-BEAM during the scan. ")
	parser.add_option("--minProjs", action="store", type="int", dest="minProjs", default=129, help="The absolute minimum number of projections; default value is %default.")
	parser.add_option("--quickstep", action="store", type="int", dest="quickstep", default=100, help="The step size to be used for GUI quick option; default value is %default.")
	parser.add_option("--maxUnclassed", action="store", type="int", dest="maxUnclassed", default=0, help="The absolute maximum number of unclassified images; default value is %default.")
	parser.add_option("-e", "--every", action="store", type="int", dest="decimationRate", default=1, help="Indicates that only every n-th projection image will be used for reconstruction; default value is %default.")
	parser.add_option("--shutterNXSPath", action="store", type="string", dest="shutNXSPath", default="/entry1/instrument/tomoScanDevice/tomography_shutter", help="The path to the location of SHUTTER's physical positions inside the input NeXus file.")
	parser.add_option("--stagePosNXSPath", action="store", type="string", dest="stagePosNXSPath", default="/entry1/instrument/tomoScanDevice/ss1_X", help="The path to the location of SAMPLE STAGE's physical positions inside the input NeXus file.")
	parser.add_option("--stageRotNXSPath", action="store", type="string", dest="stageRotNXSPath", default="/entry1/instrument/tomoScanDevice/ss1_rot", help="The path to the location of SAMPLE STAGE's physical rotations inside the input NeXus file.")
	parser.add_option("--tifNXSPath", action="store", type="string", dest="tifNXSPath", default="/entry1/instrument/pco1_hw_tif/image_data", help="The path to the location of TIFF filenames inside the input NeXus file.")
	parser.add_option("--imgkeyNXSPath", action="store", type="string", dest="imgkeyNXSPath", default="/entry1/instrument/tomoScanDevice/image_key", help="The path to the location of image-key data inside the input NeXus file.")
	parser.add_option("-o", "--outdir", action="store", type="string", dest="outdir", help="Path to folder in which directories and files are to be made. Default is current working folder")
	parser.add_option("--qsub_proj", action="store", type="string", dest="qsub_proj", default="i13", help="Name of qsub project to be used on Diamond cluster; default value is %default.")
	parser.add_option("--inImgFilenameFmt", action="store", type="string", dest="inImgFnameFmt", default="%05d.tif", help="Filename format (or mask) of TIF images; default value is %default.")
	parser.add_option("-l", "--local", action="store", type="string", dest="inLocalTomoXML", help="Path to input localTomo.xml to be used; the values from that file override any other values supplied to this script using option-argument mechanism.")
	parser.add_option("--verbose", action="store_true", dest="verbose", default=False, help="Verbose - useful for diagnosing the script")
	parser.add_option("-s", "--sino", action="store_true", dest="sino", default=False, help="If present, then the sino_listener.py script will be launched to create sinograms.")
	parser.add_option("-r", "--recon", action="store_true", dest="recon", default=False, help="If present, then the recon_arrayxml.py script will be launched to perform reconstruction.")
	parser.add_option("--quick", action="store_true", dest="quick", default=False, help="If present, then the quick directories will be created and populated for use by the reconstruction GUI.")
	parser.add_option("--dbg", action="store_true", dest="dbg", default=False, help="Debug option set to TRUE limits the number of processed images to the first 10 (useful for testing, etc.")

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
	
	inImgFilenameFmt_loc=opts.inImgFnameFmt
	stagePosNXSPath_loc=opts.stagePosNXSPath
	stageRotNXSPath_loc=opts.stageRotNXSPath
	tifNXSPath_loc=opts.tifNXSPath
	qsub_project_loc=opts.qsub_proj
	
	if not opts.inLocalTomoXML is None:
		inImgFilenameFmt_loc, stagePosNXSPath_loc, stageRotNXSPath_loc, tifNXSPath_loc, qsub_project_loc, args = getBeamlineTomoConfig(opts.inLocalTomoXML)
		
	makeLinksForNXSFile(\
					filename=opts.filename\
					, settingsfile=opts.template\
					, shutterOpenPhys=opts.shutOpenPos\
					, shutterClosedPhys=opts.shutClosedPos\
					, stageInBeamPhys=opts.inBeamPos\
					, stageOutOfBeamPhys=opts.outOfBeamPos\
					, outdir=outdir_loc\
					, minProjs=minProjs_loc\
					, quickstep=opts.quickstep\
					, maxUnclassed=maxUnclassed_loc\
					, shutterNXSPath=opts.shutNXSPath\
					, stagePosNXSPath=stagePosNXSPath_loc\
					, stageRotNXSPath=stageRotNXSPath_loc\
					, tifNXSPath=tifNXSPath_loc\
					, imgkeyNXSPath=opts.imgkeyNXSPath\
					, inImgFilenameFmt=inImgFilenameFmt_loc\
					, qsub_project=qsub_project_loc\
					, decimationRate=opts.decimationRate\
					, verbose=opts.verbose\
					, sino=opts.sino\
					, recon=opts.recon\
					, quick=opts.quick\
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

