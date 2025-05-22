'''
'miscan' - a wrapper scan that collects multiple exposures or images at each scan data point. It extends the standard 'scan' syntax 
to support the input of number of images or exposures per data point as second input parameter after detector exposure time.

It records both 'miscan' command as well as the actual standard 'scan' command in the data file.

When 'pimte' or 'pixis' is used as detector, the scan will collection multiple images per scan data point in the data file.
When 'pimte_summed' or 'pixis_summed' detector is used, the scan will collect a single summed image per scan data point in the data file.

Created on 31 Jan 2017
updated on 09 March 2022

@author: fy65
'''
import time
from gda.device.detector import NXDetector
from types import TupleType, ListType, FloatType, IntType, StringType
from gda.device.scannable import DummyScannable
from gda.device import Scannable
from gda.device.scannable.scannablegroup import ScannableGroup
from gda.jython.commands.ScannableCommands import scan
from gdascripts.metadata.nexus_metadata_class import meta

print("-"*100)
print("Creating 'miscan' - multiple images per scan data point")
print("    Syntax: miscan (scannable1, scannable2) ([1,2], [3,4],[5,6]) pixis_summed 10 0.1")

PRINTTIME = False
dummyScannable = DummyScannable("dummyScannable")
from org.slf4j import LoggerFactory
logger = LoggerFactory.getLogger("scan.miscan")


def all_elements_are_scannable(arg):
    for each in arg:
        if not isinstance(each, Scannable):
            return False
    return True


def all_elements_are_list_of_number(arg):
    for each in arg:
        if not type(each) == ListType:
            return False
        for item in each:
            if not (type(item) == FloatType or type(item) == IntType):
                return False
    return True


def all_elements_are_list_of_number_or_string(arg):
    for each in arg:
        if type(each) != ListType:
            return False
        for item in each:
            if not (type(item) == FloatType or type(item) == IntType or type(item) == StringType):
                return False
    return True


def all_elements_are_number(arg):
    for each in arg:
        if not (type(each) == FloatType or type(each) == IntType):
            return False
    return True


def all_elements_are_string(arg):
    for each in arg:
        if type(each) != StringType:
            return False
    return True


def all_elements_are_tuples_of_numbers(arg):
    for each in arg:
        # Check its a tuple or Check all elements of the tuple are numbers
        if type(each) != TupleType or not all_elements_are_number(each):
            return False
    return True


def get_adbase(collection_strategy):
    from gda.device.detector.addetector.collectionstrategy import ContinuousAcquisition, EpicsStartStop, SoftwareStartStop
    if isinstance(collection_strategy, (SoftwareStartStop, ContinuousAcquisition, EpicsStartStop)):
        return collection_strategy.getAdBase()
    else:
        return get_adbase(collection_strategy.getDecoratee())


def get_image_mode_decorator(collection_strategy, detector_name):
    from gda.device.detector.addetector.collectionstrategy import ImageModeDecorator
    if isinstance(collection_strategy, ImageModeDecorator):
        return collection_strategy
    else:
        try:
            return get_image_mode_decorator(collection_strategy.getDecoratee(), detector_name)
        except AttributeError as e:
            logger.info("There is no image_mode_decorator in the collection strategy of detector '%s'" % detector_name, e)
            return None


def get_auto_summing_proc_decorator(collection_strategy, detector_name):
    from gda.device.detector.addetector.collectionstrategy import AutoSummingProcessDecorator
    if isinstance(collection_strategy, AutoSummingProcessDecorator):
        return collection_strategy
    else:
        try:
            return get_auto_summing_proc_decorator(collection_strategy.getDecoratee(), detector_name)
        except AttributeError as e:
            logger.info("There is no auto_summing_proc_dectorator in the collection strategy of detector '%s'" % detector_name, e)
            return None


def parse_tuple_arguments(command, newargs, arg):
    command += "("
    if all_elements_are_scannable(arg):  # parsing (scannable1, scannable2,...) as scannable group
        scannable_group = ScannableGroup()
        scannable_names = []
        for each in arg:
            scannable_group.addGroupMember(each)
            scannable_names.append(each.getName())

        command += ",".join(scannable_names)
        scannable_group.setName("pathgroup")
        newargs.append(scannable_group)
    elif all_elements_are_list_of_number_or_string(arg):  # parsing scannable group's position lists
        newargs.append(arg)
        list_of_lists = []
        for each in arg:
            list_of_lists.append("[" + ",".join([str(x) for x in each]) + "]")

        command += ",".join(list_of_lists)
    elif all_elements_are_number(arg):  # parsing scannable group's position lists
        newargs.append(arg)
        command += ",".join([str(x) for x in arg])
    elif all_elements_are_tuples_of_numbers(arg):  # This case is to fix BLIX-206 when using a scannable group with a tuple of tuples of positions
        newargs.append(arg)
        list_of_tuples = []
        for each in arg:
            list_of_tuples.append("(" + ",".join([str(x) for x in each]) + ")")

        command += ",".join(list_of_tuples)
    elif all_elements_are_string(arg):
        newargs.append(arg)
        command += ",".join(arg)
    else:
        raise TypeError, "Only tuple of scannables, tuple of numbers, tuple of lists of numbers or Strings, list of numbers, or tuple of Strings are supported."
    command += ") "
    return command, newargs


def save_detector_settings_before_scan(arg):
    adbase = get_adbase(arg.getCollectionStrategy())
    acquite_time = adbase.getAcquireTime_RBV()
    image_mode = adbase.getImageMode()
    num_images = adbase.getNumImages()
    return adbase, image_mode, num_images, acquite_time


