GDA 7 and earlier
=================

Release_7_4_0 29/05/2008 (tag release_7_4_0 created)
----------------------------------------------------

 - #1587   I24 branch from 7.4
 - #1586   update MarCCD classes to work with marccd v0.16c
 - #1583   update information About.html
 - #1577   Format of "ls" and "pos" command
 - #1576   fit_gaussians.py does not run as framework should allow[I24, 2 days]
 - #1571   No list command anymore
 - #1570   update gaussian to use some more normal parameters
 - #1569   Enhancements to Actor Sample Changer and related GUI
 - #1566   get is not making sure the memory location is in bounds
 - #1565   Version number in the 7.4 branch still reads 7.3.0.9718
 - #1563   Add a add point method to the dataset class
 - #1556   Make CurentScanHolder a Findable object created by the XML
 - #1551   Filter the update call from the PlotManager so that not all dataVectorPlot panels get updated. [ required for #736]
 - #1550   Graphs not providing usefull axis[I22 #902]
 - #1549   explain severity levels
 - #1546   add pseudo-code describing how scans work to user manual
 - #1542   Make EpicsControlPoint work with phaseII interface
 - #1540   duplicated "Scan complete." message at end of time scan
 - #1539   Enhance the sample-viewing camera GUI calibration
 - #1538   "pos I0" does not return detector value anymore
 - #1537   extends tscan to collect data from more than one detector at the same [I11, #1156]
 - #1536   Change in how ISPyB stores and uses visits
 - #1533   tscan does not working anymore [I11, #1136]
 - #1532   Can not scan ID gap [I11,#1119]
 - #1531   simpleplot always saveas to user directory.
 - #1530   inform users of incoming port connections to the JythonServer
 - #1528   OEMOVE no longer updates when motors are moved outside of GDA
 - #1527   ISPyBTools timedout when waiting for response to create a data collection.
 - #1526   constants in odd units in constants.py
 - #1525   Planck spelling wrong in constants.py
 - #1523   help message not printing out
 - #1520   improve the help system inside the Jython environment
 - #1518   rename 'list' command in extended syntax to 'ls' [i16, 1 hour]
 - #1517   Jython print command eats up spaces
 - #1516   DOFAdapter broken
 - #1515   inc command broken on devices with extra inputs [i16]
 - #1512   q-cal tab printing (NCD) (i22#1069, 1 days)
 - #1506   make watch window (JythonTerminal) reusable elsewhere on the GUI [i22 #1068, 1 day]
 - #1505   allow for non-q-equidistant calibrant peaks in NCD Q-cal [i22 #1069 #1071, 2 day]
 - #1502   create component class and interface to allow an object to automatically adjust beween user and hardware units [3days]
 - #1491   Drain current readout from PEEM [#1054, 2]
 - #1490   add shutter button for photon shutter to NCD gui [i22 #776, 2 days]
 - #1487   extending GDA plot to provide user the choice to select the type of line to plot [#1037, 1day]
 - #1485   On i16, Upgrade gda 7.0->7.2, test and begin code reorganisation [i16#1031, 3days]
 - #1478   Modify gdascripts.pd.time_pds.showtimeClass so it can be scanned over
 - #1474   getMCData() method for Xspress interface only accepts integer values for collection time in seconds. [i18#937, 1day]
 - #1472   ADSC interface library not reporting state correctly
 - #1470   extends Scan to support Monitor type object internally to GDA
 - #1469   make DataSet sending accross corba Asyncrous
 - #1468   extends BeamMonitor to support other type of GDA detector or GDA monitor
 - #1467   make BeamMonitor configurable from XML file
 - #1466   merge DummyDAServer and DAServerDummy and remove DummyMemory
 - #1465   provide scripting template for scannable, detector, and monitor [I11, 1day] [I16, 1day]
 - #1464   develop a reserved GDA words class to provide protection to GDA functions that are exposed as jython commands
 - #1463   adding a beam monitor screw up the data plotting during a scan
 - #1462   GDA current plot does not work for all scan commands
 - #1461   documentation required on how to use the BeamMonitor or what it does
 - #1457   extend functions to SacnDataPoint to handle Monitor object as Monitor, not scannable with in it, just like it handles detector.
 - #1452   help command does not work on deveTrunk
 - #1447   Add getTolerance() method to MotionScannable [blocks 1166, 1167, 1369]
 - #1444   Consider adding a new gda milestone (8.0) for major breaking changes
 - #1441   selecting different NCD detector for WAXS or SAXS cuts the da.server connection of the old one
 - #1440   improve plot legend or choice box to the right to display the color of the coresponding line and show scan number.
 - #1439   change choice panel display to the right of the plot to show detector or monitor names
 - #1438   MX: DNA ES won't run if Mosflm log dir doesn't exist
 - #1437   Autoscale not set automaticaly in simpleplot anymore[I11, 1 day]
 - #1436   Add "New" button to JythonEditor to create a fresh script
 - #1435   add support for new TFG2 timer
 - #1427   remove limit violation status in EpicsMotor
 - #1418   for Scannables which represent a single number the name is returned twice
 - #1416   Add upos and uinc commands that show the motor progress [b16, i16 and user request]
 - #1413   add a new method to EpicsMotor to allow users to provide their only move callback listener
 - #1412   add a new method to EpicsMotor to allow user input move timeout
 - #1411   Make EpicsMotor monitoring motor DMOV in EPICS server but not chnaging GDA motor status
 - #1409   remove the trailing space after value data from "pos" command.
 - #1408   revert changes in EpicsController on caput(channel, value) and add caputWait(channel, value) for truely synchronous call to EPICS.
 - #1407   cannot add GUI tab to existing dock
 - #1405   make ncddetectors work in scans
 - #1403   Refactor the Visualisation Server/Client interactions
 - #1402   Correct the Data Vector panels autoscaling and updating functionality.
 - #1399   integrate plotting into the scan command[I11, 5 Days]
 - #1389   Changes to DDH to store data in per scan dirs
 - #1385   rename Scannable.GroupScan methods as they are misleading
 - #1384   JythonEditor does not report write failure on save
 - #1382   GUILabel for simple labels that observe state
 - #1381   make MX image streaming object generic
 - #1378   Repair crazy behaviour in SimplePlot: two buttons with same name are created at startup and then one later removed. [I16 I22, 3 days]
 - #1375   Errors when Jython commands are followed by comments (#)
 - #1374   2d scan with pseudo-device detector only samples detector at begining of each line [i16 trac #901]
 - #1372   pos() command not working (when called with brackets)
 - #1368   Use current scan number as plot title [i16 request 714]
 - #1367   Include scan command as metadata [i16 user request #726]
 - #1366   Remove warnings from the Analysis Code
 - #1364   GDA needs to be able to store results from a scan in ISPyB
 - #1359   data display requires wire-mash plotting or stacked 1-D data display[I11 I22, 5 days]
 - #1353   implement CAPUT CALLBACK remove DMOV lisenter
 - #1342   improve Q calibration in ncd
 - #1312   Client's terminal occasionaly does not display parts of lines and maybe whole groups of lines [multi-beamline complaint]
 - #1310   Improvement of analysis usability.
 - #1277   MX sample changer interface upgrade [MX, 5 days]
 - #1235   Full JUnit Testing of the DataSet Class
 - #1228   Tabs are not copied when in the Jython Editor in windows
 - #1223   add limit checking and offsets (dial values) to ScannableMotion interface and base class [I16request, 5days]
 - #1222   Communication instability between Rigaku sample changer and its API
 - #1219   get Actor sample changer barcode reading working
 - #1207   Create re-usable methods for parsing/creating position objects [, 5days]
 - #1204   Plan to overhaul Plotting system[I11 I15 I16 I22,20 days]
 - #1203   GDA plotting locks up.
 - #1193   Make alias command also define help
 - #1185   extra experimental options for MX data collection
 - #1183   Formalise/document PD definition [i16 request #581, 15days]
 - #1181   Allow pseudo devices with no inputs [i16 User request #528]
 - #1157   DOF adapters can't be deleted and recreated from same script
 - #1065   operate SampleChanger via a script
 - #946    GUI does not start when EpicsMCA running
 - #923    numerical analysis codes[1 day]
 - #896    Update README file for JARS [3 days]
 - #739    Enhance project object to allow mathematical operations

Release_7_2_1 17/03/2008 (tag release_7_2_1 created)
----------------------------------------------------

 - 1376    EpicsPneumatic reports demand values only   
 - 1404    space indented jython scripts fail  
 - 1424    Problems with standard directorys and new coments when reading in the SRS data files    
 - 1428    SRS filewriter broken for two dimensional scans 
 - 1451    help commands prints some incorrect commands
                            
