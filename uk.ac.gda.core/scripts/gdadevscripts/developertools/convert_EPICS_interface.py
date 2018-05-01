#!/dls_sw/apps/python/anaconda/1.7.0/64/bin/python
"""
DAQ-479 Write script to start to remove EPICS interface configuration and code
https://jira.diamond.ac.uk/browse/DAQ-479

Requires python 2.7

Please make this file executable. Edit CLASS_MAPPING to add bean classes that
are relevant to your beamline configuration. The defult set covers several of the
common ones.

Example usage: convert_EPICS_interface.py fullpath_to_live_dir full_path_to_interface.xml_file

Sometimes the indentation of your config files may be altered - if so you
may want to reformat your config files in Eclipse afterwards.

- Script that removes 'deviceName' properties from config files and
 replaces them with alternatives along with the pv name extracted from
 xxx-epics-interface.xml
 - Reduces reliance on xxx-epics-interface.xml files
 - Input is path to 'live' config directory, path to xx-epic-interface.xml
 - uses a map from bean class to information on where the PV name can be
 found in the interface file and various properties relating to how to replace
 the 'deviceName' property in the config file.
 - XML file in config is rewritten, preserving comments
 - Bean class names can be replaced
 - PV names can be truncated
 - Replacement property tags can be specified
 - A list is printed out of other bean classes found in the config that will
 need changing manually before removing the xxx-epics-interface.xml file
- A list of items not found in the epics interface file is printed

@author: Olly King, James Mudd
Last Modified March 2018
"""


# Maps qualified classname to a dictionary of the form
# {key("outer_tag"): Outer tag name (in interface.xml file) e.g. "simpleMotor",
# key("inner_tag"): Inner tag name (in interface.xml file) e.g. "RECORD",
# key("replace_prop"): Property name to replace 'deviceName' in
# config file e.g "pvName",
# Optional key("truncate_pv"): Boolean - truncates PV name to base PV
# Optional key("snip_pv"): Boolean - removes text from last colon onwards (inclusive)
# Optional key("new_class_name"): Replace the class name associated with the
# bean e.g. "gda.device.motor.newFancyEpicsMotor"
# }
CLASS_MAPPING = {
    "gda.device.motor.EpicsMotor":
        {"outer_tag": "simpleMotor", "inner_tag": "RECORD",
         "replace_prop": "pvName"
        },
    "gda.spring.EpicsMotorFactoryBean":
        {"outer_tag": "simpleMotor", "inner_tag": "RECORD",
         "replace_prop": "pvName",
         "new_class_name": "gda.device.motor.EpicsMotor"
        },
    "gda.device.motor.PiezoEpicsMotor":
        {"outer_tag": "simpleMotor", "inner_tag": "RECORD",
         "replace_prop": "pvName"
        },
    "gda.device.monitor.EpicsMonitor":
        {"outer_tag": "simplePv", "inner_tag": "RECORD",
         "replace_prop": "pvName"
        },
    "gda.spring.EpicsMonitorFactoryBean":
        {"outer_tag": "simplePv", "inner_tag": "RECORD",
         "replace_prop": "pvName",
         "new_class_name": "gda.device.monitor.EpicsMonitor"
        },
    "gda.spring.EpicsPneumaticFactoryBean":
        {"outer_tag": "pneumaticCallback", "inner_tag": "CONTROL",
         "replace_prop": "pvBase", "truncate_pv": True,
         "new_class_name": "gda.device.enumpositioner.EpicsPneumaticCallback"
        },
    "gda.device.enumpositioner.EpicsPneumaticCallback":
        {"outer_tag": "pneumaticCallback", "inner_tag": "CONTROL",
         "replace_prop": "pvBase", "truncate_pv": True
        },
    "gda.device.enumpositioner.EpicsSimpleBinary":
        {"outer_tag": "simpleBinary", "inner_tag": "RECORD",
         "replace_prop": "pvName"
        },
    "gda.device.enumpositioner.EpicsSimpleMbbinary":
        {"outer_tag": "simpleMbbinary", "inner_tag": "RECORD",
         "replace_prop": "recordName"
        },
    "gda.device.currentamplifier.EpicsCurrAmpSingle":
        {"outer_tag": "currAmpSingle", "inner_tag": "I",
         "replace_prop": "pvName", "snip_pv": True
        },
    "gda.spring.EpicsPositionerFactoryBean":
        {"outer_tag": "positioner", "inner_tag": "SELECT",
         "replace_prop": "recordName", "snip_pv": True,
         "new_class_name": "gda.device.enumpositioner.EpicsPositionerCallback"
        },
    "gda.device.enumpositioner.EpicsPositionerCallback":
        {"outer_tag": "positioner", "inner_tag": "SELECT",
         "replace_prop": "recordName", "snip_pv": True
        },
    "gda.device.enumpositioner.EpicsPositioner":
        {"outer_tag": "positioner", "inner_tag": "SELECT",
         "replace_prop": "recordName", "snip_pv": True
        }
    }

#--------------------------------------------------------------------

import os
import argparse
import xml.etree.ElementTree as ET
from collections import OrderedDict
from collections import Counter