def restore_detector_setting_after_scan(adbase, image_mode, num_images, acquire_time):
    adbase.setAcquireTime(acquire_time)
    adbase.setImageMode(image_mode)
    adbase.setNumImages(num_images)


def set_number_of_images_to_collect_per_scan_data_point(command, newargs, args, i, image_mode_decorator):
    if i < len(args) - 1:  # more than 2 arguments following detector
        if isinstance(args[i], int) and isinstance(args[i + 1], (int, float)):
            image_mode_decorator.setImageMode(1)  # this will make sure metadata in detector setting are correct as image_mode_decorator setting comes after metadata are collected
            image_mode_decorator.setNumberOfImagesPerCollection(args[i])  # support the miscan command - first input after detector is number of images per data point
        elif isinstance(args[i], float) and isinstance(args[i + 1], (int, float)):
            raise TypeError("Number of images to collect per scan data point must be int type.")
        elif isinstance(args[i], float) and not isinstance(args[i + 1], (int, float)):
            image_mode_decorator.setImageMode(0)  # single image mode
            image_mode_decorator.setNumberOfImagesPerCollection(1)
    elif i == len(args) - 1:  # followed by only one argument - must be exposure time
        image_mode_decorator.setImageMode(0)  # single image mode
        image_mode_decorator.setNumberOfImagesPerCollection(1)
        newargs.append(args[i])
    command += str(args[i]) + " "
    return i, command, newargs


def set_acquisition_time(command, newargs, args, i, auto_summing_proc_decorator):
    if i < len(args) - 1:
        if isinstance(args[i], int) and isinstance(args[i + 1], (int, float)):
            auto_summing_proc_decorator.setAcquireTime(args[i + 1])  # the actual detector exposure time in EPICS
            newargs.append(round(args[i] * args[i + 1], 3))  # exposure time for auto_summing_proc_decorator in GDA
            command += str(args[i]) + " " + str(args[i + 1]) + " "
            i = i + 1
        elif isinstance(args[i], float) and isinstance(args[i + 1], (int, float)):
            raise TypeError("Number of image to collect per scan data point must be int type.")
        else:
            raise SyntaxError("parameters after detector must be in the order of number of images first, followed by exposure time!")
    elif i == len(args) - 1:
        auto_summing_proc_decorator.setAcquireTime(args[i])
        newargs.append(args[i])
        command += str(args[i]) + " "
    return i, command, newargs


def parse_detector_arguments(command, newargs, args, i, arg):
    auto_summing_proc_decorator = get_auto_summing_proc_decorator(arg.getCollectionStrategy(), arg.getName())
    if auto_summing_proc_decorator is not None:
        # handle EPICS PROC summed detector
        i, command, newargs = set_acquisition_time(command, newargs, args, i, auto_summing_proc_decorator)
    else:
        image_mode_decorator = get_image_mode_decorator(arg.getCollectionStrategy(), arg.getName())
        if image_mode_decorator is not None:
            i, command, newargs = set_number_of_images_to_collect_per_scan_data_point(command, newargs, args, i, image_mode_decorator)
        else:
            newargs.append(args[i])  # single image per data point
            command += str(args[i]) + " "  # exposure time is the last one in the scan command
    return i, command, newargs


def parse_other_arguments(command, arg):
    if isinstance(arg, Scannable):
        command += arg.getName() + " "
    if type(arg) == IntType or type(arg) == FloatType:
        command += str(arg) + " "
    return command


def miscan(*args):
    '''   a more generalised scan that extends standard GDA scan syntax to support 
        1. scannable tuple (e.g. (s1,s2,...) argument) as scannable group, 
        2. its corresponding path tuple (e.g. list of position tuples), if exist, and
        3. area detector that takes 2 input numbers - 1st parameter is the number of images to be collected at each point, 
           if omitted default to 1, and the 2nd parameter is detector exposure time which must be provided.
        4. syntax 'miscan pixis_summed 10 0.1 ...' is supported for collecting 10 images at a single point.
    
        It parses input parameters described above before delegating to the standard GDA scan to do the actual data collection.
        Thus it can be used anywhere the standard GDA 'scan' is used.
    '''
    command = "miscan "  # rebuild the input command as String so it can be recored into data file

    starttime = time.ctime()
    start = time.time()
    if PRINTTIME: print("=== Scan started: " + starttime)
    newargs = []
    i = 0;
    while i < len(args):
        arg = args[i]
        if i == 0 and isinstance(arg, NXDetector):
            newargs.append(dummyScannable)
            newargs.append(0)
            newargs.append(0)
            newargs.append(1)
            command += str(arg.getName()) + " "
            newargs.append(arg)
        elif type(arg) == TupleType:
            command, newargs = parse_tuple_arguments(command, newargs, arg)
        else:
            newargs.append(arg)
            command = parse_other_arguments(command, arg)
        i = i + 1
        CACHE_PARAMETER_TOBE_CHANGED = False
        if isinstance(arg, NXDetector):
            adbase, image_mode, num_images, acquire_time = save_detector_settings_before_scan(arg)
            if all((adbase, image_mode, num_images, acquire_time)):
                CACHE_PARAMETER_TOBE_CHANGED = True
            i, command, newargs = parse_detector_arguments(command, newargs, args, i, arg)
            i = i + 1

    meta.addScalar("user_input", "command", command)
    try:
        scan([e for e in newargs])
    finally:
        if CACHE_PARAMETER_TOBE_CHANGED:
            restore_detector_setting_after_scan(adbase, image_mode, num_images, acquire_time)

        meta.rm("user_input", "command")

    if PRINTTIME: print("=== Scan ended: " + time.ctime() + ". Elapsed time: %.0f seconds" % (time.time() - start))


from gda.jython.commands.GeneralCommands import alias
alias("miscan")
