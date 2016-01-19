#!/usr/bin/env python

import argparse
import os.path
import pvMapping

# Run pvMapping (q.v.) on all beamline configurations

def runMapping():
    args = configure_args()
    codeRoot = args.codeRoot
    outputRoot = args.output
    
    beamlineDirectories = ['gda-dls-beamlines-b23.git',
                           'gda-dls-beamlines-i09.git',
                           'gda-dls-beamlines-i11.git',
                           'gda-dls-beamlines-i12.git',
                           'gda-dls-beamlines-i13x.git',
                           'gda-dls-beamlines-i21.git',
 
                           'gda-dls-beamlines-xas.git/b18',
                           'gda-dls-beamlines-xas.git/i08',
                           'gda-dls-beamlines-xas.git/i14',
                           'gda-dls-beamlines-xas.git/i18',
                           'gda-dls-beamlines-xas.git/i20',
                           'gda-dls-beamlines-xas.git/i20-1',

                           'gda-mt.git/configurations/b16-config',
                           'gda-mt.git/configurations/b24-config',
                           'gda-mt.git/configurations/i06-config',
                           'gda-mt.git/configurations/i07-config',
                           'gda-mt.git/configurations/i10-config',
                           'gda-mt.git/configurations/i10-shared',
                           'gda-mt.git/configurations/i15-1-config',
                           'gda-mt.git/configurations/i15-config',
                           'gda-mt.git/configurations/i16-config',
                           'gda-mt.git/configurations/mt-config',
 
                           'gda-mx.git/configurations/i02-config',
                           'gda-mx.git/configurations/i03-config',
                           'gda-mx.git/configurations/i04-1-config',
                           'gda-mx.git/configurations/i04-config',
                           'gda-mx.git/configurations/i23-config',
                           'gda-mx.git/configurations/i24-config'
                           'gda-mt.git/configurations/mt-config']


    for beamlineDir in beamlineDirectories:
        codePath = os.path.join(codeRoot, beamlineDir)
        outputPath = os.path.join(outputRoot, beamlineDir)
        pvMapping.processPath(codePath, outputPath)

    print "Finished"


# Read command-line arguments
def configure_args():
    parser = argparse.ArgumentParser(description='List mapping of PVs to GDA objects for multiple beamlines')
    parser.add_argument('codeRoot', help='root of beamline directories (will usually be called <something>/workspace_git')
    parser.add_argument('output', help='output directory')
    return parser.parse_args()


if __name__ == '__main__':
    runMapping()