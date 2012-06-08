"""

Class to start the different components of gda

Usage:

gda.py options

where the options are:

 -v, --verbose               increase verbosity
 -n, --dry-run               show what processes would be started and environment variables set
 -t, --testing               adds test classes to CLASS_PATH
 -h  --help                  show usage and help (-h works with no other options)
     --task=                 client | log_server | name_server | event_server | object_server | servers | rcp_client
     --ns_port=              name server port (def 6700)
     --ls_port=              log server port (def. 6788)
     --debug                 same as --debug_port=8000  Not need if debug_port specified
     --debug_port=           debug_port e.g. 8000, (def 0 - no debug)
     --debug_suspend         process suspends execution until attached to by a debugger
     --profile               same as --profile_port=8851
     --profile_port=         port for profiling. e.g. 8851 (def 0 - no profiling)
     --os_xml=               object server xmlfile
     --version               display version of gda
 -s  --start                 start the process
     --killonly              kill task only
     --nokill                do not kill task first
    
"""
import sys
import os
import getopt
import time

#NOTE: Unless set here GDA_TOP is assumed to be six folders above this file
# Assumed directory structure within GDA_TOP
# |-- config (-> gda-config-base)
# |-- features
# |   `-- ...
# |-- config
# |   `-- ...
# |-- plugins
# |   |-- ...
# |   |-- uk.ac.gda.core
# |       |-- ...
# |       `-- scripts/gdascripts/launcher/gda_launcher.py
# `-- users
#     |-- data
#     |-- logs
#     `-- scripts

#Find GDA_TOP for default_environment
gda_top = __file__.split(os.sep)[0:-6]
if gda_top[0] == "":	gda_top[0] = os.sep #Split has eaten a leading separator


default_environment = {
#	NOTE: Unless set GDA_TOP is assumed to be six folders above this file
	"JAVA_HOME":[os.getenv("JAVA_HOME")],
	"GDA_TOP":gda_top,
	"GDA_ROOT":["$GDA_TOP","plugins", "uk.ac.gda.core"],
	"GDA_CONFIG":["$GDA_TOP","config"],
	"GDA_USERS":["$GDA_TOP","users"],
	"GDA_JAVA_PROPERTIES":["$GDA_CONFIG","properties","java.properties"],
	"JACORB_CONFIG_DIR":["$GDA_CONFIG","properties"],
	"JCALIBRARY_PROPERTIES":["$GDA_CONFIG","properties","JCALibrary.properties"],
	"LOGSERVER_XML":["$GDA_CONFIG","xml","logServer.xml"],
	"PROFILER_DIR" :["/dls_sw","dasc","jprofiler4"],
	"PROFILER_AGENT_JAR":["$PROFILER_DIR","bin","agent.jar"],
	"PROFILER_AGENT_LIB":["$PROFILER_DIR","bin","linux-x86"],
	"GDA_LIB_SUBDIR":["$GDA_ROOT","lib","$LIBRARY_SUBDIR"],
	"RCP_CLIENT_WORKSPACE":["$GDA_USERS"],
	"RCP_CLIENT_IMAGE":["$GDA_CONFIG","eclipse","gda"],
	"ns_port":6700,
	"ls_port":6788,
	"debug":False,
	"debug_port":8000,
	"debug_suspend":False,
	"profile":False,
	"profile_port":8849
	}


