import logging
import sys
from gda.jython.logging import JythonLogHandler, PythonException
from gda.jython.logging.JythonLogHandler import LogLevel
from gda.jython import InterfaceProvider
from org.python.core import PyException

class JythonLogRedirector(logging.Handler):
    """Log handler to redirect message to the central logging framework"""
    def __init__(self, level=logging.NOTSET):
        logging.Handler.__init__(self, level)
        self._logger = JythonLogHandler()

    def createLock(self):
        # Locks are not needed as all logging is handled by the Java library
        pass

    def acquire(self):
        # Locks are not needed as all logging is handled by the Java library
        pass

    def release(self):
        # Locks are not needed as all logging is handled by the Java library
        pass

    def flush(self):
        # File handling is not needed as all logging is handled by the Java library
        pass

    def close(self):
        # File handling is not needed as all logging is handled by the Java library
        pass

    def emit(self, record):
        """Send log records to central GDA logging framework"""
        if (record.exc_info):
            # exc_info is a tuple of exception type, exception and traceback
            # Wrap these in a PyException to pass to the Java side of the logging
            # see https://docs.python.org/2/library/sys.html#sys.exc_info
            info = record.exc_info
            exc = PyException(*info)
        else:
            exc = None
        self._logger.submitLog(LogLevel.fromJython(record.levelno), record.name, record.getMessage(), exc)

class JythonTerminalPrinter(logging.Handler):
    """Log handler to print to the console"""
    def __init__(self, level=logging.NOTSET):
        logging.Handler.__init__(self, level)
        self._printer = InterfaceProvider.getTerminalPrinter()

    def createLock(self):
        # Locks are not needed as all logging is handled by the Java terminal printer
        pass

    def acquire(self):
        # Locks are not needed as all logging is handled by the Java terminal printer
        pass

    def release(self):
        # Locks are not needed as all logging is handled by the Java terminal printer
        pass

    def flush(self):
        # File handling is not needed as all logging is handled by the Java terminal printer
        pass

    def close(self):
        # File handling is not needed as all logging is handled by the Java terminal printer
        pass

    def emit(self, record):
        """Print log messages to the GDA console"""
        if (record.exc_info):
            info = record.exc_info
            exc = PythonException.from(PyException(*info))
            self._printer.print('{}: {} - {}'.format(record.name, record.getMessage(), exc.getMessage()))
            while (exc.getCause() is not None):
                self._printer.print('  ' + str(exc.getCause()))
                exc = exc.getCause()
        else:
            self._printer.print('{}: {}'.format(record.name, record.getMessage()))
