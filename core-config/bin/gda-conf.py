#!/usr/bin/env python
# -*- coding: utf-8 -*-

import logging
import sys
import os
import ConfigParser
import argparse
from collections import OrderedDict as odict

try:
    import tkinter as tk
except ImportError:
    import Tkinter as tk

logger = logging.getLogger('conf')

# Encode the icons/images
ICO_DAT = '''
    R0lGODlhIAAgAPYAAF+5V2O7XGe9YGq+Y3LCbHbEcHrGdH7HeOqKDuqNFOuRHOuUIeyZLO2eNe2f
    OO6hPu6jQu+nSO+pTfCvWfGxXvG0ZfK2afK6bvO8dIHIe/TAfVRGm1ZJnVlMn1tOoF9SomJVpGRY
    pWhcp2pfqW1iqnZrr3ltsX1ys4F2toJ4toZ9uYh+uomAu4XKgInMg5DPi5TQj5fSkpvUl5zUmKPX
    n6XYoarapq7cq7Hdrrbfsrbgsrrht7zju/XHi/bMlvbOmvfUpvnTpPfVqvjZr/nctvnfvMHlvpSL
    waObyqqjzq6n0LOt07ex1bmz1ry22L+62sK93MfC3sjD38Xmw8zry87t0dft1dzy3/riw/zoz/zq
    1fzt2svG4dLO5dPQ5tnW6d3a6+Dd7ePh7+f15ur26u347f3y4/727fD47+bj8e3r9fHu9/T78/75
    8fXz+fn3/Pb9+v///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5
    BAAAAAAALAAAAAAgACAAAAf+gHGCg4SFhoeIiYqLiG9cTFFdX2JvjIxpIBsdHR4hJ11xZTMyNjtk
    loNvKB0frR8cJGplAwABAAdlqIJSHq6tHElxNgEDAwE4unFvJ72uHiRrZATFAwVoyU2svh1LcTLE
    xsi6YSC+Hx0kblMB7AAZyWok55ofIShrVAEGNzxXyeQdRiDhAoZSHBoFTglqowvJhhNiChkZcCOO
    jwoRHhCx5OQDiDBx2FgxYsMFOx5mGCRY6eBMIjdejnjooCIOHBjs2BmrEcdCggUKEvwgpOYLFyVH
    THjgcO5IyAPgqB2AkwXoggQQGHZJQWIpBw7azq0QlCNqsQBT4lBQABSBEDXtITR1KOcMxDM1cdAU
    oHa2BZwgCoIyKBJmk8e5dMuB6ABFEA6zxqpoUcDAhxllKTgkDnv4RKUyBcDllLxgCCEpmxKbOxfF
    cU4CMKjECQKh0DJOdsPS7ZCiEhwcL3SMEXTmAQVDUeZ6AJG4GTo3h7ZMSDDB0BsTyhX36rDhAxND
    WjSoVPDAZaEnuJd3AFsiCchCQBggYAt0YyE3KDhs+CriCBfoh0iQQGALAFWdIWogkQILTKSxCAY/
    FQiUAlgkc4gWDBBYYAIVWHjIBRGyRZ2HhlQVWFANVEhiIT4FNcFlKxaihQML9BAjImZoYUggADs=
    '''

IMG_DAT = '''
    R0lGODlhyAA3APcAAGzAZWzBZWzAZmzBZm3BZm3BZ27BZ27BaG/BaG7CaG/CaG/CaXDCaXDCanHC
    anDDanHDanHCa3HDa3LCa3LDa3LDbHPDbHPDbXTDbXPEbXTEbXTEbnXEbnXEb3XFb3bEb3bEcHfE
    cHbFcHfFcHbFcXfFcXjFcnnGcnnGc3vHdXvHdnzHdnzHd3zIdnzId33IeOuVIuuWIuuWI+uXI+uW
    JOuXJOuXJeuXJuuXJ+yXJ+uYJuuYJ+yYJ+yYKOyZKOyYKeyZKeyZKuyZK+yaKuyaK+yaLOybLOya
    LeybLeybLuybL+ycL+ycMOycMeydMeydMu2dMuydM+2dM+2eM+yeNO2eNO2fNe2fNu2fN+2gOO2g
    Oe2iPILKfWFUpGFVpGJVpGJVpWJWpWNWpWNXpWRXpWRXpmRYpmVYpmVZpmVZp2ZZp2Zap2dap2db
    p2dbqGhbqGhcqGlcqGldqGldqWpdqWpeqWteqWpeqmtfqmxgqmxgq21hq25iq25irG5jrHBlrXFl
    rXFmrXJnrry4rb24rb64rb25rr65rr65r7+5r766r7+6r7+6sL+7sL+7scC6r8C6sMC7sMC7scC8
    scG8scC8ssG8ssG9ssG9s8K9ssK9s8K9tMK+tMO+tMO/tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAJ8ALAAAAADIADcA
    AAj+AD8JHEiwoMGDCBMqXMiwocOHECNKnEgxIR8ya/BoxFOxo8ePIEOKBNmlpMmTXTgWRDGypcuX
    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
    '''


