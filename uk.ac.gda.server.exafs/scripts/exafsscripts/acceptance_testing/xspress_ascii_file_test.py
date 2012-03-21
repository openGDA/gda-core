"""
This is a test of the ascii file structure for XAS beamlines.

This should performed for new GDA installations.

To use this script:

1. place pairs of xspress config files and matching expected data files
in a folder: config/scripts/acceptance_testing. This should be a Jython package.

2. write and run a script which makes calls to the testScanAgainstConfig 
method defined in this script using those pairs of config and data files (see 
I20 config for an example)

"""

from gda.configuration.properties import LocalProperties
from gda.scan import ConcurrentScan

def getHeader(lines):
    headerlines = []
    for line in lines:
        if line[0] == '#':
            headerlines.append(line)
        else:
            return headerlines
    return headerlines

def getFooter(lines):
    footerlines = []
    haveSeenData = False
    for line in lines:
        if line[0] == '#' and haveSeenData:
            footerlines.append(line)
        else:
            haveSeenData = True
    return footerlines

def testSameFormat(originalFileName, newFileName):
    
    originalFile = open(originalFileName,'r')
    testFile = open(newFileName,'r')
    
    original_lines = originalFile.readlines()
    test_lines = testFile.readlines()
    
    original_header = getHeader(original_lines)
    test_header = getHeader(test_lines)
    
    if len(original_header) != len(test_header):
        raise "Headers are different!"
    print "\t Headers are the same length"
    
    for i in range(1,len(original_header)):
        origlineName = original_header[i].split(":")[0]
        testLineName = test_header[i].split(":")[0]
        if origlineName != testLineName:
            print "Expected:",origlineName
            print "Got:",testLineName
            raise "Headers are not the same!"
    print "\t Headers match"
        
    original_footer = getFooter(original_lines)
    test_footer = getFooter(test_lines)
    
    if len(original_footer) != len(test_footer):
        raise "Footers are different!"
    print "\t Footers are the same length"

    for i in range(1,len(original_footer)):
        origlineName = original_footer[i].split(":")[0]
        testLineName = test_footer[i].split(":")[0]
        if origlineName != testLineName:
            print "Expected:",origlineName
            print "Got:",testLineName
            raise "Footers are not the same!"
    print "\t Footers match"
        
        
def testScanAgainstConfig(testScannable,xspressDetector,ionchambers,configFileName,dataFileName,ffOnly = True):
    # ensure we are using the XasAsciiDataWriter or the XasAsciiNexusDataWriter
    current_data_format = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
    if current_data_format[0:8] != 'XasAscii' :
        raise "Wrong data writer format in use!"
    
    print "Running a test scan using Xspress config in file",configFileName
    
    # set the xspress detector settings
    configDir = LocalProperties.get("gda.config")
    scalers_only_configfile = configDir + "/scripts/acceptance_testing/" + configFileName
    print "Configuring Xspress using file",scalers_only_configfile,"..."
    xspressDetector.setConfigFileName(scalers_only_configfile)
    xspressDetector.configure()
    xspressDetector.setOnlyDisplayFF(ffOnly)
    
    # run a simple scan and pick up the data file name
    print "Running a simple scan..."
    args = [testScannable, 1, 3, 1, xspressDetector, ionchambers, 1]
    myscan = ConcurrentScan(args)
    myscan.runScan()
    file = ""
    if current_data_format == "XasAsciiDataWriter" :
        file = myscan.getDataWriter().getCurrentFileName()
    else :
        file = myscan.getDataWriter().getAsciiFileName()  # don't want the nexus file
    
    # compare the data file header format, column headings and footer format
    testFile = configDir + "/scripts/acceptance_testing/" + dataFileName
    print "Testing output against test data file",dataFileName,"..."
    testSameFormat(testFile,file)

        
    