class Settings:
	def __init__(self,env, default_env):
		self._env = env
		self._default_env = default_env
		self.verbose = False

	def set_verbose(self,verbose):
		self.verbose = verbose
		if self._default_env != None:
			self._default_env.set_verbose(verbose)

	def get(self, key):
		if key in self._env:
			return self._env[key]
		else:
			if self._default_env == None:
				msg =  "Settings - error getting value for item named " + key
				raise AttributeError, msg	
			return self._default_env.get(key)
		
	def __getattr__(self, attrName):
		if attrName == "__eq__":
			raise AttributeError
		if attrName == "__cmp__":
			raise AttributeError
		if attrName == "__coerce__":
			raise AttributeError
		return self.get(attrName)

	def contains(self, key):
		if key in self._env:
			return True
		if self._default_env == None:
			return False
		return self._default_env.contains(key)

	def getValueAsList(self,key ):
		"""
		translates the setting for a key into a list
		if the value for the key is a list it iterates over the members either adding
		each item to the list or adding the result of calling getValueAsList on the item if it
		begins with a $
		"""
		val = self.get(key)
		valAslist = []
		if val.__class__ == "string".__class__:
			valAslist = [val]
		elif val.__class__ == ["list"].__class__ :
			for v in val:
				if v[0]=="$":
					valAslist += self.getValueAsList(v[1:])
				else:
					valAslist.append(v)
				
		else : 
			raise AttributeError, ("getPath cannot return a value for "+`key`+":"+`val`+":"+`val.__class__`)
		return valAslist
		
	
	def getPath(self, key):
		"""
		translates the setting for a key into a path
		if the value for the key is a list it iterates over the members either adding
		each item to the path or adding the result of calling getPath on the item if it
		begins with a $
		"""
		path=""
		val = self.getValueAsList(key)
		for v in val:
			path = os.path.join(path,v)
		if self.verbose:
			print `key`+":"+`path`
		return path
		
	def getValueForDisplay(self, key):
		"""
		Returns a value as a string. If the value is a list it is processed into a path so as to be more useful.
		"""
		val = self.get(key)
		if type(val) is list:
			path = ""
			for v in val:
				path = os.path.join(path,v)
			return path
		else:
			return str(val)
	
class GdaOS:
	"""
	class to mock the os module
	"""
	def __init__(self):
		self.verbose = False
		self.dry_run = False
	def getenv(self, env_name):
		val =  os.getenv(env_name)
		if val == None:
			val=""
		if self.verbose :
			print "getenv : " + `env_name` + "=" + `val`
		return val

	def putenv(self, env_name, env_value):
		if self.verbose :
			print "putenv : " + `env_name` + "=" + `env_value` 
		if not self.dry_run:
			os.putenv(env_name, env_value)
	def execv(self, command, args):
		if self.verbose or self.dry_run :
			print "execv:"
			print "\t"+`command`
			print "\t" +`args`
		if not self.dry_run:
			os.execv(command, args) #@UndefinedVariable
	def spawn_nowait(self, command, args):
		if self.verbose or self.dry_run :
			print "Popen:"
			print "\t"+`command`
			print "\t" +`args`
		if not self.dry_run:
		#	return subprocess.Popen((command,)+args).pid #Not in Python2.2
		#	return os.spawnlp(os.P_NOWAIT, ' '.join( (command,)+args )) #Not in Jython
			return os.popen(' '.join( ('nohup', command,)+args ), 'r')

	def popen_get_stdout(self, command, dry_run):
		if self.verbose:
			print command
		if not dry_run:
			#return subprocess.Popen(command, shell=True, stdout=subprocess.PIPE).stdout
			return os.popen(command, 'r')
		else:
			return ""
	def runcmd_get_stdout(self, command, args):
		if self.verbose:
			print command
			print args
		if not self.dry_run:
			#return subprocess.Popen((command,)+args, stdout=subprocess.PIPE).stdout
			return os.popen(' '.join( (command,)+args ), 'r')
		else:
			return ""
	def set_verbose(self,verbose):
		self.verbose = verbose
	def set_dry_run(self,dry_run):
		self.dry_run = dry_run


