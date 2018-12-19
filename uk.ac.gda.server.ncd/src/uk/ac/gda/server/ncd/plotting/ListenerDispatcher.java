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

package uk.ac.gda.server.ncd.plotting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.TimerStatus;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;
import uk.ac.gda.server.ncd.scannable.EnergyScannable;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;
import uk.ac.gda.server.ncd.subdetector.LastImageProvider;
import uk.ac.gda.server.ncd.subdetector.NcdScalerDetector;
import uk.ac.gda.server.ncd.subdetector.NcdWireDetector;

/**
 * This listens to SDPs and the NcdDetectorSystem Timer to send updates to the client with either live raw data, stored
 * raw data or processed (reduced) data from the SDP.
 */
public class ListenerDispatcher extends ConfigurableBase implements Findable, IScanDataPointObserver {

	private class RequestObject {
		public RequestObject(String type, String entity) {
			this.type = type;
			this.entity = entity;
		}

		public String type;
		public String entity;
	}

	private static final Logger logger = LoggerFactory.getLogger(ListenerDispatcher.class);
	private static final float MINIMUM_COLLECTION_TIME_FOR_RATE = 0.3f;
	private NcdDetectorSystem det;
	private String name;
	private INexusTree savedTree;
	private int lastFrame = 0;
	private int currentFrame = 0;
	private boolean detectorLive = false;
	private Map<String, RequestObject> wishList = new HashMap<String, RequestObject>();

	private EnergyScannable energyScannable = null;

	public ListenerDispatcher() {
	}

