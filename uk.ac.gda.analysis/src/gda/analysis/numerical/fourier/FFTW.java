/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.analysis.numerical.fourier;

import gda.analysis.datastructure.DataVector;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * An interface to FFTW the "Fastest Fourier Transform in the West" This code only works with FFTW3 The real,imag input
 * or outputs are split into two vectors. If you want the magnitude or power : mag = sqrt(real**2 + imag**2) power =
 * mag**2
 */
public class FFTW {
	/**
	 * @param g
	 *            A real data vector of 1,2 or 3 dimensions
	 * @return Real and imaginary parts of fft
	 */
	public static DataVector[] realForward(DataVector g) {
		int width, height, depth, wft, hft, dft, index = 0;
		// What is the dimensions of the datavector
		switch (g.getDimensions().length) {
		// 1D
		case 1:
			double[] data = g.doubleArray();
			double[] result = FFTWReal.oneDimensionalForward(data);
			int length = g.size() / 2 + 1;
			DataVector real = new DataVector(length);
			DataVector imag = new DataVector(length);
			index = 0;

			for (int i = 0; i < length; i++) {
				real.set(index, result[2 * i]);
				imag.set(index, result[2 * i + 1]);
				index++;
			}
			return new DataVector[] { real, imag };
			// 2D
		case 2:
			width = g.getDimensions()[0];
			height = g.getDimensions()[1];

			double[] result2D = FFTWReal.twoDimensionalForward(width, height, g.doubleArray());
			// width of dft data
			wft = width / 2 + 1;
			// height of dft data
			hft = height;
			System.out.println("sizes of 2D\t" + result2D.length / 2 + "\t" + wft + "\t" + hft);
			DataVector real2D = new DataVector(wft, hft);
			DataVector imag2D = new DataVector(wft, hft);

			index = 0;
			for (int i = 0; i < wft; i++) {
				for (int j = 0; j < hft; j++) {
					real2D.set(index, result2D[2 * (j * wft + i)]);
					imag2D.set(index, result2D[2 * (j * wft + i) + 1]);
					index++;
				}
			}
			return new DataVector[] { real2D, imag2D };
			// 3D
		case 3:
			width = g.getDimensions()[0];
			height = g.getDimensions()[1];
			depth = g.getDimensions()[2];

			double[] result3D = FFTWReal.threeDimensionalForward(width, height, depth, g.doubleArray());
			System.out.println("sizes of 3D\t" + result3D.length / 2);

			// width of dft data
			wft = width / 2 + 1;
			// height of dft data
			hft = height;
			// depth
			dft = depth;
			DataVector real3D = new DataVector(wft, hft, dft);
			DataVector imag3D = new DataVector(wft, hft, dft);

			// width of dft data
			wft = width / 2 + 1;
			index = 0;
			for (int i = 0; i < wft; i++) {
				for (int j = 0; j < height; j++) {
					for (int k = 0; k < depth; k++) {
						real3D.set(index, result3D[2 * (k * wft * height + j * wft + i)]);
						imag3D.set(index, result3D[2 * (k * wft * height + j * wft + i) + 1]);
						index++;
					}
				}
			}
			return new DataVector[] { real3D, imag3D };

		}
		return null;
	}

	/**
	 * Performs an inverse fft of real and imaginary parts to a a real data vector
	 * 
	 * @param real
	 *            real part (A data vector of 1,2 or 3 dimensions )
	 * @param imag
	 * @return a data vector containing real values
	 */
	public static DataVector realBackward(DataVector real, DataVector imag) {
		int width, height, depth, wft, hft;
		double[] complex;
		// build up the complex array
		complex = new double[2 * real.size()];

		for (int i = 0; i < real.size(); i++) {
			complex[2 * i] = real.get(i);
			complex[2 * i + 1] = imag.get(i);
		}

		// Now calculate the inverse fft
		switch (real.getDimensions().length) {
		// 1D
		case 1:

			double[] result = FFTWReal.oneDimensionalBackward(complex);
			return new DataVector(result);
			// 2D
		case 2:
			wft = real.getDimensions()[0];
			hft = real.getDimensions()[1];
			// real height and width of 2D array
			width = 2 * (wft - 1);
			height = hft;

			double[] result2D = FFTWReal.twoDimensionalBackward(width, height, complex);
			return new DataVector(width, height, result2D);
			// 3D
		case 3:
			wft = real.getDimensions()[0];
			height = real.getDimensions()[1];
			depth = real.getDimensions()[2];
			// width of real data
			width = 2 * (wft - 1);

			double[] result3D = FFTWReal.threeDimensionalBackward(width, height, depth, complex);
			return new DataVector(width, height, depth, result3D);

		}
		return null;
	}

	/*
	 * Performs an inverse fft of real and imaginary parts to a a real data vector @param real real part (A data vector
	 * of 1,2 or 3 dimensions ) @param imag @param imag imaginary part (A data vector of 1,2 or 3 dimensions ) @return a
	 * data vector containing real values
	 */
	// public static DataVector[] ComplexToComplexForward(DataVector
	// real,DataVector imag)
	// {
	// }
	/*
	 * Performs an inverse fft of real and imaginary parts to a a real data vector @param args @param real real part (A
	 * data vector of 1,2 or 3 dimensions ) @param imag @param imag imaginary part (A data vector of 1,2 or 3 dimensions )
	 */
	// public static DataVector[] ComplexToComplexBackward(DataVector
	// real,DataVector imag)
	// {
	// }
	/**
	 * Test Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		double Fs = 1000; // sample frequency
		double T = 1 / Fs; // Sample time
		int size = 1000; // Length of signal
		DataVector data = new DataVector(size);

		for (int i = 0; i < size; i++) {
			double t = i * T;
			// Sum of a 50 Hz sinusoid and a 120 Hz sinusoid
			double value = 0.7 * Math.sin(2 * Math.PI * 50 * t) + Math.sin(2 * Math.PI * 120 * t);
			data.set(i, value);
		}

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("fftw_before.dat"));
			for (int i = 0; i < data.size(); i++) {
				out.write(i + "\t" + data.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}

		DataVector[] res = realForward(data);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("fftw_out.dat"));
			for (int i = 0; i < res[0].size(); i++) {
				double mag = Math.pow(res[0].get(i), 2.0) + Math.pow(res[1].get(i), 2.0);
				mag = Math.sqrt(mag);
				double power = mag * mag;
				out.write(Fs / 2.0 * ((double) i / (double) res[0].size()) + "\t" + mag + "\t" + power + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
		DataVector data2D = new DataVector(100, 100);
		DataVector[] res2D = realForward(data2D);
		data2D = realBackward(res2D[0], res2D[1]);
		DataVector data3D = new DataVector(100, 100, 100);
		DataVector[] res3D = realForward(data3D);
		data3D = realBackward(res3D[0], res3D[1]);
		for (int i = 0; i < data3D.size(); i++) {
			if (data3D.get(i) != 0.0)
				System.out.println(i + "\t" + data3D.get(i) + "\n");
		}

	}
	/**
	 * FFT puts the DC component at index 0 sometimes you'd like this in the middle FFT images, for example, are usually
	 * shown with zero shifted to the centre of the image. This routine will do that shift for you
	 * 
	 * @param realData
	 */
	// public static void fftShift(DataVector realData) {
	// }
	/**
	 * FFT puts the DC component at index 0 sometimes you'd like this in the middle FFT images, for example, are usually
	 * shown with zero shifted to the centre of the image. This routine will do that shift for you
	 * 
	 * @param real
	 * @param imag
	 */
	// public static void fftShift(DataVector real,DataVector imag) {
	// }
}
