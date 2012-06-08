'''
Provide information about the Jython environment.
@author Graham Lee (ooy64567)
'''

def defaultScriptFolder():
    '''Return the default location for Jython scripts.'''
    
    from gdascripts.parameters import beamline_parameters
    jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
    command_server = jythonNameMap.command_server
    
    return command_server.getDefaultScriptProjectFolder() + "/"