ET.register_namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
ET.register_namespace("", "http://www.springframework.org/schema/beans")

def _serialize_xml(write, elem, encoding, qnames, namespaces):
    """This horrible monkeypatching of ElementTree is done to preserve
    attribute order via use of an ordered dict. It comes from
    https://stackoverflow.com/a/30902567
    """
    tag = elem.tag
    text = elem.text
    if tag is ET.Comment:
        write("<!--%s-->" % ET._encode(text, encoding))
    elif tag is ET.ProcessingInstruction:
        write("<?%s?>" % ET._encode(text, encoding))
    else:
        tag = qnames[tag]
        if tag is None:
            if text:
                write(ET._escape_cdata(text, encoding))
            for e in elem:
                _serialize_xml(write, e, encoding, qnames, None)
        else:
            write("<" + tag)
            items = elem.items()
            if items or namespaces:
                if namespaces:
                    for v, k in sorted(namespaces.items(),
                                       key=lambda x: x[1]):  # sort on prefix
                        if k:
                            k = ":" + k
                        write(" xmlns%s=\"%s\"" % (
                            k.encode(encoding),
                            ET._escape_attrib(v, encoding)
                            ))
                #for k, v in sorted(items):  # lexical order
                for k, v in items: # Monkey patch
                    if isinstance(k, ET.QName):
                        k = k.text
                    if isinstance(v, ET.QName):
                        v = qnames[v.text]
                    else:
                        v = ET._escape_attrib(v, encoding)
                    write(" %s=\"%s\"" % (qnames[k], v))
            if text or len(elem):
                write(">")
                if text:
                    write(ET._escape_cdata(text, encoding))
                for e in elem:
                    _serialize_xml(write, e, encoding, qnames, None)
                write("</" + tag + ">")
            else:
                write(" />")
    if elem.tail:
        write(ET._escape_cdata(elem.tail, encoding))

ET._serialize_xml = _serialize_xml

class OrderedXMLTreeBuilder(ET.XMLTreeBuilder):
    """Class that extends XMLTreeBuilder in order to preserve attribute
    order. From https://stackoverflow.com/a/30902567"""
    def _start_list(self, tag, attrib_in):
        fixname = self._fixname
        tag = fixname(tag)
        attrib = OrderedDict()
        if attrib_in:
            for i in range(0, len(attrib_in), 2):
                attrib[fixname(attrib_in[i])] = self._fixtext(attrib_in[i+1])
        return self._target.start(tag, attrib)


class CommentedTreeBuilder(ET.TreeBuilder):
    """ElementTreeBuilder that allows preservation of XML comments when
    parsing files come from https://stackoverflow.com/a/34324359
    """
    def __init__(self, *args, **kwargs):
        super(CommentedTreeBuilder, self).__init__(*args, **kwargs)

    def comment(self, data):
        self.start(ET.Comment, {})
        self.data(data)
        self.end(ET.Comment)


class EpicsInterfaceFile(object):
    """Class to represent an Epics Interface file"""

    def __init__(self, epics_interface_path):
        self.tree = ET.parse(epics_interface_path)
        self.not_found = []

    def pv_from_device_name(self, device_name, bean_class):
        """Extracts the pv name of a device from the Epics Interface file.

        Args:
            device_name (string): The name of the device
            (as given in the deviceName field).
            bean_class (string): The fully qualified Java class name
            associated with the bean.

        Returns:
            (pv_name, replace_name, bean_class) (tuple): The PV name of
            the corresponding device, the property name to define it
            (as in the config file e.g 'pvName') and the class name for
            the bean
        """
        action_dict = (CLASS_MAPPING.get(bean_class))
        if action_dict:
            for item in self.tree.findall(action_dict.get("outer_tag")):
                if item.attrib['name'] == device_name:
                    pv_name = item.find(action_dict.get("inner_tag")).attrib['pv']
                    if pv_name:
                        if action_dict.get("truncate_pv"):
                            # Just return the base pv
                            pv_name = pv_name.split(":")[0]
                        if action_dict.get("snip_pv"):
                            # Return the pv up to the final colon
                            pv_name = pv_name.rsplit(':', 1)[0]
                        replace_prop = action_dict.get("replace_prop")
                        replace_bean = action_dict.get("new_class_name", bean_class)
                        print ("{0}: {1} -> {2} {3}: {4}"
                               .format(bean_class, device_name,
                                       replace_bean, replace_prop, pv_name))
                        return pv_name, replace_prop, replace_bean
                    # If the PV is not found in the interface file add to list
                    self.not_found.append(("No PV found for inner tag: {0}"
                                           .format(action_dict.get("inner_tag"))))
            # If the item is not found in the interface file add to list
            self.not_found.append(("-> {0} ({1})".format(device_name, bean_class)))
        return None


