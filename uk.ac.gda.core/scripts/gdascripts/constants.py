
import math
# Useful numbers:
#	Retained for backward compatibility only
# Pi
pi = math.pi
tau = pi * 2


#	Physical constants
#	updated to reflect definitions in
#	9th edition SI Units.
#	https://www.bipm.org/documents/20126/41483022/SI-Brochure-9.pdf



eV = 1.602176634e-19 # J

# Planck's constant
hPlanck = 6.62607015e-34 # J s
hbar = hPlanck/tau # J s
hPlanckeV = hPlanck/eV # eV s
hbareV = hbar/eV # eV s

# Speed of light in vacuum
clight = 299792458 # m s-1

#electron mass
m_e = 9.1093837015e-31 # kg ( 2018 CODATA Value electron mass )

# electron radius
r_e = 2.817940325e-15 # m

# Atomic unit of mass a.k.a. Dalton (1/12 of unbound Carbon12 atom)
amu = 1.66053906660e-27 # kg

one_angstrom_in_electronvolts = 12398.41984332  # use to interconvert in both directions, a = constant / b


#	Values in unorthodox units 
#	( to be removed in the future )
#	Retained for backward compatibility only


# electron radius
re = 2.817940325e-6 #nm

# electron mass
me = 9.1093837015 # in 10^-28 grams

# Atomic unit of mass a.k.a. Dalton (1/12 of unbound Carbon12 atom)
aum = 1.66053906660e-24 # g   
