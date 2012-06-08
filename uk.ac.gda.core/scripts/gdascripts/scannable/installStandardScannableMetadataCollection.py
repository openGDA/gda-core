# To use this module (from LocalStation.py for example):
# >>> from gdascripts.scannable.installStandardScannableMetadataCollection import * #@UnusedWildImport
# >>> meta.rootNamespaceDict=globals()
# >>> note.rootNamespaceDict=globals()

__all__ = ["meta", "setmeta", "lsmeta", "addmeta", "rmmeta", "note"]

from gdascripts.scannable.metadata import MetadataOneOffNote, MetadataCollector
from gda.jython.commands.GeneralCommands import alias

print """Creating metadata scannable 'meta' and commands 'setmeta', 'lsmeta',
'addmeta','rmmeta'"""
meta = MetadataCollector('meta')
setmeta = meta.set
lsmeta = meta.ls
addmeta = meta.add
rmmeta = meta.rm

alias('note')
alias('setmeta')
alias('lsmeta')
alias('addmeta')
alias('rmmeta')

print "Creating 'note' command"
note = MetadataOneOffNote()
