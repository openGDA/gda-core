"""Utility script to help manage BSL file conversion"""
from gda.configuration.properties import LocalProperties
from gda.factory import Finder
from gda.observable import IObserver
from uk.ac.diamond.daq.persistence.jythonshelf import LocalParameters
from gda.data.metadata import GDAMetadataProvider
BSL_CONVERSION_CONFIGURED = 'gda.scan.processing.bsl.configured'

RUNNER = Finder.getInstance().find('bslConversion')

class BslConversionUpdater(IObserver):
    def update(self, source, arg):
        print 'Visit changed to ' + str(arg)
        restore()

if not LocalProperties.contains(BSL_CONVERSION_CONFIGURED):
    LocalProperties.set(BSL_CONVERSION_CONFIGURED, 'True')
    for meta in GDAMetadataProvider.getInstance().getMetadataEntries():
        if meta.name == 'visit':
            break
    if meta:
        meta.addIObserver(BslConversionUpdater())


CONFIGURATION_FILE = 'bslUsers'
DEFAULT = False

STORE = LocalParameters.getXMLConfiguration(CONFIGURATION_FILE)

def isConvertingOn():
    """Check whether new files will be converted automatically"""
    return RUNNER.enabled

def createBslFiles(create):
    """Automatically convert scan files to bsl

    Arguments:
    create: if True, scan files are converted
    """
    RUNNER.enabled = create
    _store(create)

def convertFileToBsl(filepath):
    """manually convert a file to BSL format

    Arguments:
    filepath: absolute path of file to convert
    """

    if not os.path.exists(filepath):
        raise IOError('"%s" does not exist' %(filepath))
    RUNNER.getRunner().triggerProcessing(filepath)

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

def restore():
    """restore preferences for visit - this is called automatically when visit changes"""
    STORE.reload()
    create = STORE.getBoolean(_get_store_key(), DEFAULT)
    print 'restoring bsl conversion preferences'
    createBslFiles(create)

def _store(create):
    STORE.reload()
    key = _get_store_key()
    if create != DEFAULT or STORE.containsKey(key):
        STORE.setProperty(key, create)
        STORE.save()

def _get_store_key():
    visit = GDAMetadataProvider.getInstance().getMetadataValue('visit')
    key = '%s.bsl' %visit
    return key
