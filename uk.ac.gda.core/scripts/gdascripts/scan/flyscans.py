'''
A scan that moves a scannable from start position to stop position non-stop or on the fly while collecting data
    at specified step points.
    If detector exposure time is specified, scannable motor speed will be adjusted during the fly scan.

:Usage:

To perform a fly scan of scannable 'sx' over range (start, stop, step) and measure detector 'det' at each step of 'sx'
    >>>flyscan sx start stop [step] det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if step is not given, a single exposure will be collected over a single fly

To perform centred fly scan:
    >>>flyscancn sx xstep xnumberOfPoints det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if xnumberOfPoints must be greater than 1

To perform a fly scan as the inner most scan of a nested scan
    e.g. perform a scan of 'sy' over range (ystart, ystop, ystep) and at each point of 'sy' perform the above flyscan of 'sx'
    >>>fscan sy ystart ystop ystep flyscan sx start stop [step] det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if step is not given, a single exposure will be collected over a fly scan

To perform a centred fly scan as the inner most scan of a nested scan
    e.g. perform a scan of 'sy' over range (ystart, ystop, ystep) and at each point of 'sy' perform the above flyscan of 'sx'
    >>>fscan sy ystart ystop ystep flyscancn sx xstep xnumpoints det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if step is not given, a single exposure will be collected over a fly scan

To perform a centred fly scan as the inner most scan of a nested scan
    e.g. perform a scan of 'sy' over range (ystart, ystop, ystep) and at each point of 'sy' perform the above flyscan of 'sx'
    >>>fscancn sy ystep ynumpoints flyscancn sx xstep xnumpoints det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if xnumpoints and ynumpoints must be greater than 1

To perform a fly scan as the inner most scan of a nested centred scan
    e.g. perform a scan of 'sy' over range (ystart, ystop, ystep) and at each point of 'sy' perform the above flyscan of 'sx'
    >>>fscancn sy ystep ynumpoints flyscancn sx xstep xnumpoints det [exposure_time] [dead_time] [optional_scannables_to_collect_data_from]
    if xnumpoints and ynumpoints must be greater than 1

'''
from gda.device.scannable import ScannableBase, ScannableUtils
from gda.jython.commands.GeneralCommands import alias
from gda.scan import ScanPositionProvider, ScanBase
import time
import math
from gda.device import Scannable, Detector
from types import IntType, FloatType
import sys
from time import sleep
from gda.configuration.properties import LocalProperties
from gda.jython import InterfaceProvider
from gdascripts.scan.installStandardScansWithProcessing import scan, scan_processor
from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper, isObjectScannable
from gdascripts.list_operations import split_list_by_emelent
import inspect
from gdascripts.scan.gdascans import scale
from gda.device.scannable.scannablegroup import ScannableGroup
from inspect import isfunction

SHOW_DEMAND_VALUE=False

if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16":
    from pd_WaitForBeam import wait_for_injection_scan_start, wait_for_beam_scan_start

class FlyScanPosition:
    ''' define a position required by :class:FlyScannable
    '''
    def __init__(self, stop, position, step):
        self.stop = stop
        self.position = position
        self.step = step

    def __repr__(self):
        return '[' +str(self.stop)+', '+ str(self.position)+', '+ str(self.step) +']'

class   FlyScanPositionsProvider(ScanPositionProvider):
    ''' A scan position provider that provides a list of :class:FlyScanPosition
    '''
    def __init__(self, scannable, start, stop, step):
        self.scannable = scannable
        self.start = float(start)
        self.stop = float(stop)
        self.step = ScanBase.checkStartStopStep(start, stop, step);
        number_steps = ScannableUtils.getNumberSteps(scannable, self.start, self.stop, self.step)
        self.points = []
        self.points.append(start)
        previous_point = start
        for i in range(number_steps):
            next_point = ScannableUtils.calculateNextPoint(previous_point, self.step);
            self.points.append(round(next_point, 3))
            previous_point = next_point
        self.points = self.points[:-1] #prevent collect last point at stop position in flying scan as motor already stopped at this point.

    def get(self, index):
        max_index = self.size()-1
        if index > max_index:
            raise ValueError("Position %d is outside possible range : %d" % (index, max_index))
        val = self.points[index]
        stoppos = self.stop
        return FlyScanPosition(stoppos, val, self.step);

    def size(self):
        return len(self.points)

    def __str__(self):
        return "("+",".join(map(str,self.points))+")"

    def toString(self):
        return self.__str__()

