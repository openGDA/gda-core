"""Utility script to help manage BSL file conversion"""
from gda.configuration.properties import LocalProperties
import subprocess
import os
COMMAND_PROPERTY = "gda.scan.executeAtEnd"

def bslConversionCommand():
    """Get the command used to convert files - specified in java.properties"""
    return LocalProperties.get(COMMAND_PROPERTY)


def createBslFiles(create, command=bslConversionCommand()):
    """Automatically convert scan files to bsl

    Arguments:
    create: if True, scan files are converted
    command: (optional) if a different command to the default is required
        it can be specified here
    """
    if create:
        LocalProperties.set(COMMAND_PROPERTY, command)
    else:
        LocalProperties.set(COMMAND_PROPERTY, "")

def convertFileToBsl(filepath, check=False):
    """manually convert a file to BSL format

    Arguments:
    filepath: absolute path of file to convert
    check: (optional - defaults to False) if True, return exit code from command
    """

    if not os.path.exists(filepath):
        raise IOError('"%s" does not exist' %(filepath))
    result = subprocess.check_call([bslConversionCommand(), filepath])
    if check:
        return result

def convertAllFilesToBsl(topDir, recursive=False):
    """Convert all nxs files in directory to BSL

    Arguments:
    topDir: the directory to look in for files to convert
    recursive: (optional - defaults to False) if true, files in subdirectories are included as well
    """

    topDir = topDir.rstrip('/')
    for path, dirs, files in os.walk(topDir):
        for f in files:
            if f.endswith("nxs"):
                full = path + os.sep + f
                result = convertFileToBsl(full, True)
                print "converting: %s - %s" %(full, "success" if result == 0 else "failed")
        if not recursive:
            break