class GdaLauncher:
	def usage(self, show_help=False):
		print __doc__
		if show_help:
			print "The work is done by gda_launcher.GdaLauncher(environment).process_commandline() "
			print "where environment is based on the environment you have supplied and the defaults:"
			keys = default_environment.keys()
			keys.sort()
			longest_key_length = max(map(len, keys))
			for key in keys:
				val = self.env.__getattr__(key)
				valDef = default_environment[key]
				print key.ljust(longest_key_length), ':', self.env.getValueForDisplay(key),
				if( val == valDef):
					print "(default)"
				else:
					print ""
			print "NOTE: when overriding these keys paths must be given as lists"
			print ""

	def __init__(self, environment={}):
		self.gdaos = GdaOS()
		default_environment["LIBRARY_SUBDIR"]=self.get_library_subdir()
		self.env = Settings(environment, Settings(default_environment, None))
		self.dry_run=False

	def process_commandlineArgs(self, argv):
		self.verbose = False
		self.dry_run = False
		self.testing = False
		task=""
		self.debug=self.env.get("debug")
		self.debug_port = self.env.get("debug_port")
		self.debug_suspend = self.env.get("debug_suspend")
		self.profile = self.env.get("profile")
		self.profile_port = self.env.get("profile_port")
		ns_port=self.env.get("ns_port")
		ls_port=self.env.get("ls_port")
		os_xml=""
		get_version = False
		self.killonly=False
		self.kill = True
		try:
			opts, args = getopt.getopt(argv, "hvnt", ["help", "verbose","dry-run","testing","task=", "ns_port=", "ls_port=",
													    "debug","debug_port=", "debug_suspend", "profile","profile_port=",
													    "os_xml=", "killonly", "nokill", "version", ])
		except getopt.GetoptError:
			print "Invalid options" + `argv`
			self.usage()
			return False
		for opt, arg in opts:
			if opt in ("-h", "--help"):
				self.usage(True)
				return True
			elif opt in ("-v", "--verbose"):
				self.verbose = True
			elif opt in ("-n", "--dry-run"):
				self.dry_run = True
			elif opt in ("--testing",):
				self.testing = True
			elif opt in ("--debug",):
				self.debug = True
			elif opt in ("--debug_port",):
				self.debug_port = int(arg) 
			elif opt in ("--debug_suspend",):
				self.debug_suspend = True
			elif opt in ("--profile",):
				self.profile = True
			elif opt in ("--profile_port",):
				self.profile_port = int(arg)
			elif opt in ("--killonly"):
				self.killonly = True
			elif opt in ("--nokill"):
				self.kill = False
			elif opt in ("--version"):
				get_version = True
			elif opt in ("--ns_port",):
				ns_port = int(arg)
			elif opt in ("--ls_port",):
				ls_port = int(arg)
			elif opt in ("--os_xml",):
				os_xml = arg
			elif opt in ("-t", "--task"):
				task = arg		
		
		if( self.verbose):
			print "task="+`task`

		self.set_dry_run(self.dry_run)
		self.set_verbose(self.verbose)
		self.javabin = os.path.join(self.env.getPath("JAVA_HOME"), "bin", "java")
		self.gda_root = self.getPath("GDA_ROOT")
		self.gda_config = self.getPath("GDA_CONFIG")
		self.gda_users = self.getPath("GDA_USERS")
		self.gda_top = self.getPath("GDA_TOP")
		self.jacorb_config_dir = self.getPath("JACORB_CONFIG_DIR")
		self.java_properties = self.getPath("GDA_JAVA_PROPERTIES")
		self.jcalibrary_properties = self.getPath("JCALIBRARY_PROPERTIES")
		self.gda_class_path = os.path.join(self.gda_top, "plugins","uk.ac.gda.core", "classes", "main")
		if self.testing:
			self.gda_class_path += ":" + os.path.join(self.gda_top, "plugins","uk.ac.gda.core", "classes", "test")
		self.gda_class_path += ":" + os.path.join(self.gda_top, "plugins","uk.ac.gda.core", "jars", "*")
		self.gda_class_path += ":" + os.path.join(self.gda_top, "plugins", "uk.ac.gda.libs",  "*")
		self.gda_class_path += ":" + os.path.join(self.gda_top, "plugins", "uk.ac.gda.libs",  "jython-2.2.1","jython.jar")
		self.gda_class_path += ":" + os.path.join(self.gda_top, "plugins", "uk.ac.gda.common",  "bin")

		if get_version:
			return self.get_version()

		tasks = task.split(",")
		pids=[]
		for mytask in tasks:
			if  mytask == "log_panel":
				pids.append(self.log_panel())
			elif mytask.find("log_server")>=0:
				pids.append(self.log_server(port=ls_port))
			elif mytask.find("name_server")>=0:
				pids.append(self.name_server(port=ns_port))
			elif mytask.find("event_server")>=0:
				pids.append(self.event_server())
			elif mytask.find("object_server")>=0:
				pids.append(self.object_server(xmlfile=os_xml))
			elif mytask == "servers":
				pids.append(self.log_server(port=ls_port))
				self.name_server(port=ns_port)
				self.event_server()
				self.object_server(xmlfile=os_xml)
			elif mytask == "client":
				pids.append(self.client())
			elif mytask == "rcp_client":
				pids.append(self.rcp_client())
			else:
				print "task :" + `task` + "  is not recognised"
				self.usage()
				return False
			
		return pids
	
	def set_verbose(self,verbose):
		self.verbose = verbose
		self.gdaos.set_verbose(verbose)
		self.env.set_verbose(verbose)
	def set_dry_run(self,dry_run):
		self.dry_run = dry_run
		self.gdaos.set_dry_run(dry_run)
				
	def getPath(self,key):
		"""
		See doc for Settings.getPath
		"""
		return self.env.getPath(key)


	def get_library_subdir(self):
		a = self.gdaos.popen_get_stdout("uname", False)
		lines = a.readlines()
		os = lines[0].strip()
		a = self.gdaos.popen_get_stdout("uname -i", False)
		lines = a.readlines()
		processor = lines[0].strip()
		return os+"-"+processor

	def addToLd_Library_Path(self,ld_library_path ):
		newvalue = ld_library_path
		currentvalue = self.gdaos.getenv("LD_LIBRARY_PATH")
		if len(currentvalue) >0 :
			newvalue += ":" + currentvalue
		self.gdaos.putenv("LD_LIBRARY_PATH", newvalue)

	def getpids(self,grep_phrases):
		if len(grep_phrases)==0:
			raise "getpids - grep_phrases is empty)"
		a = self.gdaos.popen_get_stdout("ps -ef --cols 6000 | grep -F -e '%s' | grep -v grep " % grep_phrases[0], False)
		lines = a.readlines()
		if self.verbose :
			print lines
		pids=()
		for line in lines :
			line = line.strip()
			parts = line.split()
			allfound = True 
			for grep_phrase in grep_phrases:
				if not grep_phrase in parts:
					allfound = False
					break
			if allfound:
				pids += (int(parts[1]),)
		if self.verbose :
			print pids
		return pids

	def kill_process(self,grep_phrases):
		pids= self.getpids(grep_phrases)
		for pid in pids:
			print "About to kill process id=%",pid
			if not self.dry_run:
				try:
