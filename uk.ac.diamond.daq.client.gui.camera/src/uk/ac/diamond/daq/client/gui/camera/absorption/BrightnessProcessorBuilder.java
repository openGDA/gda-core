package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.swt.SWT;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.diamond.daq.client.gui.camera.event.RegisterDrawableRegionEvent;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Builds a BrightnessProcessor
 * 
 * @author Maurizio Nagni
 *
 */
class BrightnessProcessorBuilder {
	private final List<ROIStatisticRow> intRows = new ArrayList<>();

	public BrightnessProcessorBuilder(ROIStatisticRow... rows) {
		Arrays.stream(rows).forEachOrdered(intRows::add);
	}

	public BrightnessProcessorBuilder addRow(ROIStatisticRow row) {
		intRows.add(row);
		return this;
	}

	public BrightnessProcessor build() {
		// the first, empty, processor collects all other processors
		return intRows.stream().map(BrightnessProcessor::new).reduce(new BrightnessProcessor(null),
				(a, b) -> a.addProcessor(b));
	}

	/**
	 * Provides methods to update the row intensity value
	 * {@link #processEvent(ROIChangeEvent)} and their ratio against the max
	 * intensity {@link #processRatio()}
	 * 
	 * @author Maurizio Nagni
	 *
	 */
	class BrightnessProcessor {
		private final List<BrightnessProcessor> processors = new ArrayList<>();
		private final ROIStatisticRow row;

		private BrightnessProcessor(ROIStatisticRow row) {
			this.row = row;
			if (row == null) {
				return;
			}
			createRegion();
		}

		/**
		 * Passes the event to the relevant {@link ROIStatisticRow}
		 * 
		 * @param roiChangeEventthe event to process
		 * @return
		 */
		public BrightnessProcessor processEvent(ROIChangeEvent roiChangeEvent) {
			processors.stream().filter(p -> p.getRow().getRowID().toString().equals(roiChangeEvent.getRoi().getName()))
					.findFirst()
					.ifPresent(p -> p.getRow().processValue(roiChangeEvent.getRoi(), roiChangeEvent.getDataset()));
			return this;
		}

		/**
		 * Updates the row intensity/maxBrighteness ratio for each row
		 */
		public void processRatio() {
			double maxBrightness = getMaximumValue();
			processors.stream().forEach(p -> p.getRow().processRatio(maxBrightness));
		}

		protected BrightnessProcessor addProcessor(BrightnessProcessor processor) {
			processors.add(processor);
			return this;
		}

		protected ROIStatisticRow getRow() {
			return row;
		}

		private double getMaximumValue() {
			return processors.stream().map(BrightnessProcessor::getRow).filter(Objects::nonNull)
					.map(ROIStatisticRow::getValue).max(Long::compare).orElse(Long.MAX_VALUE);
		}

		private void createRegion() {
			Optional<UUID> rootID = ClientSWTElements.findParentUUID(getRow().getTableItem().getParent());
			SpringApplicationContextProxy.publishEvent(new RegisterDrawableRegionEvent(this,
					SWTResourceManager.getColor(SWT.COLOR_YELLOW), getRow().getName(), rootID, getRow().getRowID()));
		}
	}
}
