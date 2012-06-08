import zipfile;
import re;
import os;
import cPickle as pickle;

#Setup the environment variables
from os import system;
from time import localtime;

from java.io import File;

class ZipImageProducerClass(object):

	def __init__(self, zipFileName, imageType):

#		self.zfilename  = modulePath + "/images100K.zip";
#		self.zfilename = modulePath + "images2M.zip";
		self.zfilename = zipFileName;
		self.imageType = imageType;
			
		# open the zipped file
		self.zfile = zipfile.ZipFile( self.zfilename, "r" );
		self.infolist=self.zfile.infolist();

		self.pointer = 0;

		self.pickleFileName='/tmp/simPilatusFileNumber.txt';
		self.fileNumber = 0;

		self.filePath = '/tmp/';
		self.filePrefix = 'psim_';
		self.fileNumber = 0;
		
	def setImageSource(self, zipFileName, imageType):
		self.zfilename = zipFileName;
		self.imageType = imageType;
		
	
	def setFilePath(self, newFilePath):
		"""Set file path"""
		
		if not os.path.exists(newFilePath):
			print "Path does not exist. Create new one."
			os.makedirs(newFilePath);
			
		if not os.path.isdir(newFilePath):
			print "Invalid path";
			return;
				
		#To check the path exists
		self.filePath = newFilePath;
		self.getFileNumber();
		print "Image file path set to " + self.filePath;
		
	def getFilePath(self):
		return self.filePath;
	
	def setFilePrefix(self, filePrefix):
		"""Set filename - not the path"""
		self.filePrefix = filePrefix

	def getFilePrefix(self):
		return self.filePrefix;

	def getNextImage(self, newFileName=None):
		while True:
			fullname=self.infolist[self.pointer].filename;
			if fullname.endswith('.tif'): #Now a valid tif file, maybe a folder name
				break;
			self.adjustPointer();

		data = self.zfile.read(fullname);
		self.adjustPointer();

		fname=fullname.split('/')[-1];

		# save the decompressed data to a new file
		if newFileName == None: # No file name is given, create one based on file number
			self.updateFileNumber();
			#filename = 'test_' + str(self.updateFileNumber());
			filename = os.path.join(self.filePath,  self.filePrefix + "%04.0f" % (self.fileNumber) + '.' + self.imageType);
		elif newFileName == newFileName.split('/')[-1] : # only a file name is given without the path, use the system file path
			filename = os.path.join(self.filePath, newFileName);
		else:#A full file name with path is given, just use it
			filename = newFileName;
		
		fout = open(filename, "w")
		fout.write(data)
		fout.close()
		return filename;

	def adjustPointer(self):
		self.pointer+=1;
		if self.pointer >= len(self.infolist):
			self.pointer = 0;

	def printList(self):
		# retrieve information about the zip file
		self.zfile.printdir();
		print '-'*40;
		
	def updateFileNumber(self):
		"""Restore the pickled file number for persistence"""
		self.fileNumber = self.getFileNumber();
		self.fileNumber += 1;
		self.saveFileNumber();
		return self.fileNumber;
			
	def getFileNumber(self):
		"""Restore the pickled file number for persistence"""
		try:
			inStream = file(self.pickleFileName, 'rb');
			self.fileNumber = pickle.load(inStream);
			inStream.close();
		except IOError:
			print "No previous pickled file numbers. Create new one";
			self.fileNumber = 0;
		return self.fileNumber;

	def saveFileNumber(self):
		"""Save the file number for persistence"""
		outStream = file(self.pickleFileName, 'wb');
		try:
			#Pickle the file number and dump to a file stream
			pickle.dump(self.fileNumber, outStream);
			outStream.close();
		except IOError:
			print "Can not preserve file numbers.";


#Example:
#from Diamond.Pilatus.ZipImageProducer import ZipImageProducerClass;
#a=ZipImageProducerClass('/scratch/Dev/gdaDev/gda-config/i07/scripts/Diamond/Pilatus/images100K.zip', 'tif');

#a.printList();
#a.setFilePath("/scratch/temp")
#a.setFilePrefix("p100k")

#for i in range(5):
#	print a.getNextImage();

