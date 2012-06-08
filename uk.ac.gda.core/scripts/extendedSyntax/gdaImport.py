'''
GDA jyhton import function that translate GDA extended syntax to true jython/Java method call 
This requires you handle the customised code differently from the normal python module, for example,

    >>> gda_mod = gdaimport("module_contains_GDA_extended_syntax.py")
    
Created on 16 Nov 2009

@author: fy65
'''
import new
import tokenize
import translator

def gdaimport(filename):
    mod= new.module(filename)
    f=open(filename)
    data = tokenize.untokenize(translator.translate(f.readline))
    exec data in mod.__dict__
    return mod
