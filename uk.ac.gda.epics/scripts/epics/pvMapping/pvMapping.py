#!/usr/bin/env python
import argparse
import os
import xml.dom.minidom
import csv
from xml.parsers.expat import ExpatError
from os.path import commonprefix
from string import rstrip
from cgi import logfile

# The purpose of this script is to produce an inventory of the classes used to address
# each PV, and the corresponding dummy beans (where such exist).
# It should be run on a single beamline at a time, in order to avoid conflicts with identically-named beans.

# The simple case, where the bean contains the PV directly
#    e.g. ss5x_motor --> {'pvName': u'BL11J-MO-SSLID-05:X', 'liveClass': u'gda.device.motor.EpicsMotor'}                                                                                          
devices = {}

# The more complicated case, where the bean refers to an entry in the EPICS interface file
#    e.g. Ic1 --> {'deviceName': u'IC1.AMP', 'liveClass': u'gda.device.currentamplifier.EpicsCurrAmpSingle'}
devicesWithRef = {}

# Mapping of deviceName to pvName from the EPICS interface file
#    e.g. IC1.AMP --> BL11I-DI-IONC-01:PHD1
deviceNameToPV = {}

# The dummy objects corresponding to the beans in the "devices" dictionaries 
#    e.g. Ic1 --> gda.device.currentamplifier.DummyCurrentAmplifier
dummyObjects = {}

# Output directory (set from program arguments)
outputDir = ''

def main():
    args = configure_args()
    processPath(args.path, args.output)
    print 'Finished'


# Read command-line arguments
def configure_args():
    parser = argparse.ArgumentParser(description='List mapping of PVs to GDA objects')
    parser.add_argument('path', help='path to search (recursively) for PV mappings')
    parser.add_argument('output', help='output directory')
    return parser.parse_args()


# Top-level function to process all files below a given root path
def processPath(path, output):
    global outputDir
    outputDir = output
    if (not os.path.exists(outputDir)):
        os.makedirs(outputDir)
    
    global logFile
    logFilePath = os.path.join(outputDir, 'pvMapping.log')
    logFile = open(logFilePath, 'w')

    devices.clear()
    devicesWithRef.clear()
    deviceNameToPV.clear()
    dummyObjects.clear()

    for root, dirs, files in os.walk(path):
        for file in files:
            filename, fileExtension = os.path.splitext(file)
            if (fileExtension == '.xml'):
                processFile(root, file)

    resolveEpicsDevices()
    combineLiveAndDummy()
    printDictionary(devices, 'devices.txt')
    printCSV('devices.csv')
    
    logFile.close()


# Process an individual (XML) file        
def processFile(directory, file):
    filePath = os.path.join(directory, file)
    
    # If there is "dummy" anywhere in the file path, assume the file defines dummy objects  
    dummy = 'dummy' in filePath
    
    try:
        dom = xml.dom.minidom.parse(filePath)
    except ExpatError as e:
        printMessage('Failure parsing ' + filePath + ' ' + str(e))
        return
    
    for child in dom.childNodes:
        if (child.localName == 'beans'):
            # Bean definitions
            beans = child.getElementsByTagName('bean')
            for bean in beans:
                processBean(bean, dummy)
        elif (child.localName == 'devices'):
            # EPICS interface mapping
            for node in child.childNodes:
                if isinstance(node, xml.dom.minidom.Element):
                    processDevice(node)


# Process an individual bean (dummy or live)                                     
def processBean(bean, dummy):
    id = bean.getAttribute('id')
    if (not id):
        return
    
    gdaClass = bean.getAttribute('class')
    if (not gdaClass):
        printMessage('No class specified for bean with id ' + id)

    # Add to appropriate dictionary (dummy or live objects)
    if (dummy):
        dummyObjects[id] = gdaClass
    else:
        for property in bean.getElementsByTagName('property'):
            propertyName = property.getAttribute('name')
            if (propertyName == 'pvName'):
                # Bean references PV directly
                pvName = property.getAttribute('value')
                devices[id] = {'liveClass': gdaClass, 'pvName': pvName}
            elif (propertyName == 'deviceName'):
                # Bean references EPICS interface file
                deviceName = property.getAttribute('value')
                devicesWithRef[id] = {'liveClass': gdaClass, 'deviceName': deviceName}


# Process an EPICS device record
def processDevice(device):
    deviceName = device.getAttribute('name')
    
    # A simple device will contain a single <RECORD> element, but more complex devices
    # may contain several elements with different PVs e.g. a Femto may contain
    # <OVERLOAD>, <SETGAIN>, <SETACDC>, <VENDOR> and <MODEL>, each with a PV.
    # In this case, find the common prefix of the PVs.
    pvs = []
    for record in device.childNodes:
        if (isinstance(record, xml.dom.minidom.Element)):
            pv = record.getAttribute('pv')
            if (pv):
                pvs.append(pv)
                
    pv = rstrip(commonprefix(pvs), ':')
    if (pv):
        deviceNameToPV[deviceName] = pv


# Resolve references to EPICS devices.
def resolveEpicsDevices():
    for deviceId in devicesWithRef:
        deviceRecord = devicesWithRef[deviceId]
        deviceName = deviceRecord['deviceName']
        liveClass = deviceRecord['liveClass']
        
        if deviceName in deviceNameToPV:
            pvName = deviceNameToPV[deviceName]
        else:
            printMessage('No PV mapping found for device ' + deviceName)
            pvName = ''
        
        devices[deviceId] = {'pvName': pvName, 'deviceName': deviceName, 'liveClass': liveClass}


# For every live object, add the dummy class if there is one
def combineLiveAndDummy():
    for objectId in devices:
        if (objectId in dummyObjects):
            devices[objectId]['dummyClass'] = dummyObjects[objectId]


# Print a mapping in Python format 
def printDictionary(dictionary, fileName):
    filePath = os.path.join(outputDir, fileName)
    f = open(filePath, 'w')
    for entry in dictionary:
        f.write(entry + ' ' + str(dictionary[entry]) + '\n')
    f.close()
  

# Print the PV mapping to a csv file
def printCSV(fileName):
    filePath = os.path.join(outputDir, fileName)
    with open(filePath, 'wb') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['id', 'deviceName', 'pvName', 'liveClass', 'dummyClass'])
        
        for objectId in devices:
            entry = devices[objectId]
            deviceName = ''
            if ('deviceName' in entry):
                deviceName = entry['deviceName']
            row = [objectId, deviceName, entry['pvName'], entry['liveClass']]
            if ('dummyClass' in entry):
                row.append(entry['dummyClass'])
            csvwriter.writerow(row)


def printMessage(message):
    print message
    logFile.write(message + '\n')
   

if __name__ == '__main__':
    main()
