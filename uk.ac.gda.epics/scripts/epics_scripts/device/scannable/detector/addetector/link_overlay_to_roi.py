'''
Created on 25 Feb 2014

@author: zrb13439
'''

from gdascripts.utils import caput_wait

def setup_overlay_plugin(pvbase_det='BL16I-EA-DET-30:', number_plugins=7):
    for i in range(1, number_plugins):
        pvbase_roi = pvbase_det + 'ROI' + str(i) + ':'
        pvbase_overlay = pvbase_det + 'OVER:' + str(i) + ':'
        _link_overlay_to_roi_plugin_and_enable(pvbase_roi, pvbase_overlay)
        caput_wait(pvbase_overlay + 'Name', 'roi' + str(i))
    caput_wait(pvbase_det + 'OVER:EnableCallbacks', True)


def _link_overlay_to_roi_plugin_and_enable(pvbase_roi='BL16I-EA-DET-30:ROI1:',
                                           pvbase_overlay='BL16I-EA-DET-30:OVER:1:'):
    
    caput_wait(pvbase_overlay + 'Use', True)
    caput_wait(pvbase_overlay + 'Shape', 'Rectangle')
    caput_wait(pvbase_overlay + 'DrawMode', 'XOR')
    
    caput_wait(pvbase_overlay + 'PositionXLink.DOL', pvbase_roi + 'MinX_RBV CP')
    caput_wait(pvbase_overlay + 'SizeXLink.DOL', pvbase_roi + 'SizeX_RBV CP')

    caput_wait(pvbase_overlay + 'PositionYLink.DOL', pvbase_roi + 'MinY_RBV CP')
    caput_wait(pvbase_overlay + 'SizeYLink.DOL', pvbase_roi + 'SizeY_RBV CP')
