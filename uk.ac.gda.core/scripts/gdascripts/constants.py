from org.jscience.physics.quantities import Constants as JScienceConstants

# Useful numbers:

# Pi
pi = 3.141592653589793238

eV = 1.602176462e-19 # J 

# Planck constant
hPlanck = 6.6260693e-34 # J s
hbar = hPlanck/(2*pi) # J s
hPlanckeV = hPlanck/eV # eV s
hbareV = hbar/eV # eV s

# Speed of light in vacuum
clight = 299792458 # m s-1

#electron mass
m_e = JScienceConstants.me.doubleValue() #@UndefinedVariable

# electron radius
r_e = 2.817940325e-15 #m

# Atomic unit of mass (1/12 of Carbone12)
amu = JScienceConstants.amu.doubleValue() #@UndefinedVariable



# stuff in funny units -- to be remove in the future
# These are kept here for  backward compatibility only

# electron radius
re = 2.817940325e-6 #nm

# electron mass
me = 9.1093826 #atomic unit of mass (e-28 g)

# Atomic unit of mass (1/12 of Carbone12)
aum = 1.66053886e-24 # g