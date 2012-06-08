# Run this module to show scannables whose (internal) name differs from their (external) label
from gda.device import Scannable

print "The following scannables have labels (for typing) different than names(that go into files)"
print "Label\tName"
for label in dir():
	if (isinstance(eval(label),Scannable)):
		name = eval(label).getName()
		if label!=name:
			print label + "\t : " + name

