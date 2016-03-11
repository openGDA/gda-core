from gdascripts.analysis.io.dataLoaders import loadImageIntoSFH
from org.slf4j import LoggerFactory
import os, sys, traceback
import threading
import time

SLOW_FILE_WARNING_PERIOD = 5

def tryToLoadDataset(path, iFileLoader):
	# Return None if the dataset could not be loaded for any reason
	logger = LoggerFactory.getLogger("DatasetProvider.tryToLoadDataset")

	if not os.path.exists(path):
		logger.info('Path {} does not exist, returning None', path)
		return None

	try:
		dataset = loadImageIntoSFH(path, iFileLoader)[0] # a dataset
		if not len(dataset.shape) == 2:
			logger.error('Expected 2 dimensions but found %r when sanity checking image %r using loader %r' % (dataset.shape, path, iFileLoader))
			print "*" * 80
			print "DatasetProvider.tryToLoadDataset got a dataset with ", dataset.shape, " dimensions."
			print "The analysis code will try again to load the image, and unless it times out everything is *OKAY*"
			print "Please call DASC support to report this (tricky to track down) bug"
			print "*" * 80
			return None
		logger.debug('Returning dataset {} from path {}', dataset, path)
		return dataset
	except:
		logger.error('Error loading or sanity checking image %r using loader %r :\n %s' % (path, iFileLoader, ''.join(traceback.format_exception(*sys.exc_info()))))
		return None


class DataSetProvider(object):

	def getDataset(self, retryUntilTimeout = True):
		raise RuntimeError("Not implemented")


class BasicDataSetProvider(DataSetProvider):
		
		def __init__(self, dataset):
			self.dataset = dataset
		
		def getDataset(self, retryUntilTimeout = True):
			return self.dataset


class LazyDataSetProvider(DataSetProvider):
	'''
	Provides a dataset which is loaded only when needed and then cached for future calls.
	If a fileLoadTimeout is specified the LazyDataSetProvider will keep trying to load the
	file until it succeeds or the timout is reached. This designed for cases where it takes
	a while either for the file to appear on the filesystem or it to be complete at least
	enougth to be readable without exception.  
	'''

	def __init__(self, path, iFileLoader=None, fileLoadTimout=None, printNfsTimes=False, wait_for_exposure_callable=None):
		self.logger = LoggerFactory.getLogger("LazyDataSetProvider:%s" % path)
		self.path = path
		self.iFileLoader = iFileLoader
		self.fileLoadTimout = fileLoadTimout
		self.printNfsTimes = printNfsTimes
		self.wait_for_exposure_callable = wait_for_exposure_callable
		
		self.configureLock = threading.Lock()
		self.dataset = None

	def getDataset(self, retryUntilTimeout = True):
		self.configure(retryUntilTimeout)
		self.logger.debug("getDataset(): dataset {}", self.dataset)
		return self.dataset

	def configure(self, retryUntilTimeout = True):
		if self.wait_for_exposure_callable:
			# Wait for exposure before trying to load file
			self.wait_for_exposure_callable.call()
		self.configureLock.acquire()
		try:	
			if self.dataset is None:
				self.__load(retryUntilTimeout)
		finally:
			self.configureLock.release()

	def __load(self, retryUntilTimeout = True):
		if retryUntilTimeout == False or self.fileLoadTimout == None:
			self.dataset = loadImageIntoSFH(self.path, self.iFileLoader)[0]
		else:
			self.dataset = self.__keepTryingToLoadDataset()
	
	
	def __keepTryingToLoadDataset(self):
		firstTryTime = time.clock()
		nextWarnTime = firstTryTime + SLOW_FILE_WARNING_PERIOD
		dataset = tryToLoadDataset(self.path, self.iFileLoader)
		
		if dataset is not None:
			self.logger.debug("__keepTryingToLoadDataset(): dataset {}", dataset)
			return dataset
		
		# Keep trying
		while dataset is None:
			if time.clock() > nextWarnTime:
				print "Waiting for file %s, %i/%is" % (self.path, nextWarnTime - firstTryTime, self.fileLoadTimout)
				self.logger.info("Waiting for file {}, {}/{}", self.path, nextWarnTime - firstTryTime, self.fileLoadTimout)
				nextWarnTime = time.clock() + SLOW_FILE_WARNING_PERIOD
			# pause() # Removed as this occasionaly interrupted for a reason I failed to trace (for a month!)
			if time.clock() - firstTryTime > self.fileLoadTimout:
				print "Could not load file %s, within specified timeout of %f s" % (self.path, self.fileLoadTimout)
				raise IOError("Could not load file '%s', within specified timeout of %f s" % (self.path, self.fileLoadTimout))
			time.sleep(0.1)
			dataset = tryToLoadDataset(self.path, self.iFileLoader)
		if self.printNfsTimes:
			print "NOTE: It took %fs for the file '%s' to cross NFS." % (time.clock() - firstTryTime, self.path)
		return dataset # a dataset
