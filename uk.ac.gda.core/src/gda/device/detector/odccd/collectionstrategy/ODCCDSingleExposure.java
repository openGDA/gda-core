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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.device.detector.nxdetector.CollectionStrategyBeanInterface;
import gda.device.detector.nxdetector.NXFileWriterPlugin;
import gda.device.detector.nxdetector.NXFileWriterWithTemplate;
import gda.device.detector.odccd.ODCCDController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionStreamIndexerProvider;
import gda.device.scannable.ScannableMotor;
import gda.scan.ScanInformation;
import uk.ac.gda.util.FilePathConverter;

/**
 * This class implements the Single exposure collection strategy for Oxford Diffraction CCD detectors.
 *
 * Since IS writes it's own files, and this controlled from here, this collection strategy must also be a file writer.
 *
 * This has been tested with the Oxford Diffraction Windows IS software (is_win32_Release.exe) version IS_v18A, File
 * revision 2.1.2039.0 (14/12/2011) and an Atlas CCD. It relies on scripts held in the gda-dls-beamlines-i15-od.git
 * repository. It also relies on various IS configuration which is not under revision control.
 * */
public class ODCCDSingleExposure implements CollectionStrategyBeanInterface, NXFileWriterPlugin, NXFileWriterWithTemplate {

	/* Class properties */
	private ODCCDController odccd;
	private String host;
	private FilePathConverter filePathConverter;
	private ContinuouslyScannableViaController dynamicPhiAxis;
	private ContinuouslyScannableViaController dynamicKappaAxis;
	private ContinuouslyScannableViaController dynamicThetaAxis;
	private ContinuouslyScannableViaController dynamicTwothetaAxis;
	private ContinuouslyScannableViaController dynamicGammaAxis;
	private PositionStreamIndexerProvider<Double> i0Monitor;
	private ScannableMotor staticPhiAxis;
	private ScannableMotor staticKappaAxis;
	private ScannableMotor staticThetaAxis;
	private ScannableMotor staticTwothetaAxis;
	private ScannableMotor staticGammaAxis;
	private ScannableMotor staticDdistAxis;
	private int binning;
	private int i0MonitorPcCapture=0;
	private boolean darkSubtraction;
	private boolean apply_repair_correction=true, poly_mscalar=false, unwarp=false, flood_poly=false;
	private boolean export_all_intermediate_images=false, export_compressed=true; // (1=true, 0=false)
	private double timeoutCollectionTimeMultiplier=1, timeoutCollectionTimeOffset=60;

	/* NXPlugin properties */
	private String name;

	/* NXFileWriterWithTemplate properties */
	private String fileTemplate;
	private String filePathTemplate;
	private String fileNameTemplate;

	/* Class */

	private static final Logger logger = LoggerFactory.getLogger(ODCCDSingleExposure.class);
	private static final String connectionProblem =
			"Stopping collection, but future collections may fail, and you may need to restart the IS application";
	private ScanInformation scanInfo;

	protected double collectionTime;
	private AcquireImageAsync acquireImageAsync = null;
	private final AtomicInteger status = new AtomicInteger(Detector.IDLE);

	protected List<String> unixFilenames;
	protected int elementsRead;
	private int elementsRequested;
	private AxisConfiguration phi, kappa, omega, twotheta, gamma, ddist;
	protected Callable<Double> i0MonitorCallable = null;

	/* CollectionStrategyBeanInterface methods */

	/* AsyncNXCollectionStrategy methods */

	/* NXCollectionStrategyPlugin methods */

	@Override
	public double getAcquireTime() throws Exception {
		logger.warn("getAcquireTime() called! Returning 0");
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		logger.warn("getAcquirePeriod() called! Returning 0");
		return 0;
	}

