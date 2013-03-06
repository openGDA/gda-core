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

import gda.factory.Configurable;
import gda.factory.Findable;
import gda.images.camera.mjpeg.FrameCaptureTask;
import gda.images.camera.mjpeg.SwtFrameCaptureTask;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.ImageData;

public class MotionJpegOverHttpReceiverSwt extends MotionJpegOverHttpReceiverBase<ImageData> implements Findable, Configurable {

	public class LatestDecoderserviceFactory implements ExecutorServiceFactory {

		@Override
		public ExecutorService create(ThreadFactory decoderThreadFactory) {
			return new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,	new LatestLinkedBlockingQueue<Runnable>(), decoderThreadFactory);			
		}
	}

	private String name;


	public MotionJpegOverHttpReceiverSwt() {
		super();
		//we are ony interested in decoding and processing the lastest image 
		setImageQueue(new LatestImageDataBlockingQueue());
		setExecutiveServiceFactory( new LatestDecoderserviceFactory());
	}
	

	@Override
	protected FrameCaptureTask<ImageData> createFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService, BlockingQueue<Future<ImageData>> receivedImages) {
		return new SwtFrameCaptureTask(urlSpec, imageDecodingService, receivedImages);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
class LatestLinkedBlockingQueue<E> extends LinkedBlockingQueue<E>{

	LatestLinkedBlockingQueue(){
		super(1);
	}
	
	@Override
	public boolean offer(E e) {
		super.clear();
		return super.offer(e);
	}
	
}
class LatestImageDataBlockingQueue extends LinkedBlockingQueue<Future<ImageData>>{

	LatestImageDataBlockingQueue(){
		super(1);
	}
	
	@Override
	public boolean offer(Future<ImageData> e) {
		Vector<Future<ImageData>> c = new Vector<Future<ImageData>>();
		super.drainTo(c);
		for( Future<ImageData> f : c){
			//we need to cancel all futures so that threads 
			//accessing the result will not wait forever now that they
			//will no longer be processed.
			f.cancel(true);
		}
		return super.offer(e);
	}
	
}