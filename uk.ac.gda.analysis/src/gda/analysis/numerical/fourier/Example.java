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
 * Example Class.
 */
public class Example {

	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Just creating some data with 50hz and 120hz frequency
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
		// output this data as an example
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("fftw_before.dat"));
			for (int i = 0; i < data.size(); i++) {
				out.write(i + "\t" + data.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
		// FFTW this mofo
		DataVector[] res = FFTW.realForward(data);

		// Output the magnitude and power spectrum
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
		// just testing 2D and 3D fft don't give errors.....

		DataVector data2D = new DataVector(100, 100);
		DataVector[] res2D = FFTW.realForward(data2D);

		data2D = FFTW.realBackward(res2D[0], res2D[1]);
		DataVector data3D = new DataVector(100, 100, 100);

		DataVector[] res3D = FFTW.realForward(data3D);
		data3D = FFTW.realBackward(res3D[0], res3D[1]);

	}

}