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

package gda.events.benchmark;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

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
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.NameFilter;
import gda.scan.ScanDataPoint;

/**
 * A benchmark for the EventService when using JMS + ActiveMQ to provide the transport.
 * <br>
 * To run this benchmark you will need a ChannelServer running on localhost
 *
 * @author James Mudd
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Throughput e.g. operations per second
@Warmup(iterations=8) // Do 8 iterations for warmup
@Measurement(iterations=10) // Do 10 repeats after the warmup these are the measured ones
@Fork(2) // Do 2 complete runs new JVMs warmup + measurement
public class EventServiceJmsBenchmark {
	private ScanDataPoint sdp;
	private CountDownLatch latch = new CountDownLatch(0); // Assign here to prevent NPE from lambda in the sendEvents benchmark

	@Setup
	public void buildScanDataPoint() {
		// Switch to JMS
		System.setProperty("gda.events.useJMS", "true");

		sdp = new ScanDataPoint();
		sdp.addScannable(new DummyScannable());
		// Just set something random
		sdp.setCurrentFilename(UUID.randomUUID().toString());
		sdp.setCurrentPointNumber(new Random().nextInt());

		// When a message is received decrement the latch
		EventService.getInstance().subscribe(msg -> latch.countDown(), new NameFilter("test", null));
	}

	@Benchmark
	public void sendEvents() {
		EventService.getInstance().getEventDispatcher().publish("test", sdp);
	}

	@Benchmark
	public void endToEnd() throws InterruptedException {
		// Use a latch to wait until the message is received
		latch = new CountDownLatch(1);
		EventService.getInstance().getEventDispatcher().publish("test", sdp);
		latch.await();
	}
}
