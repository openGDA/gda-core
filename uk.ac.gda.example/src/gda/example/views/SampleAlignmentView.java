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

package gda.example.views;

import gda.device.DeviceException;
import gda.example.providers.SampleAlignmentViewerContentProvider;
import gda.example.viewer.ISampleAlignmentViewer;
import gda.example.viewer.ISampleAlignmentViewerListener;
import gda.example.viewer.SampleAlignmentViewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.client.device.collection.DeviceCollectionFactory;
import uk.ac.gda.example.device.collection.ISampleAlignmentDeviceCollectionListener;
import uk.ac.gda.example.device.collection.SampleAlignmentDeviceCollection;

public class SampleAlignmentView extends ViewPart {

	private ISampleAlignmentViewer sampleViewer;
	private SampleAlignmentDeviceCollection collection;
	
	private static String ID = "gda.example.views.samplealignment";
	
	@Override
	public void createPartControl(Composite parent) {
		sampleViewer = new SampleAlignmentViewer();
		sampleViewer.addListener(new ViewerListener());
		
		
		collection = (SampleAlignmentDeviceCollection) DeviceCollectionFactory.INSTANCE.getDeviceCollection(ID);
		collection.addListener(new SampleAlignmentDeviceCollectionListener());
		sampleViewer.setContentProvider(new SampleAlignmentViewerContentProvider(collection));
		sampleViewer.createContents(parent);
		sampleViewer.refresh();
	}

	@Override
	public void setFocus() {
		sampleViewer.setFocus();
	}
	
	class ViewerListener implements ISampleAlignmentViewerListener {
		@Override
		public void newXMotorValue(final double val) {
			Job job = new Job("Move X Motor"){

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						collection.getScannable().moveTo(val);
					} catch (DeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return Status.OK_STATUS; 
				}			
			};
			job.setUser(true);
			job.schedule();			
		}

		@Override
		public void newYMotorValue(final double val) {
			Job job = new Job("Move Y Motor"){
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						collection.getScannablemotorunits().moveTo(val);
					} catch (DeviceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return Status.OK_STATUS; 
				}				
			};

			job.setUser(true);
			job.schedule();
		}
	}

	class SampleAlignmentDeviceCollectionListener implements ISampleAlignmentDeviceCollectionListener {
		@Override
		public void motorXHasMovedTo(double val) {
			sampleViewer.refresh();
			
		}

		@Override
		public void motorYHasMovedTo(double val) {
			
			sampleViewer.refresh();
			
		}
	}	

}
