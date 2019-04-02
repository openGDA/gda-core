/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui.histogram;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.client.live.stream.view.customui.AbstractLiveStreamViewCustomUi;

public class LiveStreamWithHistogramTopUI extends AbstractLiveStreamViewCustomUi {

	private Text imageHigh;
	private Text imageLow;

	private DoubleToStringConverter doubleToStringConverter = new DoubleToStringConverter();

	public void setImageLow(double imageLowValue) {
		imageLow.setText(doubleToStringConverter.convert(imageLowValue));
	}

	public void setImageHigh(double imageHighValue) {
		imageHigh.setText(doubleToStringConverter.convert(imageHighValue));
	}

	@Override
	public void createUi(Composite composite) {
		createImageRangeGroup(composite);
	}

	private void createImageRangeGroup(Composite parent) {
		Group imageRangeGroup = new Group(parent, SWT.BORDER);
		imageRangeGroup.setLayout(new GridLayout(5, false));
		imageRangeGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		imageRangeGroup.setText("Image Range");

		int boxWidthHint = new BoxWidthHintCalculator().calculate(parent);
		addImageLowText(imageRangeGroup, boxWidthHint);
		addImageRangeLabel(imageRangeGroup);
		addImageHighText(imageRangeGroup, boxWidthHint);
		addAlwaysAutoscaleCheckbox(imageRangeGroup);
		addAutoScaleOnceButton(imageRangeGroup);
	}

	private void addImageLowText(Group group, int boxWidthHint) {
		imageLow = new Text(group, SWT.BORDER | SWT.RIGHT);
		imageLow.setLayoutData(GridDataFactory.swtDefaults().hint(boxWidthHint, SWT.DEFAULT).create());
		imageLow.setToolTipText("Set image colour low value.");
		imageLow.addVerifyListener(new DoubleVerifyListener());
		imageLow.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
				final IImageTrace iTrace = getImageTrace();

				if (plottingSystem != null && iTrace != null) {
					String text = imageLow.getText();
					try {
						final double min = Double.parseDouble(text);
						iTrace.getImageServiceBean().setMin(min);
						iTrace.setMin(min);
						iTrace.setPaletteData(iTrace.getPaletteData());
						plottingSystem.repaint();
					} catch (Exception ex) {
						setImageLow(iTrace.getImageServiceBean().getMin().doubleValue());
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Do nothing.
			}
		});

		imageLow.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				imageHigh.setFocus();
			}
		});
	}

	private void addImageRangeLabel(Group group) {
		Label imageRange = new Label(group, SWT.None);
		imageRange.setText("-");
	}

	private void addImageHighText(Group group, int boxWidthHint) {
		imageHigh = new Text(group, SWT.BORDER | SWT.RIGHT);
		imageHigh.setLayoutData(GridDataFactory.swtDefaults().hint(boxWidthHint, SWT.DEFAULT).create());
		imageHigh.setToolTipText("Set image colour high value.");
		imageHigh.addVerifyListener(new DoubleVerifyListener());
		imageHigh.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				final IPlottingSystem<Composite> plottingSystem = getPlottingSystem();
				final IImageTrace iTrace = getImageTrace();

				if (plottingSystem != null && iTrace != null) {
					String text = imageHigh.getText();
					try {
						final double max = Double.parseDouble(text);
						iTrace.getImageServiceBean().setMax(max);
						iTrace.setMax(max);
						iTrace.setPaletteData(iTrace.getPaletteData());
						plottingSystem.repaint();
					} catch (Exception ex) {
						setImageHigh(iTrace.getImageServiceBean().getMin().doubleValue());
					}
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Nothing
			}
		});

		imageHigh.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				imageLow.setFocus();
			}
		});
	}

	private void addAlwaysAutoscaleCheckbox(Group group) {
		Button alwaysAutoScale = new Button(group, SWT.CHECK);
		alwaysAutoScale.setText("Always Autoscale");
		alwaysAutoScale.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IImageTrace iTrace = getImageTrace();
				if (iTrace != null) {
					iTrace.setRescaleHistogram(alwaysAutoScale.getSelection());
					if (alwaysAutoScale.getSelection()) {
						iTrace.rehistogram();
					}
				}
			}
		});

	}

	private void addAutoScaleOnceButton(Group group) {
		Button autoScaleOnce = new Button(group, SWT.PUSH);
		autoScaleOnce.setText("Autoscale");
		autoScaleOnce.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IImageTrace iTrace = getImageTrace();
				if (iTrace != null) {
					iTrace.rehistogram();
				}
			}
		});
	}
}
