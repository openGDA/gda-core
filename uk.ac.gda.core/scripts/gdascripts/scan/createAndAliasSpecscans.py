from gdascripts.scan import specscans
from gda.jython.commands.GeneralCommands import alias

ascan  = specscans.Ascan()
a2scan = specscans.A2scan()
a3scan = specscans.A3scan()
mesh   = specscans.Mesh()
dscan  = specscans.Dscan()
d2scan = specscans.D2scan()
d3scan = specscans.D3scan()

alias('ascan');    print ascan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('a2scan');print a2scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('a3scan');print a3scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('mesh');print mesh.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('dscan');print dscan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('d2scan');print d2scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('d3scan');print d3scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]

