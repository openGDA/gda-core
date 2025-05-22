'''
Created on 4 Dec 2024

@author: fy65
'''
from gda.configuration.properties import LocalProperties
from org.slf4j import LoggerFactory
from org.eclipse.dawnsci.hdf5.nexus import NexusFileHDF5
from org.eclipse.dawnsci.nexus import NexusException

logger = LoggerFactory.getLogger(__name__)

def set_nexus_tamplate(template_yaml_file_path):
    '''If only file name is given, it assumes the given file is in ${gda.config}/nexus/template/ folder.
    '''
    import os
    if not os.path.isabs(template_yaml_file_path):
        template_yaml_file_path = str(LocalProperties.get("gda.config")) + "/nexus/templates/" + template_yaml_file_path
    if not os.path.isfile(template_yaml_file_path):
        logger.error("Cannot find the given YAML template file {}", template_yaml_file_path)
        raise ValueError("Cannot find the given YAML template file %s" % template_yaml_file_path)
    return template_yaml_file_path


def preprocess_spring_expression_in_template(template_file, spel_expression_node = []):
    from gda.spring.spel import NexusYamlTemplateSpELProcessor # process field value with SpEL expression
    processor = NexusYamlTemplateSpELProcessor()
    processor.setNodeNameContainingSpELExpressions(spel_expression_node)
    processor.init() # initialise SpEL processor
    template = processor.process(set_nexus_tamplate(template_file))
    return template

def apply_template_to_nexus_file(nexus_file_name, template_file, spel_expression_node = []):
    '''apply a give template yaml file (2nd argument) to a give Nexus data file (1st argument).
    If the template file contains node that has Spring SpEL expression, the node name (end with '/') must be provided as list in 3rd argument.
    '''
    f = None
    try:
        template = preprocess_spring_expression_in_template(template_file, spel_expression_node)
        f = NexusFileHDF5.openNexusFile(nexus_file_name)
        template.apply(f)
    except NexusException, e:
        logger.error("Function throws exception during applying template {} to nexus file {}", template_file, nexus_file_name, e)
        print(e)
    finally:
        if f:
            f.close()
