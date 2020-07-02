import sys;
from gda.factory import Finder;
from gdascripts.messages import handle_messages
from gda.util.converters import IReloadableQuantitiesConverter

def reload_tables(logInfo=True):
    """reloads all lookup tables on the ObjectServer"""
    ok = True
    controller = None
    prefix = "reload_tables:"
    if( logInfo ):
        handle_messages.log(controller, prefix + " - started")
    converters = Finder.listFindablesOfType(IReloadableQuantitiesConverter)
    for converter in converters:
        try:
            if( logInfo ):
                handle_messages.log(controller, prefix + "..." + converter.getName() )
            converter.reloadConverter()
        except:
            type1, exception, traceback = sys.exc_info()
            handle_messages.log(controller, prefix + " - ", type1, exception, traceback, False)
            ok = False
    if( logInfo ):
        handle_messages.log(controller, prefix + " - completed")
    if( not ok):
        print "reload_tables completed with error"
    return ok