class ScannableError(Exception):
    pass

class CommandError(Exception):
    """Exception raised for errors in the command input.

    Attributes:
        expr -- input expression in which the error occurred
        msg  -- explanation of the error
    """

    def __init__(self, expr, msg):
        self.expr = expr
        self.msg = msg

class FlyScannable(ScannableBase):
    """
    define a scannable that moves a given scannable from start position to stop position continuously while collecting data at each step point specified.

    This class takes a 'standard' scannable and convert it to :class:`FlyScannable`

    It takes 1 position input of type :class:`FlyScanPosition`:
        Each position it receives is a tuple with
        - the first element being the stop value to be sent to the scannable on first point in scan
        - the second element is the position of the scannable at which data is collected i.e.it reports isBusy = false
        - the third element is the step size of this scannable

    :method: `rawGetPosition` returns the demand value at which the isBusy is to return false

    :usage:
    #You must set the speed of flying motor first before using following
    from  flyscan_command import flyscannable, FlyScanPositionsProvider
    scan flyscannable(scannable) FlyScanPositionsProvider(scannable, 0, 10, 1) det [exposure_time]

    @param param: scannable - the scannable to move in fly mode
    @param param: timeout_secs - time out in seconds, default is 1.0 second
    """
    def __init__(self, scannable, timeout_secs=1.0):
        self.scannable = scannable
        if len( self.scannable.getInputNames()) != 1:
            raise ScannableError("scannable '%s must have single inputName" % (self.scannable.getName()))
        self.name = scannable.getName()+"_fly"
        self.inputNames = [scannable.getInputNames() [0]+"_actual"]
        self.extraNames= []
        self.outputFormats=[scannable.getOutputFormat()[0], scannable.getOutputFormat()[0]]
        self.level = 3
        self.positive = True
        self.requiredPosVal = 0.
        self.startVal=None
        self.stopVal=0.
        self.stepVal=0.1
        self.timeout_secs = timeout_secs
        self.lastreadPosition=0.
        self.speed=None
        self.origionalSpeed=None
        self.alreadyStarted=False
        self.moveToStartCompleted=False
        self.showDemandValue=False

    def getCurrentPositionOfScannable(self):
        return ScannableUtils.positionToArray(self.scannable.getPosition(), self.scannable)[0]

    def isBusy(self):
        if not self.scannable.isBusy():
            res = False;
        else:
            self.lastreadPosition = self.getCurrentPositionOfScannable()
            if self.positive:
                res = self.requiredPosVal > self.lastreadPosition
            else:
                res = self.requiredPosVal < self.lastreadPosition
        return res

    def waitWhileBusy(self):
        clock_at_start=time.clock()
        while self.isBusy() and (time.clock()-clock_at_start < self.timeout_secs or math.fabs((self.lastreadPosition-self.requiredPosVal)/self.stepVal)> 4):
            time.sleep(.01)

    def getScannableMaxSpeed(self):
        return self.scannable.getMotor().getMaxSpeed()

    def setSpeed(self, speed):
        self.speed=speed

    def atScanStart(self):
        self.origionalSpeed= self.scannable.getSpeed()

    def atScanLineStart(self):
        self.alreadyStarted=False
        self.moveToStart()
        if self.speed is None:
            raise RuntimeError("flying motor speed is not set")
        if self.origionalSpeed is None:
            raise RuntimeError("The original motor speed is not captured")
        if self.speed != self.origionalSpeed:
            print("change motor speed from %r to %r" % (self.origionalSpeed, self.speed))
            self.scannable.setSpeed(self.speed)

    def moveToStart(self):
        if self.startVal is not None:
            print( "move to start position %f " % (self.startVal))
            self.scannable.asynchronousMoveTo(self.startVal)
            count=0
            while self.scannable.isBusy():
                sleep(1)
                sys.stdout.write(".")
                count=count+1
                if count % 80 == 0 :
                    sys.stdout.write("\n")
            print("\n")
            print("Start position is reached after %f seconds" % (count))
            self.moveToStartCompleted=True

    def atScanLineEnd(self):
        self.restoreMotorSpeed()
        self.moveToStartCompleted = False
        self.alreadyStarted=False

    def atScanEnd(self):
        self.origionalSpeed=None
        self.speed=None
        self.alreadyStarted=False
        self.moveToStartCompleted=False

    def restoreMotorSpeed(self):
        if self.origionalSpeed is not None:
            sleep(1.0)
            print("Restore motor speed from %r to %r" % (self.speed, self.origionalSpeed))
            if self.scannable.isBusy():
                self.scannable.stop()
                sleep(2)
            try:
                self.scannable.setSpeed(self.origionalSpeed)
            except:
                print("Restore motor speed failed with Exception, try again after 5 second sleep")
                sleep(5)
                self.scannable.setSpeed(self.origionalSpeed)
                raise

    def stop(self):
        self.scannable.stop()
        self.restoreMotorSpeed()

    def atCommandFailure(self):
        self.restoreMotorSpeed()

    def moveTo(self, val):
        self.scannable.moveTo(val)

    def rawAsynchronousMoveTo(self, new_position):
        if  not  isinstance( new_position,  FlyScanPosition):
            raise TypeError("FlyScannable only supports positions of type FlyScanPosition")
        self.scannable_position = new_position.position
        self.requiredPosVal = ScannableUtils.positionToArray(self.scannable_position, self.scannable)[0]
        self.stepVal = ScannableUtils.positionToArray(new_position.step, self.scannable)[0]
        if new_position.stop is None:
            return;
        if self.alreadyStarted:
            return
        stop_position = new_position.stop
        print("Move to stop position %f" % stop_position)
        self.scannable.asynchronousMoveTo(stop_position)
        self.alreadyStarted=True
        self.stopVal = ScannableUtils.positionToArray(stop_position, self.scannable)[0]
        self.positive = self.stopVal > self.requiredPosVal

    def rawGetPosition(self):
        if self.showDemandValue:
            return [self.scannable.getPosition(), self.requiredPosVal]
        else:
            return self.scannable.getPosition()

