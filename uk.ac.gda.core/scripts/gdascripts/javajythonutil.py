# Tools to convert Java collection to/from Python collections
# Extracted from http://www.ibm.com/developerworks/java/tutorials/j-jython2/index.html
from types import *

from java import lang
from java import util

def mapMapFromJava (map):
    """ Convert a Map to a Dictionary. """
    result = {}
    iter = map.keySet().iterator()
    while iter.hasNext():
        key = iter.next()
        result[mapFromJava(key)] = mapFromJava(map.get(key))
    return result

def mapCollectionFromJava (coll):
    """ Convert a Collection to a List. """
    result = []
    iter = coll.iterator();
    while iter.hasNext():
        result.append(mapFromJava(iter.next()))
    return result

def mapFromJava (object):
    """ Convert a Java type to a Jython type. """
    if object is None: return object
    if   isinstance(object, util.Map):        
        result = mapMapFromJava(object)
    elif isinstance(object, util.Collection): 
        result = mapCollectionFromJava(object)
    else:                                     
        result = object
    return result

def mapSeqToJava (seq):
    """ Convert a sequence to a Java ArrayList. """
    result = util.ArrayList(len(seq))
    for e in seq:
        result.add(mapToJava(e));
    return result

def mapDictToJava (dict):
    """ Convert a Dictionary to a Java HashMap. """
    result = util.HashMap()
    for key, value in dict.items():
        result.put(mapToJava(key), mapToJava(value))
    return result

def mapToJava (object):
    """ Convert a Jython type to a Java type. """
    if object is None: return object
    t = type(object)
    if   t == TupleType or t == ListType:  
        result = mapSeqToJava(object)
    elif t == DictType:  
        result = mapDictToJava(object)
    else:                
        result = object
    return result