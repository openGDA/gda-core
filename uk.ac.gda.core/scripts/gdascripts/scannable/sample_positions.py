from gda.device import DeviceException #@UnresolvedImport
from gda.device.scannable import ScannableBase #@UnresolvedImport
from gda.jython import InterfaceProvider #@UnresolvedImport

import json
import os
import logging
import copy
from collections import OrderedDict

def dict_values_to_list(position_row):
    return [values for values in position_row.values()]

def key_value_positions_to_str(key_positions):
    #This is needed because even though we use an OrderedDict, it only iterates in order.
    #Using in a string it still displays it in random order. Use this to display in order.
    string = "{"
    for i, (k, v) in enumerate(key_positions.iteritems()):
        if i != 0:
            string = string + ", "
        string = string + str(k) + ": " + str(v)
    string = string + "}"
    return string

class SamplePositions(ScannableBase):
    """
    Syntax:
    1: NAME.newfile(output_filename=None, override=False) - Create a new set of sample positions to be saved to a file.
        If output_filename=None, create file using default. If file already exists, overwrite must be True. Otherwise will throw error.
    2: NAME.loadfile(input_filename=None, output_filename = None) - Store positions from existing file into scannable.
        If input_filename=None, load the default file. If output_filename=None, then use the input_filename to save and override new positions to.
    3: NAME.savepos(key) - Save the current sample manipulator positions to a key in an OrderedDict. Will also be saved to file.
    4: NAME.changekey(oldkey, newkey) - Change an existing key storing sample positions to a new one.
    5: NAME.removekey(key) - Remove an existing key holding sample positions
    6: NAME.addexcluded(scannable) - Add a sample manipulator scannable which will be ignored for moving and displaying positions.
    7: NAME.removeexcluded(scannable) - Allow a sample manipulator scannable to take part in moving and displaying positions again.
    8: NAME.sortkeys() - Sort the keys in numerical and then alphabetical order.
    9: pos NAME "1a" - Move the sample manipulator to the positions saved at "1a". Scannables excluded will not be moved.
    10: scan NAME ("1a", "1b", "1c", ...) - Scan the sample manipulator over the positions saved at ("1a", "1b", "1c", ...). Scannables excluded will not be moved.
    """

    DEFAULT_POSITION = None
    FILE_EXTENSION = "json"
    VALUE_UNAVAILABLE = "UNAVAILABLE"

    def __init__(self, name, scannables):
        self._SCANNABLES = tuple(scannables)
        self._EXCLUDED_SCANNABLES = []
        self.setName(str(name))
        self._configured = False
        self._filename = None
        self._cached_file_data = {}
        self._cached_file_data["excluded_scannables"] = []
        self.key = None
        self.logger = logging.getLogger(self.__class__.__module__ + "." + self.__class__.__name__)
        self.logger.info("Created %s", self.getName())
        self.dict_mode = True

        #Update the doc string to be the name of this object for useful help text
        self.__doc__ = self.__doc__.replace("\n", "", 1).replace(" NAME", " " + self.getName())

    def getExcludedScannables(self):
        return self._EXCLUDED_SCANNABLES

    def getExcludedScannableNames(self):
        return [str(excluded_scannable.getName()) for excluded_scannable in self.getExcludedScannables()]

    def addexcluded(self, scannable):
        """
        Add a scannable to exclude from saving positions

        Parameters
        ----------
        scannable: str, Scannable
            Scannable name or scannable object to search through getScannables() to exclude
        """
        self.checkConfiguration()
        #Check this is a valid scannable
        scannable = self.getScannables()[self.getScannableIndex(scannable)]
        self._EXCLUDED_SCANNABLES.append(scannable)
        self._cached_file_data["excluded_scannables"] = self.getExcludedScannableNames()
        print("\nAdded {} to excluded_scannables list".format(scannable.getName()))
        print("excluded_scannables: {}".format(self.getExcludedScannableNames()))

        if self._getCachedPositions() is not None:
            self._savejson(self.getFilename(), self._cached_file_data)

    def removeexcluded(self, scannable):
        """
        Remove a scannable from the exclude_scannables list to allow saving this position again

        Parameters
        ----------
        scannable: str, Scannable
            Scannable name or scannable object to remove from exclude_scannables
        """
        self.checkConfiguration()
        #If provided a string, search for scannable obj equivalent via getName()
        if isinstance(scannable, basestring):
            scannable = self.getScannables()[self.getScannableIndex(scannable)]
        self._EXCLUDED_SCANNABLES.remove(scannable)
        self._cached_file_data["excluded_scannables"] = self.getExcludedScannableNames()
        print("\nRemoved {} from excluded_scannables list".format(scannable.getName()))
        print("excluded_scannables: {}".format(self.getExcludedScannableNames()))
        self._savejson(self.getFilename(), self._cached_file_data)

    def isScannablesExcluded(self):
        return len(self.getExcludedScannables()) > 0

    def getScannables(self, remove_excluded = False):
        if remove_excluded:
            return [scannable for scannable in self._SCANNABLES if scannable not in self.getExcludedScannables()]
        return self._SCANNABLES

    def getScannableNames(self, remove_excluded = False):
        return [str(scannable.getName()) for scannable in self.getScannables(remove_excluded)]

    def getScannableIndex(self, scannable):
        scannables = self._SCANNABLES
        if isinstance(scannable, basestring):
            scannables = self.getScannableNames()
            scannable_name = scannable
        else:
            scannable_name = scannable.getName()
        try:
            return scannables.index(scannable)
        except ValueError:
            raise ValueError("{} is not a valid scannable. Please choose from: {}".format(scannable_name, self.getScannableNames()))

    def _createDefaultPosition(self):
        default_pos = OrderedDict()
        for scannable_name in self.getScannableNames(remove_excluded = False):
            default_pos[scannable_name] = SamplePositions.DEFAULT_POSITION
        return default_pos

    def getMaxPosIndex(self):
        return len(self._getCachedPositions())

    def _savejson(self, filename, data):
        with open(filename, 'w') as f:
            json.dump(data, f, indent = 4)
        self.logger.info("Saved to file: %s", filename)

    def newfile(self, output_filename = None, override = False):
        """
        Configure number of new positions that can be stored and saved to a file.

        Parameters
        ----------
        output_filename : str, optional
            Default is None. The output_filename path to save new positions. Can be absolute or relative path from data directory. If None, uses objects name. Will override existing file.
        override: boolean, optional
            Default is False. Stops overriding existing positions file unless explicitly given argument. Raises OSError if False and file already exists
        """

        output_filename = self._createDefaultFileName(output_filename)

        if os.path.isfile(output_filename) and not override:
            raise OSError(
                "File \"{}\" already exists. \nTo override, provide override = True as an argument or use {}.loadpos(file_to_load=None, output_filename=None)".format(
                    output_filename, self.getName()
                )
            )

        if override:
            print("WARNING: File \"{}\" already exists! \nOverriding with new positions on next save.".format(output_filename))

        self._cached_file_data = OrderedDict()
        self._cached_file_data["positions"] = OrderedDict()

        if self.isScannablesExcluded():
            self._cached_file_data["excluded_scannables"] = self.getExcludedScannableNames()

        self._cached_file_data["name"] = self.getName()

        self._configured = True
        self._filename = output_filename

        print("Saving new positions to output_filename: {}\n".format(self.getFilename()))

    def loadfile(self, input_filename = None, output_filename = None):
        """
        Load existing positions from a file.

        Parameters
        ----------
        input_filename : str, optional
            Default is None. The input_filename path to read in positions. Can be absolute or relative path from data directory. If None, uses objects name.
        output_filename : str, optional
            Default is None. The output_filename path to save new positions. Can be absolute or relative path from data directory. If None, uses input_filename. Will override existing file.
        """

        input_filename = self._createDefaultFileName(input_filename)
        if output_filename == None:
            output_filename = input_filename
        output_filename = self._createDefaultFileName(output_filename)

        with open(input_filename) as f:
            self._cached_file_data = json.load(f, object_pairs_hook=OrderedDict)
        self._cached_file_data["positions"] = OrderedDict(self._getCachedPositions())

        #Load in excluded scannables from name to scannable object
        if "excluded_scannables" in self._cached_file_data.keys():
            for excluded_scannable in self._cached_file_data["excluded_scannables"]:
                excluded_scannable_index = self.getScannableIndex(excluded_scannable)
                excluded_scannable = self.getScannables()[excluded_scannable_index]
                self._EXCLUDED_SCANNABLES.append(excluded_scannable)

        self._filename = output_filename

        self._configured = True

        print("Loaded positions from input_file {}".format(input_filename))
        print("Number of positions: {}".format(self.getMaxPosIndex()))
        print("Saving new positions to output_filename: {}\n".format(self.getFilename()))

    def savepos(self, key):
        """
        Save position of current sample positions as a key in a OrderedDict. Positions will also be saved to file.

        Parameters
        ----------
        key: str
            The key that you would like to save the position to.
        """

        self.checkConfiguration()

        key = str(key)
        overwriting = True
        prev_positions_filtered = self.getSavedPositions(remove_excluded=True)
        new_positions_filtered = self._getCachedPositions()
        if key not in prev_positions_filtered:
            prev_positions_filtered[key] = self._createDefaultPosition()
            new_positions_filtered[key] = self._createDefaultPosition()
            print("Creating new position as key \"{}\" doesn't exist.".format(key))
            overwriting = False

        prev_pos_row_filtered = prev_positions_filtered[key]
        new_pos_row_filtered = new_positions_filtered[key]

        for ikey in new_pos_row_filtered:
            for scannable in self.getScannables(remove_excluded=False):
                if scannable.getName() == ikey:
                    new_pos_row_filtered[ikey] = scannable.getPosition()

        self._savejson(self.getFilename(), self._cached_file_data)

        new_pos_row_filtered = self.getSavedPositions(remove_excluded=True)[key]
        if overwriting:
            print("Overwriting position {}".format(key))
            print("Previous position: {}".format(key_value_positions_to_str(prev_pos_row_filtered)))
            print("New position: {}".format(key_value_positions_to_str(new_pos_row_filtered)))
        else:
            print("Saved position: \"{}\" : {}".format(key, key_value_positions_to_str(new_pos_row_filtered)))

    def sortkeys(self):
        """
        Sort the keys in numerical and then alphabetical order
        """
        self.checkConfiguration()
        # helper function to perform sort
        def num_sort(ordered_dict_tuple):
            if ordered_dict_tuple[0].isnumeric():
                return float(ordered_dict_tuple[0])
            else:
                return ordered_dict_tuple[0]

        saved_positions = self._getCachedPositions()
        sorted_dict = OrderedDict(sorted(saved_positions.items(), key=num_sort))
        self._cached_file_data["positions"] = sorted_dict

        print("Successfully sorted the keys!")

    def changekey(self, oldkey, newkey):
        """
        Change an existing key holding positions to a new one

        Parameters
        ----------
        oldkey: the existing key you want to update
        newkey: the key you want to change it to
        """
        self.checkConfiguration()

        oldkey = str(oldkey)
        newkey = str(newkey)
        positions_dict = self._getCachedPositions()
        #Change key without losing key position in OrderedDict
        for _ in range(len(positions_dict)):
            k, v = positions_dict.popitem(False)
            positions_dict[newkey if oldkey == k else k] = v

        if self.getKey() == oldkey:
            self.key = newkey

        self._savejson(self.getFilename(), self._cached_file_data)
        print("Position key changed from {} to {} successfully".format(oldkey, newkey))

    def removekey(self, key):
        """
        Delete an existing key

        Parameters
        ----------
        key: the existing key that you want to remove from the sample positions.
        """
        self.checkConfiguration()
        key = str(key)

        saved_positions = self.getSavedPositions(remove_excluded=True)[key]
        del self._getCachedPositions()[key]
        print("Position key {} : {} successfully removed.".format(key, saved_positions))

    #@Override
    def asynchronousMoveTo(self, key):
        key = str(key)
        self.checkConfiguration()

        #Needed because scan returns integers as floats, so need to convert from float to integer back to string
        if key not in self.getSavedPositions(remove_excluded=True):
            key = str(int(float(key)))

        saved_positions = self.getSavedPositions(remove_excluded=True)[key]
        for scannable in self.getScannables(remove_excluded=True):
            if scannable.getName() in saved_positions:
                new_scannable_pos = saved_positions[scannable.getName()]
                if new_scannable_pos is None:
                    raise DeviceException("New position cannot be None for key: {}, scannable: {}".format(new_scannable_pos, scannable.getName()))
                scannable.asynchronousMoveTo(new_scannable_pos)
        self.key = key

    #@Override
    def isBusy(self):
        busy = False
        for scannable in self.getScannables(remove_excluded=True):
            busy = busy or scannable.isBusy()
        return busy

    #@Override
    def getPosition(self):
        self.checkConfiguration()

        key = self.getKey()
        if key == None:
            key = "NOT SELECTED"
            positions_row = {}
            for scannable in self.getScannableNames(remove_excluded=True):
                positions_row[scannable] = SamplePositions.VALUE_UNAVAILABLE
        else:
            positions_row = self.getSavedPositions()[key]
        return [key] + dict_values_to_list(positions_row)

    def checkConfiguration(self):
        if not self._configured:
            raise DeviceException("\n{} is not configured. Please use: \n\t{}.newfile(output_filename=None, override=False) \nOR \n\t{}.loadpos(file_to_load=None, output_filename=None)".format(self.getName(), self.getName(), self.getName()))

    def _createDefaultFileName(self, filename = None):
        """
        Create the default filename using a getName() by default, or provide a relative or absolute path

        Parameters
        ----------
        filename: str, optional
            the filename to build off. If an absolute path, returns same value. If relative path, join data directory to it. If None, use getName() to build have relative path.
        """

        if filename == None:
            filename = self.getName()

        filename = os.path.splitext(filename)[0]
        extension = SamplePositions.FILE_EXTENSION

        filename = filename + "." + extension

        if os.path.isabs(filename):
            return filename

        if not os.path.exists(self._getDefaultFilePath()):
            os.makedirs(self._getDefaultFilePath())

        return os.path.join(self._getDefaultFilePath(), filename)

    #@Override
    def __str__(self):
        self.checkConfiguration()

        positions = self.getSavedPositions(remove_excluded=True)
        SELECTED = " --> SELECTED"
        filename = "\nfile: " + self.getFilename()
        string = filename + "\nname: " + self.getName()

        for key, position_values in positions.iteritems():
            string = string + "\n" + str(key) + ": " + key_value_positions_to_str(position_values)
            if self.getKey() is not None and self.getKey() == key:
                string = string + SELECTED

        string = string + "\nexcluded_scannables: " + str(self.getExcludedScannableNames()) + "\n"

        return string

    #@Override
    def getInputNames(self):
        return ["key"]

    #@Override
    def getExtraNames(self):
        return [str(scannable_name) for scannable_name in self.getScannableNames(remove_excluded=True)]

    #@Override
    def getOutputFormat(self):
        pos_formats = ["%f"] * len(self.getScannableNames(remove_excluded=True))
        return ["%i"] + pos_formats

    def getFilename(self):
        return self._filename

    def setFilename(self, filename):
        self._filename = self._createDefaultFileName(filename)

    def _getCachedPositions(self):
        if "positions" in self._cached_file_data:
            return self._cached_file_data["positions"]
        return None

    def getSavedPositions(self, remove_excluded = True):
        """
        Return an OrderedDict containing a copy of all of the saved positions.

        Parameters
        ---------
        remove_excluded: boolean, optional
            Default is True.
            If True, returns a list of saved positions while filtering out the excluded_scannable positions
            If False, returns a list containing all positions including excluded_scannable
        """
        positions = copy.deepcopy(self._getCachedPositions())

        #If we have excluded scannables, we need to filter these out from the positions
        if remove_excluded:
            positions = self._removeExcludedFromPositions(positions)

        return positions

    def _removeExcludedFromPositions(self, positions):
        #If we have excluded scannables, we need to filter these out from the positions
        if self.isScannablesExcluded() :
            for position_values in positions.values():
                for key in position_values:
                    if key in self.getExcludedScannableNames():
                        del position_values[key]

        return positions

    def getKey(self):
        return self.key

    def _getDefaultFilePath(self):
        return InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("")

    #Override
    def stop(self):
        self.logger.info("Attempting to stop {}".format(self.getName()))
        for scannable in self.getScannables(remove_excluded=True):
            self.logger.info("Stopping {}".format(scannable.getName()))
            scannable.stop()
        self.logger.info("Successfully stopped all scannables.")
