/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.odccd.collectionstrategy;

import gda.data.PathConstructor;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.scan.ScanInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODCCDOverflow extends ODCCDSingleExposure {

	/* Class properties */

	private int multifactor;
	private String runfileName;
	private String experimentName;
	private int finalFileSequenceNumber;

	/* Class */

	private static final Logger logger = LoggerFactory.getLogger(ODCCDOverflow.class);
	private List<String> fastFilenames;
	private List<String> finalFilenames;
	protected int fastFilenamesRead;

	/* NXCollectionStrategyPlugin methods */

	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("prepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		super.prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
		if (scanInfo == null) {
			logger.info("...prepareForCollection() null scanInfo! Returning...");
			return;
		}
		fastFilenames = new ArrayList<String>();
		finalFilenames = new ArrayList<String>();
		fastFilenamesRead = 0;
	}

	/**
	 * @see gda.device.Detector#waitWhileBusy()
	 */
	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		logger.trace("waitWhileBusy() called, collecting={} fastFilenames.size()={}, unixfilenames.size()={} stack trace {}",
				collecting, fastFilenames.size(), unixFilenames.size(), Arrays.toString(Thread.currentThread().getStackTrace()));

		if (collecting) {
			waitForImageTaken();

			boolean fast = (fastFilenames.size() == unixFilenames.size());

			String unixFilename = getUnixFilename(fast ? "fast" : "slow");
			logger.trace("Image taken, saving to {}", unixFilename);
			String odccdFilename = getOdccdFilePath(unixFilename);
			logger.trace("using the windows filename {}", odccdFilename);

			ensureDirectoryExists(unixFilename);

			int intensity_integral=0;
			// TODO: Work out how to use i0Monitor to get intensity_integral here.
			String parameters = geometryParameters()+" "+fileParameters(isDarkSubtraction())+" "+intensity_integral+" "+collectionTime+" "+getBinning();

			if (fast) { // TODO: Is this reliable? Is exposure.getPosition always correct value in collectData?
				logger.debug("Saving fast image to {}", unixFilename);
				saveImageOverflow(odccdFilename, parameters+overflowParameters(multifactor, "", "", experimentName));
				fastFilenames.add(unixFilename);
			} else if (fastFilenames.size() > unixFilenames.size()) {
				String runfileOdccdFilename = getOdccdFilePath(runfileName);
				String finalUnixFilename = String.format("%s/spool/%s/frames/%s_%d_%d.img", PathConstructor.createFromDefaultProperty(),
						experimentName, experimentName, finalFileSequenceNumber, unixFilenames.size()+1);
				String finalOdccdFilename = getOdccdFilePath(finalUnixFilename);
				logger.debug("Saving slow image to {} & final image to {} ", unixFilename, finalUnixFilename);
				saveImageOverflow(odccdFilename, parameters+overflowParameters(1, finalOdccdFilename, runfileOdccdFilename, experimentName));
				unixFilenames.add(unixFilename);
				finalFilenames.add(finalUnixFilename);
			} else {
				throw new RuntimeException("Trying to take the slow image before the fast image has been taken!");
			}
			collecting = false;
		}
		logger.trace("...waitWhileBusy()");
	}

	private String overflowParameters(int multifactor, String final_filename, String run_filename, String experiment_name) {
		logger.trace("overflowParameters(multifactor={}, final_filename={}, run_filename={}, experiment_name={}",
										multifactor, final_filename, run_filename, experiment_name);
		return String.format(" 220000 %d \"%s\" \"%s\" \"%s\"", multifactor, final_filename, run_filename, experiment_name);
	}

	private void saveImageOverflow(String odccdfilename, String parameters) throws IOException {
		/* call smi_exps2b_atlas <1. filename> <2. phiStart> <3. phiStop> <4. phiVel>
				<5. kappaStart> <6. kappaStop> <7. kappaVel> 
				<8. omegaStart> <9. omegaStop> <10. omegaVel> 
				<11. twothetaStart> <12. twothetaStop> <13. twothetaVel> 
				<14. gammaStart> <15. gammaStop> <16. gammaVel> 
				<17. detector distance> 
				<18. apply repair correction> <19. poly mscalar> <20. unwarp>
				<21. flood poly> <22. export all intermediate images>
				<23. subtract correlated dark> <24. export compressed> (1=true, 0=false)
				<25. intensity integral> <26.time> <27. binning>
				<28. detector maxval> <29. multifactor>
				<30. final filename> <31. run filename> <32. experiment name>
		*/
		odccd.runScript("call smi_exps2b_atlas \"" + odccdfilename + "\" " + parameters);
		logger.trace("Waiting for api:IMAGE EXPORTED");
		odccd.readInputUntil("api:IMAGE EXPORTED");
	}

	/* PositionInputStream<NXDetectorDataAppender> methods */

	/** 
	 * Since it is the Oxford Diffraction IS software which writes the files, this class needs to return 
	 * the unix filenames of the files written.
	 */
	@Override
	public List<String> getInputStreamNames() {
		List<String> list = Arrays.asList("fast", "slow", "final");
		logger.trace("getInputStreamNames() returning {} stack trace {}", list, Arrays.toString(Thread.currentThread().getStackTrace()));
		return list;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> list = Arrays.asList("%s", "%s", "%s");
		logger.trace("getInputStreamFormats() returning {} stack trace {}", list, Arrays.toString(Thread.currentThread().getStackTrace()));
		return list;
	}

	@Override
	protected List<NXDetectorDataAppender> readPoll(int maxToRead, int index) {
		logger.trace("readPoll({} ignored, {}), fastFilenamesRead={}, elementsRead={}",
								maxToRead, index,  fastFilenamesRead, elementsRead);
		List<NXDetectorDataAppender> appenders = new ArrayList<>();
		// Since we only expect to return one element at a time with this collection strategy, use the simpler, but less
		// efficient option of returning one at once, letting the caller request more elements if needed.
		if (index % 2 == 0) {	//(fast) {
								//	^^ doesn't work, since we get multiple calls to read() from multiple regions
			if (fastFilenames.size() == (index/2)+1) { // > fastFilenamesRead) {
				logger.trace("fastFilenames.size() = {}", fastFilenames.size());
				appenders.add(new NXDetectorDataFileAppenderForSrs(fastFilenames.get(fastFilenamesRead), getInputStreamNames().get(0))); // TODO: Add pixel size and units?
				fastFilenamesRead++;
				logger.trace("Returning 1 new filename: {}", appenders);
			}
		} else { //if (unixFilenames.size() == finalFilenames.size()) {
			if (finalFilenames.size() == (index/2)+1) { // > elementsRead) {
				logger.trace("unixFilenames.size() = {}, finalFilenames.size() = {}", fastFilenames.size(), finalFilenames.size());
				// Can't get this to work for the moment as ADDetector calls NXDetectorDataWithFilepathForSrs.addFileName rather than NXDetectorDataWithFilepathForSrs.addFileNames
				//List<NXDetectorDataAppender> inner_appenders = new ArrayList<>();
				//inner_appenders.add(new NXDetectorDataFileAppenderForSrs(unixFilenames.get(elementsRead), getInputStreamNames().get(/*0*/1))); // TODO: Add pixel size and units?
				//inner_appenders.add(new NXDetectorDataFileAppenderForSrs(finalFilenames.get(elementsRead), getInputStreamNames().get(/*1*/2))); // TODO: Add pixel size and units?
				//appenders.add(new NXDetectorSerialAppender(inner_appenders));
				appenders.add(new NXDetectorDataFileAppenderForSrs(finalFilenames.get(elementsRead), getInputStreamNames().get(2))); // TODO: Add pixel size and units?
				elementsRead++;
				logger.trace("Returning 1 new pair of filenames: {}", appenders);
			}
		}
		return appenders;
	}

	/* Property methods */

	public int getMultifactor() {
		return multifactor;
	}

	public void setMultifactor(int multifactor) {
		this.multifactor = multifactor;
	}

	public String getRunfileName() {
		return runfileName;
	}

	public void setRunfileName(String runfileName) {
		this.runfileName = runfileName;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public int getFinalFileSequenceNumber() {
		return finalFileSequenceNumber;
	}

	public void setFinalFileSequenceNumber(int finalFileSequenceNumber) {
		this.finalFileSequenceNumber = finalFileSequenceNumber;
	}
}