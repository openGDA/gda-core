"""
NOTE: For post scan processing to work with mrscan, the following must be added to localStation.py:

>>> from gdascripts.scan.installStandardScansWithProcessing import * #@UnusedWildImport
>>> scan_processor.rootNamespaceDict=globals()

This will add the correct globals() which is available from __main__ which is needed for post scan processing. 
You can then import mrscan to localStation.py using:

>>> from gdascripts.scan.installMultiRegionalScanWithProcessing import mrscan
"""

print("-"*100)
print("Installing multi regional scan 'mrscan'.")
from gdascripts.scan.mrscan import MultiRegionalScanClass
from gdascripts.scan.installStandardScansWithProcessing import scan_processor
from gda.jython.commands.GeneralCommands import alias
mrscan = MultiRegionalScanClass([scan_processor])
alias('mrscan')
print(mrscan.__doc__)