#define a global variable for detector dead time - see I16-757
detector_dead_time = 0.0

def setflyscandeadtime(arg):
    '''set global variable 'detector_dead_time' for 'flyscan' function to use - see I16-757 for the reason
    '''
    globals()["detector_dead_time"] = float(arg)

def getflyscandeadtime():
    return globals()["detector_dead_time"]

def flyscannable(scannable, timeout_secs=1.):
    return FlyScannable(scannable, timeout_secs)

def create_fly_scannable_and_positions(newargs, arg, startpos, stoppos, stepsize):
    flyscannablewraper = FlyScannable(arg)
    flyscannablewraper.startVal = startpos
    newargs.append(flyscannablewraper)
    newargs.append(FlyScanPositionsProvider(flyscannablewraper.scannable, startpos, stoppos, stepsize))
    return newargs, flyscannablewraper

def configure_fly_scannable_extraname(arg, flyscannablewraper):
    flyscannablewraper.showDemandValue = SHOW_DEMAND_VALUE
    if SHOW_DEMAND_VALUE:
        flyscannablewraper.setExtraNames([arg.getInputNames()[0] + "_demand"])
        flyscannablewraper.setOutputFormat(flyscannablewraper.getOutputFormat())
    else:
        flyscannablewraper.setExtraNames([])
        flyscannablewraper.setOutputFormat([flyscannablewraper.getOutputFormat()[0]])

