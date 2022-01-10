# Running javac -h requires CLASSPATH environment variable to be set correctly
# To do this from eclipse:
#
# Run -> External Tools -> External Tools configuration (or via icon on toolbar)
# New launch configuration
# On main tab:
#     Name: Anything, eg "make libgdacommon"
#     Location: Path to 'make' command, eg /usr/bin/make
#     Working directory: Path to libgda_common. This can use variable expansion
#         so something like ${workspace_loc:/uk.ac.gda.common/libgda_common}
# On environment tab add variables for
#     CLASSPATH: ${project_classpath}
#     JAVA_HOME: ${/path/to/your/java/installation}
#
# Running the configuration should then rebuild the shared library

all: header lib

lib:
ifndef JAVA_HOME
	$(error JAVA_HOME is required)
endif
	gcc -I${JAVA_HOME}/include\
		-I${JAVA_HOME}/include/linux -shared\
		-o ../lib/linux-x86_64/libgda_common.so\
		-fPIC\
		gda_util_LibGdaCommon.c

header:
ifndef CLASSPATH
	$(error CLASSPATH is required)
endif
	javac -h . -classpath ${CLASSPATH} ../src/gda/util/LibGdaCommon.java

clean:
	rm -f gda_util_LibGdaCommon.o ../lib/linux-{x86,x86_64}/libgda_common.so