Release_7_2_0 11/02/2008 (tag release_7_2_0 created)
----------------------------------------------------

 - 1365    Add printJythonEnvironment command to utils.py
 - 1360    fix the errors due to Epics interface changes - 2 PVs are removed from pneumatic interface
 - 1358    ISPyBTools needs to be rewritten so that it can talk directly to the ISPyB server process
 - 1357    ISPyBTools.getLoadedSample is called twice when "Retrieve data" is clicked
 - 1356    Object server wont start on trunk
 - 1352    remove TextDocumentSizeLimiter from DebugAppender
 - 1351    Add cousin to scannable.isPositionValid() to find out what makes a position invalid
 - 1347    Put lost limit scannable-limits back into ScannableMotionBase
 - 1345    pos does not return array of values which scaler produces, just scaler status.
 - 1344    pos does not retun String represent enum position anymore
 - 1343    New scannable implementation stops monitor return unit along with value
 - 1340    Create CorbaDeviceException from complete exception stack.
 - 1339    Protect JCameraManDisplayPanel from crashes due to unreachable devices
 - 1338    Add port for log client to java properties.
 - 1337    Add a device that allows a method for calling public static methods of classes on Server from Client without the need to write idl
 - 1336    Allow for non-default timeout in calls to EpicsController.CreateChannel
 - 1335    Prevent EpicsDevices blocking other Corba threads
 - 1334    Corba thread pool can be used up when starting up gui.
 - 1333    Unit on motor limit update is wrong, while unit on position is correct
 - 1332    Reorganise gda.jython.commands
 - 1331    To alow jython imports (rather than run): provide jython accesable java method to add aliases
 - 1330    Repair DOF adapter so it checks underlying dof limits
 - 1327    Bugfix for ObjectShelf
 - 1325    Checkin prototype jython code for creating group scannables and for providing dotted access to fields
 - 1324    Create gdadevscripts in gda/scripts folder
 - 1323    Add PD to dummy_pds.py with multipl input and extra fields
 - 1322    update gda/scripts/gda/scripts/pd/mca_pds for new scannable location
 - 1321    reset namespace leaves old scannables instantiated somehow
 - 1320    Adding a default from localStation.py will results in duplicates
 - 1318    MX Beam centre should be calculated from the detector distance.
 - 1317    MX Do not display the wavelength field in the experiment control panel until it has an effect
 - 1316    MX Wavelength and beam x & y boxes should be for information only
 - 1315    JCameramanDisplayPanel used incorrect logic to determine if the mouse press was inside the window
 - 1309    Provide option over whether the gda.gui.epics.EpicsMonitor class calls back using Swing Event Thread
 - 1308    Make epicsDevice compatible with new xml interface
 - 1307    Add optional feature to CombinedDOF to report last moveTo position rather than calc from moveables.
 - 1306    When requesting the position of a dof in the terminal the units are not displayed.
 - 1305    When listing an OE the values for the individual dofs are in SI base units rather than reporting units
 - 1304    email test ticket #2
 - 1303    email test ticket
 - 1302    ScannableBase toString method crashes if there are any extra fields.
 - 1301    scannable toString() method catches exception without reporting the cause
 - 1299    To MotionScannable add wrappers around asynchMoveTo(), getPosition() and isBusy() methods, and provide a default completeInstantiation() method
 - 1297    increase refresh rate of sample image camera
 - 1296    i18 request for a Straight Line fit function for thursday
 - 1295    GenericScanning in stnBase does not work
 - 1294    move logging port from 6000/6001 to 6788/6789, and make client port configurable
 - 1292    add move validation objects to SampleChanger load and unload methods
 - 1291    add atStart and atEnd method to Scannable interface
 - 1290    Add dummy pseudo devices to gdascripts folder
 - 1289    implement pause and stop functionality into the fitting routines.
 - 1287    Thread crashes occur if too many graphs are drawn at the same time
 - 1286    Jython interpreter dosent initialize correctly if some dirs dont exist
 - 1285    problem in I16 configuration stops check-out diamond config as project
 - 1282    minor problems running scripts
 - 1281    JythonEditor uses proptotional font and wraps lines
 - 1278    pos <OE> prints out to one line with \n's in the string
 - 1276    documentation for persistence
 - 1275    update documentation scanning mechanism
 - 1273    MX - make the Define Data Collection table less 'cludgy'.
 - 1272    MX - validate input in the Define Data Collection table
 - 1271    MX scripts to run experiments should check state of beamstop.
 - 1270    Messages from mx scripts should not appear across two screens but rather in the middle of the GUI
 - 1269    Various changes to mx script to allow the progress to be monitored and problems diagnosed.
 - 1268    Attenuation control added to MX gui
 - 1267    Log/Status panel added to mx gui's.
 - 1266    The GUI can now start with panels in the hidden state but accessible via the View menu.
 - 1265    Update the ScanDataFile plotting functionality
 - 1264    Support for general purpose epicsdevice
 - 1263    Parser fails with: pos mu FindScanPeak('ct3')['mu']
 - 1262    Run jprofiler on the client during weekend long scan collection
 - 1261    Improve error reporting in ConcurrentScan
 - 1260    Add default isPositionValid method back into ScanableBase
 - 1258    display raw micro glide values in sample control GUI
 - 1257    ignore clicks on sample viewing GUI outside of camera signal
 - 1256    unusual characters in header file title seem to crash da.server
 - 1255    Error in recent change to GDASchema for MicroFocusPane
 - 1254    no output at all if no logger running
 - 1253    allow writing labels for RAPID scaler inputs into BSL header files
 - 1252    define window system icons for GDA GUI and login window
 - 1249    DNA: connect automatically to GDA on start-up of the DNA GUI
 - 1247    Upgrade NeXus version to 4.2 beta
 - 1246    Improve the basic intereactions with the DataVector plot window
 - 1244    Add functionality to obtain the differential from a dataset
 - 1243    Add aditional linefunctions to the gda.analysis package
 - 1240    Update time_pds with new location for Scannable
 - 1239    Remove code that sets scannable limits to the underlying DOF limits
 - 1238    Provide way to add extra-meta-data to header of SRS files
 - 1237    Add a command to /scrpts/gdascripts/util for displaying contents of an iiterable
 - 1236    output format is not applied
 - 1233    I18 immediate development required
 - 1232    Error strings resulting from DOF limit violations don't indicate which DOF the problem is with
 - 1231    The toString method of an OE no longer displays the current position of all positioners
 - 1230    getReportingUnits not available for a DOF in a jython script
 - 1227    Add Internal Data Gathering functionality to Newport XPS Driver
 - 1221    Implementation of basic data plotting and image visualisation for I18
 - 1220    Add a way to write user specified metadata to SRS files (Temporary solution)
 - 1217    Add CSV file reader/writer
 - 1216    Add helper class to access gda database via the Java Persistence API
 - 1215    change scanning mechanism to work with new Scannable interface
 - 1214    Make alias work with commands that take no inputs.
 - 1212    no diagnostic messages when user directory does not exist
 - 1211    Move Jython extended syntax logic into Java
 - 1210    Create gda.server package
 - 1209    ScannableBase.getCurrentPositionArray() doesn't return ExtraNames
 - 1208    in MX control panel, phi value not always displayed correctly
 - 1205    Rename gda/scripts/gdascripts to gda/scripts/gdapy
 - 1200    get MX simulation working
 - 1196    cleanup /bin
 - 1195    Make JythonServerFacade.print() synchronous
 - 1191    gda.jython.JythonServerFacade.getInstance().print(String) not functional
 - 1190    gda.analysis.DataSet constructors should throw errors, and log reasons for failure
 - 1189    Provide facility for i16 to fit functions to data using underlying fitting framework
 - 1188    Change DataSet display routines to display directly to the jython terminal
 - 1186    return microglide to previous positions after a restart
 - 1184    prevent microglide movement beyond its limits
 - 1182    Get i15 peak finding routines working provisionaly
 - 1179    Can only plot one element from a vector pseudo-device (User request)
 - 1178    Graph axis labels sometime not useful (User complaint- I16)
 - 1177    MX GUI operates sample alignment from any phi angle
 - 1176    Jython Server reverted to redirecting all scandatapoints through to the Jythonterminal
 - 1172    Increase the flexibility of the scanning mechanism
 - 1171    Create beamline configuration manager for saving/restoring axis positions/limits
 - 1170    Create simple persistance for scripters [depends on 1168]
 - 1169    Create simple parameter storage for java programmers [depends on 1168]
 - 1168    Add jdbc database to beamlines
 - 1165    Indicate that an epics move failed
 - 1164    Get retry deadband (RDBD) setting from epics
 - 1161    Develop efficient peakfinding method for standard scans
 - 1160    Improve the proformance of the DataVector object
 - 1159    Add basic peakfiting functionality
 - 1158    Check in collision avoidance software
 - 1152    make build.xml ant script build gda_alpha.jar target not gda.jar
 - 1129    allow command-line interaction from within a script
 - 1128    make Finder through FindableNotFindException when it can not find the findable specified
 - 1126    Implement new methods for getting sample info from ISPyB (gda.px.util.ISPyBTools)
 - 1114    Add test cases for DummyExafsServer
 - 1106    refactor of gda.px package for new MX beamlines
 - 1104    Prevent overwrite of default (template) CCFs for IIS
 - 1086    Add MarCCD support
 - 1082    review/simplify gda.px.detector interface
 - 1060    "list S1" is broken
 - 1059    Generic Image File Producing Dummy Detector
 - 1056    move test folder outside src.
 - 1047    implement and integrate the new EPICS XML interface
 - 1043    General improvements to NeXus writer
 - 1025    Client becomes unresponsive and eventually crashes after taking many scans
 - 1012    Translator reports error with multiline python statements
 - 1011    Test new Corba (idl & jacorb jars and jacorb NameServer) on Windows
 - 1009    extened syntax parsing error adds extra commas
 - 954     Evaluate having seperate build folders for GDA class files
 - 914     Headers not listed in tabular data file for PDs with multiple outputs
 - 895     tidy up of /lib
 - 807     When doing repeated XAFS scans only the last is shown on the plot.
 - 769     need to recognise \r in JythonTerminal
 - 751     Using peak-fit to determine detector centre
 - 738     "plot" command in Jython environment
 - 601     Add function fitting to Ncd calibrate panel
 - 590     An exception is thrown if there is not a file Limits and Offsets that corresponds to a particular motor name.
 - 4       remove JythonServer script queue
 - 3       merge test and tests folder
 - 2       remove unused gda.eps.package

Release_7_0_3 11/01/2008 (tag release_7_0_3 created)
----------------------------------------------------

 - 1288    Adjust tuning parameters during a newport motor move 
 - 1284    Cannot start GDA while XPS is off
 - 1274    Add option to plot: always clear last scan data 
 - 1248    DNA abort_request not handled by GDA    
 - 1242    Fix log4j memory runaway problem in client 
 - 1241    Fix simpleplot memory leak problem  
 - 1234    Multi dimensional scans don't write column header names 
 - 1226    release 7.0.2 fails to start due to: class gda.px.util.ISPyBTools cannot access its superinterface
 - 1225    7.0.2 still has old GDA_StartServers script 
 - 1224    Release 7.0.2 won't work    
 - 1192    ncd Time frame scan does not finish in GDA  
 - 1173    Add trunk fix to branch: remove defaults does not remove defaults

Release_7_0_2 27/11/2007 (tag release_7_0_2 created)
----------------------------------------------------

 - 1202    Provide a way to turn off plotting (work around for some plot problems) 
 - 1206    tick box on JythonTerminalPanel to optionally prevent plotting  
 - 1201    Limit JythonTerminal's output textbox field 
 - 1192    Time frame scan does not finish in GDA  
 - 1108    flag in Jython so that all scans move their pseudo devices back to their original positions

Release_7_0_1 02/11/2007 (branch release_7_0_1 created)
-------------------------------------------------------

 - 1      Remove obvious bug during detector readout in NexusDataWriter    
 - 1153    Base configuration scans all fail with null pointer exception

NB release_7_0_0 onwards requires Java 6.

