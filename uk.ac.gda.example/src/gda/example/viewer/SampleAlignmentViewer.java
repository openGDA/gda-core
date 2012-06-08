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

package gda.example.viewer;

import gda.example.providers.ISampleAlignmentViewerProvider;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.richbeans.components.scalebox.StandardBox;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class SampleAlignmentViewer implements ISampleAlignmentViewer {

	private final class ValueListenerClass extends ValueAdapter {
		@Override
		public void valueChangePerformed(ValueEvent e) {
			for (ISampleAlignmentViewerListener listener : listeners) {
				listener.newXMotorValue(setXBox.getNumericValue());
			}					
		}
	}

	private List<ISampleAlignmentViewerListener> listeners = new LinkedList<ISampleAlignmentViewerListener>();
	ISampleAlignmentViewerProvider deviceModelProvider;
	
	private Text currentXPosition;
	private Composite parent;
	
	private StandardBox setXBox;
	private StandardBox setYBox;
	
	@Override
	public void createContents(Composite parent){
		this.parent = parent;
		Group mainGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainGroup.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		mainGroup.setLayoutData(gridData);	
		mainGroup.setText("Sample Alignment");
	
		Label curPosLabel = new Label(mainGroup, SWT.NONE);
		curPosLabel.setText("Sample Position: ");
		currentXPosition = new Text(mainGroup, SWT.BORDER | SWT.SINGLE);
		GridData gridData2 = new GridData();
		gridData2.widthHint = 65;
		currentXPosition.setLayoutData(gridData2);	
		
		Label setXPos = new Label(mainGroup, SWT.SINGLE);
		setXPos.setText("Set X Position:");
		setXBox = new StandardBox(mainGroup, SWT.NONE);
		setXBox.setMaximum(360);
		setXBox.setUnit("°");
	
		Label setYPos = new Label(mainGroup, SWT.SINGLE);
		setYPos.setText("Set Y Position:");
		setYBox = new StandardBox(mainGroup, SWT.NONE);
		setYBox.setMaximum(360);
		setYBox.setUnit("°");

		setXBox.addValueListener(new ValueListenerClass());
		
		setYBox.addValueListener(new ValueAdapter() {			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				for (ISampleAlignmentViewerListener listener : listeners) {
					listener.newYMotorValue(setYBox.getNumericValue());
				}					
			}
		});
		
		setYBox.on();
		setXBox.on();	
	}

	@Override
	public void addListener(ISampleAlignmentViewerListener viewerListener) {
		listeners.add(viewerListener);
		
	}

	@Override
	public void refresh() {
		final Object xmotorValue = deviceModelProvider.getXMotorValue();
		final Object ymotorValue = deviceModelProvider.getYMotorValue();
		parent.getDisplay().asyncExec(new Runnable() {	
			@Override
			public void run() {
				currentXPosition.setText(xmotorValue.toString() + "," + ymotorValue.toString());	
			}
		});
		

		
	}

	@Override
	public void removeListener(ISampleAlignmentViewerListener viewerListener) {
		listeners.remove(viewerListener);
	}

	@Override
	public void setContentProvider(ISampleAlignmentViewerProvider viewerContentProvider) {
		this.deviceModelProvider = viewerContentProvider;
		
	}

	@Override
	public void setFocus() {
		if (currentXPosition != null) currentXPosition.setFocus();
	}
}