class ConfigFolder(object):
    """Class to represent the directory containing
    the config files with associated methods
    """
    BEANS = '{http://www.springframework.org/schema/beans}bean'
    PROPERTIES = '{http://www.springframework.org/schema/beans}property'

    def __init__(self, config_dir_path, epics_interface_path):
        self.config_dir_path = config_dir_path
        self.epics_interface = EpicsInterfaceFile(epics_interface_path)
        self.other_device_name_classes = set()
        self.counter = Counter()

    def replace_epics_interface(self):
        """Traverses the XML tree for a file and calls the
        replace_device_with_pv method on beans corresponding to classes
        in the classes_to-change list. Writes out updated
        file if the XML tree is changed. Adds other bean class names to
        a set if they have a 'deviceName' tag, for future reference.
        """
        # Look for all the .xml files
        for folder, _, files in os.walk(self.config_dir_path):
            for filename in files:
                if filename.endswith('.xml'):
                    changed_flag = False
                    file_path = os.path.join(folder, filename)
                    print file_path
                    # Get the XML tree - use a parser that preserves
                    # comments and attribute order.
                    tree = ET.parse(file_path,
                                    OrderedXMLTreeBuilder(target=CommentedTreeBuilder()))
                    # Get list of all beans
                    all_beans = tree.findall(self.BEANS)
                    for bean in all_beans:
                        if not bean.attrib.get('class'):
                            print "Warning: bean " + bean.attrib["id"] + " has no associated class"
                        else:
                            bean_class = bean.attrib['class']
                            changed_flag = self.replace_device_with_pv(bean,
                                                                       bean_class)
                            if changed_flag:
                                # Write out the updated config file
                                tree.write(file_path, encoding="UTF-8")


    def replace_device_with_pv(self, bean, bean_class):
        """Method that replaces 'deviceName' property with a new property,
        defined by 'replace_name' which is linked to the name of the pv as
        extracted from the epics interface file.

        Args:
            bean: The bean element extracted from the configuration file
            bean_class (string): The fully qualified Java class name
            associated with the bean.

        Returns:
            changed_flag (boolean): True if the bean has been altered
        """
        changed_flag = False
        props_with_device_name = self.get_props_with_device_name(bean)
        for prop in props_with_device_name:
            device_name = prop.attrib['value']
            print "Bean ID: {0}".format(bean.attrib["id"])
            find_pv_result = (self.epics_interface
                              .pv_from_device_name(device_name, bean_class))
            if find_pv_result and (bean_class in CLASS_MAPPING):
                changed_flag = True
                # Add the class instance that is being changed to the Counter
                self.counter.update({bean_class: 1})
                pv_name, replace_name, class_name = find_pv_result
                bean.set('class', class_name)
                bean.remove(prop) # Remove the device name property
                new_element = ET.Element('property', {'name': replace_name,
                                                      'value': pv_name})
                bean.append(new_element)
            elif bean_class not in CLASS_MAPPING:
                self.other_device_name_classes.add(bean_class)
                self.counter.update({bean_class: 1})

        return changed_flag

    def get_props_with_device_name(self, bean):
        """Given a bean element, returns a list of properties from that bean
        with a 'deviceName' name attribute

        Args:
            bean: The bean element extracted from the configuration file

        Returns:
            list: Property elements from the bean with name="deviceName"
        """

        return [prop for prop
                in bean.findall(self.PROPERTIES)
                if prop.attrib['name'] == 'deviceName']

def print_summary():
    """Prints out some summary information on what the script has
    done
    """
    changed_counter = 0

    print "\n"
    print "The following classes were changed:\n"
    for key in CLASS_MAPPING.keys():
        count = CONFIG_FOLDER_EDITOR.counter[key]
        changed_counter += count
        if count:
            print "-> Class {0} : Instances {1}".format(key, count)

    print "\n" + ("-" * 45)
    print "The following classes with a 'deviceName' attribute were also found:\n"
    for key in CONFIG_FOLDER_EDITOR.other_device_name_classes:
        print "-> Class {0} : Instances {1}".format(key, CONFIG_FOLDER_EDITOR.counter[key])

    not_found_list = CONFIG_FOLDER_EDITOR.epics_interface.not_found
    if not_found_list:
        print "\n"
        print "The following items weren't found in the interface file:\n"
        for item in not_found_list:
            print item

    print "\nSummary:\n" + ("-" * 45)
    print ("Total number of Beans with deviceName attribute: {0}"
           .format(sum(CONFIG_FOLDER_EDITOR.counter.values())))
    print "Number changed: {0}".format(changed_counter)


if __name__ == '__main__':

    PARSER = argparse.ArgumentParser(description=("A script to help remove dependency on the "
                                                  "xxx-epics-interface.xml file. "
                                                  "Please 'module load python' before running"))
    PARSER.add_argument("config_dir_path", help=("Full path to the 'live' "
                                                 "directory in the beamline "
                                                 "configuration folder"))
    PARSER.add_argument("epics_interface_path", help=("Full path to the "
                                                      "'xxx-gda-interface-xml'"
                                                      " file in the beamline "
                                                      "configuration"))
    ARGS = PARSER.parse_args()

    CONFIG_FOLDER_EDITOR = ConfigFolder(ARGS.config_dir_path,
                                        ARGS.epics_interface_path)
    CONFIG_FOLDER_EDITOR.replace_epics_interface()
    print_summary()