Release_7_0_0 18/10/2007 (release_7_0_branch and release_7_0_0 tag created)
---------------------------------------------------------------------------

 - 261 Integrate an image display GUI panel to Medical Imaging for displaying data files created by ImagePro  
 - 465 Is gda.oe.dofs.SingleAxisWavelengthDOF used ?  
 - 752 display informationon peak, intensity, distance between 2 peaks, given an order of peak height  
 - 798 Delay between images during PX data collection and humidity scan  
 - 817 Reading and writing Lookup files from GDA  
 - 818 make default formatting for Psuedo Devices configurable  
 - 850 no plots from a scan started in a script  
 - 922 Add energy peaks to McaGUI  
 - 937 remove unnecessary jars  
 - 985 Document metadata configuration and setup.  
 - 994 Remove unecessary IIS functionality  
 - 997 Contruction of data directory for Nexus files at DLS needs to be more configurable.  
 - 1039 NCD Configure panel throws exceptions in base configuration  
 - 1040 i16 configuration scripts have duplicate name entries (case ignored)  
 - 1041 simplePlot batching null pointer error when changed  
 - 1044 Implement delta tau motor system  
 - 1045 Capture transient exceptions in adapter classes  
 - 1051 Null pointer exception in AcquisitionFrame if not using docking.  
 - 1053 GDA hangs due to deadlock in gov.aps.jca.event.LatestMonitorOnlyQueuedEventDispatcher  
 - 1054 common directory structure folder clash if using seperate output folder 'bin' (the default)  
 - 1055 Please add GDA test component in Bugzilla  
 - 1061 uniqueName generated by scan might get duplicated.  
 - 1062 allow setting of IIS LMF name from GDA GUI  
 - 1063 create user defined auto-series LMFs for IIS  
 - 1064 simplification of the gda.px.samplechanger.SampleChanger interface  
 - 1074 add barcode reading functionality to the gda.px.SampleChanger interface  
 - 1078 Add SRB archival of files associated with NeXus data files.  
 - 1079 Deadlock observed when synchronized DOF methods call caget leading to timeout  
 - 1080 upgrade castor jar to 1.1.2.1 from 1.1.1  
 - 1081 upgrade xerces to 2.9.1  
 - 1083 "Can't find the named subsystem" during object server startup  
 - 1084 Add Irelec sample changer  
 - 1088 Very high CPU in client filtering CORBA events  
 - 1089 gda/function/InterpolationFunction.java did not handle corner cases properly  
 - 1090 gda/function/ColumnDataFile.java should stop reading files when it finds an empty line  
 - 1091 gda/scan/ScanBase.java. setUp method does not move Detectors form Scannables to Detectors list  
 - 1092 gda/device/filterarray/EpicsFilterArray.java should use the EpicsChannelManager to handle connections.  
 - 1093 gda/device/MotorException.java should always contain a valid motor status  
 - 1094 Deadlock in DOFs that drive other dofs  
 - 1095 gda/oe/commands/AbsoluteMove.java should unlock a DOF if an excpetion is seen in the call to doMove.  
 - 1096 gda/device/detector/countetimer/EpicsScaler.java status showing not busy straight after start.  
 - 1097 gda/device/motor/EpicsMotor.java isMoving method should check both l IOC and local cache  
 - 1100 add Cobold Expt Panel for IIS  
 - 1105 gda build fails due to looking for Manifest file in wrong place  
 - 1107 write test cases for gda.device.adc.dummyAdc class  
 - 1108 flag in Jython so that all scans move their pseudo devices back to their original positions  
 - 1111 cannot import from shared Jython scripts folder  
 - 1112 Out of memory error caused by logging  
 - 1113 write test cases for gda.device.DeviceBase class  
 - 1118 write test cases for gda.device.detector.DummyImageCreatorTest  
 - 1119 Added scannable-limits to scannables  
 - 1120 Add overideLock() method to OEBase  
 - 1121 Testing the gda.analysis package  
 - 1122 write JUnit tests for DummyCoboldTDC  
 - 1123 write JUnit tests for DummyCoboldTDC  
 - 1124 create JUnit test for DummyCoboldCriptController  
 - 1137 Put IIS GUI ops in event thread  

Release_6_14_0 30/08/2007 (release_6_14_branch and release_6_14_0 tag created)
------------------------------------------------------------------------------

 - 485 Remove hard-coded parameters in NcdController  
 - 487 Remove hard coded parameters from gda.px.centring.Centring  
 - 584 ContinuousScan XAFS Plot to be redrawn at end of scan  
 - 610 2D GeneralScan does not plot data  
 - 683 Intermittent failure of undulator moves using slave mode interfacing to POMS (PINCER) on 5U  
 - 703 Levenberg-Marquardt enhancement  
 - 735 AmplifierStatus in device package needs looking at.  
 - 753 Two versions of monochromator 2d used.  
 - 771 Add basic JUnit tests to Jython packages via AllValidSuites  
 - 791 "run" command fails if script indented using spaces  
 - 833 fix "run" command to prevent a race condition  
 - 834 PEEM support with new jar  
 - 835 validation of Scannables upon instantiation  
 - 836 Undulator GUI panel (and often more) hangs in OEMOVE on 5U  
 - 839 Add PositionCompare methods for Newport XPS motors  
 - 845 allow for custom translators in jython  
 - 851 clearing the plot after a scan has started prevents any more plotting from that scan  
 - 856 Several SimplePlot zooming/magnifying problems.  
 - 860 limit "watches" refresh rate in JythonTerminal panel  
 - 861 minor bugs with syntax parsing  
 - 903 TimeScan data not shown in scripting terminal during IISScan  
 - 908 create DummyCoboldPC & DummyCoboldPCCMC  
 - 924 Notification of GDA document update  
 - 941 Test latest version of Subclipse  
 - 943 prune old svn repository branches and tags to sensible number  
 - 947 New soft limits from Epics motors do not account for offsets in positioners  
 - 948 refactoring of devices has broken native interface for NI6602 class  
 - 949 NI6602 class should trap count times less than 1 msec  
 - 950 Unexpected update in GeneralScanPanel  
 - 951 running event service without name service gives silly messages  
 - 955 simplify structure of gda svn repository above gda/trunk/src  
 - 956 GDA GUI error with Java 6.0 on Windows Systems & some Linux  
 - 960 IIS experiment fails to stop CoboldPC  
 - 962 Improve PX Detector error handling  
 - 963 Problems with printing to Jython Terminal.  
 - 964 Movements of non-epics motors no longer work properly.  
 - 968 Slow GDA crash due to XPS  
 - 969 ExafsPanel does not update total time when paramters read from file.  
 - 970 adding Help menu to GDA  
 - 971 base configuration for MonoDOF is mis-matched with schema and mapping file.  
 - 972 new element are required to be present in configurePanel and ExafsPanel in the base configuration  
 - 973 pos report wrong position at end.  
 - 975 ExafsPanel in stnBase_Client.xml produce the following error  
 - 978 Epics motors cannot move when Positioner stepsPerUnit negative  
 - 980 Implement unit tests for obtaining metadata within GDA.  
 - 981 Implement unit tests for DDH operating in storaged mode.  
 - 982 Create an Icat browser panel for GDA.  
 - 983 Allow GDA to operate with different modes of metadata saving.  
 - 993 Update XERCES jar  
 - 996 NcdDetectorSystem sets usedByDefault true  
 - 999 Add configurable support for extendedTFG  
 - 1000 LinearPositioner.setSpeed has unnecessary restrictions.  
 - 1002 First failure to move XPS motor is not reported  
 - 1005 Remove deprecated xalan code  
 - 1006 Ant script to create gda jar file  
 - 1007 improve AuthenticationFrame.java to support single/multiple screens dynamically.  
 - 1008 No ControlPoint Scannable Adapter  
 - 1010 OtokoWriter missing from Mapping.xml  
 - 1014 add camera controls to CMU camera GUI  
 - 1015 EpicsMCA does not always count for correct period of time.  
 - 1018 gda.gui.ncd.headerfilewriter classes not Corba-ised as expected  
 - 1021 Remove BCM experiment counter and use experiment number from collect_Request  
 - 1027 Current saving of password at login must be encrypted.  
 - 1028 Running TestNG suite fails if each creates objects via ObjectServer.  
 - 1029 Correct any old bugs with target milestone left as 'needs_accepting'
 - 1035  SimplePlot batching does not work correctly

