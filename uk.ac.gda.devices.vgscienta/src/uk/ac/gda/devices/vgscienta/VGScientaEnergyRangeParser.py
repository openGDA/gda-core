#!/usr/bin/env python

'''
This script allows you to auto-generate the Spring XML configuration file needed for the
VGScientaAnalyserEnergyRange class from the data provided from SES.

To get the data from SES Calibration -> Voltages -> View -> Energy Range -> Copy To Clipboard
and then save it into a file (called SES_energy_range.txt by default) it then outputs a Spring
XML file called analyser_energy_range.xml which can be placed straight into the configuration.

This script is intended for developer use only and should not be considered robust!
'''
__author__ = 'James Mudd'


import csv

inputFileName = 'SES_energy_range.txt'
outputFileName = 'analyser_energy_range.xml'

print 'Running VG Scienta energy range parser'

newPsuMode = False
psuModes = []
lensModes = []

with open(inputFileName) as tsv:

    lines = csv.reader(tsv, delimiter='\t')

    for line in lines:
        if newPsuMode == True:
            newPsuMode = False
            psuModes.append(line[0])
            passEnergies = line[1:]
            continue

        if len(line) == 0:
            newPsuMode = True
            continue

        if lensModes.count(line[0]) == 0:
            lensModes.append(line[0])
        # print line

# Clean up the pass energies list remove empty strings
passEnergies = filter(None, passEnergies)

print 'PSU modes:', psuModes, 'Pass energies:', passEnergies, 'Lens modes:', lensModes

def lookupEnergyRange(lines, psuMode, lensMode, passEnergies, passEnergy):
    with open(inputFileName) as tsv:
        lines = csv.reader(tsv, delimiter='\t')

        for line in lines:
            # First find the PSU mode line
            if len(line) == 0 or line[0] != psuMode:
                continue

            for line in lines:
                # Then find the lens mode under the correct PSU mode
                if line[0] != lensMode:
                    continue

                # Now find the correct column
                column = passEnergies.index(passEnergy) + 1
                energyRange = line[column]
                print 'Energy range for', psuMode, lensMode, passEnergy, 'is', energyRange

                if energyRange == '-' or energyRange == 'none':
                    return None
                else:
                    return energyRange.split('-')

# Header for Spring XML. Tabs are important!
header = '''<?xml version="1.0" encoding="UTF-8"?>

<!-- This file was auto generated using VGScientaEnergyRangeParser.py

    You should create this file from the data obtained from SES software via:
    Calibration  -> Voltages -> View -> Energy Range -> Copy To Clipboard
    then save this into a text file which VGScientaEnergyRangeParser.py can convert
    into this spring configuration file. -->

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="analyser_energy_range" class="uk.ac.gda.devices.vgscienta.VGScientaAnalyserEnergyRange">
        <constructor-arg>
            <map>\n'''

# Footer for Spring XML. Tabs are important!
footer = '''            </map>
        </constructor-arg>
    </bean>
</beans>'''

print 'Starting build the Spring XML file'
with open(outputFileName, 'w') as xml:
    # Write header
    xml.write(header)

    # Loop over PSU modes
    for psuMode in psuModes:
        xml.write('\t' * 4 + '<entry key="' + psuMode + '">\n')
        # Loop over lens modes
        xml.write('\t' * 5 + '<map>\n')
        for lensMode in lensModes:
            xml.write('\t' * 6 + '<entry key="' + lensMode + '">\n')
            xml.write('\t' * 7 + '<map>\n')
            # Loop over pass energies
            for passEnergy in passEnergies:
                # Get the energy range for this PSU, lens and PE combination
                energyRange = lookupEnergyRange(lines, psuMode, lensMode, passEnergies, passEnergy)
                if energyRange != None:  # If this is a valid combination make a entry for it
                    xml.write('\t' * 8 + '<entry key="' + passEnergy + '">\n')
                    xml.write('\t' * 9 + '<bean class="uk.ac.gda.devices.vgscienta.VGScientaAnalyserEnergyRange$EnergyRange">\n')
                    xml.write('\t' * 10 + '<constructor-arg><description>MinKE</description><value>' + energyRange[0] + '</value></constructor-arg>\n')
                    xml.write('\t' * 10 + '<constructor-arg><description>MaxKE</description><value>' + energyRange[1] + '</value></constructor-arg>\n')
                    xml.write('\t' * 9 + '</bean>\n')
                    xml.write('\t' * 8 + '</entry>\n')
            xml.write('\t' * 7 + '</map>\n')
            xml.write('\t' * 6 + '</entry>\n')
        xml.write('\t' * 5 + '</map>\n')
        xml.write('\t' * 4 + '</entry>\n')

    # Write the footer
    xml.write(footer)

print 'Finished creating file:', xml.name
