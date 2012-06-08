'''
Created on 16 Nov 2009

@author: fy65
'''
import tokenize

vararg_aliased = ['pos', 'upos', 'inc', 'uinc','level','scan','pscan','cscan','zacscan','testscan','gscan','tscan','timescan']
aliased = ['ls','help','list_default','add_default','remove_default','pause','reset_namespace','run','alias','vararg_alias']
def translate(readline):
    firstInLoop = True
    aliased_cmd = False
    for type, name, _,_,_  in tokenize.generate_tokens(readline):
        # convert to function call
        if type==tokenize.NAME:
            for each in aliased:
                if name == each:
                    yield tokenize.NAME, name
                    yield tokenize.OP, '('
                    aliased_cmd = True
                    
            if name == 'pos':
                yield tokenize.NAME, 'pos'
                yield tokenize.OP, '(['
            elif name == 'upos':
                yield tokenize.NAME, 'upos'
                yield tokenize.OP, '(['
            elif name == 'inc':
                yield tokenize.NAME, 'inc'
                yield tokenize.OP, '(['
            elif name == 'uinc':
                yield tokenize.NAME, 'uinc'
            elif name == 'scan':
                yield tokenize.NAME, 'scan'
            elif name == 'pscan':
                yield tokenize.NAME, 'pscan'
            elif name == 'cscan':
                yield tokenize.NAME, 'cscan'
            elif name == 'gscan':
                yield tokenize.NAME, 'gscan'
            elif name == 'timescan':
                yield tokenize.NAME, 'timescan'
            elif name == 'tscan':
                yield tokenize.NAME, 'tscan'
            elif name == 'testscan':
                yield tokenize.NAME, 'testscan'
            elif name == 'level':
                yield tokenize.NAME, 'level'
            elif name == 'list_defaults':
                yield tokenize.NAME, 'list_defaults'
            elif name == 'remove_default':
                yield tokenize.NAME, 'remove_default'
            elif name == 'add_default':
                yield tokenize.NAME, 'add_default'
            elif name == 'help':
                yield tokenize.NAME, 'help'
            elif name == 'ls':
                yield tokenize.NAME, 'ls'
            elif name == 'pause':
                yield tokenize.NAME, 'pause'
            elif name == 'run':
                yield tokenize.NAME, 'run'
            elif name == 'reset_namespace':
                yield tokenize.NAME, 'reset_namespace'
            elif name == 'alias':
                yield tokenize.NAME, 'alias'
            elif name == 'alias':
                yield tokenize.NAME, 'alias'
            elif name == 'vararg_alias':
                yield tokenize.NAME, 'vararg_alias'
        else:
            if firstInLoop:
                firstInLoop = False
            else:
                yield tokenize.OP, ','
            yield type, name
    yield tokenize.OP, ')'
    
            