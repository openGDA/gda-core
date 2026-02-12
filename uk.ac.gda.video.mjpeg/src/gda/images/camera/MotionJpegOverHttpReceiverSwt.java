/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.ImageData;

import gda.factory.Findable;
import gda.images.camera.mjpeg.FrameCaptureTask;
import gda.images.camera.mjpeg.SwtFrameCaptureTask;

public class MotionJpegOverHttpReceiverSwt extends MotionJpegOverHttpReceiverBase<ImageData>
	implements Findable {

	public static final ExecutorService latestDecoderServiceFactory(ThreadFactory factory) {
		return new ThreadPoolExecutor(1,
										1,
										1,
										TimeUnit.SECONDS,
										new LatestLinkedBlockingQueue<>(),
										factory);
	}

	private String name;

	public MotionJpegOverHttpReceiverSwt() {
		//we are only interested in decoding and processing the latest image
		var queue = new LatestImageDataBlockingQueue();
		setImageQueue(queue);
		setExecutiveServiceFactory(MotionJpegOverHttpReceiverSwt::latestDecoderServiceFactory);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected FrameCaptureTask<ImageData> createFrameCaptureTask(ExecutorService imageDecodingService,
																	BlockingQueue<ImageData> receivedImages) {
		var urlSpec = this.getUrl();
		return new SwtFrameCaptureTask(urlSpec,
										imageDecodingService,
										receivedImages);
	}

	public static final class LatestLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

		LatestLinkedBlockingQueue() {
			super(1);
		}

		@Override
		public boolean offer(E e) {
			clear();
			return super.offer(e);
		}
	}

	public static final class LatestImageDataBlockingQueue extends LinkedBlockingQueue<ImageData> {

		LatestImageDataBlockingQueue() {
			super(1);
		}

		@Override
		public boolean offer(ImageData e) {
			Collection<ImageData> itemsToCancel = new ArrayList<>();
			drainTo(itemsToCancel);
			return super.offer(e);
		}
	}
}
