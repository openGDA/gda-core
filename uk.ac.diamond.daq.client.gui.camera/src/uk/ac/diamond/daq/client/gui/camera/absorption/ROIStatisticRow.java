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

package uk.ac.diamond.daq.client.gui.camera.absorption;

import static org.eclipse.january.dataset.DatasetUtils.convertToDataset;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Calculates, from a region shape, and displays, as table row, an image
 * intensity.
 * <p>
 * To dynamically calculate the intensity, the class exposes
 * {@link #getProcessEvent()} to external instances handling a region area on
 * the plotting system.
 * </p>
 *
 * <p>
 * To dynamically calculate the intensity ration with another column, the
 * instance exposes {@link #getIntensity()} so that another instance can use it
 * calling {@link #calculateRatioWith(LongSupplier, ClientMessages)}.
 * </p>
 *
 * <p>
 * Locking the row value, selecting the {@code lockIntensity} button causes the
 * row to stop updating the intensity value. However region and its dataset are
 * updated are always updated , so unlocking a row causes its immediate update.
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
class ROIStatisticRow {
	private final TableItem tableItem;

	private Label intensityLabel;
	private Label ratioLabel;
	private Button lockIntensity;

	private IDataset dataset;
	private IROI roi;
	private long intensity;

	private LongSupplier otherIntensity;
	private ClientMessages otherIntensityName;

	/**
	 * @param table the {@link Table} where attach the {@link TableItem}
	 * @param name  the name of the row
	 */
	public ROIStatisticRow(Table table, ClientMessages name) {
		this.tableItem = new TableItem(table, SWT.NONE);
		addColumns(name);
	}

	public void calculateRatioWith(LongSupplier otherIntensity, ClientMessages otherIntensityName) {
		this.otherIntensity = otherIntensity;
		this.otherIntensityName = otherIntensityName;
	}

	private BiConsumer<IROI, IDataset> processEvent = (flux, data) -> {
		this.roi = flux;
		this.dataset = data;
		if (!lockIntensity.getSelection()) {
			updateValue();
		}
	};

	/**
	 * @return the process event (as defined above)
	 */
	BiConsumer<IROI, IDataset> getProcessEvent() {
		return processEvent;
	}

	private LongSupplier intensitySupplier = () -> this.intensity;

	public LongSupplier getIntensity() {
		return intensitySupplier;
	}

	/**
	 * Calculate the ration of this region intensity com
	 */
	private void processRatio() {
		if (otherIntensity != null && otherIntensity.getAsLong() != 0) {
			String ratioString = String.format("Ratio with %s: %.3f",
					ClientMessagesUtility.getMessage(otherIntensityName),
					(double) this.intensity / otherIntensity.getAsLong());
			this.ratioLabel.setText(ratioString);
		}
	}

	private void addColumns(ClientMessages name) {
		Table table = tableItem.getParent();

		TableEditor editor = new TableEditor(table);
		lockIntensity = createClientButton(table, SWT.CHECK, EMPTY_MESSAGE, EMPTY_MESSAGE);
		GridDataFactory.fillDefaults().applyTo(lockIntensity);

		editor.grabHorizontal = true;
		editor.setEditor(lockIntensity, tableItem, 0);

		addWidgetDisposableListener(lockIntensity, SWT.Selection, event -> updateValue());

		editor = new TableEditor(table);
		Label nameLabel = createClientLabel(table, SWT.NONE, name);
		GridDataFactory.fillDefaults().applyTo(nameLabel);

		editor.grabHorizontal = true;
		editor.setEditor(nameLabel, tableItem, 1);

		editor = new TableEditor(table);
		intensityLabel = createClientLabel(table, SWT.NONE, EMPTY_MESSAGE);
		GridDataFactory.fillDefaults().applyTo(intensityLabel);

		editor.grabHorizontal = true;
		editor.setEditor(intensityLabel, tableItem, 2);

		editor = new TableEditor(table);
		ratioLabel = createClientLabel(table, SWT.NONE, EMPTY_MESSAGE);
		GridDataFactory.fillDefaults().applyTo(intensityLabel);

		editor.grabHorizontal = true;
		editor.setEditor(ratioLabel, tableItem, 3);
	}

	private void updateValue() {
		// Takes the bounding box containing whatever shape
		IRectangularROI boundingBox = roi.getBounds();
		int[] xy = boundingBox.getIntPoint();
		int[] length = boundingBox.getIntLengths();

		int[] start = new int[] { xy[0], xy[1] };
		int[] end = new int[] { xy[0] + length[0], xy[1] + length[1] };
		int[] step = new int[] { 1, 1 };

		// resets the intensity
		this.intensity = 0;
		// crop the image using the bounding box
		Dataset intDataset = convertToDataset(dataset.getSliceView(start, end, step));

		// filters the points contained in the roi, sum up their intensities
		IntStream.range(0, length[1])
				.forEach(y -> IntStream.range(0, length[0])
					.filter(x -> roi.containsPoint((double) x + xy[0], (double) y + xy[1]))
					.forEach(x -> sumIntensities(x, y, intDataset)));
		this.intensityLabel.setText(Long.toString(this.intensity));

		processRatio();
	}

	private void sumIntensities(int x, int y, Dataset intDataset) {
		this.intensity += intDataset.getLong(x, y);
	}
}
