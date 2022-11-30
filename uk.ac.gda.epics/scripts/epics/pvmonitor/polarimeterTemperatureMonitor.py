'''
create 2 scannables that monitor Retardation motor temperature and Analyser motor temperature in a polarimeter.

Created on 18 Dec 2009

@author: fy65
'''
from threadedMonitor import EpicsPVWithMonitorListener

rettemp = EpicsPVWithMonitorListener('rettemp', 'ME02P-MO-RET-01:ROT:TEMP', 'degree', '%.4.1f')
anatemp = EpicsPVWithMonitorListener('anatemp', 'ME02P-MO-ANA-01:ROT:TEMP', 'degree', '%.4.1f')