#					os.kill(pid, 15)	# Non in Jython
#					os.kill(pid, 9)		# Non in Jython
#					self.gdaos.runcmd_get_stdout("kill", ('-15',str(pid)) )
					self.gdaos.runcmd_get_stdout("kill", ('-9', str(pid)) )
				except:
					type, exception, traceback = sys.exc_info()
					print "Exception raised when trying to kill process "+ `pid` + ". " + `exception`
					print "You may not have privilege"
					return False
	
		if self.dry_run:
			return True
		pids= self.getpids(grep_phrases)
		for pid in pids:
			print "Error -unable to kill process id=%d",pid
		return len(pids)==0


	def java(self, class_path,class_name, prevmargs=(), vmargs=(), args=(), wait=False):
		self.gdaos.putenv("CLASSPATH", class_path)
		class_name_tuple = (class_name,)
		return self.start_process(self.javabin, prevmargs, vmargs, class_name_tuple + args, wait)

	def start_process(self, imagePath, prevmargs=(), vmargs=(),  args=(), wait=False):
		self.addToLd_Library_Path(self.getPath("GDA_LIB_SUBDIR"))
		vmargs_tuple = ()
		if self.verbose:
			vmargs_tuple = ("-verbose",)

		if self.debug:
			suspend = "n"
			if self.debug_suspend:
				suspend = "y"
			vmargs_debug = (
				"-Xdebug",
				"-Xrunjdwp:transport=dt_socket,"+
				"address="+`self.debug_port` +
				",server=y,suspend="+suspend, )
			vmargs_tuple += vmargs_debug
			
		if self.profile:
			vmargs_profile = (
				"-agentlib:jprofilerti=port="+`self.profile_port`,
				"-Xbootclasspath/a:"+self.getPath("PROFILER_AGENT_JAR")
				) 
			vmargs_tuple += vmargs_profile
			self.addToLd_Library_Path(self.getPath("PROFILER_AGENT_LIB"))
		vmargs_tuple += vmargs
		self.gdaos.putenv("JAVA_HOME", self.env.getPath("JAVA_HOME"))
		if wait :
			return self.gdaos.runcmd_get_stdout( imagePath , prevmargs+vmargs_tuple + args)
		else :
			return self.gdaos.spawn_nowait( imagePath , prevmargs+vmargs_tuple + args)

	def stop_start_java(self, classpath, class_name, vmargs=(), args=(), sleep=0):
		if self.kill and not self.kill_process((class_name,)+vmargs+args):
			return 0
		if self.killonly:
			return 0
		pid = self.java(self.gda_class_path, class_name , vmargs=vmargs, args=args )
		if sleep <> 0:
			time.sleep(sleep)
		return pid
	
	def stop_start_process(self, imagePath, prevmargs=(), vmargs=(), args=(), sleep=0):
		if self.kill and not self.kill_process((imagePath,)+args):
			return 0
		if self.killonly:
			return 0
		pid = self.start_process(imagePath, prevmargs, vmargs, args )
		if sleep <> 0:
			time.sleep(sleep)
		return pid
		
	def log_panel(self ):
		class_name="gda.util.LogPanel"
		vmargs = (
					"-Dgda.root="+self.gda_root,
					"-Dgda.users="+self.gda_users,				  
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config,
				)
		return self.stop_start_java(self.gda_class_path, class_name , vmargs=vmargs)

	def log_server(self, port ):
		class_name="gda.util.LogServer"
		args=( `port`, self.getPath("LOGSERVER_XML"))
		return self.stop_start_java(self.gda_class_path, class_name , args=args, sleep=2)
	
	def name_server(self, port ):
		class_name="org.jacorb.naming.NameServer"
		vmargs = (
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB", 
					"-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton", 
					"-DOAPort=" + `port`
				)
		return self.stop_start_java(self.gda_class_path, class_name , vmargs=vmargs, sleep=2)

	def event_server(self):
		class_name="gda.factory.corba.util.ChannelServer"
		vmargs = (
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config
				)
		return self.stop_start_java(self.gda_class_path, class_name , vmargs=vmargs, sleep=2)


	def object_server(self, xmlfile=""):
		class_name="gda.util.ObjectServer"
		args=()
		if xmlfile != "":
			args=("-f",xmlfile)
		vmargs = (
					"-Dgda.root="+self.gda_root,
					"-Dgda.users="+self.gda_users,				  
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config,
					"-Dgov.aps.jca.JCALibrary.properties="+self.jcalibrary_properties,
				)
		return self.stop_start_java(self.gda_class_path, class_name , vmargs=vmargs, args=args)

	def client(self):
		class_name="gda.gui.AcquisitionGUI"		
		vmargs = (
					"-Dgda.root="+self.gda_root,
					"-Dgda.users="+self.gda_users,				  
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config,
					"-Dgov.aps.jca.JCALibrary.properties="+self.jcalibrary_properties
				)
		return self.stop_start_java(self.gda_class_path, class_name , vmargs=vmargs)

	def rcp_client(self):
		vmargs = (  "-Dgda.root="+self.gda_root,
					"-Dgda.users="+self.gda_users,				  
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config,
					"-Dgov.aps.jca.JCALibrary.properties="+self.jcalibrary_properties
				)
		return self.stop_start_process(self.getPath("RCP_CLIENT_IMAGE"), 
				prevmargs=("-data", self.getPath("RCP_CLIENT_WORKSPACE"), "-vmargs"),vmargs=vmargs)

	def get_version(self):
		""" method that can call onto a class to test the system works
		"""
		vmargs = (
					"-Dgda.root="+self.gda_root,
					"-Dgda.users="+self.gda_users,				  
					"-Djacorb.config.dir="+ self.jacorb_config_dir,
					"-Dgda.propertiesFile=" + self.java_properties,
					"-Dgda.config="+self.gda_config,
					"-Dgov.aps.jca.JCALibrary.properties="+self.jcalibrary_properties
				)
		class_name="gda.util.GDALauncherHelper"
		class_path = self.gda_class_path+":" + os.path.join(self.gda_root, "plugins","uk.ac.gda.core", "classes", "test")
		outputFilePath = "TestStartupScript_Output.py"
		verbose = self.verbose
		self.verbose = False
		a = self.java(class_path, class_name, vmargs=vmargs, args=(outputFilePath,), wait=True)
		self.verbose = verbose
		lines = a.readlines()
		if self.verbose:
			print lines
		script =""
		for line in lines:
			script += line
		exec(script)
		class_path_returned = eval("TestStartupScript_result[\"CLASSPATH\"]")
		if `class_path` != `class_path_returned`:
			print "Error get_version returned incorrect classpath"
			print `class_path_returned`
			print `class_path`
			return "Error get_version returned incorrect classpath"
		else:
			return eval("TestStartupScript_result[\"Release\"]")

def main(argv):
	return GdaLauncher(default_environment).process_commandlineArgs(sys.argv[1:])
if __name__ == '__main__':
	print main(sys.argv[1:])
