
from gda.configuration.properties import LocalProperties

# These functions make the assumption that the beamline has defined its own Jython scripts area in the
# command server with a package <beamline name>_exafs.
#
# There should be a module called setupExperiment.py with two functions: setup and finish
#

def setup(beanGroup):
    extraColumns = None
    if LocalProperties.get("gda.beamline.name") != "":
        beamline_name = LocalProperties.get("gda.beamline.name")
        module_name = beamline_name + "_exafs.setupExperiment"
        function_name = "setup" 
        exec("from " + module_name + " import " + function_name)
        exec("extraColumns = " + function_name + "(beanGroup)")
    return extraColumns
        
def finish(beanGroup):
    function_name = ""
    try:
        if LocalProperties.get("gda.beamline.name") != "":
            beamline_name = LocalProperties.get("gda.beamline.name")
            module_name = beamline_name + "_exafs.setupExperiment"
            function_name = "finish" 
            exec("from " + module_name + " import " + function_name)
            exec("extraColumns = " + function_name + "(beanGroup)")
    except:
        return 
    # if get here then run the imported function
    exec(function_name + "()")

