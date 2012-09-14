/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GdaMetadata;
import gda.device.DeviceException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.IScanDataPointObserver;
import gda.jython.JythonServerFacade;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.ExternalFunction;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleStageMotorHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltBallLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.view.handlers.exceptions.ExternalProcessingFailedException;

/**
 *
 */
public class TiltController implements ITiltController {

	private ExternalFunction externalProgram1;
	private ExternalFunction externalProgram2;
	private ITiltBallLookupTableHandler lookupTableHandler;

	private ISampleStageMotorHandler sampleStageMotorHandler;

	private ICameraHandler cameraHandler;

	private String result;

	private boolean test = false;

	public String getResult() {
		return result;
	}

	private static final Logger logger = LoggerFactory.getLogger(TiltController.class);

	public void setLookupTableHandler(ITiltBallLookupTableHandler lookupTableHandler) {
		this.lookupTableHandler = lookupTableHandler;
	}

	public int getMinY(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return Integer.MIN_VALUE;
		}
		return lookupTableHandler.getMinY(selectedCameraModule.getValue());
	}

	public int getMaxY(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return Integer.MIN_VALUE;
		}
		return lookupTableHandler.getMaxY(selectedCameraModule.getValue());
	}

	@Override
	public TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE module, double exposureTime)
			throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Preparing for Tilt alignment", 100);

		// 1. set roi minY and maxY to values in the lookuptable so that the scanned images are cropped.
		int minY = getMinY(module);
		int maxY = getMaxY(module);

		int minX = getMinX(module);
		int maxX = getMaxX(module);

		String firstScanFolder = null;
		String secondScanFolder = null;
		try {
			cameraHandler.setUpForTilt(minY, maxY, minX, maxX);
			logger.debug("Set the camera minY at:" + minY + " and maxY at:" + maxY);
			double txOffset = getTxOffset(module);
			GdaMetadata gdaMetadata = (GdaMetadata) Finder.getInstance().find("GDAMetadata");
			String subdir = gdaMetadata.getMetadataValue("subdirectory");
			try {
				gdaMetadata.setMetadataValue("subdirectory", "tmp");
				// Move tx by offset
				logger.debug("the tx offset is:{}", txOffset);
				if (!monitor.isCanceled()) {
					// Relative to where it was - conclusion from testing with Mike
					sampleStageMotorHandler.moveSs1TxBy(progress, txOffset);
					// 2. scan 0 to 340 deg in steps of 20
					logger.debug("will run scan command next");
					if (!monitor.isCanceled()) {
						scanThetha(progress, exposureTime);
						if (!monitor.isCanceled()) {

							// 3. call matlab - first time
							firstScanFolder = runExternalProcess(progress, 1);
							String result = getResult();
							// 4. read output from matlab and move motors
							// output= x,yz,z
							Double[] motorsToMove = getTiltMotorPositions(result);
							logger.debug("motorsto move:{}", motorsToMove);
							if (!progress.isCanceled()) {
								if (motorsToMove != null) {
									logger.debug("Current rz is :{}", sampleStageMotorHandler.getSs1RzPosition());
									logger.debug("Current rx is :{}", sampleStageMotorHandler.getSs1RxPosition());
									double rz = motorsToMove[0];// roll
									double rx = motorsToMove[1];// pitch

									sampleStageMotorHandler.moveSs1RzBy(progress, -rz);
									sampleStageMotorHandler.moveSs1RxBy(progress, -rx);

									logger.debug("After move ss1_rz is :{}", sampleStageMotorHandler.getSs1RzPosition());
									logger.debug("After move ss1_rx is :{}", sampleStageMotorHandler.getSs1RxPosition());
								}
							}

							if (!monitor.isCanceled()) {
								// 5. scan 0 to 340 deg in steps of 20
								scanThetha(progress, exposureTime);
								if (!monitor.isCanceled()) {
									// 6. call matlab
									secondScanFolder = runExternalProcess(progress, 2);
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				throw ex;
			} finally {
				sampleStageMotorHandler.moveSs1TxBy(progress, -txOffset);
				gdaMetadata.setMetadataValue("subdirectory", subdir);
			}
		} finally {
			// - move the motor back
			// motorHandler.moveSs1TxBy(progress, -txOffset);
			cameraHandler.resetAfterTilt();
			sampleStageMotorHandler.moveRotationMotorTo(progress, 0);
			// Return the plottable points
			progress.done();
		}

		return getPlottablePoint(firstScanFolder, secondScanFolder);
	}

	private void scanThetha(final IProgressMonitor progress, double exposureTime) {
		SubMonitor subMonitor = SubMonitor.convert(progress);
		subMonitor.beginTask("Scan", 10);

		String scanCmd = String.format("scan %1$s 0 340 20 %2$s %3$f", sampleStageMotorHandler.getThethaMotorName(),
				cameraHandler.getCameraName(), exposureTime);
		logger.debug("Scan command being executed:{}", scanCmd);
		PrepareTiltSubProgressMonitor prepareTiltSubProgressMonitor = new PrepareTiltSubProgressMonitor(subMonitor, 80);

		prepareTiltSubProgressMonitor.subTask(String.format("Command:%1$s", scanCmd));
		JythonServerFacade.getInstance().addIObserver(prepareTiltSubProgressMonitor);

		JythonServerFacade.getInstance().evaluateCommand(scanCmd);

		JythonServerFacade.getInstance().deleteIObserver(prepareTiltSubProgressMonitor);

		prepareTiltSubProgressMonitor.done();
		subMonitor.done();
	}

	private static class PrepareTiltSubProgressMonitor extends SubProgressMonitor implements IScanDataPointObserver,
			Findable {

		private String name;

		public PrepareTiltSubProgressMonitor(IProgressMonitor monitor, int ticks) {
			super(monitor, ticks);
		}

		@Override
		public void setName(String name) {
			this.name = name;

		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void update(Object source, Object arg) {
			logger.debug("PrepareTiltSubProgressMonitor#update#{}", source);
			String msg = arg.toString();
			if (msg.startsWith("point")) {

				subTask(String.format("Scan data information: %s", arg));
				worked(1);
			}
		}

	}

	protected String runExternalProcess(IProgressMonitor monitor, int count) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		result = null;
		progress.beginTask("Matlab Processing", 10);
		ArrayList<String> cmdAndArgs = new ArrayList<String>();
		cmdAndArgs.add(externalProgram1.getCommand());
		String path = PathConstructor.createFromDefaultProperty();
		long filenumber = new NumTracker("i12").getCurrentFileNumber();
		//
		String imagesPath = path + File.separator + filenumber + File.separator + "projections" + File.separator;

		String lastImageFilename = "p_00017.tif";
		if (!externalProgram1.getArgs().isEmpty()) {
			cmdAndArgs.add(externalProgram1.getArgs().get(0));
		}
		if (test) {
			cmdAndArgs.add("'/dls_sw/i12/software/tomoTilt/images/projections/p_00017.tif'" + ",1,true");
		} else {
			String lastPartOfCmd = "'" + imagesPath + lastImageFilename + "',1,true";
			cmdAndArgs.add(lastPartOfCmd);
			logger.info("imageLastFileName:{}", lastPartOfCmd);
		}
		logger.info("CommandAndArgs1:{}", cmdAndArgs);
		runExtProcess(progress, cmdAndArgs);

		cmdAndArgs.clear();
		cmdAndArgs.add(externalProgram2.getCommand());
		if (!externalProgram2.getArgs().isEmpty()) {
			cmdAndArgs.add(externalProgram2.getArgs().get(0));
			if (test) {
				String cmdArgs = externalProgram2.getArgs().get(1)
						+ "'/dls_sw/i12/software/tomoTilt/images/projections/p_00017.tif','/dls_sw/i12/software/tomoTilt/images/projections/calculated_flatfield.tif'"
						+ "," + count;
				logger.info("Test cmd:{}", cmdArgs);
				cmdAndArgs.add(cmdArgs);
			} else {
				String cmdArgs = externalProgram2.getArgs().get(1) + "'" + imagesPath + lastImageFilename + "','"
						+ imagesPath + "calculated_flatfield.tif'," + count;
				logger.info("External program being run:{}", cmdArgs);
				cmdAndArgs.add(cmdArgs);
			}
		}
		logger.info("CommandAndArgs2:{}", cmdAndArgs);
		runExtProcess(progress, cmdAndArgs);
		progress.done();
		return imagesPath;

	}

	protected void runExtProcess(IProgressMonitor monitor, List<String> cmdAndArgs) throws Exception {
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		pb.command(cmdAndArgs);
		final Process p = pb.start();
		try {
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = output.readLine()) != null) {
				logger.info(line);
				if (!line.equals("")) {
					monitor.subTask(line);
					if (line.startsWith("output =")) {
						result = line;
					}
				}
			}
			int exitValue = p.waitFor();

			closeStream(p.getInputStream(), "output");
			if (exitValue != 0) {
				throw new ExternalProcessingFailedException("External Processing Failed" + cmdAndArgs);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage());
			throw ex;
		} finally {
			p.destroy();
		}
	}

	private static void closeStream(Closeable stream, String name) {
		try {
			stream.close();
		} catch (IOException ioe) {
			logger.warn(String.format("Unable to close process %s stream", name), ioe);
		}
	}

	public void setExternalProgram1(ExternalFunction externalProgram1) {
		this.externalProgram1 = externalProgram1;
	}

	public void setExternalProgram2(ExternalFunction externalProgram2) {
		this.externalProgram2 = externalProgram2;
	}

	public double getTxOffset(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return Double.NaN;
		}
		return lookupTableHandler.getTxOffset(selectedCameraModule.getValue());
	}

	protected TiltPlotPointsHolder getPlottablePoint(String firstScanFolder, String secondScanFolder)
			throws IOException {
		TiltPlotPointsHolder tiltPlotPointsHolder = new TiltPlotPointsHolder();

		tiltPlotPointsHolder.setCenters1(getDoublePointList(firstScanFolder + File.separator + "centers_1.csv"));
		tiltPlotPointsHolder.setCenters2(getDoublePointList(secondScanFolder + File.separator + "centers_2.csv"));
		tiltPlotPointsHolder.setLine2(getDoublePointList(secondScanFolder + File.separator + "line_2.csv"));
		return tiltPlotPointsHolder;
	}

	private DoublePointList getDoublePointList(String fileName) throws IOException {
		File firstScanCentersFile = new File(fileName);
		FileInputStream fis = new FileInputStream(firstScanCentersFile);
		InputStreamReader inpStreamReader = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(inpStreamReader);
		String rl = null;
		DoublePointList pointList = new DoublePointList();
		rl = br.readLine();
		while (rl != null) {
			StringTokenizer strTokenizer = new StringTokenizer(rl, ",");
			if (strTokenizer.countTokens() != 2) {
				throw new IllegalArgumentException("Invalid row in the table");
			}
			double x = Double.parseDouble(strTokenizer.nextToken());
			double y = Double.parseDouble(strTokenizer.nextToken());
			pointList.addPoint(x, y);
			rl = br.readLine();
		}
		fis.close();
		br.close();
		inpStreamReader.close();
		return pointList;
	}

	public int getMinX(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return Integer.MIN_VALUE;
		}
		return lookupTableHandler.getMinX(selectedCameraModule.getValue());
	}

	public int getMaxX(CAMERA_MODULE selectedCameraModule) throws DeviceException {
		if (CAMERA_MODULE.NO_MODULE.equals(selectedCameraModule)) {
			return Integer.MIN_VALUE;
		}
		return lookupTableHandler.getMaxX(selectedCameraModule.getValue());
	}

	private Double[] getTiltMotorPositions(String result) {
		if (result != null) {
			String values = result.substring("output =".length());
			StringTokenizer tokenizer = new StringTokenizer(values, ",");
			int count = 0;
			Double[] motorsToMove = new Double[tokenizer.countTokens()];
			while (tokenizer.hasMoreElements()) {
				String token = tokenizer.nextElement().toString();
				motorsToMove[count++] = Double.parseDouble(token);
			}
			return motorsToMove;
		}
		return null;
	}

	public void setCameraHandler(ICameraHandler cameraHandler) {
		this.cameraHandler = cameraHandler;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void setSampleStageMotorHandler(ISampleStageMotorHandler sampleStageMotorHandler) {
		this.sampleStageMotorHandler = sampleStageMotorHandler;
	}
}