def enable_topup_check(newargs, args, total_time, det_index):
    ''' setup beamline-specific beam top up checker's minimum time threshold.
    '''
    topup_checker = None
    the_original_thresold = None
    if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16": # try to retrieve waitforinjection object from scan arguments
        fwaitforbeam  = next((x for x in args if isinstance(x, Scannable) and str(x.getName()) == "wait_for_beam_scan_start"), None)
        if fwaitforbeam is None:
            fwaitforbeam = wait_for_beam_scan_start
            if fwaitforbeam is not None :
                newargs.insert(det_index, fwaitforbeam)
        fwaitforinjection = next((x for x in args if isinstance(x, Scannable) and str(x.getName()) == "wait_for_injection_scan_start"), None)
        if fwaitforinjection is None:
            fwaitforinjection = wait_for_injection_scan_start
            if fwaitforinjection is not None and total_time < 590:
                fwaitforinjection.minimumThreshold = total_time + 5
                newargs.insert(det_index, fwaitforinjection)
    elif str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) in ["i21", "i10", "i10-1", "i06", "i06-1"]:
        check_beam = next((x for x in args if isinstance(x, ScannableGroup) and str(x.getName()) == "checkbeam"), None)
        if check_beam:
            topup_checker = check_beam.getDelegate().getGroupMember("checktopup_time")
            the_original_thresold = topup_checker.minimumThreshold
            topup_checker.minimumThreshold = total_time + 5
    return topup_checker, the_original_thresold

def parse_detector_parameters_set_flying_speed(newargs, args, i, numpoints, startpos, stoppos, flyscannablewraper):
    '''calculate and set flying scannable speed based on total detector exposure time over the flying range and set wait for injection time
    '''
    if (i + 1) < len(args) and (type(args[i + 1]) == IntType or type(args[i + 1]) == FloatType): # calculate detector total time including dead time given
        total_time = (float(args[i]) + float(args[i + 1])) * numpoints
        deadtime_index = i + 1
    elif globals()['detector_dead_time'] != 0: # I personally don't like this way adding detector dead time but see I16-757 for the reason!
        total_time = (float(args[i]) + globals()['detector_dead_time']) * numpoints
        deadtime_index = -1 # no dead time input
    else:
        total_time = float(args[i]) * numpoints # calculate detector total time without dead times
        deadtime_index = -1 # no dead time input

    motor_speed = math.fabs((float(stoppos - startpos)) / float(total_time))
    max_speed = flyscannablewraper.getScannableMaxSpeed()
    if motor_speed > 0 and motor_speed <= max_speed: #when exposure time is too large, change motor speed to roughly match
        flyscannablewraper.setSpeed(motor_speed)
    elif motor_speed > max_speed: #when exposure time is small enough use maximum speed of the motor
        flyscannablewraper.setSpeed(max_speed)
    return i, deadtime_index, newargs, total_time

def append_command_metadata_for_nexus_file(command):
    if str(LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT)) == "NexusScanDataWriter":
        from gdascripts.metadata.nexus_metadata_class import meta
        meta.addScalar("user_input", "cmd", command)
    if str(LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT)) == "NexusDataWriter":
        from gdascripts.metadata.metadata_commands import meta_add
        meta_add("cmd", command)

def clear_command_metadata_for_nexus_file():
    if str(LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT)) == "NexusScanDataWriter":
        from gdascripts.metadata.nexus_metadata_class import meta
        meta.rm("user_input", "cmd")
    if str(LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT)) == "NexusDataWriter":
        from gdascripts.metadata.metadata_commands import meta_rm
        meta_rm("cmd")

def add_command_metadata(command):
    append_command_metadata_for_nexus_file(command)
    # ASCII file header
    cmd_string = "cmd='" + str(command).strip() + "'\n"
    jython_namespace = InterfaceProvider.getJythonNamespace()
    existing_info = jython_namespace.getFromJythonNamespace("SRSWriteAtFileCreation")
    if existing_info:
        ascii_info = str(existing_info) + "\n" + cmd_string
    else:
        ascii_info = cmd_string
    jython_namespace.placeInJythonNamespace("SRSWriteAtFileCreation", ascii_info)
    return jython_namespace, existing_info