Release_6_12_0 19/04/2007 (6.10.n branch closed, 6.12 branch created)
---------------------------------------------------------------------

 -  30  restart or continue scan after beam monitor detector loss of beam 
 -  35  temperature gui on status panel shows incorrect .dat file 
 -  36  intermittent display of file name on dsc/temperature plot 
 - 361  PX exposure timing units 
 - 490  create experiment panel for IIS 
 - 496  write classes to send script to dummy CoboldPCC for IIS 
 - 509  create IIS experiment classes 
 - 529  The Undulator representation and current position fields are not 
 -      updated during a move, only when it has finished. 
 - 530  GridScan does not do final point (and there are some inaccuracies in calculations) if the scan starts at a high number and finishes at a lower number. 
 - 531  Improve error reporting during pre-scan checks. 
 - 532  5U users setting the Undulator Energy require the Monochromator photon energy to be set concurrently and vice versa. 
 - 564  OEMove button sensitivities when switching between DOFs 
 - 598  Phase II EPICS integration 
 - 603  Allow GeneralScanPanel detector names and count times to be xml configurable per scan dimension 
 - 625  write classes to link timestamped beamline data to IIS events 
 - 634  setDefaultAcceptableUnits contains incorrect code (which eclipse allows) 
 - 645  OEMove and use refresh when in set mode problem 
 - 654  make GeneralScanPanel multi-dimensional 
 - 659  Rationalise start up files in gda.bin and installation files 
 - 671  Make it possible to disable positional homing on motor move for IIS slave mono 
 - 682  Formatting desirable in generalscan.Region.getInterpreterCommand 
 - 685  move cobold detector code to detector package 
 - 687  JavaDoc CoboldTDC 
 - 699  Duplicate points in XAFS scans. 
 - 702  EpicsMCA and EpicsScaler need attention (from Javadocs day) 
 - 706  ImageMagnifierWindow.getMagnifier() never used 
 - 707  LinePropoertiesEditor.setCurrentLine() - current line can be null 
 - 709  SimpleLegendItem - remove complicated which is never used. 
 - 710  SimpleLegendTitle failure to pass correct Object. 
 - 711  UndulatorMoveCalculator:checkMoveMoveables ignoreMe argument 
 - 714  ObservableComponent should implement IObservable interface  
 - 715  LockableComponent should implement Lockable interface 
 - 717  Remove compiler warnings 
 - 725  Investigate impact of upgrading to Java 1.6 
 - 728  Implement changes to utilise new runnum tracking in device-temperature as per bug 592 
 - 743  HarmonicDOFInputDisplay has two FIXMEs that look important 
 - 745  make Monitor interface more generic 
 - 746  refactor CoboldDetector & CoboldTDC 
 - 754  rename and version Documentation in line with Knowledge Tree 
 - 760  New Asynchronous Epics MCA, ADC and TCA classes 
 - 766  Some McLennan motors respond badly to STOP when already stopped. 
 - 767  SimplePlot - dependent X axis -scale goes wrong when the graph is zoomed 
 - 772  Integrate Rigaku OSCAR sample changer system into GDA 
 - 773  Folder for shared GDA scripts. 
 - 780  add scrolling history of values in BeamMonitor OEPlugin 
 - 787  OEMove moveby selected but moveTo performed  
 - 788  daServer timeout ignored  
 - 789  Scan fails if a Psuedo Device which is not moved in scan command is already moving. 
 - 790  Report errors from localStation.py in GUI 
 - 794  Can't set OEPlugin to show at startup  
 - 803  Modify metadata access classes to read data from mini stations icat 
 - 804  Write metadata to central icat from NeXus files in DDH 
 - 806  Initial size of AcquisitionFrame is wrong on double monitors. 
 - 808  DOF speeds not reset correctly after continuous scans. 
 - 811  Steps/points confusion in XAFS scans.  
 - 813  problem with Epics motors access  
 - 814  setting motor offsets dynamically and in Epics  
 - 819  Remove Message.log, add Message.warn and review of messages 
 - 820  Analysis of PX images to determine optimum crystal humidity. 
 - 821  error should be thrown if size of object arg in moveto incorrect 
 - 823  empty catch clause in gda.jython.scannable.DetectorAdapter 
 - 824  Positioner in trunk does not work  
 - 826  remove castor-1.0.1.jar, migrate to castor-1.0.3.jar for full Java 1.5 support 
 - 827  Newport XPS controller fails to connect to more than three/four motors at the same time 
 - 828  To enable setting of real speed value using double 
 - 830  move temp files generated during scripting to gda/config/var 
 - 831  validate scan command without moving anything  
 - 832  implement isHomed for NewportXPS motors  
 - 838  Ability to obtain length of a scan 
 - 840  integrate MX shutter and complex shutter/phi motion 
 - 841  SimplePlot Zooming has stopped working 
 - 842  DOF update() should not do sync request to the low level object, but handle the event object only. 
 - 843  add isInitialised() method to Motor.java interface 
 - 844  caget("...") returns truncated data - 5 decimal only 
 - 847  Microfocus mapping panel does not display maps correctly 
 - 849  Lauda water bath not updating GUI 
 - 852  Implement JythonServerFacade.restartCurrentScan() 
 - 855  Change EpicsMCA , EpicsTca , EpicsADC and EpicsScaler classes to Use new EpicsChannelManager 
 - 857  SimplePlots will not print out. 
 - 863  add images from Cameraman to ExperimentControlPanel 
 - 864  Refactor to use new NeXus API package 
 - 865  Socket intput to Jython is not equivalent to JythonTerminal input 
 - 867  GDA startup script overwrites the log files whenever GDA is restarted  
 - 868  Icat schema change affects extraction of metadata 
 - 869  Modify Nexus classes to allow access of metadata from GDA entering filename 
 - 872  New methods in gda.device.Detector interface 
 - 873  new scan to take snapshots 
 - 875  move some device to device.detector  
 - 876  Possible inconsistency with the use of "active" in detectors.  
 - 878  can not checkout configuration from DLS_Beamlines 
 - 880  Newport xps motor crashes Object Server if the Newport xps controller is not found  
 - 882  ConfigurableScanPanel / GeneralScanPanel does not retain previous values  
 - 884  GeneralScan not working 
 - 885  Distinguish between stand-alone commands and scripts in JythonServerStatus 
 - 886  create corba jar and remove generated classes from SVN 
 - 888  Enable/Disable message displaying 
 - 889  URL for data in SRB changed. 
 - 890  DDH handler for ICAT and SRB copying needs to be more configurable. 
 - 891  Potential race in connection to ICAT RCommands server. 
 - 892  General scan, plot doesn't automatically raise to top when scan started. 
 - 893  create /legal directory 
 - 897  add config/bin to the PATH 
 - 901  Alarm when looking for OE "Detectors" in IIS scan 
 - 909  Construct proper dataset name for SRB storage of metadata. 
 - 910  Check for existence of dataset in Icat before try to create. 
 - 911  Store proposal in Icat 
 - 912  Allow configuration of rcommands username, password and version 
 - 913  Pseudo device with extra outputs not reporting values correctly  
 - 915  add Cobold Start file to CoboldTDC  
 - 917  Add set/get Dwell time for epicsMCA 
 - 918  Put graphics update command into IIS experiment  
 - 920  Problems with ddh and SRB/Icat archival if investigation, proposal, visit not valid 
 - 921  Create a file to indicate a scan complete for DDH. 
 - 925  New SESO XBPM Detector class 
 - 926  Make EpicsMCA work more efficiently  
 - 927  Remove the thread block in initialisationCompletion() callback  
 - 928  improve the property initialisation method so it would not wait forever.  
 - 929  extend CreateChannel() in EpicsChannelManager to enable set initial PV value on channel creation.  
 - 930  improve GDA Monitor efficiency  
 - 931  Make EpicsMCA channel monitor event handling more efficient and thread safe.  
 - 932  implement xspress1 with da.server  
 - 933  GDA Build Error and associated compilation errors  
 - 938  add methods for caput callback  
 - 940  epics motor getStatus() should only return motorStatus hold by the motor object 
 - 942  remove dlsplot.jar as it is no longer required  
 - 944  pre-release 6.12 won't run outside eclipse on Linux or Windows 

Release_6_10_0 08/12/2006 (6.8.n branch closed, 6.10 branch created)
--------------------------------------------------------------------
 - 359  dl.px.camera.CameraBase:home ignores axis 
 - 466  Incorporate JCameraman API in a new samplechanger class 
 - 474  Implement VISA device support in gda.device.visa 
 - 510  save IIS data to central data store with suitable retreival mechanism 
 - 600  Can't Stop GeneralScan when DOF moving to scan start 
 - 627  Inconsistent composite movements using Epics motors.  
 - 701  Check each point in a scan is allowed before running scan 
 - 713  throw error if scan command ambiguous about the number of steps to be taken 
 - 716  Update Jython documentation 
 - 720  add flag to NewportXPS motor to home (or not) during configure  
 - 721  New features to support ETL Scintillator Detector via EPICS 
 - 729  Implement changes to utilise new runnum tracking in scan as per bug 592 
 - 732  ADSCDetector method canTakeOwnDark method always returns true 
 - 733  JCameramanDisplay has several TODOs that look more like FIXMEs 
 - 740  rename DataHandler to DataWriter 
 - 741  DummyValve class to simulate shutters/valves 
 - 744  JythonTerminal restricted in dealing with detectors which produce filename. 
 - 747  GUI still does not size OEMove menu bar correctly. 
 - 749  MCA GUI can't start if EpicsMCA already running 
 - 758  Rename gda.beamline.name 
 - 759  Add new methods to analyser interface to set/get number of channels 
 - 762  From GeneralScan gda.scan.MultiRegionScan has no attribute dataHandler 
 - 763  Problem with concurrently moving motors on a Newport XPS. 
 - 765  validation of internal arrays in Scannables 
 - 768  Make Monitor Scannable  
 - 769  need to recognise \r in JythonTerminal 
 - 770  Improve PXGENImagePanel to read imgCIF files 
 - 774  AsynEpicsMotor still blocks on getStatus() when EPICS servers are down 
 - 775  Refactor initial implementation of metadata access/storage classes 
 - 776  Allow get of single metadata value. 
 - 777  Add locally stored metadata entry type. 
 - 778  remove pulldown menu when no options are available in OEMove and IISScanPanel 
 - 779  GDA should handle Channel access resource gracefully on exit  
 - 782  Q axis calibration panel not working  
 - 783  Add password protection to Oemove tabbed pane.  
 - 784  Too many time frames allowed  
 - 785  NCD output time info failure if tfg configured in waxs system.  
 - 786  configurable option not to display speed JComboBox on OEMovePanel 
 - 787  OEMove moveby selected but moveTo performed  
 - 788  daServer timeout ignored 
 - 792  Scan does not fail if error in isBusy method 
 - 793  JCameramanDisplayPanel has different beamcentre/beamsize for every zoom level 
 - 795  EPICS related error exiting GDA GUI, when not using epics. 
 - 797  Problems setting default mode in OEMove 
 - 800  EPICS context error on exit() from GDA 
 - 801  "run" command used in localStation not working properly 
 - 802  JythonServerFacade.panicStop doesn't work for scans inside loops 
 - 810  optional login dialog to GUI 
 - 815  create new DOF to drive Slave Mono in eV or Angstrom GUI units 
 - 816  tscan command in scripting environment 

Release_6_8_0 20/10/2006 (6.4.n branch closed, 6.8 branch created)
------------------------------------------------------------------

This release is intended to be the base for a stable DLS release and a branch
will therefore be produced.  This release contains bug fixes done in the 6.4 
branch in addition to any enhancements that have been done.

