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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.text.DecimalFormat;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pulls relevant information from IDataset
 * pertaining to a detector snapshot as a spectrum (or series of spectra)
 * and creates widgets for displaying that information
 */
public class LineSnapshotStatsViewer implements SnapshotStatsViewer {

	private static final Logger logger = LoggerFactory.getLogger(LineSnapshotStatsViewer.class);
	private DecimalFormat oneDecimalPlace = new DecimalFormat("##########0.0#");

	private Combo elementCombo;
	private Text  totalCountText,
				  totalCountRateText,
				  countsText,
				  countRateText,
				  maxCountText,
				  maxPosText;

	/**
	 * Draw widgets ready to display stats from a detector snapshot (spectrum)
	 * @param parent
	 */
	public LineSnapshotStatsViewer(Composite parent) {
		//Prepare a common layout
		GridDataFactory gd = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);

		(new Label(parent, SWT.NONE)).setText("Total counts");
		totalCountText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(totalCountText);

		(new Label(parent, SWT.NONE)).setText("Total count rate");
		totalCountRateText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		gd.applyTo(totalCountRateText);

		(new Label(parent, SWT.NONE)).setText("Element");
		elementCombo = new Combo(parent, SWT.NONE);
		gd.applyTo(elementCombo);

		(new Label(parent, SWT.NONE)).setText("Counts");
		countsText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(countsText);

		(new Label(parent, SWT.NONE)).setText("Count rate");
		countRateText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(countRateText);

		(new Label(parent, SWT.NONE)).setText("Maximum count");
		maxCountText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(maxCountText);

		(new Label(parent, SWT.NONE)).setText("Max count position");
		maxPosText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(maxPosText);
	}

	@Override
	public void update(IDataset dataset, double exposure) {
		if (elementCombo.getItems().length < 1) initialiseElementCombo(dataset, exposure);

		SnapshotStatsCalculator stats = new SnapshotStatsCalculator();

		double count = stats.calculateTotalCount(dataset);
		totalCountText.setText(oneDecimalPlace.format(count));
		totalCountRateText.setText(oneDecimalPlace.format(count/exposure));

		// for selected channel:
		int index = elementCombo.getSelectionIndex();
		count = stats.calculateSliceCount(dataset, index);
		countsText.setText(oneDecimalPlace.format(count));
		countRateText.setText(oneDecimalPlace.format(count/exposure));
		IDataset spectrum = dataset.getSliceView(new Slice(index, index+1, 1));
		try {
			maxCountText.setText(oneDecimalPlace.format(stats.findMaximumIntensity(spectrum)));
			maxPosText.setText(String.valueOf(stats.findMaximumPosition(spectrum)[1]));
		} catch (IndexOutOfBoundsException e) {
			maxCountText.setText("NaN");
			maxPosText.setText("");
			logger.info("Corrupt data in dataset");
		}
	}

	private void initialiseElementCombo(IDataset dataset, double exposure) {
		int numberOfDetectorElements = dataset.getShape()[0];
		String[] elementsArray = new String[numberOfDetectorElements];
		for (int element=0; element<numberOfDetectorElements; element++) {
			elementsArray[element] = String.valueOf(element+1);
		}
		elementCombo.setItems(elementsArray);
		elementCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				update(dataset, exposure);
			}
		});

		elementCombo.select(0);
	}

}