def remove_command_metadata(jython_namespace, existing_info):
    clear_command_metadata_for_nexus_file()
    jython_namespace.placeInJythonNamespace("SRSWriteAtFileCreation", existing_info)


def construct_user_command(args):
    tokens = [inspect.stack()[1][3]] # caller's function name - For Python 2 each frame record is a list. The third element in each record is the caller name.
    for arg in args:
        if isObjectScannable(arg):
            tokens.append(arg.name)
        elif isfunction(arg):
            tokens.append(arg.func_name)
        else:
            tokens.append(str(arg))
    return ' '.join(tokens)


def parse_flyscan_scannable_arguments(args, newargs):
    if not isinstance(args[0], Scannable):
        raise CommandError(args, "The first argument after 'flyscan' is not a Scannable!")
    deadtime_index = -1
    det_index = -1
    i = 0
    while i < len(args):
        arg = args[i]
        if i == 0:
            startpos = args[i + 1]
            stoppos = args[i + 2]
            if type(args[i + 3]) == IntType or type(args[i + 3]) == FloatType:
                stepsize = args[i + 3]
                i = i + 4
            else:
                stepsize = (stoppos - startpos)
                i = i + 3
            number_steps = ScannableUtils.getNumberSteps(arg, startpos, stoppos, stepsize)
            newargs, flyscannablewraper = create_fly_scannable_and_positions(newargs, arg, startpos, stoppos, stepsize)
            configure_fly_scannable_extraname(arg, flyscannablewraper)
        else:
            if i != deadtime_index:
                newargs.append(arg)
            i = i + 1
            if isinstance(arg, Detector) and i < len(args) and (type(args[i]) == IntType or type(args[i]) == FloatType):
                det_index = len(newargs) - 1
                i, deadtime_index, newargs, total_time = parse_detector_parameters_set_flying_speed(newargs, args, i, number_steps, startpos, stoppos, flyscannablewraper)

    topup_checker, the_original_threshold = enable_topup_check(newargs, args, total_time, det_index)
    return newargs, topup_checker, the_original_threshold


def parse_flyscancn_scannable_arguments(args, newargs):
    if not isinstance(args[0], Scannable):
        raise CommandError(args, "The first argument after 'flyscancn' is not a Scannable!")
    deadtime_index = -1 # signify no dead time input
    det_index = -1
    i = 0
    while i < len(args):
        arg = args[i]
        if i == 0:
            current_position = float(arg.getPosition())
            stepsize = float(args[i + 1])
            numpoints = float(args[i + 2])
            if numpoints <= 1:
                raise ValueError("Number of points must be greater than 1!")
            intervals = float(numpoints - 1)
            halfwidth = stepsize * intervals / 2.
            neg_halfwidth = stepsize * (-intervals / 2.)
            startpos = current_position + neg_halfwidth
            stoppos = current_position + halfwidth
            newargs, flyscannablewraper = create_fly_scannable_and_positions(newargs, arg, startpos, stoppos + stepsize, stepsize) #add a step to end so num_points is correct
            configure_fly_scannable_extraname(arg, flyscannablewraper)
            i = i + 3
        else:
            if i != deadtime_index: #skip dead time input
                newargs.append(arg)
            i = i + 1
            if isinstance(arg, Detector) and i < len(args) and (type(args[i]) == IntType or type(args[i]) == FloatType):
                det_index = len(newargs) - 1
                i, deadtime_index, newargs, total_time = parse_detector_parameters_set_flying_speed(newargs, args, i, numpoints, startpos, stoppos, flyscannablewraper)

    topup_checker, the_original_threshold = enable_topup_check(newargs, args, total_time, det_index)
    return newargs, flyscannablewraper, current_position, topup_checker, the_original_threshold


