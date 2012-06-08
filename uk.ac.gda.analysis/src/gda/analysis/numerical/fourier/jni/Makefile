#
#                   Makefile of jfftw c directory
#
CC=gcc
CFLAGS=-march=pentium  -Wall $(FFTWINCLUDE) -I/home/pq67/local/packages/jdk1.5.0_05/include -I/home/pq67/local/packages/jdk1.5.0_05/include/linux
LIBRARY=libjfftw3.a
SHARED=libjfftw3.so
AR=ar rv
LD=ld -share -whole-archive
FFTWLIB=-L/scratch/fftw/lib 
FFTWINCLUDE=-I/scratch/fftw/include
.suffixes: .o .c
.c.o:
	${CC} -c ${CFLAGS} $<

libops: gda_analysis_numerical_fourier_FFTWImp.o \
	gda_analysis_numerical_fourier_FFTWComplexImp.o \
	gda_analysis_numerical_fourier_FFTWRealImp.o 
	${AR} ${LIBRARY} $?
	ranlib ${LIBRARY}
	${LD} -o ${SHARED} $(FFTWLIB) -lfftw3 -lm ${LIBRARY}


gda_analysis_numerical_fourier_FFTWImp.o: gda_analysis_numerical_fourier_FFTWImp.c gda_analysis_numerical_fourier_FFTW.h
gda_analysis_numerical_fourier_FFTWComplexImp.o: gda_analysis_numerical_fourier_FFTWComplexImp.c gda_analysis_numerical_fourier_FFTWComplex.h
gda_analysis_numerical_fourier_FFTWRealImp.o: gda_analysis_numerical_fourier_FFTWRealImp.c gda_analysis_numerical_fourier_FFTWReal.h

clean:
	rm -f *.o

cleanall:
	rm -f *.o
	rm -f ${LIBRARY}
	rm -f ${SHARED}