The main inclusions in this release are changes to automated file numbering and
naming, more scripting changes, the addition of more colours and categories to 
EPS, the addition of the analysis package and two automated install mechanisms.

 -  43 Add NeXus file writing
 - 463 Changing number of experiments in PX collect tab gives error 
 - 491 Implement Newport XPS motor and controller classes 
 - 493 Messages are missing from MessageLogPanels that appear on the console.
 - 494 Update EPS to have ability to standalone without problems seen in testing for release
 - 520 Create new GUI panels for Diamond MX beamlines
 - 528 Allow the input table in scanning panels to be configured with deeper rows.
 - 533 Available energy range for 5U scanning needs extending from <1000 to >1200.
 - 547 Implement interface to Epics positioners
 - 562 Add optional configuration at startup 
 - 565 OEMove menu bar cut off if too long - fixed with a modified flow layout.
 - 582 eps errors
 - 588 Documentation required to fit within the GDA architecture document describing EPS.
 - 592 Consistent method for file name increment
 - 632 Start/Restart gda from remote client
 - 633 Install gda using rpm
 - 638 Need to change colour coding and add a category to EPS to deal with warnings that appear on message panels.
 - 639 Enhance error, log, warning and alarm messages throughout the codebase.
 - 640 Plot on ExafsPanel run pane does not allow change of line colour, type etc.
 - 641 Separate MicroFocus and gda.gui.microfocus.Exafs into interface/implementation
 - 642 change NewportXPSMotor to with OEMove 
 - 643 mapping file error in NcdDetectorSystem
 - 647 GDA installer generator using IzPack 
 - 649 add ability to record terminal output to file
 - 652 problems with subversion/subclipse/eclipse to be clarified/fixed
 - 657 Create library of arrows for OEMove for a new perspective
 - 661 add detector selector to GeneralScanPanel
 - 662 allow JCameraman sample changer to have variable zoom conversion
 - 663 make gda.scan.TimeScan compatible with GridScan
 - 667 new epics.jar with extension to enable valve and shutter control
 - 668 Create classes to control Epics valves and shutters
 - 674 Don't add area for message panels in AquisitionGUI if not creating any.
 - 675 scanning bugs 
 - 676 Jython can't import from jars not explicitly listed in classpath
 - 677 Allow jdl viewing of xray images to be configured out using xml
 - 678 Stored darks not being loaded with snaps.
 - 679 buildup of relativeTime fields in TimeScan child
 - 681 GUI has ceased to display anything 
 - 689 add demonstration of a panel which runs and receives feedback from scripts
 - 692 Fix scanning mechanism error handling
 - 694 Slit Limits do not update in GDA when EPICS slit limit changes
 - 695 GDA startup blocks if some of the EPICS pv not found
 - 697 null pointer exception for da.server reading encoder counter 
 - 698 scan info not printed to terminal if no detector included in scan

Release_6_6_0 25/08/2006
------------------------

This release contains bug fixes done in the 6.4 branch in addition to any
enhancements that have been done.  OEeditor has been reinstated, several cameras
detectors and analysers have been added.  Some changes to scripting and plotting
have been done.

 - 20 OEeditor not working
 - 71 Add Q axis to SAXS/WAXS plots
 - 223 OEMove saving states broken
 - 275 Update developer's reference manual
 - 283 Implement fast scanning for exafs
 - 374 The MonoSetupPanel for 5U does not work properly
 - 425 Remove naming confusion for auto-generated scannables - change of case needed for some Undulator files
 - 440 DOF.setOffset  removed test for homeable may effect homing
 - 471 Integration of EPICS and GDA for detectors
 - 472 Third party software integration with GDA
 - 482 New package and interface for Analysers
 - 489 Aerotech Motor classes need an overhaul
 - 493 Messages are missing from MessageLogPanels that appear on the console.
 - 494 Update EPS to have ability to standalone without problems seen in testing for release
 - 501 if motor position file doesn't exist sets position to zero but doesn't save.
 - 502 produce LMF file for 1 TDC & 2 TDCs in sync  for IIS using CoboldPC
 - 504 Configure CoboldPC to display only coincident events
 - 505 visualise 2D data with CoboldPC for IIS
 - 506 visualise data from 2 MCP detector simultaneously
 - 507 Put  ability to drive 5D & 3.2 mono into IIS GUI
 - 508 add ability to drive IIS polarizer into IIS experiment panel
 - 511 monitor beam intensity and polarization in IIS GUI
 - 513 Usability improvements to scripting environment
 - 520 Create new GUI panels for Diamond MX beamlines
 - 521 Add softLimits accessors to OE interface
 - 527 Enable a DOF to be specified to show as the default in the drop down box on scanning panels.
 - 535 StartMove button should work for UndulatorHarmonic and UndulatorPolarization
 - 538 EPS crashes gda.px.detector.Quantum4CCD during initialisation
 - 540 change offset accessors in Moveable interface
 - 542 remove need for tabs-only indentation on GDA Jython command-line
 - 543 Combined monochromator and table movement required for 9.3
 - 546 DummyDisplayMotor and DisplayMotor do not operate correctly.
 - 548 Implement interface to Epics filter arrays.
 - 550 Move MetadataList classes into gda.data and remove gda.util.database package
 - 551 add method to gda.device.Detector interface to return data dimensions
 - 552 Move the datahandler package from gda/scan into a new package called scan in gda/data
 - 553 Loss of resource history when using Eclipse/Subclipse to move and refactor the datahandler package and several other associated classes.
 - 554 add plot annotations  
 - 555 mapping error: ScriptControllerDemoPanel  
 - 556 Add a new GUI for MultichannelAnalyser with energy calibartion capability
 - 557 Add second  x axis for SimplePlot
 - 558 PositionalValues should save both positionOffset and homeOffset  
 - 559 Use of new property when scanning  
 - 560 New ADSC Q315 detectors
 - 561 Update installation and configuration files in preparation for the main release.
 - 563 Save terminal command history between GUI restarts
 - 565 OEMove menu bar cut off if too long.  
 - 566 GDA client GUI to check Server version on startup  
 - 567 Validation of oemove.xml mystery
 - 568 New Navitator Motor
 - 569 New Generic Plugin for OEMove
 - 570 Allow zoom and focus levels to be set via XML
 - 571 New Firewire Camera
 - 572 New plugin for collimator slit zeroing
 - 574 Remove JDO tags from mapping.xml file  
 - 575 Switch GDA to use Jython2.2a from 2.1  
 - 576 Gdhist not configured correctly for HOTSAX detector
 - 577 graphical display error for HOTSAX detector
 - 578 dummy motor speed always 1.0
 - 579 Need a DummyEnumPositioner class for simulation/testing
 - 580 Scanning process only takes data for 1st scan point.  
 - 581 Socket input to Jython works but produces no output to socket.  
 - 582 eps errors
 - 583 Negative move of DOF goes past target and doesn't finish.
 - 585 PX Detector leads to exception in DetectorBase
 - 586 Update IDE files and update GDA coding standards documentation with screen prints to match.
 - 587 Documentation required to fit within the GDA architecture document describing DDH.
 - 591 Epics - unable to find the types and devices xml from epics.jar.
 - 593 Null Pointer Exception in McLennanController if Serial Device not found
 - 594 Add getValue method to Monitor interface
 - 595 new EpicsMonitor object
 - 596 Default speed for DummyMotor 1 not slow speed
 - 597 AerotechMotor needs further changes.
 - 602 Scripting requires explicit implementation of interfaces
 - 604 ncd calibration plot issues
 - 605 Missing element from Castor mapping file  
 - 608 NiUsb9472 throws exceptions when reading from the hardware
 - 609 PIMotor is not working correctly
 - 612 Change Ncd default data type
 - 613 DOF decimal place selection in XML
 - 614 Calibration channel output failure
 - 615 Incorrect calibration channel plots
 - 616 JythonServerFacade: no panel named JythonTerminalfound
 - 617 Users request a default value in drop down DOF menu and request that behaviour of the OEMove Controls be changed
 - 618 MultipleMove requires changes
 - 619 Security access exception with MarkerFactory running under Windows
 - 620 Scripts containing Scans fail under some circumstances.  
 - 622 create new TDC detector class for IIS
 - 623 DoubleAxisAngularDOF not working properly.  
 - 624 Add ability to save IIS data at regular time intervals
 - 626 create SlaveMotor class for IIS mono driving
 - 628 Need to add gov.aps.jca.JCALibrary.properties to GDA startup script  
 - 629 spurious errors regarding lock files when using RXTX
 - 631 Bring back coordinate and unit control parameters, EPICS parameters
 - 635 DoubleAxisAngularDOF not working properly.  
 - 636 Create NcdDetectorSystem for scripting
 - 637 In jython terminal 'pos oename' and 'pos dofname' can give different positions 
 - 646 Utiltiy Class to add directories based on package information in a class file
 - 649 add ability to record terminal output to file
 - 650 remove dependency of 2d double array from ScanDataPoint
 - 651 add "scan complete" message when scans finish
 - 655 ScannableBase setPosition is not consisten with OEMove  
 - 658 New class required to allow users to read the Motor Position files
 - 660 Important Manifest file restrictions
 - 670 Review Scannable interface / DummyScannable  
 - 672 remove DefaultFileHeader.getInstance()  
 - 673 implement ADSCController intialise method  
 - 688 messages from scanning not reaching Jython terminal  
 - 690 addition of curve fitting routines to gda and creation of analysis package  
 - 691 multi-region and centroid scans not working  
 - 693 Add getSoftLimits accessor to motor interface  
 - 700 getPosition should be called only once per Scannable per scan point 

Release_6_4_0 26/05/2006
------------------------
The main features of this release are modifications and enhancements to 
scripting, detectors and analysers.  Validation against the GDASchema has been
permanently enabled and OEMove has been rewritten to use an XML file.