def flyscan(*args):
    ''' A scan that moves a scannable from start position to stop position non-stop or on the fly while collecting data
    at specified step points.

    :usage:
        flyscan scannable start stop [step] det [exposure_time] [dead_time] [other_scannables]
    '''
    if len(args) < 3:
        raise CommandError(args, "Not enough parameters provided: You must provide 'scannable start stop [step]' after 'flyscan' command")

    newargs=[]
    newargs, topup_checker, the_original_threshold = parse_flyscan_scannable_arguments(args, newargs)

    command = construct_user_command(args)
    if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16":
        jython_namespace, existing_info = add_command_metadata(command)
    else:
        append_command_metadata_for_nexus_file(command)
    try:
        scan([e for e in newargs])
    finally:
        if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16":
            remove_command_metadata(jython_namespace, existing_info)
        else:
            clear_command_metadata_for_nexus_file()
            if topup_checker:
                topup_checker.minimumThreshold = the_original_threshold

alias('flyscan')

def flyscancn(*args):
    '''
    USAGE:

    flyscancn scn stepsize numpoints det [exposure_time] [dead_time] [other_scannables]

    Performs a scan with specified stepsize and numpoints centred on the current scn position, and returns to original position.
    scn scannable is moving continuously from start position to stop position non-stop or on the fly while collecting data
    at specified step points.
    '''
    if len(args) < 3:
        raise CommandError(args, "Not enough parameters provided: You must provide '<scannable> <step_size> <number_of_points>' after 'flyscancn' command")

    newargs=[]
    newargs, flyscannablewraper, current_position, topup_checker, the_original_threshold = parse_flyscancn_scannable_arguments(args, newargs)

    command = construct_user_command(args)
    if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16":
        jython_namespace, existing_info = add_command_metadata(command)
    else:
        append_command_metadata_for_nexus_file(command)
    try:
        scan([e for e in newargs])
    finally:
        if str(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)) == "i16":
            remove_command_metadata(jython_namespace, existing_info)
        else:
            clear_command_metadata_for_nexus_file()
            if topup_checker:
                topup_checker.minimumThreshold = the_original_threshold
        # return to original position
        flyscannablewraper.scannable.moveTo(current_position)

alias('flyscancn')

class Fscan(ConcurrentScanWrapper):
    """USAGE:

  fscan scn1 start stop step [scnN [start [stop [step]]]] ... flyscan fscn fstart fstop [fstep] detector [exposure_time] [dead_time]

  Perform a nested fly scan! if fstep is not given, only a single image is collected per fly scan.

  or

  fscan scn1 start stop step [scnN [start [stop [step]]]] ... flyscancn fscn fstep numpoints detector [exposure_time] [dead_time]

  Perform a nested centred fly scan!

  e.g.: fscan x 1 2 1 flyscan z 1 10 1 pil 0.5 0.2               --> loop x, for each step do a fly scan of z and returns 10 images for each fly scan
        fscan x 1 2 1 flyscan z 1.0 1.2 pil 0.5 0.2              --> loop x, for each step do a fly scan of z and return single image per fly scan
        fscan x 1 2 1 flyscancn z 0.1 10 pil 0.5 0.2             --> loop x, for each step do a centred fly scan of z and returns 10 images for each fly scan

        Use scan.yaxis = 'axis_name' to determine which axis will be analysed and plotted by default.

  See also scan."""

    def __init__(self, scanListeners = None):
        ConcurrentScanWrapper.__init__(self, False, False, scanListeners)

    def convertArgStruct(self, argStruct):
        return argStruct

    def parseArgsIntoArgStruct(self, args):
        if list(args).count(flyscan) == 1:
            args_before, fly_args = split_list_by_emelent(list(args), flyscan)
            return ConcurrentScanWrapper.parseArgsIntoArgStruct(self, parse_flyscan_scannable_arguments(fly_args[1:], args_before)[0])
        elif list(args).count(flyscancn) == 1:
            args_before, fly_args = split_list_by_emelent(list(args), flyscancn)
            return ConcurrentScanWrapper.parseArgsIntoArgStruct(self, parse_flyscancn_scannable_arguments(fly_args[1:], args_before)[0])
        elif list(args).count(flyscan) == 1 and list(args).count(flyscancn) == 1:
            raise CommandError(args, "Input command cannot contain both flyscan and flyscancn function objects")
        else:
            raise CommandError(args, "Input command doesn't contain exactly one flyscan or flyscancn function object")

    def _constructUserCommand(self, args):
        tokens = [self.__class__.__name__.lower()] # get the scan name
        for arg in args:
            if isObjectScannable(arg):
                tokens.append(arg.name)
            elif isfunction(arg):
                tokens.append(arg.func_name)
            else:
                tokens.append(str(arg))
        return ' '.join(tokens)

    def _appendStringToSRSFileHeader(self, s):
        append_command_metadata_for_nexus_file(s)
        ConcurrentScanWrapper._appendStringToSRSFileHeader(self, s)

    def _clearStringToSRSFileHeader(self):
        clear_command_metadata_for_nexus_file()
        ConcurrentScanWrapper._clearStringToSRSFileHeader(self)

