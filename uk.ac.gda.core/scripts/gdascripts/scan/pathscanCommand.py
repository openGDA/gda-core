from gda.device.scannable.scannablegroup import ScannableGroup
from gdascripts.scan.installStandardScansWithProcessing import scan
from gda.jython.commands.GeneralCommands import alias
from gda.jython import InterfaceProvider

def create_pathgroup(scannables_list):
    pathgroup = ScannableGroup("pathgroup")
    pathgroup.setGroupMembersWithList(scannables_list, True)
    #Add to Jython namespace in case anything relies on being able to find all scannables that take part in a scan
    InterfaceProvider.getJythonNamespace().placeInJythonNamespace(pathgroup.getName(), pathgroup);
    return pathgroup

def pathscan(scannables, path, *args):
    """
    Usage:
    pathscan (x,y,z) ([1,1,1],[2,2,2],[3,4,5]) edxd 5.0

    Scan a group of scannables following the specified path and collect data at each point from scannables args.
    """
    pathgroup = create_pathgroup(scannables)

    new_args = [pathgroup, path] + list(args)
    scan(new_args)

alias("pathscan")