/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.samplerack;

import static java.util.Arrays.stream;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.scan.StaticScan;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;

public class TfgRackScanRunner implements RackScanRunner {
	private static final Logger logger = LoggerFactory.getLogger(TfgRackScanRunner.class);
	private NcdDetectorSystem ncddetectors;
	private List<Scannable> otherScannables = Collections.emptyList();
	private Metadata meta;
	private double deadTime = 10;
	private int deadPort;
	private int livePort;
	private int deadPause;
	private int livePause;

	@Override
	public void runScan(Sample sample, Scannable... other) throws SampleRackException {
		logger.info("Running scan: {}", sample);
		runWithRetry(3, sample);
	}

	public void runWithRetry(int maxAttempts, Sample sample, Scannable... other) throws SampleRackException {

		SampleRackException lastException = null;
		for (int count = 0; count < maxAttempts; count++) {
			meta.setMetadataValue("sample_name", sample.getName());
			meta.setMetadataValue("title", "Sample Rack Scan for: " + sample.getName());
			try {
				var tfg = getNcddetectors().getTimer();
				logger.debug("Setting tfg");
				tfg.clearFrameSets();
				tfg.addFrameSet(sample.getFrames(), getDeadTime(), 1000 * sample.getTpf(), deadPort, livePort, deadPause,
						livePause);
				tfg.loadFrameSets();
				var scannables = Stream.of(Stream.of(getNcddetectors()), getOtherScannables().stream(), stream(other)).flatMap(s -> s)
						.toArray(Scannable[]::new);
				new StaticScan(scannables).runScan();
				break;
			} catch (DeviceException e) {
				logger.error("Error during the scan or setting (clear/add/load) tfg Frame Sets {} for the sample {} ", sample.getFrames(), sample, e);
				lastException = new SampleRackException("Error during the scan or setting (clear/add/load) tfg Frame Sets for sample " + sample.getName(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SampleRackException(sample.getName() + " sample scan runner interrupted ", e);
			} catch (Exception e) {
				logger.error("Error running sample scan {} for {} times ", sample, count+1, e);
				lastException = new SampleRackException("Error running sample scan for " + sample.getName(), e);
			}
		}
		if (lastException != null) {
			throw lastException;
		}
	}

	public NcdDetectorSystem getNcddetectors() {
		return ncddetectors;
	}

	public void setNcddetectors(NcdDetectorSystem ncddetectors) {
		this.ncddetectors = ncddetectors;
	}

	public List<Scannable> getOtherScannables() {
		return otherScannables;
	}

	public void setOtherScannables(List<Scannable> otherScannables) {
		this.otherScannables = otherScannables;
	}

	public double getDeadTime() {
		return deadTime;
	}

	public void setDeadTime(double deadTime) {
		this.deadTime = deadTime;
	}

	public Metadata getMeta() {
		return meta;
	}

	public void setMeta(Metadata meta) {
		this.meta = meta;
	}

	public void setDeadPort(String deadPort) {
		this.deadPort = convertStringToInt(new StringBuilder(deadPort).reverse().toString(), 2);
	}

	public void setLivePort(String livePort) {
		this.livePort = convertStringToInt(new StringBuilder(livePort).reverse().toString(), 2);
	}

	public void setDeadPause(String deadPause) {
		this.deadPause = convertStringToInt(deadPause, 10);
	}

	public void setLivePause(String livePause) {
		this.livePause = convertStringToInt(livePause, 10);
	}

	private Integer convertStringToInt(String string, int radix) {
		return Integer.parseInt(string, radix);
	}
}