fscan = Fscan([scan_processor])
alias("fscan")

class Fscancn(Fscan):
    """USAGE:

  fscancn scn1 stepsize numpoints [scnN [start [stop [step]]]] ... flyscancn fscn fstep num_points detector [exposure_time] [dead_time]

  Performs a scan with specified stepsize and numpoints centred on the current scn1 position, and at each outer point do a nested centred fly scan and returns to original position.

  or

  fscancn scn1 stepsize numpoints [scnN [start [stop [step]]]] ... flyscan fscn fstart fstop [fstep] detector [exposure_time] [dead_time]

  Performs a scan with specified stepsize and numpoints centred on the current scn1 position, and at each outer point do a nested fly scan and returns to original position.

  e.g.: fscancn x 0.1 2 flyscancn z 1 10 pil 0.5 0.2     --> loop x, for each step do a centred fly scan of z and returns 10 images for each fly scan
        fscancn x 0.1 2 flyscan z 1 1.2 pil 0.5 0.2      --> loop x, for each step do a fly scan of z and return single image per fly scan
        fscancn x 0.1 2 flyscan z 1 10 1 pil 0.5 0.2     --> loop x, for each x loop y, for each (x,y) step do a fly scan of z and returns 10 images for each fly scan

        Use scan.yaxis = 'axis_name' to determine which axis will be analysed and plotted by default.

  See also scan."""

    def __init__(self, scanListeners = None):
        ConcurrentScanWrapper.__init__(self, True, True, scanListeners)

    def convertArgStruct(self, argStruct):
        motor, stepsize, numpoints = argStruct.pop(0)

        intervals = float(numpoints - 1)
        halfwidth = scale(stepsize, intervals/2.)
        neg_halfwidth = scale(stepsize, -intervals/2.)

        result = [ [motor, neg_halfwidth, halfwidth, stepsize] ]
        while len(argStruct) > 0:
            result.append(argStruct.pop(0))
        return result

    def parseArgsIntoArgStruct(self, args):
        if list(args).count(flyscan) == 1:
            args_before, fly_args = split_list_by_emelent(list(args), flyscan)
            return ConcurrentScanWrapper.parseArgsIntoArgStruct(self, parse_flyscan_scannable_arguments(fly_args[1:], args_before)[0])
        elif list(args).count(flyscancn) == 1:
            args_before, fly_args = split_list_by_emelent(list(args), flyscancn)
            return ConcurrentScanWrapper.parseArgsIntoArgStruct(self, parse_flyscancn_scannable_arguments(fly_args[1:], args_before)[0])
        elif list(args).count(flyscan) == 1 and list(args).count(flyscancn) == 1:
            raise CommandError(args, "Input command cannot contain both flyscan and flyscancn function objects")
        else:
            raise CommandError(args, "Input command doesn't contain exactly one flyscan or flyscancn function object")

fscancn = Fscancn([scan_processor])
alias("fscancn")
