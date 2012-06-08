# The PySrc module is copied from org.python.pydev/PySrc. The only use of PySrc within
# GDA is for the command completion features. The command completer was originally
# copied from IPython by PySrc.
# Within this module the intention is to have the minimum of changes when compared
# to the original PyDev/PySrc directory. Ideally all changes should be pushed
# back upstream to make future upgrades easier. The only changes at the moment
# are working around warnings caused by uk.ac.gda.core being a Jython project
# but many Python 3 items being used within the file. If the project were
# a Python 2.x project, all the warnings would not exist.



