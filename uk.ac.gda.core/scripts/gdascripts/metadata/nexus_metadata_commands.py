from uk.ac.gda.epics.nexus.device import EpicsNexusMetadataUtility
from org.eclipse.scanning.device.utils import NexusMetadataUtility
from argparse import ArgumentError
from org.eclipse.scanning.device.utils.NexusMetadataUtility.FieldType import SCALAR, SCANNABLE, PV, LINK  # @UnresolvedImport
from gda.device import Scannable
from gda.jython.commands.GeneralCommands import alias

def add_meta(device_name, field_name, field, unit=None, field_type=SCANNABLE, nexus_class="NXcollection"):
    """
    add a specified field with specified field name to a specified device of specified nexus class in metadata

    @param device_name: the group under which the new field to be added to
    @param field: field from which its value to be add
    @param field_name: the node name of the field in nexus file
    @param unit: the physical unit for this field if available, default is None
    @param field_type: the type of field to be added, the default is SCANNABLE
    @param nexus_class: the nexus class of the device to be created for the new device_name, only required if creating a new group, the default is NXcollection
    """
    if field_type in [SCALAR, PV, LINK] and field_name is None:
        raise ArgumentError("field_name must be provided !")
    if field_type == PV:
        EpicsNexusMetadataUtility.INSTANCE.add(device_name, field_name, field, unit, nexus_class)
    else:
        NexusMetadataUtility.INSTANCE.add(device_name, field_name, field, unit, field_type, nexus_class)

alias("add_meta")

# typed function for convenience
def add_meta_scalar(device_name, field_name, field, unit=None, nexus_class="NXcollection"):
    '''
    add a SCALAR field with a given field name to the specified device group.
    @param device_name: the name of metadata device
    @param field_name: the name of the field to be added to the device
    @param field: the SCALAR value to be added.
    @param unit : the physical unit for this field if available, default is None
    @param nexus_class: the nexus class of the device to be created for the new device_name, only required if creating a new group, the default is NXcollection
    '''
    add_meta(device_name, field_name, field, unit=unit, field_type=SCALAR, nexus_class=nexus_class)

alias("add_meta_scalar")

def add_meta_scannable(device_name, field, unit=None, nexus_class="NXcollection"):
    '''
    add a SCANNABLE to the specified device group.
    @param device_name: the name of metadata device
    @param field: the Scannable or scannable name to be added.
    @param unit : the physical unit for this field if available, default is None in which case the scannable's unit will be used by default if exists
    @param nexus_class: the nexus class of the device to be created for the new device_name, only required if creating a new group, the default is NXcollection
    '''
    if isinstance(field, Scannable):
        scannable_name = field.getName()
    elif isinstance(field, str):
        scannable_name = field
    else:
        raise ValueError("Input 'field' is neither a Scannable or scannable name as String!")
    field_name = scannable_name.lstrip(device_name) if scannable_name.startswith(device_name) else scannable_name
    add_meta(device_name, field_name, scannable_name, unit=unit, nexus_class=nexus_class)

alias("add_meta_scannable")

def add_meta_pv(device_name, field_name, field, unit=None, nexus_class="NXcollection"):
    '''
    add a PV field with a given field name to the specified device group.
    @param device_name: the name of metadata device
    @param field_name: the name of the field to be added to the device
    @param field: the PV name to be added.
    @param unit : the physical unit for this field if available, default is None
    @param nexus_class: the nexus class of the device to be created for the new device_name, only required if creating a new group, the default is NXcollection
    '''
    add_meta(device_name, field_name, field, unit=unit, field_type=PV, nexus_class=nexus_class)

alias("add_meta_pv")

def add_meta_link(device_name, field_name, field, nexus_class="NXcollection"):
    '''
    add a LINK field with a given field name to the specified device group.
    @param device_name: the name of metadata device
    @param field_name: the name of the field to be added to the device
    @param field: the LINK path to be added.
    @param nexus_class: the nexus class of the device to be created for the new device_name, only required if creating a new group, the default is NXcollection
    '''
    add_meta(device_name, field_name, field, field_type=LINK, nexus_class=nexus_class)

alias("add_meta_link")

def rm_meta(device_name, field_name):
    """
    remove user added field from the given metadata device. It will remove the metadata device if the device has no other children.
    @param device_name: the name of metadata device from which a given field to be removed
    @param field_name: the name of the field to be removed from the give metadata device
    """
    NexusMetadataUtility.INSTANCE.remove(device_name, field_name)

alias("rm_meta")

def disable_meta(*args):
    """
    disable the given metadata device in subsequent scans. That is metadata of this device will not be collected in any scan afterwards.
    @param args: list of metadata device names to be disabled
    """
    for device_name in args:
        NexusMetadataUtility.INSTANCE.disable(device_name)

alias("disable_meta")

def enable_meta(*args):
    """
    enable the given metadata device in subsequent scans. That is metadata of this device will be collected in any scan afterwards.
    @param args: list of metadata device names to be enabled
    """
    for device_name in args:
        NexusMetadataUtility.INSTANCE.enable(device_name)

alias("enable_meta")

def ll_meta(*args):
    """
    display all metadata devices or a given set of metadata devices along with their field values to be put into the scan metadata.
    @param args: list of device names to be displayed in Jython terminal, if empty or not supplied it will display all metadata
    """
    if len(args) == 0:
        NexusMetadataUtility.INSTANCE.list(True)
    else:
        for each in args:
            if isinstance(each, str):
                NexusMetadataUtility.INSTANCE.display(each, True)
            else:
                raise ValueError("Input must be name or names of metadata devices in String!")

alias("ll_meta")

def ls_meta(*args):
    """
    display only device names and field names of all metadata devices or a given set of metadata devices to be put into the scan metadata.
    @param args: list of device names to be displayed in Jython terminal, if empty or not supplied it will display all metadata
    """
    if len(args) == 0:
        NexusMetadataUtility.INSTANCE.list(False)
    else:
        for each in args:
            if isinstance(each, str):
                NexusMetadataUtility.INSTANCE.display(each, False)
            else:
                raise ValueError("Input must be name or names of metadata devices in String!")

alias("ls_meta")

def clear_meta():
    '''
    clear all user added metadata.
    '''
    NexusMetadataUtility.INSTANCE.clear()

alias("clear_meta")