	@Override
	public void configure() throws FactoryException {
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		setConfigured(true);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScanDataPoint) {
			ScanDataPoint sdp = (ScanDataPoint) arg;
			NexusTreeProvider pro = (NexusTreeProvider) extractDetectorObject(sdp, det.getName());
			if (pro != null) {
				INexusTree nexusTree = pro.getNexusTree();
				if (treeHasNcdDetectors(nexusTree)) {
					savedTree = nexusTree;
					updateTreeClients();
				}
			}
		} else if (arg instanceof TimerStatus) {
			TimerStatus status = (TimerStatus) arg;
			currentFrame = status.getCurrentFrame();
			if (!detectorLive && !"IDLE".equals(status.getCurrentStatus())) {
				detectorLive = true;
				lastFrame = 0;
			}
			if (detectorLive) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							// ignore
						}
						updateFrameClients(lastFrame, currentFrame);
					}
				}).start();
			}
			if ("IDLE".equals(status.getCurrentStatus())) {
				detectorLive = false;
			}
		} else {
			// ignore
		}
	}

	private void updateFrameClients(int lastFrameUFC, int currentFrameUFC) {
		logger.trace("updateFrameClients: last: {}, current: {}", lastFrameUFC, currentFrameUFC);
		int frame = lastFrameUFC;
		if (frame > 0) {
			frame--;
		}

		float countingTime = 1;
		final Collection<Object> rateCollection = new ArrayList<Object>(4);

		try {
			for (INcdSubDetector sub : det.getDetectors()) {
				if (sub.getDetectorType().equalsIgnoreCase(NcdDetectorSystem.CALIBRATION_DETECTOR)
						&& (sub instanceof NcdWireDetector)) {
					float[] floats = ((NcdWireDetector) sub).readFloat(0, 0, frame, 1, 1, frame + 1);
					countingTime = floats[0] / 1e8f; // Tfg operates with 10ns ticks
					break;
				}
			}

			for (INcdSubDetector sub : det.getDetectors()) {
				for (String type : new String[] { NcdDetectorSystem.SAXS_DETECTOR, NcdDetectorSystem.WAXS_DETECTOR,
						NcdDetectorSystem.FLUORESCENCE_DETECTOR, NcdDetectorSystem.OTHER_DETECTOR }) {
					if (sub.getDetectorType().equalsIgnoreCase(type)) {
						Dataset ds = null;
						if (sub instanceof NcdWireDetector) {
							double[] data = ((NcdWireDetector) sub).read(frame);
							int[] dims = sub.getDataDimensions();
							ds = DatasetFactory.createFromObject(data, dims);
							ds.setName(String.format("%s frame %d of %d", type, frame + 1, det.getNumberOfFrames()));

							/*
							 * cps is only reliable if readout of tfg (collection time) and counts coincide relatively
							 * well within the time collected in that frame. Either we have collected for long enough
							 * that the delay between the readouts does not matter, or the frame is complete anyway.
							 * Otherwise we do not hazard a guess.
							 */
							if (countingTime > MINIMUM_COLLECTION_TIME_FOR_RATE || frame != (currentFrameUFC - 1)) {
								DetectorRates dr = createDetectorRate(sub, countingTime, ds);
								rateCollection.add(dr);
							}
						}
						if (sub instanceof LastImageProvider) {
							ds = ((LastImageProvider) sub).readLastImage();
							ds.setName(String.format("%s ", type));

							// see above
							if (frame != (currentFrameUFC - 1)) {
								DetectorRates dr = createDetectorRate(sub, countingTime, ds);
								rateCollection.add(dr);
							}
						}
						if (ds != null) {
							try {
								DiffractionMetadata dm = new DiffractionMetadata(null, sub.getDetectorProperties(), energyScannable == null ? null : energyScannable.getDiffractionCrystalEnvironment());
								ds.setMetadata(dm);
							} catch (Throwable t) {
								logger.warn("had trouble getting diffraction metadata for "+type,t);
							}
							for (String panel : wishList.keySet()) {
								RequestObject request = wishList.get(panel);
								if (request.type.equalsIgnoreCase("LIVE") && request.entity.equalsIgnoreCase(type)) {
									logger.debug("Plotting frame {}", frame);
									logger.debug("Dataset name {}", ds.getName());
									plotData(panel, ds);
								}
							}
							break;
						}
					}
				}
				if (sub instanceof NcdScalerDetector) {
					NcdScalerDetector nsd = ((NcdScalerDetector)sub);
					float[] data = nsd.readFloat(frame+1);
					logger.debug("data: {}, length: {}", data, data.length);
					if (data.length > 0) {
						rateCollection.add(new NormalisationUpdate(nsd.getName(), data[frame]/countingTime));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception preparing or sending live data: ", e);
		}

		lastFrame = currentFrameUFC;

		if (!rateCollection.isEmpty()) {
			logger.debug("dispatching a detector rate");
			Runnable anotherUpdate = new Runnable() {
				@Override
				public void run() {
					det.notifyRateCollection(rateCollection);
				}
			};
			new Thread(anotherUpdate).start();
		}
	}

	private Map<String, Float> detspec2maskedval = new HashMap<String, Float>(2);

	private DetectorRates createDetectorRate(INcdSubDetector sub, float countingTime, Dataset ds) {
		DetectorRates dr = new DetectorRates();
		dr.detName = sub.getName();
		dr.countingTime = countingTime;
		//TODO check performance with PC Chang (iterates over array twice)
		dr.maxCounts = ds.max().floatValue();
		dr.integratedCounts = ((Number) ds.sum()).floatValue();
		if (dr.maxCounts > DetectorRates.getThreshold(dr.detName)) {
			dr.setHighCounts(ds);
		}

		// correct for masked (negative) numbers
		String detspec = dr.detName + Arrays.toString(ds.getShape());
		if (!detspec2maskedval.containsKey(detspec)) {
			// issue bogus values in new threads that might come in here later until we fix it at the end of this if
			detspec2maskedval.put(detspec, 0f);

			Double maskedval = new Double(0);
			double val = 0;

			IndexIterator iter = ds.getIterator();
			while (iter.hasNext()) {
				val = ds.getElementDoubleAbs(iter.index);
				if (val < 0.0) {
					maskedval += val;
				}
			}

			detspec2maskedval.put(detspec, maskedval.floatValue());
		}
		dr.integratedCounts -= detspec2maskedval.get(detspec);
		try {
			dr.detType = sub.getDetectorType();
		} catch (DeviceException e) {
			logger.error("error getting detector type from " + sub.getName(), e);
		}
		return dr;
	}

	private void updateTreeClients() {
		for (String panel : wishList.keySet()) {
			RequestObject request = wishList.get(panel);
			if ("SDP".equals(request.type)) {
				INexusTree detNode = savedTree.getChildNode(request.entity, NexusExtractor.NXDetectorClassName);

				if (detNode != null) {
					INexusTree dataNode = detNode.getChildNode("data", NexusExtractor.SDSClassName);
					if (dataNode != null) {
						plotData(panel, getDSfromNGD(dataNode.getData(), request.entity));
					}
				}
			}
		}
	}

	private void plotData(String panel, Dataset ds) {
		ds.squeeze();
		try {
			switch (ds.getShape().length) {
			case 1:
				SDAPlotter.plot(panel, ds);
				break;
			case 2:
				SDAPlotter.imagePlot(panel, ds);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("could not plot", e);
		}
	}

	private static DoubleDataset getDSfromNGD(NexusGroupData ngd, String name) {
		DoubleDataset ds = (DoubleDataset) ngd.toDataset().cast(Dataset.FLOAT64);
		ds.setName(name);
		return ds;
	}

	public void setNcdDetector(NcdDetectorSystem det) {
		if (this.det != null) {
			this.det.deleteIObserver(this);
		}
		this.det = det;
		if (this.det != null) {
			this.det.addIObserver(this);
		}
	}

	public NcdDetectorSystem getNcdDetector() {
		return det;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void monitorSDP(String panelName, String detectorName) {
		wishList.put(panelName, new RequestObject("SDP", detectorName));
		updateTreeClients();
	}

	public void monitorLive(String panelName, String detectorName) {
		wishList.put(panelName, new RequestObject("Live", detectorName));
		updateFrameClients(lastFrame, currentFrame);
	}

	public void monitorStop(String panelName) {
		wishList.remove(panelName);
	}

	public List<String> getSDPDetectorNames() {
		List<String> detNames = new ArrayList<String>();
		if (savedTree != null) {
			for (INexusTree branch : savedTree) {
				if (branch.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
					INexusTree dataNode = branch.getChildNode("data", NexusExtractor.SDSClassName);
					if (dataNode == null)
						continue;
					INexusTree type = branch.getChildNode("sas_type", NexusExtractor.SDSClassName);
					if (type != null) {
						String typeStr = null;
						typeStr = ((String[]) type.getData().getBuffer())[0];
						for (String allowed : NcdDetectorSystem.detectorTypes) {
							if (allowed.equals(typeStr)) {
								detNames.add(branch.getName());
								break;
							}
						}
					}
				}
			}
		}
		return detNames;
	}

	public String getSDPDetectorNamesAsString() {
		StringBuilder sb = new StringBuilder("");
		List<String> names = getSDPDetectorNames();
		boolean first = true;
		for (String string : names) {
			if (!first) {
				sb.append(",");
			}
			sb.append(string);
			first = false;
		}
		return sb.toString();
	}

	private static boolean treeHasNcdDetectors(INexusTree nexusTree) {
		for (INexusTree branch : nexusTree) {
			if (branch.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
				INexusTree type = branch.getChildNode("sas_type", NexusExtractor.SDSClassName);
				if (type != null && type.getData().isChar()) {
					String typeStr = ((String[]) type.getData().getBuffer())[0];

					for (String allowed : NcdDetectorSystem.detectorTypes) {
						if (allowed.equals(typeStr)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private Object extractDetectorObject(ScanDataPoint thisPoint, String detectorName) {
		Object object = null;
		try {
			int index = thisPoint.getDetectorNames().indexOf(detectorName);
			object = thisPoint.getDetectorData().get(index);
		} catch (Exception e) {
			// we return null in this case
		}
		return object;
	}

	public EnergyScannable getEnergyScannable() {
		return energyScannable;
	}

	public void setEnergyScannable(EnergyScannable energyScannable) {
		this.energyScannable = energyScannable;
	}
}
