from gda.device.scannable import ScannableBase, PseudoDevice
import time
import java.lang.Exception  # @UnresolvedImport

KEY = 'SRSWriteAtFileCreation'


def _is_scannable(obj):
    return isinstance(obj, PseudoDevice) or isinstance(obj, ScannableBase)


def _check_all_scannable(objects):
    for obj in objects:
        if not _is_scannable(obj):
            raise TypeError("Object %s is not Scannable" % repr(obj))


class MetadataCollector(ScannableBase):
    """
    A zero-input-extra field Scannable that can be used in a scan to append
    the position of scannables to the header of an SRS file.

    For example:

    >>>mds = MetadataCollector("mds", globals(), [phi,chi,eta])  # make a small one
    >>>mdb = MetadataCollector("mdb", globals(), [enf,hkl,euler])  # make a big one
    >>>scan x 1 10 1 mds mdb
    """
    def __init__(self, name, rootNamespace=None, scannablesToRead=[]):
        """Create a MetadataCollector Scannable, for use with SRSDataWriters
        """
        self.name = name
        self.inputNames = []
        self.extraNames = []
        self.outputFormat = []

        self.rootNamespaceDict = rootNamespace
        self.scannables_to_read = scannablesToRead
        self.verbose = False
        self.quiet = False
        self.prepend_keys_with_scannable_names = True

    def set(self, *args):  # @ReservedAssignment
        _check_all_scannable(args)
        self.scannables_to_read = list(args)
        return self.ls()

    def ls(self):
        names = []
        for scn in self.scannables_to_read:
            names.append(scn.name)
        return ' '.join(names)

    def add(self, *args):
        _check_all_scannable(args)
        self.scannables_to_read.extend(args)
        return self.ls()

    def rm(self, *args):
        _check_all_scannable(args)
        for arg in args:
            try:
                self.scannables_to_read.remove(arg)
            except ValueError:
                print "Scannable %s not in %s" % (arg.name, self.name)
        return self.ls()

    def __str__(self):
        s = self.name + ':\n'
        s += self._createHeaderStringForScannables('%s = %s\n')
        return s

    def isBusy(self):
        return False

    def asynchronousMoveTo(self, _):
        pass

    def getPosition(self):
        pass

    def atScanStart(self):
        """
        Create or append metadata pairs to the SRSWriteAtFileCreation string
        in the rootNamespace dict. SRSWriteAtFileCreation will then be ready for
        the SRSScanWriter to write into the header when it is created after the
        first point of the scan has been completed.
        """
        if not self.quiet:
            print "Collecting metadata from: " + self.ls()

        self._append("\ndate='" + time.ctime() + "'\n")  # time
        self._append(self._createHeaderStringForScannables())  # positions

        if not self.quiet:
            print "Metadata collection complete"

    def atCommandFailure(self):
        self._clear()

    def atScanEnd(self):
        self._clear()

    def _append(self, s):
        """Not thread safe"""
        h = self.rootNamespaceDict.get(KEY, '')
        if h is None:
            h = ''
        self.rootNamespaceDict[KEY] = h + s

    def _clear(self):
        #Creates as a side effect
        self.rootNamespaceDict[KEY] = ''

    def _createNamePositionPairs(self, scn):
        pairs = []
        names = tuple(scn.inputNames) + tuple(scn.extraNames)
        formats = scn.outputFormat
        if self.verbose:
            t = time.time()
            print self.name + " Reading " + scn.name

        # Create a position string for each field
        try:
            pos = scn.getPosition()
            if isinstance(pos, (str, unicode)):
                pos = (str(pos),)
        except (Exception, java.lang.Exception), e:
            self._warn(" could not record the position of " + scn.name +
                       " as it's getPosition is throwing: " + repr(e))
            pos = ('unavailable',) * len(names)
            formats = ('%s',) * len(names)
        try:
            pos_tuple = tuple(pos)
        except TypeError:
            pos_tuple = (pos,)
        if len(names) != len(pos_tuple):
            raise ValueError(
                '%s could not collect metadata as the scannable %s returned a '
                'position inconsistent with its number of input/output field '
                'names' % (self.name, scn.name))

        if len(names) == 1:
            try:
                pairs.append((scn.name, formats[0] % pos_tuple[0]))
            except TypeError:
                pairs.append((scn.name, str(pos_tuple[0])))
        else:
            for fieldname, fmt, value in zip(names, formats, pos_tuple):
                if self.prepend_keys_with_scannable_names:
                    key = scn.name + '.' + fieldname
                else:
                    key = fieldname
                try:
                    pairs.append((key, fmt % value))
                except:
                    pairs.append((key, str(value)))

        if self.verbose:
            print "  %f s" % (time.time() - t)
        return pairs

    def _createHeaderStringForScannables(self, fmt='%s=%s\n'):
        s = ""
        for scn in self.scannables_to_read:
            pairs = self._createNamePositionPairs(scn)
            for key, value in pairs:
                s += fmt % (key, value)
        return s

    def _warn(self, msg):
        print "WARNING: ", msg


class MetadataOneOffNote():
    """A note command used to add quick notes to the next scan.

    For example:

    >>> note = MetadataOneOffNote(globals())
    >>> alias(note)
    >>> note 'big finger print on last sample, use this one'
    """

    def __init__(self, rootNamespaceDict=None, key='note'):
        self.rootNamespaceDict = rootNamespaceDict
        self.key = key

    def __call__(self, s):
        #Not thread safe
        h = self.rootNamespaceDict.get(KEY, '')
        if h in (None, ''):
            h = '\n'
        self.rootNamespaceDict[KEY] = h + "%s='%s'\n" % (self.key, s)