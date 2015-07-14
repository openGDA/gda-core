/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.xspress;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;

public class PositionStreamingXspress extends Xspress2Detector implements PositionCallableProvider<NexusTreeProvider>, PositionInputStream<NexusTreeProvider> {
	private static Logger logger = LoggerFactory.getLogger(PositionStreamingXspress.class);
	private PositionStreamIndexer<NexusTreeProvider> indexer;
	private int nextFrameToRead = 0;

	@Override
	public void atScanLineStart() throws DeviceException {
		indexer = new PositionStreamIndexer<NexusTreeProvider>(this);
		super.atScanLineStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		indexer = null;
		super.atScanLineEnd();
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}

	@Override
	public List<NexusTreeProvider> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		List<NexusTreeProvider> listOfTress = new ArrayList<NexusTreeProvider>();
		if (controller.getTotalFrames() == 0) {
			listOfTress.add(readout());
			return listOfTress;
		}
		int highestFrameNumAvailable = controller.getNumberFrames() - 1;
		if (highestFrameNumAvailable < nextFrameToRead)
			highestFrameNumAvailable = nextFrameToRead;
		logger.info("readout from " + nextFrameToRead + " to " + highestFrameNumAvailable);
		NexusTreeProvider[] trees = readout(nextFrameToRead, highestFrameNumAvailable);
		for (NexusTreeProvider tree : trees)
			listOfTress.add(tree);
		lastFrameCollected = highestFrameNumAvailable;
		nextFrameToRead = highestFrameNumAvailable + 1;
		return listOfTress;
	}

}