	@Override @Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		logger.warn("configureAcquireAndPeriodTimes({}) called! Ignoring", collectionTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("prepareForCollection({}, {}, {}) stack trace {}", collectionTime, numberImagesPerCollection, scanInfo,
				Arrays.toString(Thread.currentThread().getStackTrace()));
		this.collectionTime = collectionTime;
		if (numberImagesPerCollection != 1) {
			logger.warn("numberImagesPerCollection != 1 !");
		}
		// TODO: Is this valid in every case?
		if (scanInfo == null) {
			logger.debug("...prepareForCollection() null scanInfo! Returning...");
			return;
		}
		if (this.scanInfo != null) {
			logger.warn("redefining scanInfo, was {}", this.scanInfo);
		}
		this.scanInfo = scanInfo;

		unixFilenames = new ArrayList<String>();
		elementsRead = 0;
		elementsRequested = 0;

		if (!odccd.isConnected()) {
			try {
				odccd.connect(host);
			} catch (Exception ex) {
				logger.error("odccd.connect() failed. " + connectionProblem + " on {}", host, ex);
				throw ex;
			}
		} else {
			logger.warn("{} was already connected!", host);
		}
		logger.trace("...prepareForCollection()");
	}

	/**
	 * @see gda.device.Detector#collectData()
	 */
	@Override
	public final void collectData() throws Exception {
		logger.trace("collectData() collectionTime={} acquireImageAsync={} stack trace {}", collectionTime, acquireImageAsync, Arrays.toString(Thread.currentThread().getStackTrace()));
		// Start thread
		setStatus(Detector.BUSY);
		acquireImageAsync = new AcquireImageAsync();
		acquireImageAsync.start();
		logger.trace("...collectData()");
	}

	@Override
	public int getStatus() {
		final int status = this.status.get();
		logger.trace("getStatus() called, returning {}", status);
		return status;
	}

	private void setStatus(int newValue) {
		synchronized (status) {
			status.set(newValue);
			status.notifyAll();
		}
	}

