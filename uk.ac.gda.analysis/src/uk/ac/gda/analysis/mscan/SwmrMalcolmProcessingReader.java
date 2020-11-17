/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.mscan;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.dataset.slicer.SimpleDynamicSliceViewIterator;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.core.GDACoreActivator;

/**
 * Reads a datafile using a Swmr iterator extracting each frame and passing to
 * a list of {@link MalcolmSwmrProcessor}s.
 */
public class SwmrMalcolmProcessingReader {

	private static final Logger logger = LoggerFactory.getLogger(SwmrMalcolmProcessingReader.class);

	private final Path filepath;
	private final int count;
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final Collection<MalcolmSwmrProcessor> procs;
	private final Optional<MessagingService> messageService;
	private final String detFrameEntry;
	private final String detUidEntry;
	private Future<?> future;

	/**
	 * @param filepath path for source datafile
	 * @param count total number of frames to read
	 * @param procs processes to receive frames
	 * @param detFrameEntry name of detector frame dataset
	 * @param detUidEntry name of detector uid dataset
	 */
	public SwmrMalcolmProcessingReader(Path filepath, int count, Collection<MalcolmSwmrProcessor> procs, String detFrameEntry, String detUidEntry) {
		this.filepath = filepath;
		this.count = count;
		this.procs = procs;
		this.detFrameEntry = detFrameEntry;
		this.detUidEntry = detUidEntry;
		this.messageService = GDACoreActivator.getService(MessagingService.class);
	}

	/**
	 * Start a task asynchronously to iterate over the detector dataset as it is written
	 * and pass data to each of the configured processors.
	 *
	 * This method may be called multiple times, the task will only be started/submitted once
	 * per lifetime of this object.
	 */
	public void startAsyncReading() {
		if (started.compareAndSet(false, true)) {
			this.future = Async.submit(this::readFramesAndDispatchToProcessing, "%s-task", this.getClass().getSimpleName());
		}
	}

	/**
	 * Wait until the reading/processing task is completed
	 */
	public void waitUntilComplete() {
		if (future != null) {
			try {
				future.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Swmr reading interrupted", e);
			} catch (ExecutionException e) {
				logger.error("Error running Swmr processing", e);
			}
		}
	}

	private void readFramesAndDispatchToProcessing() {
		IDataHolder data = null;
		while (data == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
				logger.error("Swmr reading interrupted whilst waiting to open file", e1);
				return;
			}
			try {
				data = LoaderFactory.getData(filepath.toString());
			} catch (Exception e) {
				// File not ready to be loaded yet or non existent
			}
		}

		IDynamicDataset image = (IDynamicDataset) data.getLazyDataset(detFrameEntry);
		IDynamicDataset uuid = (IDynamicDataset) data.getLazyDataset(detUidEntry);

		SimpleDynamicSliceViewIterator it = new SimpleDynamicSliceViewIterator(image, uuid, 2, count);

		while (it.hasNext()) {

			ILazyDataset next = it.next();
			SliceFromSeriesMetadata md = next.getFirstMetadata(SliceFromSeriesMetadata.class);
			logger.debug("Ready for slice {}", Slice.createString(md.getSliceFromInput()));

			Dataset slice;
			try {
				slice = DatasetUtils.convertToDataset(next.getSlice());
			} catch (DatasetException e) {
				logger.error("Error obtaining slice, meta: {}", md, e);
				return;
			}
			slice.clearMetadata(null);

			procs.forEach(proc -> proc.processFrame(slice, md));

			// Send an artificial update event to cause the plotting to update
			// The actual arguments are not relevant here apart from ScanStatus.UPDATED and SwmrStatus.ACTIVE
			ScanMessage message = new ScanMessage(ScanMessage.ScanStatus.UPDATED, filepath.toString(), filepath.toString(),
					SwmrStatus.ACTIVE, 1, null, null, null, 1, null);

			messageService.ifPresent(jms -> jms.sendMessage(message));

			logger.debug("Complete for slice {}", Slice.createString(md.getSliceFromInput()));
		}
	}

}
