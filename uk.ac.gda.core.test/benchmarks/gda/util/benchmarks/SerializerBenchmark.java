/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.util.benchmarks;

import java.util.Random;
import java.util.UUID;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import gda.device.scannable.DummyScannable;
import gda.scan.ScanDataPoint;
import gda.util.Serializer;

/**
 * <p>Benchmark to testing the performance of {@link Serializer}</p>
 *
 * <p>To run this benchmark execute the ant target 'run-benchmarks'</p>
 *
 * @author James Mudd
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Throughput e.g. operations per second
@Fork(3) // Do 3 complete runs, new JVMs warmup + measurement
@Warmup(iterations=5) // Do 5 iterations for warmup
@Measurement(iterations=20) // Do 20 repeats after the warmup these are the measured ones
public class SerializerBenchmark {

	ScanDataPoint sdp;
	byte[] serializedSdp;

	@Setup
	public void buildScanDataPoint() {
		sdp = new ScanDataPoint();
		sdp.addScannable(new DummyScannable());
		// Just set something random
		sdp.setCurrentFilename(UUID.randomUUID().toString());
		sdp.setCurrentPointNumber(new Random().nextInt());

		// Get the serialized form for deserialization benchmark
		serializedSdp = Serializer.toByte(sdp);
	}

	@Benchmark
	public byte[] serializeScanDataPoint() {
		// return the result to avoid JIT (doesn't actually seem to happen in this case but its good practice)
		return Serializer.toByte(sdp);
	}

	@Benchmark
	public Object deserializeScanDataPoint() {
		// return the result to avoid JIT (doesn't actually seem to happen in this case but its good practice)
		return Serializer.toObject(serializedSdp);
	}

}
