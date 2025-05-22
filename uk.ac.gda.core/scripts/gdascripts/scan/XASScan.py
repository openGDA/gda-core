'''
A Scan wrapper command or function for XAS experiments. It loads NXxas Nexus template before scan starts and remove it after scan completed or aborted/failed.

Created on Dec 2, 2024

@author: fy65
'''
from gda.jython.commands.ScannableCommands import scan
from gda.device import Scannable
import time
from gdascripts.scan.miscan import parse_other_arguments, parse_tuple_arguments
from types import TupleType
from gdascripts.metadata.nexus_metadata_class import meta
from gda.device.scannable import ScannableMotionBase
from gdascripts.functions.nexusYamlTemplateProcessor import preprocess_spring_expression_in_template
from gda.factory import Finder

PRINTTIME = False

def xasscan(*args):
    '''a wrapper scan parser for XAS experiments which apply NXxas Application Definition template before scan data collection and remove it after data collection completed.
    Syntax example (i06 GDA):
        xasscan energy start stop step ca51sr 1 ca52sr 1 ca53sr 1 ca54sr 1 xasmode TEY
    '''
    if len(args) == False:
        raise SyntaxError("No argument is given to scan command!")
    if not isinstance(args[0], Scannable):
        raise SyntaxError("First argument to scan command must be a scannable")
    command = "xasscan "
    original_mode = None
    starttime = time.ctime()
    start = time.time()
    if PRINTTIME: print("=== Scan started: " + starttime)
    newargs = []
    i = 0;
    while i < len(args):
        arg = args[i]
        if type(arg) == TupleType:
            command, newargs = parse_tuple_arguments(command, newargs, arg)
        elif isinstance(arg, ScannableMotionBase) and arg.getName() == xasscan.xasmode_scannable_name:
            if arg.getPosition() != args[i+1]: #XAS mode changed
                original_mode = arg.getPosition()
                xas_mode_scannable = arg
            arg.asynchronousMoveTo(args[i+1])
            command += arg.getName() + " " + args[i+1]
            i = i + 1
        else:
            newargs.append(arg)
            command = parse_other_arguments(command, arg)
        i = i + 1

    meta.addScalar("user_input", "command", command)
    ndwc = Finder.find("nexusDataWriterConfiguration")
    template = preprocess_spring_expression_in_template(xasscan.NEXUS_TEMPLATE_YAML_FILE_NAME, spel_expression_node = ["absorbed_beam/"])
    ndwc.addNexusTemplate(xasscan.NEXUS_TEMPLATE_YAML_FILE_NAME, template)
    print("NXxas nexus template is added before scan")
    try:
        scan([e for e in newargs])
    finally:
        if original_mode: # restore pre-scan XAS mode
            xas_mode_scannable.asynchronousMoveTo(original_mode)
        meta.rm("user_input", "command")    
        ndwc.removeNexusTemplate(xasscan.NEXUS_TEMPLATE_YAML_FILE_NAME)
        print("NXxas nexus template is removed after scan")

    if PRINTTIME: print ("=== Scan ended: " + time.ctime() + ". Elapsed time: %.0f seconds" % (time.time() - start))

from gda.jython.commands.GeneralCommands import alias 
alias("xasscan")