79   CoupledDOF updatePosition has trouble determining when position is valid
91   refactor EPICSMotor class
124 Optionally password protect individual DOF modes in XML file rather than as general property.
204 configurable unit setting for motor speed
305 Resolve SRS station XML instance files and schema compatibility
310 Investigate and fix JUnit tests which fail in nightly build.
338 move the DriveUnit setting in GDA from Positioner or other low level codes to GUI output to Command server or object server, allow setting of real physical units for the motor.
344 EPS default port number same as existing PX Camera default port
353 PXBasePanel bug
374 The MonoSetupPanel for 5U does not work properly
404 Use of EPS via Message:setDebugLevel() causes VM error
405 A program argument is required to get EPS messages working vis the Message class
409 identify and review  slavery classes' hard coded  DOF names when translating commands
425 remove naming confusion for auto-generated scannables
437 PX Scripting fails with DNA
438 CoupledDOF getPosition returns Quantity, not correct subclass
443 Remove setName() from Findable interface (?)
444 in PXGENImagePanel make displayIntervalInMilliSecs variable at runtime
452 Add docking framework to GDA GUI
455 Button to operate CryoFlap for 10.1  
456 MicroFocusPanel should be able to map ion chamber output.
457 Another way to move the MicroFocus sample motors at 9.2
466 Incorporate JCameraman API in a new samplechanger class
477 Generic BCM-type object for systems of related panels and scripts
480 Provide display of xspress detector count rates
484 Prevent scannables being overwritten in Jython environment
486 Remove hard-coded parameters from BcmFinder / BCM
488 MotorStatus code extended to include LIMITVIOLATION
495 Rewrite DataLoggerPanel as a plugin similar to the CounterTimer one.
498 User changeable scanning DOF in ExafsPanel
499 OEMove error if view a representation more than once then delete one
500 ScanDataPoints not getting through to scans in GDA
514 stnBase server & client config files are out of sync with GDA  
515 DOFs in different OEs cannot have the same names
517 plugin panel not resizing correctly
518 BCMFinder fails to find rotation camera
519 Lack of DataMonitorLogger class causes problems.
522 On GDA Server closing, EPS does not close. When Launch GDA server again, the following error occurs. This result the subsequent message does not go to message panel.
523 Plot on Exafs run panel should be switchable mDeg to keV
524 Remove ability to switch off validation of Instance XML files against the schema.
525 enable serial port communication using gnu.io.rxtx instead of SUN's comm.jar
526 Auto scale in DataPlot tab of ScanPanel5U should work in the same way as the Run Tab.
535 StartMove button should work for UndulatorHarmonic and UndulatorPolarization
536 OEMove current position background
537 Add the option of logarithmic axes to SimplePlot
539 Make new OEPlugin for DigitalIO
545 current xerces.jar in gda does not support some of the features used in EPS XML parsers 
 
Release_6_2_0 31/03/2006
------------------------

The main features of this release are modifications and enhancements to the EPS 
package and the inclusion of GUI undocking code and related jars.  DOFS have 
also been modified to have configurable "acceptable units".

 - 262 Devise new digital IO class for Medical Imaging post mono shutter to give TTL pulse capability on Parallel Port or PCI card.
 - 270 Investigate converting OEMove tree into XML format (possibly integrated with Instance XML files) with image files moved within oemove package.
 - 272 Complete first draft of End User manual
 - 348 Reorganise Java and dl trees in code repository
 - 351 Fixed Focus move on MirrorAndGratingDOF causes motors to switch off and become unstable.
 - 362 Exposure:setStartAngle necessary?
 - 363 dl.px.camera.AdscBLExposure:getImageTime unused
 - 364 Undulator Tuning Scans just stop with no error or warning
 - 406 Modify all DOFS to have XML configurable "acceptableUnits " 
 - 407 Calls to "Finder.Find" with hard coded parameters need to be identified and reviewed, task split into areas such as PX, NCD.
 - 410 hard coded DOF names for certain OE's need to be identified and reviewed
 - 413 Improve ImagePro class & plugin robustness for initial user testing.
 - 414 ImagePro class & plugin - startup failure - protocol out of sync
 - 416 mcs0 error during creation of scaler
 - 422 Write scannables to integrate kappa diffractometer control into scripting
 - 434 null pointer exception in device.temperature classes
 - 439 DOF.addMoveables_db needs looking at by RJW
 - 441 revisit gda.oe.dofs.* use of positionValid
 - 446 Move getStatus method from CounterTimer interface up to Detector interface
 - 447 2D concurrent scans not working
 - 449 DSC not executing ramp 1, saving data
 - 450 Intermittent startup failure
 - 451 Not displaying correct host/directory 
 - 453 Undo and Redo capability for the jython Script editor
 - 454 Nested scans not working
 - 458 EPICS debug for talking to I16 Diffractometer 
 - 459 EpicsDOF doesn't seem to work
 - 460 ScatteringVectorDOF doesn't seem to work
 - 461 gda.oe.CoordinatedDOF configure method has "FIXME urgently " in
 - 462 gda.oe.dofs.DoubleAxisAngularDOF updatePosition method needs validity logic checking
 - 464 remove hard-coding of minWavelength and maxWavelength in gda.oe.dofs.MirrorAndGratingMonoDOF
 - 468 Arguments of scans with no variables should readout only
 - 469 List of default scannable should be merged with active detectors
 - 470 DOFAdapter doesn't handle exponential number format positions
 - 473 oemove getResource() failure for jar file images
 - 475 Scanning: new type of scan and enhancement to ConcurrentScan 
 - 478 Xspress get MC data fails for long collection times
 - 481 Provide OEPlugin which displays ion chamber counts as OEs move
 - 483 Missing XML element in stnBase_Server.xml

Release_6_0_1 20/03/2006
------------------------

This release is to allow testing, but was made from a copy of the trunk as 
bugfixes had not been committed to the branch.  A record of all bugs relating to
this release will be included at 6_2_0.

Release_6_0_0 01/02/2006 (beta)
-------------------------------

This includes a tag marked "release_5_7_0", which was not a true release, but 
was a special jar file to allow real time testing on stations.

The main features of this release are the change of name from dl to gda, an 
extensive reorganisation of the repository including changes to allow selectable 
configurations and an oemove rewrite.  It also includes the merge of PX 
scripting.

 - 90 remove package dl.datastructs
 - 115 add support for diffraction images in dl.images.SampleImagePanel
 - 175 Terminating GDA GUI doesn't terminate active scans.
 - 184 The automatic recalculation for Grid Scan input in the GeneralScanPanel often results in a negative number of steps.
 - 195 Provide automatic pitch peaking (requested for 10.1)
 - 239 Upgrade JFreechart to version 1.0.0
 - 266 dentify mechanism to store "Validation/Environment State" metadata for backward compatibility issues with configuration
 - 269 Identify and convert first phase of "Miscellaneous Configuration" 
 - 334 Xspress detector elements calibration from GUI does not work
 - 356 GeneralScanModel throws arrayOutOfBounds error if adding a row to an empty scan list
 - 403 MicroFocusPanel should be able to read in exisiting .xrf file and display
 - 420 Chooch will not run on 10.1 or 14.2
 - 423 DummyCamera fails due to removal of code (bug 380)
 - 424 gda.util.Unix fails after dl to gda repackaging
 - 428 Failed to create HeaderfilewriterAdapter & Impl after GDA refactor
 - 429 Add slicing and output parameters to all scannables.
 - 432 JythonServer's tempScript2 is written to gdaScriptDir causing permissions problems.
 - 433 EPS causes objectserver to crash
 - 435 Merged PX scripting version of fails to create Quantum4CCDController
 - 436 GUI configuration for merged PX scripting fails to create ExperimentSetupPanel
 - 445 PX data collection without sample changer not working
 - 446 Move getStatus method from CounterTimer interface up to Detector interface
 - 448 Scripting fails to move detector to safe distance at end of PX data collection with DNA  
 - 449 DSC not executing ramp 1, saving data
 - 450 Intermittent startup failure
 - 451 Not displaying correct host/directory.
 - 454 Nested scans not working.

Release_5_6_0 25/11/2005 (beta)
-------------------------------
This includes a tag marked "probably_5u_working_04nov2005", which was not a 
release, but was a special jar file upgrade to a station to provide extra urgent 
functionality in addition to urgent bug fixes.

The main features of this release are better error reporting from Motors through 
Positioner, various scanning bugs and substantial work on improving Undulator 
code.  Configuration work has provided a validation schema, changing local 
properties to Jakarta and string interpolated paths in both java.properties files
and Instance XML files.

The change over to Subversion from CVS was also completed in this release.

 - 51 Input fields on GeneralScanPanel do not clear when highlighted.
 - 52 McLennan Motors should not loop indefinitely when a controller is switched off.
 - 63 request to add getNumberOfChannels or similar to Detector interface.
 - 134 Set DOF units lists on an individual basis.
 - 161 Allow relative proportion of tabbed and message panels to be set in AquisitionFrame.
 - 231 The OEMove panel on 5U appears to have a units problem with the Undulator.
 - 232 JBuilder will not compile updated/ freshly checked out code since the new configuration package was added.
 - 245 create DummyGripCamera.
 - 274 Update architecture document to reflect changes.
 - 286 Add more GUIs and other objects to base installation.
 - 303 Do string interpolation on XML file before Castor unmarshalling.
 - 304 Validate XML file with GDA schema in "live mode" unmarshalling .
 - 308 migrate properties files to XML format (and relative paths).
 - 325 remove EPS warnings.
 - 336 scripting problems - reopened to deal with exception handling.
 - 346 Bugs ob socket connection used in EPS.
 - 347 switch code repository to Subvsersion (freeze CVS).
 - 349 need ability to disable sample changer auto centring
 - 350 When large moves are attempted on the mono on 5u (MirrorAndGratingDOF), one or more axes switch off with a "Current overload" error reported in the Aerotech software (NView).
 - 352 Unicode special character lost at Java/Jython interface,
 - 354 5U scanning produces datapoints as if a collection point is happening at theend of the first part of the backlash move (AerotechMotor and Aerotech3200Controller need refactoring).
 - 357 when saving temporary script files, save in userScriptDir not gdaScriptDir.
 - 365 Undulator will not move to first requested energy/harmonic position.
 - 367 Additional header information for 5U scans no longer appears in files.
 - 368 Scripts with multiple scan commands cannot be killed.
 - 369 Typing a # into the Jython Editor at the end of the file causes exception.
 - 370 Need option in terminal window to add new lines to same or new plot.
 - 372 Undulator moving problems at 5.1.
 - 373 Implement dummy time frame generator.
 - 376 The 5U Slavery does not work completely.
 - 375 The message in MoveableExceptions does not cross the CORBA gap.
 - 377 DummyTemp not performing ramps.
 - 378 MicroFocusPanel does not get correct sample positions for XAFS scans.
 - 379 MicroFocusPanel ought to save map data regularly.
 - 380 Removed unused local variables.
 - 381 MicroFocus XAFS scan runs but no data is returned to GUI.
 - 382 GUI freezes on daServer failure.
 - 383 Add undulator position to output of UndulatorTuningGridScan.
 - 384 Remove all occurences of unused local or private members.
 - 385 Removal of 'unused' variable breaks PXExperiment.
 - 386 GDA GUI can not be cloased by clicking  the top-right corner cross.
 - 387 Add line numbering to script editor.
 - 389 Need to able to specify angles of images for crystal centring.
 - 390 Need to be able to collect a background image for crystal pre centring.
 - 391 Ensure don't use background image for full crystal centring.
 - 392 Default zoom level for crystal pre centring too high.
 - 393 Some int variables in crystal centring impair precision of calculations.
 - 394 Default background image for crystal centring set to null can cause errors.
 - 395 User directory for crystal centring can have missing separator on end.
 - 396 Modify undulator dofs so that only energy DOF actually moves.
 - 397 Change method name in Scannable interface from isMoving to isBusy.
 - 399 Progress messages required during crystal alignment.
 - 400 Hard coded background image name in px crystal image centring.
 - 401 Crystal camera zoom doesn't update in GUI when set from crystal centring.
 - 402 Apply pixel to mm scale factor for c3d crystal pre-centring.
 - 415 Sample changer override should be protected by a confirmation dialog.
 - 417 Radiation on/off gifs corrupted.
 - 418 Plotting inhibits screen refresh.
 - 419 DummyMemory give array out of bounds exception.

