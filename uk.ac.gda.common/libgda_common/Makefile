all: header libs

libs: 32 64

32:
	gcc -m32 -fPIC -c gda_util_LibGdaCommon.c
	gcc -m32 -shared -o ../lib/linux-x86/libgda_common.so gda_util_LibGdaCommon.o

64:
	gcc -m64 -fPIC -c gda_util_LibGdaCommon.c
	gcc -m64 -shared -o ../lib/linux-x86_64/libgda_common.so gda_util_LibGdaCommon.o

header:
	javah -classpath ../bin gda.util.LibGdaCommon

clean:
	rm -f gda_util_LibGdaCommon.o ../lib/linux-{x86,x86_64}/libgda_common.so