	/**
	 * @see gda.device.Detector#waitWhileBusy()
	 */
	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		logger.trace("waitWhileBusy() called, status={} stack trace {}", status, Arrays.toString(Thread.currentThread().getStackTrace()));
		synchronized (status) {
			try {
				while (status.get() == Detector.BUSY) {
					status.wait(60000);
					if (status.get() == Detector.BUSY) {
						logger.warn("waitWhileBusy() waiting for 1 minute");
					}
				}
			} finally {
				//if interrupted clear the status state as the IOC may have crashed
				if ( status.get() != Detector.IDLE) {
					setStatus(Detector.IDLE);
					logger.warn("waitWhileBusy() status set to IDLE");
				}
			}
		}
		logger.trace("...waitWhileBusy()");
	}

	private void acquireImage() {
		logger.trace("aquireImage()... binning {}, collectionTime {}", binning, collectionTime);
		// call smi_exps1_atlas <1. binning> <2. timeout>
		odccd.runScript("call smi_exps1_atlas " + binning + " " + collectionTime);
		logger.trace("Waiting for api:IMAGE TAKEN");
		try {
			odccd.readInputUntil("api:IMAGE TAKEN");
		} catch (IOException e) {
			logger.error("acquireImage() failed.", e);
		}
	}

	protected void saveImage() {
		try {
			logger.trace("saveImage() called, status={}, unixfilenames.size()={}, stack trace {}",
					getStatus(), unixFilenames.size(), Arrays.toString(Thread.currentThread().getStackTrace()));

			String unixfilename = getUnixFilename("");
			logger.trace("Image taken, saving to {}", unixfilename);
			String odccdfilename = getOdccdFilePath(unixfilename);
			logger.trace("using the windows filename {}", odccdfilename);

			ensureDirectoryExists(unixfilename);

			logger.trace("getting intensity_integral for {}...", i0MonitorPcCapture);
			final int intensity_integral = i0MonitorCallable == null ? 0 : (int)Math.round(i0MonitorCallable.call());
			logger.trace("...intensity_integral {}", intensity_integral);

			/*  call smi_exps2_atlas <1. filename> <2. phiStart> <3. phiStop> <4. phiVel> <5. kappaStart> <6. kappaStop> <7. kappaVel>
					<8. omegaStart> <9. omegaStop> <10. omegaVel> <11. twothetaStart> <12. twothetaStop> <13. twothetaVel>
					<14. gammaStart> <15. gammaStop> <16. gammaVel> <17. detector distance>
					<18. apply repair correction> <19. poly mscalar> <20. unwarp> <21. flood poly>
					<22. export all intermediate images> <23. subtract correlated dark> <24. export compressed>  (1=true, 0=false)
					<25. intensity integral> <26.time> <27. binning>
			e.g.
				call smi_exps2_atlas X:/currentdir//scan_test_77_4.img 59.5 60 0.25 -134.76 -134.76 0
					89.996 89.996 0 -0.004 -0.004 0
					0 0 0 0
					1 0 0 0
					0 0 0
					0 2 1
			 */
			saveImage(odccdfilename, geometryParameters()+" "+fileParameters(darkSubtraction)+" "+intensity_integral+" "+collectionTime+" "+binning);
			unixFilenames.add(unixfilename);
		} catch (Exception e) {
			logger.error("saveImage() failed.", e);
			unixFilenames.add(null);
		}
	}

	protected String getUnixFilename(String fileNameTemplatePrefix) {
		String unixfilename = String.format(fileTemplate, filePathTemplate, fileNameTemplatePrefix+fileNameTemplate, unixFilenames.size()+1);
		int scanNumber = scanInfo.getScanNumber();
		unixfilename = StringUtils.replace(unixfilename, "$datadir$", PathConstructor.createFromDefaultProperty());
		unixfilename = StringUtils.replace(unixfilename, "$scan$", String.valueOf(scanNumber));
		return unixfilename;
	}

	protected void ensureDirectoryExists(String unixfilename) throws IOException {

		Path imageFolder = Paths.get(new File(unixfilename).getParent());
		if (Files.notExists(imageFolder)) {
			logger.trace("Attempting to create folder {}", imageFolder);
			Files.createDirectories(imageFolder);
			if (Files.notExists(imageFolder)) {
				logger.warn("... Unable to create ", imageFolder);
			} else {
				AclFileAttributeView view = Files.getFileAttributeView(imageFolder, AclFileAttributeView.class);
				logger.trace("... Created {} with acls {}", imageFolder, view);
			}
		}
	}

	//"x:/y2015/cm12167-4/_test_dark.img" 58.0 60.0 0.2 -133.99974216000004 -133.99974216000004 0.0 55.987428304800005 55.987428304800005 0.0 9.999581449199999 9.999581449199999 0.0 0.0 0.0 0.0 250.00000000000006 1 0 0 0 0 1 1 0 10.0 2

	protected String geometryParameters() {
		AxisConfiguration theta;
		phi = 		new AxisConfiguration("phi", 		dynamicPhiAxis,			staticPhiAxis);
		kappa = 	new AxisConfiguration("kappa",		dynamicKappaAxis,		staticKappaAxis);
		theta = 	new AxisConfiguration("theta",		dynamicThetaAxis,		staticThetaAxis);
		twotheta =	new AxisConfiguration("twotheta",	dynamicTwothetaAxis,	staticTwothetaAxis);
		gamma = 	new AxisConfiguration("gamma",		dynamicGammaAxis,		staticGammaAxis);
		ddist = 	new AxisConfiguration("ddist",		null,					staticDdistAxis);
		omega = 	new AxisConfiguration(theta.start+90, theta.stop+90, theta.step, theta.vel);

		/*  <2. phiStart> <3. phiStop> <4. phiVel> <5. kappaStart> <6. kappaStop> <7. kappaVel>
			<8. omegaStart> <9. omegaStop> <10. omegaVel> <11. twothetaStart> <12. twothetaStop> <13. twothetaVel>
			<14. gammaStart> <15. gammaStop> <16. gammaVel> <17. detector distance>
		*/
		String parameters = phi.start+" "+phi.stop+" "+phi.vel+" "+kappa.start+" "+kappa.stop+" "+kappa.vel+" "+
				omega.start+" "+omega.stop+" "+omega.vel+" "+twotheta.start+" "+twotheta.stop+" "+twotheta.vel+" "+
				gamma.start+" "+gamma.stop+" "+gamma.vel+" "+ddist.start;
		logger.trace("Using geometry parameters: {}", parameters);
		return parameters;
	}

	protected String fileParameters(boolean subtract_correlated_dark) {
		//		<4. apply repair correction> <5. poly mscalar>
		//		<6. unwarp> <7. flood poly> (0=apply correction, 0=no correction)
		//		<8. export all intermediate images> <9. subtract correlated dark (not used here)> <10. export compressed>  (1=true, 0=false)
		String parameters = (apply_repair_correction?1:0)+" "+(poly_mscalar?1:0)+" "+(unwarp?1:0)+" "+(flood_poly?1:0)+" "+
				(export_all_intermediate_images?1:0)+" "+(subtract_correlated_dark?1:0)+" "+(export_compressed?1:0);
		logger.trace("Using file parameters: {}", parameters);
		return parameters;
	}

	protected class AxisConfiguration {
		public AxisConfiguration(double start, double stop, double step, double vel) {
			this.start = start;
			this.stop = stop;
			this.step = step;
			this.vel = vel;
		}
		public AxisConfiguration(String axisName, ContinuouslyScannableViaController dynamicAxis, ScannableMotor staticAxis) {
			if (dynamicAxis != null) {
				if (Arrays.asList(scanInfo.getScannableNames()).contains(dynamicAxis.getName())) {
					logger.trace("Dynamic Axis for {} ({}) is in the list of scanInfo Scannables {}, configure dynamic position from move controller",
							axisName, dynamicAxis.getName(), scanInfo.getScannableNames());

					if (dynamicAxis.getContinuousMoveController() instanceof ConstantVelocityMoveController) {
						ConstantVelocityMoveController controller = (ConstantVelocityMoveController)dynamicAxis.getContinuousMoveController();
						// TODO: Work out how to enforce that these calculations match up with the calculations in the Continuous Move Controller.
						step = controller.getStep();
						double delta = (step>0 ? 1 : -1)*step/2;
						start = controller.getStart() - delta;
						stop = controller.getEnd() + delta;
						vel = controller.getStep()/collectionTime;
						logger.trace("Axis {} ({}) has a ConstantVelocityMoveController ({}) start={}, stop={}, vel={} (from step={} & collectionTime={})",
								axisName, dynamicAxis.getName(), controller.getName(), start, stop, vel, controller.getStep(), collectionTime);
						return;
					}
					logger.warn("Axis {} ({}) has no ConstantVelocityMoveController, configuring static position!", axisName, dynamicAxis.getName());
				} else {
					logger.trace("Axis {} ({}) is not in the list of scanInfo Scannables {}, configuring static position.", axisName, dynamicAxis.getName(), scanInfo);
				}
			}
			if (staticAxis == null) {
				logger.trace("StaticAxis for {} is null, configure axis as all zero", axisName);
				start = stop = step = vel = 0;
				return;
			}
			Object position=null;
			double position_d;
			try {
				position = staticAxis.getPosition();
			} catch (DeviceException e) {
				logger.error("Could not getPosition() for {}", staticAxis.getName(), e);
			}
			try {
				position_d = (double)position;
			} catch (Exception e) {
				logger.error("Could not convert {} into a double for {}", position, staticAxis.getName(), e);
				position_d = 0;
			}
			logger.trace("Axis {} ({}), configuring static position {}", axisName, staticAxis.getName(), position);
			start = position_d;
			stop = start;
			step = 0;
			vel = 0;
		}
		public final double start;
		public final double stop;
		public final double step;
		public final double vel;
	}

	private void saveImage(String odccdfilename, String parameters) throws IOException {
		/*  call smi_exps2_atlas <1. filename> <2. phiStart> <3. phiStop> <4. phiVel> <5. kappaStart> <6. kappaStop> <7. kappaVel>
				<8. omegaStart> <9. omegaStop> <10. omegaVel> <11. twothetaStart> <12. twothetaStop> <13. twothetaVel>
				<14. gammaStart> <15. gammaStop> <16. gammaVel> <17. detector distance>
				<18. apply repair correction> <19. poly mscalar> <20. unwarp> <21. flood poly>
				<22. export all intermediate images> <23. subtract correlated dark> <24. export compressed>  (1=true, 0=false)
				<25. intensity integral> <26.time> <27. binning>
		*/
		odccd.runScript("call smi_exps2_atlas \"" + odccdfilename + "\" " + parameters);
		logger.trace("Waiting for api:IMAGE EXPORTED");
		odccd.readInputUntil("api:IMAGE EXPORTED");
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		logger.warn("setGenerateCallbacks({}) called! Ignoring", b);
	}

	@Override
	public boolean isGenerateCallbacks() {
		logger.warn("isGenerateCallbacks() called! Returning false");
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.warn("getNumberImagesPerCollection({}) called! Returning 1", collectionTime);
		return 1;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		logger.warn("requiresAsynchronousPlugins() called! Returning false");
		return false;
	}

	/* NXPlugin methods */

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		logger.warn("willRequireCallbacks() called! Returning false");
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.error("prepareForCollection({}, {}, {}) called!", numberImagesPerCollection, scanInfo.toString());
		throw new UnsupportedOperationException("Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo)");
	}

	@Override
	public void prepareForLine() throws Exception {
		logger.warn("prepareForLine()");
		// Set up any assigned i0 monitor now, so it gets started at the correct time.
		if (i0Monitor != null) {
			if (i0MonitorCallable != null) {
				logger.error("i0MonitorCallable for {} should be null at this point!", i0MonitorCallable);
			}
			i0MonitorCallable = i0Monitor.getPositionSteamIndexer(i0MonitorPcCapture).getNamedPositionCallable("ODCCD.i0Monitor",1);
			logger.info("i0MonitorCallable for {} is now {}", i0MonitorPcCapture, i0MonitorCallable);
		}
	}

	@Override
	public void completeLine() throws Exception {
		logger.warn("completeLine() called!");
	}

	@Override
	public void completeCollection() throws Exception {
		logger.trace("completeCollection() called");
		try {
			odccd.logout();
		} catch (Exception ex) {
			logger.error("odccd.logout() failed. "+ connectionProblem + " on {}", host, ex);
		} finally {
			setStatus(Detector.IDLE);
			logger.trace("...completeCollection()");
		}
	}

	@Override
	public void atCommandFailure() throws Exception {
		logger.trace("atCommandFailure() called");
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		logger.trace("stop() called");
		completeCollection();
	}

	/* PositionInputStream<NXDetectorDataAppender> methods */

	/**
	 * Since it is the Oxford Diffraction IS software which writes the files, this class needs to return
	 * the unix filenames of the files written.
	 */
	@Override
	public List<String> getInputStreamNames() {
		logger.trace("getInputStreamNames() called, returning {}", Arrays.asList("filename"));
		return Arrays.asList("filename");
	}

	@Override
	public List<String> getInputStreamFormats() {
		logger.trace("getInputStreamFormats() called, returning {}", Arrays.asList("%s"));
		// If we use NXDetectorDataFileLinkAppender's in read(), what format should we specify?
		return Arrays.asList("%s");
	}

	protected long millisFromSeconds(double t_s) {
		return (long)(t_s*1000);
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		logger.trace("read({}) stack trace {}", maxToRead, Arrays.toString(Thread.currentThread().getStackTrace()));
		logger.trace("elementsRead = {}, elementsRequested {}, status {}, filenames {}",
					elementsRead, elementsRequested, status, unixFilenames);
		long sleep_time_ms = Math.max(200, millisFromSeconds(collectionTime)); // Poll at no more than 5Hz
		long new_element_timeout_ms = millisFromSeconds(collectionTime*timeoutCollectionTimeMultiplier + timeoutCollectionTimeOffset);
		// A new element timeout of collectionTime + 50 seconds does not always seem to be enough. If darks are being subtracted, then the
		// atlas may spend collection time again twice collecting a dark image. We even saw collection times as low as 20
		// failing to return within 20+50 seconds.
		long log_timeout_ms = Math.min(new_element_timeout_ms/2, millisFromSeconds(collectionTime + 5));
		Date log_time = new Date();
		int index = elementsRequested;
		elementsRequested++;

		// Since we are expected to be providing a stream, we shouldn't report the same filename more than once, but we should
		// always return at least one item. If the caller needs more items than we provide, it should call this again.
		Date new_element_time = new Date();
		while (true) {
			List<NXDetectorDataAppender> newItems = readPoll(maxToRead, index);
			if (newItems.size() > 0) return newItems;

			if ((new Date().getTime() - log_time.getTime()) > log_timeout_ms) {
				logger.trace("{}: waiting for new filenames, filenames.size = {}", index, unixFilenames.size());
				log_time = new Date();
			}
			if ((new Date().getTime() - new_element_time.getTime()) > new_element_timeout_ms) {
				logger.error("{}:no new elements for {} ms, filenames.size = {}", index, new_element_timeout_ms, unixFilenames.size());
				throw new NoSuchElementException("no new elements for "+new_element_timeout_ms+" ms.");
			}
			Thread.sleep(sleep_time_ms);
		}
	}

	protected List<NXDetectorDataAppender> readPoll(int maxToRead, int index) {
		List<NXDetectorDataAppender> appenders = new ArrayList<>();

		if (maxToRead > Integer.MAX_VALUE - elementsRead) {
			maxToRead = Integer.MAX_VALUE - elementsRead;
			logger.trace("Limited maxToRead to {} so that it doesn't wrap and become negative when we add elementsRead {}", maxToRead, elementsRead);
		} // :
		List<String> filenamesSubset = unixFilenames.subList(elementsRead, Math.min(maxToRead+elementsRead,
				unixFilenames.size()));
		// TODO: Does subList cope with from and to being the same?

		if (filenamesSubset.size() > 0) {
			logger.trace("filenames.size = {} filenamesSubset.size = {}", unixFilenames.size(), filenamesSubset.size());
			for (String filename : filenamesSubset) {
				appenders.add(new NXDetectorDataFileAppenderForSrs(filename, getInputStreamNames().get(0))); // TODO: Add pixel size and units?
				elementsRead++;
			}
			logger.trace("Returning {} new filenames: {}", filenamesSubset.size(), appenders);
		}
		return appenders;
	}

	/* InitializingBean methods */

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.trace("afterPropertiesSet() called");
		if (odccd == null || host == null || filePathConverter == null || name == null) {
			logger.warn("odccd = {} host = {} filePathConverter = {} name = {}",
						 odccd,     host,     filePathConverter,     name);
		}
		if (staticDdistAxis == null) {
			logger.warn("staticDdistAxis = {}", staticDdistAxis);
		}
		if (fileTemplate == null || filePathTemplate == null || fileNameTemplate == null) {
			logger.debug("fileTemplate = {} filePathTemplate = {} fileNameTemplate = {}",
						  fileTemplate,     filePathTemplate,     fileNameTemplate);
		}
	}

	/* NXFileWriterPlugin methods */

	@Override
	public boolean appendsFilepathStrings() {
		logger.trace("appendsFilepathStrings() called, returning true");
		return true;
	}

	@Override @Deprecated
	public String getFullFileName() throws Exception {
		logger.error("getFullFileName() called! Returning null");
		return null;
	}

	/* NXFileWriterWithTemplate methods */

	@Override
	public void setFileTemplate(String fileTemplate) {
		logger.trace("setFileTemplate({}) called", fileTemplate);
		this.fileTemplate = fileTemplate;
	}

	@Override
	public void setFilePathTemplate(String filePathTemplate) {
		logger.trace("setFilePathTemplate({}) called", filePathTemplate);
		this.filePathTemplate = filePathTemplate;
	}

	@Override
	public void setFileNameTemplate(String fileNameTemplate) {
		logger.trace("setFileNameTemplate({}) called", fileNameTemplate);
		this.fileNameTemplate = fileNameTemplate;
	}

	/* Class methods */

	public void setFloodFile(String floodFile) throws IOException {
		if (!odccd.isConnected()) {
			try {
				odccd.connect(host);
			} catch (IOException ex) {
				logger.error("odccd.connect() failed. " + connectionProblem + " on {}", host, ex);
				throw ex;
			}
			setFloodFile(floodFile);
			odccd.logout();
		} else {
			// 	call setFloodFile_atlas <1. relative flood field file name>
			odccd.runScript("call setFloodFile_atlas \"" + floodFile + "\"");
			logger.info("Floodfile now set to {}", floodFile);
		}
	}

	public void takeDarksAndCorrelate(double exposureTime, String filename) throws IOException {
		if (!odccd.isConnected()) {
			try {
				odccd.connect(host);
			} catch (IOException ex) {
				logger.error("odccd.connect() failed. " + connectionProblem + " on {}", host, ex);
				throw ex;
			}
			takeDarksAndCorrelate(exposureTime, filename);
			odccd.logout();
		} else {
			// call correlateDark_atlas <1. time> <2. binning> <3. filepath>
			//		<4. apply repair correction> <5. poly mscalar>
			//		<6. unwarp> <7. flood poly> (0=apply correction, 0=no correction)
			//		<8. export all intermediate images> <9. subtract correlated dark (not used here)> <10. export compressed>  (1=true, 0=false)
			String unixfilename = PathConstructor.createFromDefaultProperty()+"/"+filename;
			String odccdfilename = getOdccdFilePath(unixfilename);
			odccd.runScript("call correlateDark_atlas " + exposureTime + " "+binning+" \""+odccdfilename+"\" "+fileParameters(false));
			odccd.readInputUntil("ATLAS End");
			logger.info("Saving dark image to {}", unixfilename);
		}
	}

	/* Inner classes */

	private class AcquireImageAsync extends Thread {
		@Override
		public void run() {
			acquireImage();
			setStatus(Detector.IDLE);
			saveImage();
		}
	}

	/* Helper functions */

	public String getOdccdFilePath(String unixfilepath){
		String odccdFilePath = filePathConverter != null ? filePathConverter.converttoInternal(unixfilepath)  : unixfilepath;
		logger.trace("getOdccdFilePath({}) called, returning {}", unixfilepath, odccdFilePath);
		return odccdFilePath;
	}

	/* Property methods */

	public ODCCDController getOdccd() {
		logger.trace("getOdccd() called");
		return odccd;
	}

	public void setOdccd(ODCCDController odccd) {
		logger.trace("setOdccd({}) called", odccd.getName());
		this.odccd = odccd;
	}

	public String getHost() {
		logger.trace("getHost() called, returning {}", host);
		return host;
	}

	public void setHost(String host) {
		logger.trace("setHost({}) called", host);
		this.host = host;
	}

	/* getName() is in NXPlugin interface*/

	public void setName(String name) {
		logger.trace("setName({}) called", name);
		this.name = name;
	}

	/*
	public FilePathConverter getFilePathConverter() {
		logger.trace("getFilePathConverter() called");
		return filePathConverter;
	}
	*/

	public void setFilePathConverter(FilePathConverter filePathConverter) {
		logger.trace("setFilePathConverter({}) called", filePathConverter);
		this.filePathConverter = filePathConverter;
	}

	public ContinuouslyScannableViaController getDynamicPhiAxis() {
		return dynamicPhiAxis;
	}

	public void setDynamicPhiAxis(ContinuouslyScannableViaController phiAxis) {
		this.dynamicPhiAxis = phiAxis;
	}

	public ContinuouslyScannableViaController getDynamicKappaAxis() {
		return dynamicKappaAxis;
	}

	public void setDynamicKappaAxis(ContinuouslyScannableViaController kappaAxis) {
		this.dynamicKappaAxis = kappaAxis;
	}

	public ContinuouslyScannableViaController getDynamicThetaAxis() {
		return dynamicThetaAxis;
	}

	public void setDynamicThetaAxis(ContinuouslyScannableViaController thetaAxis) {
		this.dynamicThetaAxis = thetaAxis;
	}

	public ContinuouslyScannableViaController getDynamicTwothetaAxis() {
		return dynamicTwothetaAxis;
	}

	public void setDynamicTwothetaAxis(ContinuouslyScannableViaController twothetaAxis) {
		this.dynamicTwothetaAxis = twothetaAxis;
	}

	public ContinuouslyScannableViaController getDynamicGammaAxis() {
		return dynamicGammaAxis;
	}

	public void setDynamicGammaAxis(ContinuouslyScannableViaController gammaAxis) {
		this.dynamicGammaAxis = gammaAxis;
	}

	public PositionStreamIndexerProvider<Double> getI0Monitor() {
		return i0Monitor;
	}

	public void setI0Monitor(PositionStreamIndexerProvider<Double> i0Monitor) {
		this.i0Monitor = i0Monitor;
	}

	public ScannableMotor getStaticPhiAxis() {
		return staticPhiAxis;
	}

	public void setStaticPhiAxis(ScannableMotor staticPhiAxis) {
		this.staticPhiAxis = staticPhiAxis;
	}

	public ScannableMotor getStaticKappaAxis() {
		return staticKappaAxis;
	}

	public void setStaticKappaAxis(ScannableMotor staticKappaAxis) {
		this.staticKappaAxis = staticKappaAxis;
	}

	public ScannableMotor getStaticThetaAxis() {
		return staticThetaAxis;
	}

	public void setStaticThetaAxis(ScannableMotor staticThetaAxis) {
		this.staticThetaAxis = staticThetaAxis;
	}

	public ScannableMotor getStaticTwothetaAxis() {
		return staticTwothetaAxis;
	}

	public void setStaticTwothetaAxis(ScannableMotor staticTwothetaAxis) {
		this.staticTwothetaAxis = staticTwothetaAxis;
	}

	public ScannableMotor getStaticGammaAxis() {
		return staticGammaAxis;
	}

	public void setStaticGammaAxis(ScannableMotor staticGammaAxis) {
		this.staticGammaAxis = staticGammaAxis;
	}

	public ScannableMotor getStaticDdistAxis() {
		return staticDdistAxis;
	}

	public void setStaticDdistAxis(ScannableMotor staticDdistAxis) {
		this.staticDdistAxis = staticDdistAxis;
	}

	public int getBinning() {
		return binning;
	}

	public void setBinning(int binning) {
		if (1 > binning || binning > 3 ){
			throw new IllegalArgumentException("binning ("+binning+") must be between 1 and 3.");
		}
		this.binning = binning;
	}

	public int getI0MonitorPcCapture() {
		return i0MonitorPcCapture;
	}

	public void setI0MonitorPcCapture(int i0MonitorPcCapture) {
		this.i0MonitorPcCapture = i0MonitorPcCapture;
	}

	public boolean isDarkSubtraction() {
		return darkSubtraction;
	}

	public void setDarkSubtraction(boolean darkSubtraction) {
		this.darkSubtraction = darkSubtraction;
	}

	public boolean isApply_repair_correction() {
		return apply_repair_correction;
	}

	public void setApply_repair_correction(boolean apply_repair_correction) {
		this.apply_repair_correction = apply_repair_correction;
	}

	public boolean isPoly_mscalar() {
		return poly_mscalar;
	}

	public void setPoly_mscalar(boolean poly_mscalar) {
		this.poly_mscalar = poly_mscalar;
	}

	public boolean isUnwarp() {
		return unwarp;
	}

	public void setUnwarp(boolean unwarp) {
		this.unwarp = unwarp;
	}

	public boolean isFlood_poly() {
		return flood_poly;
	}

	public void setFlood_poly(boolean flood_poly) {
		this.flood_poly = flood_poly;
	}

	public boolean isExport_all_intermediate_images() {
		return export_all_intermediate_images;
	}

	public void setExport_all_intermediate_images(boolean export_all_intermediate_images) {
		this.export_all_intermediate_images = export_all_intermediate_images;
	}

	public boolean isExport_compressed() {
		return export_compressed;
	}

	public void setExport_compressed(boolean export_compressed) {
		this.export_compressed = export_compressed;
	}

	public double getTimeoutCollectionTimeMultiplier() {
		return timeoutCollectionTimeMultiplier;
	}

	public void setTimeoutCollectionTimeMultiplier(double timeoutCollectionTimeMultiplier) {
		this.timeoutCollectionTimeMultiplier = timeoutCollectionTimeMultiplier;
	}

	public double getTimeoutCollectionTimeOffset() {
		return timeoutCollectionTimeOffset;
	}

	public void setTimeoutCollectionTimeOffset(double timeoutCollectionTimeOffset) {
		this.timeoutCollectionTimeOffset = timeoutCollectionTimeOffset;
	}
}