Release_5_4_0 13/10/2005 (alpha)
--------------------------------
This includes IDs marked for target 5.3.0 which was not a release, but marked
as a tag "code_freeze_release_5_3".

 - 31 Beamline schematic in OEMove 
 - 33 Refactor Moveable, DOF and Positioner 
 - 75 Add title & condition fields to EXAFS GUI panel 
 - 80 Choosing new edge in exafs GUI may display default scan in wrong units 
 - 104 Review of items stored in XML and java.properties files 
 - 162 XAFS GUI can produce NaN in start/end/increment fields 
 - 176 Hard coded xafs data channels 
 - 177 XAFS muliple scans errors. 
 - 187 General lack of feedback in the GeneralScan Panel. 
 - 188 When 'move to position' button is pressed in GeneralScanPanel, there is no way to stop the motor and using the OEMove Panel results in an unstable environment. 
 - 191 XPS Motor class 
 - 267 Change object specific items (identified in LocalProperties document) from java properties to Instance XML. 
 - 271 Update DL tree with latest version of copyright notice. 
 - 272 Complete first draft of End User manual 
 - 273 Update coding standards document and related preferences files for IDEs 
 - 275 Update developer's reference manual 
 - 278 Create a new Positioner (probably) that will allow movement of two independent motors with positional feedback from a single encoder. 
 - 285 provide dummy plot (Gaussian ?) for dummy scans 
 - 288 update Bugzilla procedure 
 - 289 set up test pc for windows installations, test cases etc in b19 
 - 290 set up Eclipse on B19b Linux test PC - for unit tests, pair prog etc 
 - 291 set up test subversion on a server for evaluation, ide support etc. 
 - 292 GDA freezes on JVM when Bios hyperthreading enabled (?) 
 - 307 Jakarta properties - code review & commit to CVS if approved 
 - 309 Fix Javadoc warnings - as reported in maven reports 
 - 314 The CheckScan button on TimeScan pane does not work properly 
 - 321 Phi drive in SampleChangerPanel > 270 defaults to 360 
 - 330 EPS more meaningful and better formatted messages. 
 - 331 EPS add new warning message type 
 - 332 EPS log file content needs to be more readable 
 - 333 EPS Logserver dies no messages appear 
 - 335 plot points and %done controls in scans do not appear 
 - 336 scripting problems 
 - 337 OEBase & OE setReportingUnits signature difference compile error 
 - 339 Missing classes in mapping file 
 - 340 Message.Level.ONE messages should not appear by default 
 - 341 No way to pass objects from java into jython. 
 - 342 limit problem for X,Y,Z moves in SampleImageDisplayPanel 
 - 343 MultiRegionScan only accepts GridScans 
 - 347 switch code repository to Subvsersion (freeze CVS) 
 - 355 GDA (from repository) does not compile with JBuilder 

Release_5_2_0 05/09/2005 (alpha)
--------------------------------
 - 20  OEeditor not working  
 - 69  Change in behaviour of OEAdapter.getPosition()  
 - 89  remove hb15.zip 
 - 109  Integrate SampleImageDisplay into PXGen++  
 - 114  save beam posn to CameraMan configuartion file in dl.images  
 - 116  Add SIF file format to dl.images  
 - 154  Autoscaling of scan plots  
 - 168  Some fields in XAFS KSpaceScan have bizarre accuracies  
 - 189  Precision of mouse position does not adjust to the degree...  
 - 196  Provide mechanism to automatically set fluorescence detec...  
 - 198  Provide option to produce exafs output in Chooch format  
 - 212  Add magnifiying capability to SimplePlot  
 - 214  Restart Jython Interpreter command  
 - 215  hard loop if StringInFile  
 - 216  Make sensible default directories for XspressPanel file c...  
 - 220  ImagePro plugin needed to replace IPPServer JNI dll  
 - 222  Integration of EMS into ObjectServer  
 - 226  Simplification of Scannable interface  
 - 227  enhancements to scanning mechanism and the scripting GUI  
 - 233  MicroFocusPanel to write xrf files to specified directory  
 - 234  Local GripCamera cannot access remote GripClient  
 - 235  popup error dialogs need parent window jframe  
 - 236  toolTips don't work properly with SampleImagePanel  
 - 237  add auto-increment to move value in SamplechangerButtonPanel  
 - 238  enableQuit java property has no effect in ImageFrame  
 - 240  Phi values incorrect type in SampleChangerButtonPanel  
 - 241  sample holder centring not working properly in dl.images  
 - 243  add camera zoom & focus buttons to SampleImageMenuPanel  
 - 246  SampleChangerButtonPanel not updating current holder and ...  
 - 247  SampleChangerButtonPanel does not go inactive during robo...  
 - 248  update sample image after every sampleChanger operation  
 - 249  spelling mistake in dl.images UPDATE button toolTip  
 - 250  dl.images toolTip for READ SAVe show incorrect file name  
 - 251  ImagePanel (dl.images) READ / SAVE should change default ...  
 - 253  tfgscaler gives null pointer exception  
 - 254  Sample ImagePanel's required holder and sample are incor...  
 - 256  imageOperator does file selection for imagebuttonPanel bu...  
 - 258  CollectPanel gives null pointer exception  
 - 259  Convert 9.4 Instance XML files, OEMove Panels and OEMove ...  
 - 260  Write new classes for new PI piezo motor and controller f...  
 - 264  Extend CVS repository to accomodate new folders for stati...  
 - 265  Create prototype for using Jakarta properties to replace ...  
 - 276  set position does not work for DOFs with more than one mo...  
 - 277  MotorBase.setSoftLimits called at least twice during setP...  
 - 279  Enhancements to scanning mechanism  
 - 280  Scripting useability enhancements  
 - 281  No file filter in ImagePanel read / save operations  
 - 282  Class hierarchy not implemented properly in ImageDisplay  
 - 284  ImageFrame null pointer exception if no image file  
 - 287  Implement new logging changes with CVS tagged and locked  
 - 294  Move dl.util.MotordController  
 - 295  change the startupscript mechanism in DAServer  
 - 297  inhibit adapterfactory locating stale objects  
 - 298  splash screen interferes when debugging  
 - 299  Add generics to enhance type-safety and robustness  
 - 301  ncd graphics problems  
 - 302  Write doc for maintaining GDA schema alongside mapping file.  
 - 306  Migrate Local Properties class into dl.configuration package  
 - 312  LocalProperties issues - backslashes in path strings & in...  
 - 313  CounterTimer devices must explicitly implement Detector  
 - 316  Excessive number of image updates when start GDA for PX  
 - 317  Error popping up sample changer error dialog  
 - 318  Not setting current sample at grip after correcting an error  
 - 319  Erroneous load of gif file in SampleChangerErrorDialog  
 - 322  Remove default scannables list class  
 - 323  McLennan600Motor class must be able to deal with echoed c...  
 - 324  upgrade generated corba to 2.2  
 - 326  Create plugin class. 
 - 328  upgrade to jars  
 - 329  Run each scripting command in its own thread  

Release_5_1_0 30/08/2005 (alpha)
--------------------------------

This was implemented as tag "java_pre_logging_5_1_0" only in cvs so is not 
a branch yet. Features marked complete in 5_1_0 appear under 5_2_0

Release_5_0_0 30/06/2005 (alpha)
--------------------------------

 -  8  DNA in PXGEN, reactivation after collect.  
 - 14  get error if near end of move and start another  
 - 18  Standardize and enhance JTable behaviour  
 - 29  Combine xml files for gui and object server  
 - 34  Refactor SineDrive Dofs  
 - 50  Errors while saving and/or loading default representations  
 - 61  it is not possible to have a GeneralScanPanel and a ScanPanel5U in the same GUI  
 - 62  SimplePlot zooming from a dragged rectangle  
 - 64  Better formatting of SimplePlot mouse coordinate display  
 - 66  script checks for object server even when not using  
 - 67  Password causes shutter start to change  
 - 72  detector initialise button  
 - 82  update quantity classes to new release  
 - 85  remove destroy() from thread code  
 - 86  remove calls to hide() and show()  
 - 87  remove lazy instantiation from singleton patterns.  
 - 94  Logging/Alarms : design and preliminary implementation  
 - 101  Move GDA development and run-time environments to Java 1.5  
 - 103  Database for dynamic recording of experiment parameters  
 - 109  Integrate SampleImageDisplay into PXGen++  
 - 111  When OEMove "Stop Move" button is used to stop the motor moving, any additional backlash move still happens.  
 - 128  Matlab interface to GDA  
 - 132  DOF reporting unit "deg" is used in XML file but does not exist in Quantity class  
 - 133  Reporting units "joule" and "planck" don't seem to work  
 - 145  memory usage panel not working  
 - 147  Add gda standard directory tree and portable test cases to cvs  
 - 149  remove TODO auto generated message  
 - 156  DOF method getPosition(Unit) fails ungracefully  
 - 157  Eurotherm temperature controller not working on stn6.2  
 - 163  XAFS scan pane can be hidden by periodic table.  
 - 164  can't modify java.properties whith local ObjectServer running  
 - 165  ImageFrame produces camera error  
 - 166  Confusion between userFile and imageFile in SampleImageFrame application  
 - 169  Scans fail if no dl.scan.datahandler.datadir  
 - 170  Include XAFS GUI in PX configuration  
 - 173  File name in first ScanDataPoint null  
 - 178  XAFS start active straight after stop  
 - 183  changes for Rapid2 in 2D mode  
 - 185  Status icon feedback panel on OEMovePanel is often hidden because it drops down behind the message panel.  
 - 186  When the mouse pointer is moved outside the chart area, the co-ordinates still increase until the pointer reaches the outer edge of the holding panel.  
 - 190  Zoom is only possible about the midpoint of the axis range on a plotted graph panel.  
 - 192  re-package general scan  
 - 200  ** Open for latest on GDA changes/releases/planning etc.**  
 - 205  Scripting does not give feedback when the data directory is NOT found, the application just hangs.  
 - 213  SelectPointAndMoveTo button on GeneralDataHandler could cause null pointer exception  
 - 218  scripting pos command ignores units unless enclosed in parentheses  
 - 219  JythonTerminal does not compile in Java 1.5  
 - 221  concurrent scan nested scans are sensitive to order of command arguments  
 - 223  OEMove saving states broken  
 - 224  Loading of prviously saved darks no longer works  
 - 225  Changing of Q4 binning and scan mode not working  
 - 228  Remote PX Sample changer not updating observers  
 - 229  GDA Schema, Config package & Object editor GUI  

