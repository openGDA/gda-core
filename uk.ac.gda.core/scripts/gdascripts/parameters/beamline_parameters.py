import gda.configuration.properties.LocalProperties as LocalProperties
from gda.jython import InterfaceProvider
import gda.factory.Finder as Finder;

JYTHON_NAMESPACE_MAPPING_FILE_PROPERTY = 'gda.jython.namespaceMappingFile'
FINDER_NAME_MAPPING_FILE_PROPERTY      = 'gda.jython.finderNameMappingFile'
BEAMLINE_PARAMETERS_FILE_PROPERTY      = 'gda.jython.beamlineParametersFile'

def readDictionaryFromFile(mapping_file, mapping):
	lines = read_lines_from_file(mapping_file)
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


	def __getitem__(self, item_name):
		return self.__getattr__(item_name)


	def __getattr__(self, attr_name):
		if attr_name.startswith('__') and attr_name.endswith('__'):
			raise AttributeError
		if( self.jythonNamespaceMapping.has_key(attr_name)) :
			name_in_name_space = self.jythonNamespaceMapping[attr_name]
		else:
			name_in_name_space = attr_name
		try:
			return self.commandServer.getFromJythonNamespace(name_in_name_space)
		except:
			msg =  "Error getting value for item named " + name_in_name_space + " in jythonNamespace for attribute " +attr_name
			# print out now as it is not output by the Jython terminal
			print msg
			raise AttributeError, msg


	def reload(self, command_server):
		self.commandServer = command_server
		self.jythonNamespaceMapping={}
		self.jythonNamespaceMapFilePath = LocalProperties.get(JYTHON_NAMESPACE_MAPPING_FILE_PROPERTY)
		if(self.jythonNamespaceMapFilePath != None):
			self.jythonNamespaceMapping = readDictionaryFromFile(self.jythonNamespaceMapFilePath, self.jythonNamespaceMapping)


class FinderNameMapping:
	''''''
	def __init__(self):
		self.reload()


	def __getitem__(self, item_name):
		return self.__getattr__(item_name)


	def __getattr__(self, attr_name):
		if attr_name.startswith('__') and attr_name.endswith('__'):
			raise AttributeError
		if( self.finderNameMapping.has_key(attr_name)) :
			name_to_find = self.finderNameMapping[attr_name]
		else:
			name_to_find = attr_name
		try:
			obj = Finder.find(name_to_find)
		except:
			# print out now as it is not output by the Jython terminal
			msg = "Error getting value for item named " + name_to_find + " in finderNameMap for attribute " + attr_name
			print(msg)
			raise AttributeError, msg
		if( obj == None):
			msg = "Error getting value for item named " + name_to_find + " in finderNameMap for attribute " + attr_name
			print(msg)
			raise AttributeError, msg
		return obj


	def reload(self):
		self.finderNameMapping={}
		self.finderNameMapFilePath = LocalProperties.get(FINDER_NAME_MAPPING_FILE_PROPERTY)
		if(self.finderNameMapFilePath != None):
			self.finderNameMapping = readDictionaryFromFile(self.finderNameMapFilePath, self.finderNameMapping)


class DictionaryWrapper:

	def __init__(self, source_map):
		self.map = source_map


	def __getitem__(self, item_name):
		return self.__getattr__(item_name)


	def __getattr__(self, attr_name):
		if( self.map.has_key(attr_name)) :
			return self.map[attr_name]
		else:
			raise AttributeError, attr_name


class Parameters:

	def __init__(self, parameters_file_path=None ):
		self.reload(parameters_file_path=parameters_file_path)


	def __getitem__(self, item_name):
		return self.__getattr__(item_name)


	def __getattr__(self, attr_name):
		if( self.parameters.has_key(attr_name)) :
			return self.parameters[attr_name]
		else:
			raise AttributeError, attr_name


	def getValueOrNone(self, attr_name):
		if( self.parameters.has_key(attr_name)) :
			return self.parameters[attr_name]
		else:
			return None


	def getValueOrDefault(self, attr_name, default):
		if( self.parameters.has_key(attr_name)) :
			return self.parameters[attr_name]
		else:
			return default


	def reload(self, parameters_file_path=None):
		if parameters_file_path == None:
			self.parametersFilePath = LocalProperties.get(BEAMLINE_PARAMETERS_FILE_PROPERTY)
			if(self.parametersFilePath == None):
				raise AttributeError, "property " + BEAMLINE_PARAMETERS_FILE_PROPERTY + " is not set"
		else:
			self.parametersFilePath = parameters_file_path
		self.parameters={}
		self.parameters = readDictionaryFromFile(self.parametersFilePath, self.parameters)


	def getValueFromObjectOrNone(self, attr_name, source):
		if( self.parameters.has_key(attr_name)) :
			return source.__getattr__(self.parameters[attr_name])
		else:
			return None


	def saveToFile(self, file_path):
		f = open(file_path,"w")
		for key,item in self.parameters.items():
			f.write(key + "=" + str(item) + "\n")
		f.close()