def get_image(dat):
    img = tk.PhotoImage(data=dat)
    return img


DEFAULT_BEAMLINE = os.environ.get('BEAMLINE', '')

COMMAND = '/usr/bin/env gda servers'

class Config(tk.Tk):
    def __init__(self, config_file=None, title=None, beamline=None, *a, **kw):
        tk.Tk.__init__(self, *a, **kw)
        self._config_file = config_file
        self._load_config(config_file)
        self.beamline = beamline or self._get_option('beamline', DEFAULT_BEAMLINE)

        self.devices = self._conf.options('devices')
        self.devices = odict((dev, self._conf.getboolean('devices', dev)) for dev in self.devices)

        self.selections = odict()
        self.buttons = odict()

        _fallback_title = (self.beamline + ' Configuration').strip()
        self.title(title or self._get_option('title', _fallback_title))
        self.call('wm', 'iconphoto', self._w, get_image(ICO_DAT))
        self.bind('<Escape>', lambda x: self.quit())
        self.wm_resizable(False, False)

        self._create_layout()

    def launch(self):
        '''Launch GDA with the selected components'''
        os.environ['BEAMLINE'] = self.beamline
        profiles = ','.join(self.selected)
        if profiles:
            logger.info('Launching with %s', profiles)
            option = ' --springprofiles ' + profiles
        else:
            logger.info('Launching with no profile')
            option = ' --nospringprofiles'
        os.system(COMMAND + option)
        self.quit()

    def store_defaults(self):
        '''Store the current selection in the configuration file'''
        logger.debug('Storing default profiles to %s', self.selected)
        for device, value in self.selections.items():
            self._conf.set('devices', device, bool(value.get()))
        with open(self._config_file, 'w') as out:
            self._conf.write(out)
        pass

    def store_and_launch(self):
        '''Store the selection and then launch GDA'''
        self.store_defaults()
        self.launch()

    @property
    def selected(self):
        return [k for k, v in self.selections.items() if v.get()]

    def _load_config(self, config):
        self._conf = ConfigParser.ConfigParser(dict_type=odict)
        self._conf.add_section('options')
        self._conf.add_section('devices')
        if config is None:
            return
        self._conf.read(config)

    def _create_layout(self):
        # have to keep a reference to the image or python garbage collects it too soon
        self.image = get_image(IMG_DAT)
        tk.Label(self, image=self.image).grid(rowspan=4, columnspan=2, padx=10, pady=10)
        for i, device in enumerate(self.devices):
            var = tk.IntVar(value=self.devices[device])
            button = tk.Checkbutton(self, text=device, variable=var)
            self.selections[device] = var
            self.buttons[device] = button
            button.grid(row=i, column=2, sticky='W')

        i = max(len(self.devices), 4) # image is 4 rows
        tk.Button(self, text='Launch', command=self.launch).grid(row=i, column=0)
        tk.Button(self, text='Launch (save selection)', command=self.store_and_launch).grid(row=i, column=1)
        tk.Button(self, text='Cancel', command=self.quit).grid(row=i, column=2)

    def _get_option(self, key, default):
        try:
            return self._conf.get('options', key)
        except ConfigParser.NoOptionError:
            return default

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-v', action='count', dest='verbose', default=0)
    parser.add_argument('--title')
    parser.add_argument('--beamline')
    parser.add_argument('config_file', help='Beamline configuration and options')
    args = parser.parse_args()

    logging.basicConfig(level=40-10*args.verbose)

    conf = Config(args.config_file, title=args.title, beamline=args.beamline)
    conf.mainloop()
