/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.data;

import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.util.TestUtils;

import java.io.File;

import org.springframework.util.StopWatch;

/**
 * Tests a few different ways of loading Mythen data to see which is fastest.
 */
public class DataLoadAlgorithmTest {
	
	private static final String TEST_FILE = "Si_15keV_5.dat";
	
	abstract static class Algorithm {
		
		String name;
		
		public Algorithm(String name) {
			this.name = name;
		}
		
		public abstract void run() throws Exception;
	}
	
	public static void main(String args[]) throws Exception {
		final File dataFile = TestUtils.getResourceAsFile(DataLoadAlgorithmTest.class, TEST_FILE);
		final String filename = dataFile.getAbsolutePath();
		
		StopWatch sw = new StopWatch(DataLoadAlgorithmTest.class.getSimpleName());
		
		Algorithm loadUsingCurrentAlgorithm = new Algorithm(
			"loadUsingCurrentAlgorithm") {
				@Override
				public void run() throws Exception {
					MythenDataFileUtils.readMythenProcessedDataFile(filename, false);
				}
		};
		
		Algorithm loadByUsingSplit = new Algorithm(
			"loadByUsingSplit") {
			@Override
			public void run() throws Exception {
				MythenDataFileUtils.loadByUsingSplit(filename);
			}
		};
		
		Algorithm loadByUsingStreamTokenizer = new Algorithm(
			"loadByUsingStreamTokenizer") {
			@Override
			public void run() throws Exception {
				MythenDataFileUtils.loadByUsingStreamTokenizer(filename, FileType.PROCESSED);
			}
		};
		
		Algorithm loadByReadingFileContentAndUsingSplit = new Algorithm(
			"loadByReadingFileContentAndUsingSplit") {
			@Override
			public void run() throws Exception {
				MythenDataFileUtils.loadByReadingFileContentAndUsingSplit(filename);
			}
		};
		
		Algorithm loadByReadingFileContentAndUsingStreamTokenizer = new Algorithm(
			"loadByReadingFileContentAndUsingStreamTokenizer") {
			@Override
			public void run() throws Exception {
				MythenDataFileUtils.loadByReadingFileContentAndUsingStreamTokenizer(filename, FileType.PROCESSED);
			}
		};
		
		Algorithm[] algorithms = new Algorithm[] {
			loadUsingCurrentAlgorithm,
			loadByUsingSplit,
			loadByUsingStreamTokenizer,
			loadByReadingFileContentAndUsingSplit,
			loadByReadingFileContentAndUsingStreamTokenizer
		};
		
		for (Algorithm a : algorithms) {
			System.out.printf("Testing '%s' algorithm...\n", a.name);
			
			// warm-up
			for (int i=0; i<1000; i++) {
				a.run();
			}
			
			// timing
			sw.start(a.name);
			for (int i=0; i<1000; i++) {
				a.run();
			}
			sw.stop();
		}
		
		// display results
		System.out.println(sw.prettyPrint());
	}
	
}
