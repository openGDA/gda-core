'''
GDA translator is a gda scripts preprocessor that translates GDA extended syntax 
to proper true Jython syntax.

After this code gets run( e.g. place it in the site.py or before localstation.py)
any code starting with the comment 

    #coding: GDASyntax

will automatically be translated through this pre-processing step.

There are problems to this preprocessor approach.  The main one is debugging. 
All python sees is the pre-processed file, so if you have performed significant translation,
this may be very different from your source text.

Created on 16 Nov 2009

@author: fy65
'''
import tokenize
from gda.jython import JythonServerFacade
#from java.util import Vector

jsf=JythonServerFacade.getInstance()

gdaAlias = jsf.getAliasedCommands()
gdaAliasVararg = jsf.getAliasedVarargCommands().getAliasedCommands()

def translate(readline):
    '''
    translate GDA specific commands, i.e. alias, to true Jython syntax.
    '''
    firstInLoop = True
    aliased = False
    vararg_aliased = False
    for type, name, _,_,_  in tokenize.generate_tokens(readline):
        # convert to GDA commands to Jython functions 
        if type==tokenize.NAME:
            if gdaAlias.contains(name):
                # aliasd commands
                yield tokenize.NAME, name
                yield tokenize.OP, '('
                aliased = True
            elif gdaAliasVararg.contains(name):
                #vararg_alised commands
                yield tokenize.NAME, name
                yield tokenize.OP, '(['
                vararg_aliased=True
            else:
                #object names
                if firstInLoop:
                    firstInLoop = False
                else:
                    yield tokenize.OP, ','
                yield type, name
        elif type == tokenize.SEMI:
            # handle multiple commands on the same line
            if aliased:
                yield tokenize.OP, ')'
                aliased = False
            if vararg_aliased:
                yield tokenize.OP, '])'
                vararg_aliased=False
            yield type, name
        elif type == tokenize.NEWLINE:
            # handle multiple commands on the same line
            if aliased:
                yield tokenize.OP, ')'
            if vararg_aliased:
                yield tokenize.OP, '])'
            yield type, name
        else:
            #all other types  
            yield tokenize.OP, ','
            yield type, name

        
import codecs, cStringIO, encodings
from encodings import utf_8

class StreamReader(utf_8.StreamReader):
    def __init__(self, *args,**kwargs):
        codecs.StreamReader.__init__(self, *args, **kwargs)
        data = tokenize.untokenize(translate(self.stream.readline))
        self.stream = cStringIO.StringIO(data)
    
def search_function(s):
    if s!='GDASyntax': return None
    utf8=encodings.search_function('utf8') # assuming utf8 encoding
    return codecs.CodecInfo(name='GDASyntax',
                            encode=utf8.encode,
                            decode=utf8.decode,
                            incrementalencoder=utf8.incrementalencoder,
                            incrementaldecoder=utf8.incrementaldecoder,
                            streamreader=StreamReader,
                            streamwriter=utf8.streamwriter)

codecs.register(search_function)

      
            