# For various operations to succeed (such as taking a snapshot to set up processing), there must be an image
# in the appropriate detector's array. This script can be called at startup to do this.

from gda.epics import CAClient
from __builtin__ import None

def caput(pv, val):
	CAClient.put(pv, val)

def caget(pv):
	return CAClient.get(pv)

def add_colon_if_necessary(pv):
	if pv.endswith(':'):
		return pv
	return pv + ':'

def initialise_detector(detector_name, ad_base_pv, ndarray_base_pv, trigger_mode, image_mode = None):
	"""
	Parameters:
		detector_name: name of detector to use in status and error messages
		addetector: addetector object for the detector
		trigger_mode: value of the trigger mode required to acquire data in this way. Normally Software or Internal.
		image_mode: value of the image mode required to acquire data in this way (if available). Normally Single or Fixed.
					Defaults to None, in which case NumImages is used to ensure one image is taken
	"""
	ad_base_pv_norm = add_colon_if_necessary(ad_base_pv)
	ndarray_base_pv_norm = add_colon_if_necessary(ndarray_base_pv)

	try:
		# Do nothing if the detector is already acquiring
		if caget(ad_base_pv_norm + 'Acquire_RBV') == 'Acquiring':
			print('Detector {} is already acquiring: no initialisation required'.format(detector_name))
			return

		# Enable callbacks in the array plugin, then acquire a single frame
		print('Initialising array plugin for {}: ad_base_pv = {} ndarray_base_pv = {}'.format(detector_name, ad_base_pv_norm, ndarray_base_pv_norm))
		caput(ndarray_base_pv_norm + "EnableCallbacks", "Enable")

		# set trigger mode to software
		prev_trigger_mode = caget(ad_base_pv_norm + "TriggerMode")
		if (prev_trigger_mode != trigger_mode):
			caput(ad_base_pv_norm + "TriggerMode", trigger_mode)

		# Ensure that a single image is taken
		# For detectors that have an "image mode", use this; otherwise, set the number of images to 1
		if image_mode is None:
			print('Using NumImages to take one image')
			prev_num_images = caget(ad_base_pv_norm + "NumImages_RBV")
			if (prev_num_images != 1):
				caput(ad_base_pv_norm + "NumImages", 1)
			
			# acquire single frame
			caput(ad_base_pv_norm + "Acquire", "Acquire")
			
			# restore number of images if changed
			if (prev_num_images != 1):
				caput(ad_base_pv_norm + "NumImages", prev_num_images)		
		
		else:
			print('Using ImageMode to take one image')
			prev_img_mode = caget(ad_base_pv_norm + "ImageMode")
			if (prev_img_mode != image_mode):
				caput(ad_base_pv_norm + "ImageMode", image_mode)
	
			# acquire single frame
			caput(ad_base_pv_norm + "Acquire", "Acquire")
			
			# restore image mode if changed
			if (prev_img_mode != image_mode):
				caput(ad_base_pv_norm + "ImageMode", prev_img_mode)

		# restore trigger mode if changed
		if (prev_trigger_mode != trigger_mode):
			caput(ad_base_pv_norm + "TriggerMode", prev_trigger_mode)

	except:
		print('Error initialising detector {}'.format(detector_name))
