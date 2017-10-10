import gda.configuration.properties.LocalProperties as LocalProperties
from gda.jython import InterfaceProvider
import gda.factory.Finder as Finder;

JYTHON_NAMESPACE_MAPPING_FILE_PROPERTY = 'gda.jython.namespaceMappingFile'
FINDER_NAME_MAPPING_FILE_PROPERTY      = 'gda.jython.finderNameMappingFile'
BEAMLINE_PARAMETERS_FILE_PROPERTY      = 'gda.jython.beamlineParametersFile'

def readDictionaryFromFile(mappingFile, mapping):
    
    lines = read_lines_from_file(mappingFile)
    
    for line in lines:
        
        # remove comment, if any
        line = line.split("#")[0].strip()
        
        whitespace = ' \t'
        separators = '='
        
        if line: # don't process empty lines
            
            limit = len(line)
            key_length = 0
            value_start = limit
            has_separator = False
            
            # determine the key
            while key_length < limit:
                c = line[key_length]
                if c in separators:
                    # separator means we've reached the end of the key
                    value_start = key_length + 1
                    has_separator = True
                    break
                elif c in whitespace:
                    # whitespace means we've reached the end of the key
                    value_start = key_length + 1
                    # key/value separated by whitespace - no separator
                    break
                key_length += 1
            
            # determine the value
            while value_start < limit:
                c = line[value_start]
                if c not in whitespace: # skip past whitespace
                    if not has_separator and c in separators:
                        # found separator, and hadn't already found one; skip
                        # past it
                        has_separator = True
                    else:
                        # found first character of value
                        break
                value_start += 1
            
            key = line[:key_length]
            value = line[value_start:]
            mapping[key] = value
    return mapping

def read_lines_from_file(filename):
    f = open(filename, "r")
    lines = f.readlines()
    f.close()
    lines = map(remove_trailing_newlines, lines)
    return lines

remove_trailing_newlines = lambda l: l.rstrip("\r\n")

class JythonNameSpaceMapping:
    ''''''
    def __init__(self, commandServer=None ):
        if commandServer==None:
            commandServer = InterfaceProvider.getJythonNamespace()
        self.reload(commandServer)
    def __getitem__(self, itemName):
        return self.__getattr__(itemName)
    def __getattr__(self, attrName):
        if attrName.startswith('__') and attrName.endswith('__'):
            raise AttributeError
        if( self.jythonNamespaceMapping.has_key(attrName)) :
            nameInNameSpace = self.jythonNamespaceMapping[attrName]
        else:
            nameInNameSpace = attrName
        try:
            return self.commandServer.getFromJythonNamespace(nameInNameSpace)
        except:
            msg =  "Error getting value for item named " + nameInNameSpace + " in jythonNamespace for attribute " +attrName
            # print out now as it is not output by the Jython terminal
            print msg
            raise AttributeError, msg
    def reload(self,commandServer):
        self.commandServer = commandServer
        self.jythonNamespaceMapping={}
        self.jythonNamespaceMapFilePath = LocalProperties.get(JYTHON_NAMESPACE_MAPPING_FILE_PROPERTY)
        if(self.jythonNamespaceMapFilePath != None):
            self.jythonNamespaceMapping = readDictionaryFromFile(self.jythonNamespaceMapFilePath, self.jythonNamespaceMapping)

class FinderNameMapping:
    ''''''
    def __init__(self, finder=None):
        if finder == None:
            finder = Finder.getInstance()
        self.reload(finder)
    def __getitem__(self, itemName):
        return self.__getattr__(itemName)
    def __repr__(self):
        return "FinderNameMapping(finder=%s)" % self.finder
    def __getattr__(self, attrName):
        if attrName.startswith('__') and attrName.endswith('__'):
            raise AttributeError
        if( self.finderNameMapping.has_key(attrName)) :
            nameToFind = self.finderNameMapping[attrName]
        else:
            nameToFind = attrName
        try:
            obj = self.finder.find(nameToFind)
        except:
            # print out now as it is not output by the Jython terminal
            msg = "Error getting value for item named " + nameToFind + " in finderNameMap for attribute " + attrName
            print msg
            raise AttributeError, msg
        if( obj == None):
            msg = "Error getting value for item named " + nameToFind + " in finderNameMap for attribute " + attrName
            print msg
            raise AttributeError, msg
        return obj
    def reload(self,finder):
        self.finder = finder
        self.finderNameMapping={}
        self.finderNameMapFilePath = LocalProperties.get(FINDER_NAME_MAPPING_FILE_PROPERTY)
        if(self.finderNameMapFilePath != None):
            self.finderNameMapping = readDictionaryFromFile(self.finderNameMapFilePath, self.finderNameMapping)

class DictionaryWrapper:
    def __init__(self, map):
        self.map = map
    def __getitem__(self, itemName):
        return self.__getattr__(itemName)
    def __getattr__(self, attrName):
        if( self.map.has_key(attrName)) :
            return self.map[attrName]
        else:
            raise AttributeError, attrName
        
class Parameters:
    def __init__(self, parametersFilePath=None ):
        self.reload(parametersFilePath=parametersFilePath)
    def __getitem__(self, itemName):
        return self.__getattr__(itemName)
    def __getattr__(self, attrName):
        if( self.parameters.has_key(attrName)) :
            return self.parameters[attrName]
        else:
            raise AttributeError, attrName
    def getValueOrNone(self, attrName):
        if( self.parameters.has_key(attrName)) :
            return self.parameters[attrName]
        else:
            return None
    def reload(self, parametersFilePath=None):
        if parametersFilePath == None:
            self.parametersFilePath = LocalProperties.get(BEAMLINE_PARAMETERS_FILE_PROPERTY)
            if(self.parametersFilePath == None):
                raise AttributeError, "property " + BEAMLINE_PARAMETERS_FILE_PROPERTY + " is not set"
        else:
            self.parametersFilePath = parametersFilePath
        self.parameters={}
        self.parameters = readDictionaryFromFile(self.parametersFilePath, self.parameters)
    def getValueFromObjectOrNone(self, attrName, object):
        if( self.parameters.has_key(attrName)) :
            return object.__getattr__(self.parameters[attrName])
        else:
            return None
    def saveToFile(self, filePath):
        f = open(filePath,"w")
        for key,item in self.parameters.items():
            f.write(key + "=" + item + "\n")
        f.close()

