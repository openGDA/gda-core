''' define a class to provide methods for dynamic metadata operation during runtime

@since: GDA 9.23
'''
from uk.ac.gda.epics.nexus.device import EpicsNexusMetadataUtility
from org.eclipse.scanning.device.utils import NexusMetadataUtility
from gdascripts import installation
from gdascripts.synchrotron_operation_mode import get_machine_state, USER, SPECIAL

class Metadata:
    '''provide methods for dynamic metadata operation during runtime'''
    
    def __init__(self):
        self.initialized = False

    def addScalar(self, device_name, field_name, field_value, unit=None):
        '''
        add a SCALAR field_value with a given field_value name to the specified device group.
        @param device_name: the name of metadata device
        @param field_name: the name of the field_value to be added to the device
        @param field_value: the SCALAR value to be added.
        @param unit : the physical unit for this field_value if available, default is None
        '''
        self.get_metadata_instance().addScalar(device_name, field_name, field_value, unit)
  
    def addScannable(self, device_name, scannable):
        '''
        add a SCANNABLE to the specified device group.
        @param device_name: the name of metadata device
        @param scannable: the Scannable or scannable name to be added.
        '''
        self.get_metadata_instance().addScannable(device_name,  scannable)
    
    def addPV(self, device_name, field_name, pv_name, unit=None):
        '''
        add a PV pv_name with a given pv_name name to the specified device group.
        @param device_name: the name of metadata device
        @param field_name: the name of the pv_name to be added to the device
        @param pv_name: the PV name to be added.
        @param unit : the physical unit for this pv_name if available, default is None
        '''
        self.get_epics_metadata_instance().addPV(device_name, field_name, pv_name, unit)    
    
    def addLink(self, device_name, field_name, link_path, file_name=None):
        '''
        add a LINK link_path with a given link_path name to the specified device group.
        @param device_name: the name of metadata device
        @param field_name: the name of the link_path to be added to the device
        @param link_path: the LINK path to be added, starting from Tree ROOT '/'.
        @param file_name: the external file name in which the link path to be added, if None the link will be internal
        '''
        self.get_metadata_instance().addLink(device_name, field_name, file_name, link_path)
    
    def rm(self, device_name, field_name):
        """
        remove user added field from the given metadata device. It will remove the metadata device if the device has no other children.
        @param device_name: the name of metadata device from which a given field to be removed
        @param field_name: the name of the field to be removed from the give metadata device
        """
        self.get_metadata_instance().remove(device_name, field_name)
    
    def disable(self, *device_names):
        """
        disable the given metadata device in subsequent scans. That is metadata of this device will not be collected in any scan afterwards.
        @param args: list of metadata device names to be disabled
        """
        for device_name in device_names:
            self.get_metadata_instance().disable(device_name) 
    
    def enable(self, *device_names):
        """
        enable the given metadata device in subsequent scans. That is metadata of this device will be collected in any scan afterwards.
        @param args: list of metadata device names to be enabled
        """
        for device_name in device_names:
            self.get_metadata_instance().enable(device_name) 
    
    def ll(self, *args):
        """
        display all metadata devices or a given set of metadata devices along with their field values to be put into the scan metadata.
        @param args: list of device names to be displayed in Jython terminal, if empty or not supplied it will display all metadata
        """
        if len(args) == 0:
            self.get_metadata_instance().list(True)
        else:
            for each in args:
                if isinstance(each, str):
                    self.get_metadata_instance().display(each, True)
                else:
                    raise ValueError("Input must be name or names of metadata devices in String!")
    
    def ls(self, *args):
        """
        display only device names and field names of all metadata devices or a given set of metadata devices to be put into the scan metadata.
        @param args: list of device names to be displayed in Jython terminal, if empty or not supplied it will display all metadata
        """
        if len(args) == 0:
            self.get_metadata_instance().list(False)
        else:
            for each in args:
                if isinstance(each, str):
                    self.get_metadata_instance().display(each, False)
                else:
                    raise ValueError("Input must be name or names of metadata devices in String!")
    
    def clear(self):
        '''
        clear all user added metadata.
        '''
        self.get_metadata_instance().clear()
        
    def initialize_common_device_configuration(self):
        if not self.initialized:
            from org.eclipse.scanning.device import CommonBeamlineDevicesConfiguration
            CommonBeamlineDevicesConfiguration.getInstance().setEnforceMandatoryDeviceNames(
                    installation.isLive() and get_machine_state() in [USER, SPECIAL])
            self.initialized = True

    def get_metadata_instance(self):
        self.initialize_common_device_configuration()
        return NexusMetadataUtility.INSTANCE
    
    def get_epics_metadata_instance(self):
        self.initialize_common_device_configuration()
        return EpicsNexusMetadataUtility.INSTANCE

meta = Metadata()