30/06/2005

The following notes outline the key feature implemented in each release with
bug fixes specified in .0x point releases (SRCG modified cvs policy, Dec 2003). 

e.g.
 - 3_0_0 major release on cvs branch "release_3_0_branch", existing interfaces changed
 - 3_0_1 bug fix release, still on "release_3_0_branch" (normal cvs tag)
 - 3_1_0 minor release, some new features, interfaces unchanged, "release_3_1_branch"
 - 4_0_0 major release on cvs branch "release_4_0_branch", interfaces changed

Planned features are listed ready for installation, use the static method
which returns a Java String - "dl.util.Version.getRelease()"

Release_4_5_0 13/05/2005 (alpha)
--------------------------------

 - 11 Implement move by in sample changer 
 - 15 Failure to notify objectserver of undulator gap position change 
 - 19 Problem transferring Ev through CORBA. 
 - 24 line colour problems after deleting line 
 - 27 start button on exafs run panel 
 - 39 refresh frame loses xy scaling on plots 
 - 40 multi-frame plotting 
 - 41 OEMove: opened as moveBy but is actually moveTo 
 - 46 Freeplot(?) crashes for very large datasets 
 - 54 Need to run GUI-based scan before scripted scan for data display 
 - 57 transient scripts created by scripting are placed into the wrong location 
 - 58 Move the Jython engine from the client to a server process 
 - 60 ability to move scannable to selected point on plot 
 - 65 GeneralScanPanel and Exafs Panel fail to appear 
 - 70 refactor object server 
 - 81 incorrect units displayed in jython terminal 
 - 83 jEdit.jar now needed to compile CVS HEAD 
 - 84 improve graph display on JythonTerminal panel 
 - 88 remove reserved word enum 
 - 93 Integration of PXGEN++ into GDA 
 - 95 Coupled DOFs to implement Dynamic focusing of mono 
 - 96 Preliminary (vanilla) GDA Install script 
 - 97 Object Server refactor, to add mapping file to jar file 
 - 98 Include OEMove gif files in DL jar file 
 - 99 Optional Splitting of java.properties on science technique basis 
 - 100 Add Datahandler button so scannable moves to point selected 
 - 107 When performing a scan, have a default list of scannables whose values will be recorded. 
 - 108 start JGrip from wothin Px application on any platform 
 - 112 write a method to create an ObjectServer process in dl.ObjectServer 
 - 113 write a method to create an EventServer (ChannelServer) process in dl.util 
 - 117 McLennan motors do not apprear to set speed or speedLevel at all. 
 - 118 Add millidegrees to the acceptable units for SineDriveAngular DOF. 
 - 119 saving states in OEMove with no specifed TargetPosition causes problems 
 - 120 ImagePro initial implementation tested 
 - 121 ImagePro initial implementation tested 
 - 122 Add Pincer SEAM DLL support to Win32 GDA via JSeam class 
 - 123 remove hard-coded dof names 
 - 125 empty motpos file causes null pointer exception in MotorBase 
 - 126 refactor NCD environment panel 
 - 127 Reorganise cvs to centrally store all native code libraries files 
 - 129 Gdhist memory readout failure 
 - 130 TfgScaler doesnt appear as an active detector 
 - 131 Invocation Target Exception during SaveAs in SampleImageFrame application 
 - 136 absolute pathnames in java.properties 
 - 138 make output from Matlab interface more convenient for plotting 
 - 139 Improve error handling associated with default scannables 
 - 140 NCD data acq. not starting 
 - 143 Start Button on EXAFS panel still not displaying properly 
 - 146 JComponent.setEnabled() does not set the state of descendents 
 - 150 k-space XAFS scans are only doing the k-region 
 - 151 5u mono calibration problems 
 - 152 Undulator scan problems 
 - 155 Unsolicited XAFS scanning 
 - 159 Double clicking Start button on OEMove hangs DOF 
 - 160 Allow acquisition border to be turned off. 
 - 167 DOFAdapter creates Quantity classes not correct sub-classes 
 - 171 XAFS not working with current scripting. 
 - 174 EXAFS dofs to scan and move hard coded 
 - 179 SubProcess needs to provide access to sub-process ID 
 - 180 pxgen++ will not start up 
 - 181 HPDatalogger no longer works with scripting, due to Detector Interface changes. 
 - 182 Queensgate requested positions were not being reached 
 - 193 Extend EPICSMotor getStatus method to enable PMAC control 
 - 194 StringInFile panel not central on screen 
 - 197 Detector transform process hard coded 
 - 199 px classes and starting/stopping adxv 
 - 200 Document updated Bugzilla procedures and correct status fields 
 - 201 put splash image into jar file 
 - 202 DummyTemp setPoint does not work 
 - 203 Configure CLASSPATH in jar file 

Release_4_4_0 17/12/2004 (alpha)
--------------------------------

 - Full replacement of JClam with Jython scripting. 
 - DataLogger code has been improved to work with scans 
 - The piezo package has been removed, and all piezo code moved into the motor package. 
 - Queensgate, QueensgatePanel, PiezoController and DummyPiezoController have all been reworked and have now had initial testing. 
 - NewportMotor code has been brought up to date with Castor. 
 - McLennanMotor, McLennanController, McLennanStepperMotor refactor for 381 channel select bugfix is complete.
 - medical imaging - All queensgate code should be ready for inclusion in the December release
 - Control of EPICS motors. 
 - Complete FIXME and TODO items where appropriate

Release_4_3_0 13/10/2004 (alpha)
--------------------------------

Javadoc workshop output, with no mods to actual code.

Release_4_2_0 12/10/2004 (alpha)
--------------------------------

 - PX Remote Detector, Camera and Sample Changer
 - Image display for loop centring.
 - Medical Imaging data logger.

Release_4_1_0 21/09/2004 (alpha)
--------------------------------

Problems have been encountered with release_4_0_x consequently it is un-usable.
Lessons have been learnt from this and incorporated into the CVS Use Policy. 
This in effect, replace the broken version 4.0. 

Release_4_0_0 18/08/2004 (alpha designate)
------------------------------------------

 - New XML schema using attributes and Castor and OEs become a composite of "moveables". DOF names are now parameterised in XML.
 - Initial Jython scripting for EXAFS and 5U within-side a Java GUI tabbed pane.
 - Replacement of plotting package with JFreeChart.
 - 5U monochromator and Undulator DOFs.
 - Control of Newport and Aerotech motors.

Release_3_3_0 23/06/2004
------------------------

 - Motor persistence code added
 - Test version of new scripting code as a tabbed pane
 - SimplePlot graphics has open source JFreeChart instead of commercial JChart
 - 5U1 code developments to date
 - Support for Exafs Xpress boards
 - New devices supported include :
    - National Instruments pci-6602 counter-timer
    - Aerotech motor (firewire)
    - Newport MM4006 (RS232)
    - Marlow temperature controller
    - pre grating monochromator (for use with undulator) a quantity factory some new DOFs, including a fixed polarization dof

Release_3_2_0 16/03/2004
------------------------

Follows phase 1 of editing for stlye and comment/Javadoc improvment and adherence to group standards.

Release_3_1_0 11/03/2004
------------------------

DoubleAxisAngularDOF - Fixed calculation of linear positions when doing a relative
move. Suppresed calculation of validity for now, always valid.
developers branch is scrapped and is replaced by the HEAD of the trunk/root

Release_3_0_0 16/01/2004
------------------------

 - Build/run requires Java 1.4, with recommended version of at least 1.3.1_04
 - Replacement of Orbacus CORBA with Jacorb (fixes bugs #1,#2,#3)
   - bug #1 gives occasional PC access violation crashes of Java VM during scans
   - bug #2 cross platform hanging in Positioners from Jython fixed (PCS)
   - bug #3 graphics plotting using CORBA MHPlotable objects often lose data points
 - Integration of Sample environments (GRM)
 - Sample environment plots (GRM)
 - Calibration channel setup GUI (GRM)
 - Interworks with JClam release_3_0_0

Known bugs :
^^^^^^^^^^^^
bug #4 with excessive object server activity, the abort panel can loses the
   abort/halt buttons and their functionality.

Release_2_0 (current) 31/03/03
------------------------------

 - GUI with tabbed panes for NCD/Exafs/OEMove/JClam interpreter (GRM,PCS) 
 - Integration of Rapid control (GRM)
 - 1D graphics for sax & wax (GRM)
 - Save/Load parameter files (GRM)
 - I0 and It status display (GRM)
 - PXGen GUI (SHK,KA)
 - ASCII representation files for OEMove (SHK)
 - JClam / GUI "MessageHandler" and "Abort" CORBA'ised for remote GUI's (GRM, MCM)
 - GUI graphics generated by JClam notifies in place using CORBA (MCM, GRM)
 - A test Java client PlotGUI is available (MCM)
 - DigitalIO interface added with DigitalIO base and DigitalIO6602 (MCM)
 - SEAM i/o interface for Pincer DLL sharing using JSeam.java / Seam.dll (MCM)
 - obsolete VUV.jar code removed (NV, MCM)
 - dl_compile.bat PC users to compile dl tree but Python script will replace it (MCM)

Release_1_0_branch 28/6/02
--------------------------

 - Initial release for OEMove
 - JClam observer-observable pattern for mutiple Java GUI's accessing JClam (MCM)
 - A test Java client CommandGUI is available (MCM)
 - JClam hard abort, soft abort and clear functions handled by Java (MCM)
 - non-DL code counter-timer card NI PCI-6602 via SEAM2.java / als6602.dll JNI (MCM)
