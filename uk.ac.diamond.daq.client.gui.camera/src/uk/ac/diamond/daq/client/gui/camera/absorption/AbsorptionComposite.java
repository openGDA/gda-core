package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.absorption.BrightnessProcessorBuilder.BrightnessProcessor;
import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Creates a composite to compares the intensities of two ROI. Each ROI is
 * wrapped into a {@link ROIStatisticRow} which is displayed as row in a table.
 * 
 * <p>
 * The {@link BrightnessProcessor} uses {@link ROIStatisticRow} to create a
 * {@code ROIRectangle} into the {@code IPlottingSystem}. Note that
 * {@link ROIStatisticRow#getRowID()} and {@code ROIRectangle.getName} are
 * equal: this is how the {@link BrightnessProcessor} matches events from one
 * ROI with the connected row.
 * <p>
 * 
 * @author Maurizio Nagni
 *
 */
public class AbsorptionComposite implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(AbsorptionComposite.class);

	@Override
	public Composite createComposite(final Composite parent, int style) {
		Table table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		createTableColumn(table);

		// Creates the processor for the dark/bright lines
		ROIStatisticRow bright = new ROIStatisticRow(table, ClientMessages.BRIGHT);
		ROIStatisticRow dark = new ROIStatisticRow(table, ClientMessages.DARK);

		BrightnessProcessorBuilder builder = new BrightnessProcessorBuilder(bright, dark);
		BrightnessProcessor brigthenssProcessor = builder.build();

		try {
			// Listens to ROIChangeEvent
			SpringApplicationContextProxy.addDisposableApplicationListener(table, getROIChangeListener(brigthenssProcessor));
		} catch (GDAClientException e) {
			logger.error("Cannot append ROIChangeListener to Spring");
		}
		return table;
	}

	/**
	 * @param brigthenssProcessor the class which will process the event
	 * @return the listener
	 */
	private ApplicationListener<ROIChangeEvent> getROIChangeListener(BrightnessProcessor brigthenssProcessor) {
		return new ApplicationListener<ROIChangeEvent>() {
			public void onApplicationEvent(ROIChangeEvent event) {
				brigthenssProcessor.processEvent(event).processRatio();
			}
		};
	}

	private void createTableColumn(Table table) {
		ClientMessages[] headers = { ClientMessages.ROI, ClientMessages.VALUE, ClientMessages.RATIO_TO_BRIGHTEST };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}
}
