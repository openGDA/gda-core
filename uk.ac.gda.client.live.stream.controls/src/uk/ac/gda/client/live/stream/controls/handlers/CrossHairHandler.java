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

package uk.ac.gda.client.live.stream.controls.handlers;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.controls.Activator;
import uk.ac.gda.client.live.stream.controls.ImageConstants;
import uk.ac.gda.client.live.stream.handlers.SnapshotData;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

/**
 * A handler to create a cross hair on top of the image in the {@link LiveStreamView}.
 * <p>
 * This implementation supports add and remove cross-hair, and save cross hair position to cached file {@link LocalProperties#GDA_VAR_DIR}/crosshair.csv,
 * Users can also control the visibility and move_ability of the cross hair.
 * </p>
 */
public class CrossHairHandler extends AbstractHandler implements IElementUpdater {

	public static final String commandID = "uk.ac.gda.client.live.stream.controls.crosshair";
	private static final String PARAMETER_ID="uk.ac.gda.client.live.stream.controls.crosshairParameter";
	public static final String CROSSHAIR_X="crosshair_x";
	public static final String CROSSHAIR_Y="crosshair_y";
	private static final Logger logger = LoggerFactory.getLogger(CrossHairHandler.class);
	private String fileName;
	private Map<String, CrosshairBean> beansMap=new HashMap<>();
	private CrosshairBean bean;
	private String displayName;
	private boolean crosshairAdded=false;
	private boolean moveable=true;
	private boolean visible=true;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String name = event.getParameter(PARAMETER_ID);

		final LiveStreamView liveStreamView = (LiveStreamView) HandlerUtil.getActivePart(event);
		displayName = liveStreamView.getActiveCameraConfiguration().getDisplayName();
		fileName = LocalProperties.get(LocalProperties.GDA_VAR_DIR) + File.separator + "crosshair.csv";

		bean = createCrosshairBean(liveStreamView);

		if (name.equals("Remove") && crosshairAdded) {
			removeCrosshair(liveStreamView);
		} else if (name.equals("Add") && !crosshairAdded) {
			addCrosshair(liveStreamView, bean);
		} else if (name.equals("Save")) {
			persistCrosshair(liveStreamView);
		} else if (name.equals("Visible")) {
			taggleVisible(liveStreamView);
		} else if (name.equals("Moveable")) {
			taggleMovable(liveStreamView);
		}
		//update command state in UI.
		ICommandService service = HandlerUtil.getActiveWorkbenchWindow(event).getService(ICommandService.class);
		service.refreshElements(commandID, null);

		return null;
	}

	private void taggleMovable(LiveStreamView liveStreamView) {
		IRegion xregion = liveStreamView.getPlottingSystem().getRegion(CROSSHAIR_X);
		IRegion yregion = liveStreamView.getPlottingSystem().getRegion(CROSSHAIR_Y);
		if (moveable) {
			xregion.setMobile(false);
			yregion.setMobile(false);
			moveable=false;
		} else {
			xregion.setMobile(true);
			yregion.setMobile(true);
			moveable=true;
		}
	}

	private void taggleVisible(LiveStreamView liveStreamView) {
		IRegion xregion = liveStreamView.getPlottingSystem().getRegion(CROSSHAIR_X);
		IRegion yregion = liveStreamView.getPlottingSystem().getRegion(CROSSHAIR_Y);
		if (visible) {
			xregion.setVisible(false);
			yregion.setVisible(false);
			visible=false;
		} else {
			xregion.setVisible(true);
			yregion.setVisible(true);
			visible=true;
		}
	}

	private void removeCrosshair(LiveStreamView liveStreamView) {
		final IPlottingSystem<Composite> plottingSystem = liveStreamView.getPlottingSystem();
		IRegion xregion = plottingSystem.getRegion(CROSSHAIR_X);
		IRegion yregion = plottingSystem.getRegion(CROSSHAIR_Y);
		CrosshairBean crosshairBean = new CrosshairBean();
		crosshairBean.setCameraName(displayName);
		crosshairBean.setxPosition(xregion.getROI().getPointX());
		crosshairBean.setyPosition(yregion.getROI().getPointY());
		beansMap.put(displayName, crosshairBean);
		writeCrosshairToFile(beansMap);

		plottingSystem.removeRegion(xregion);
		plottingSystem.removeRegion(yregion);
		crosshairAdded=false;
	}

	private void persistCrosshair(LiveStreamView liveStreamView) {
		final IPlottingSystem<Composite> plottingSystem = liveStreamView.getPlottingSystem();
		IRegion xregion = plottingSystem.getRegion(CROSSHAIR_X);
		IRegion yregion = plottingSystem.getRegion(CROSSHAIR_Y);
		CrosshairBean crosshairBean = new CrosshairBean();
		crosshairBean.setCameraName(displayName);
		crosshairBean.setxPosition(xregion.getROI().getPointX());
		crosshairBean.setyPosition(yregion.getROI().getPointY());
		beansMap.put(displayName, crosshairBean);
		writeCrosshairToFile(beansMap);
		xregion.setMobile(false);
		yregion.setMobile(false);
		moveable=false;
	}

	private void addCrosshair(final LiveStreamView liveStreamView, CrosshairBean bean) throws ExecutionException {
		final IPlottingSystem<Composite> plottingSystem = liveStreamView.getPlottingSystem();
		final Color crosshairColour = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		IRegion xRegion;
		try {
			xRegion = plottingSystem.createRegion(CROSSHAIR_X, RegionType.XAXIS_LINE);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating crosshairx", e);
		}
		xRegion.setRegionColor(crosshairColour);
		IROI roix = new XAxisBoxROI();
		roix.setPoint(bean.getxPosition(), 0);
		xRegion.setROI(roix);
		//the following 4 lines have no effect at the moment see SCI-8826, SCI-8827
		xRegion.setActive(false);
		xRegion.setMobile(false);
		xRegion.setUserRegion(false);
		xRegion.setFromServer(false);
		plottingSystem.addRegion(xRegion);

		IRegion yRegion;
		try {
			yRegion = plottingSystem.createRegion(CROSSHAIR_Y, RegionType.YAXIS_LINE);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating crosshairy", e);
		}
		yRegion.setRegionColor(crosshairColour);
		IROI roiy = new YAxisBoxROI();
		roiy.setPoint(0, bean.getyPosition());
		yRegion.setROI(roiy);
		yRegion.setActive(false);
		yRegion.setMobile(false);
		yRegion.setUserRegion(false);
		yRegion.setFromServer(false);
		plottingSystem.addRegion(yRegion);

		crosshairAdded=true;
	}

	private CrosshairBean createCrosshairBean(final LiveStreamView liveStreamView) throws ExecutionException {
		if (!Files.exists(Paths.get(fileName))) {
			logger.info("File {} is not available in cache, create a new file", fileName);
			// when 1st time start no crosshair.csv in cache so create the cached file.
			return createDefaultCrosshairBean(liveStreamView);
		}
		try {
			List<CrosshairBean> beans = readBeansFromCsv(Paths.get(fileName));
			for (CrosshairBean each : beans) {
				beansMap.put(each.getCameraName(), each);
			}
			// retrieve cross hair bean from cached data file
			if (beansMap.containsKey(displayName)) {
				return beansMap.get(displayName);
			} else {
				// create a default cross hair bean at the centre of the camera image view
				return createDefaultCrosshairBean(liveStreamView);
			}
		} catch (IOException e1) {
			logger.error("Error reading {} ", fileName);
			throw new ExecutionException(e1.getMessage(), e1);
		}
	}

	private CrosshairBean createDefaultCrosshairBean(final LiveStreamView liveStreamView) {

		SnapshotData dataset;
		double x=10.0, y=10.0;
		try {
			dataset = liveStreamView.getSnapshot();
			IDataset xAxis = dataset.getxAxis();
			IDataset yAxis = dataset.getyAxis();
			if (xAxis != null) {
				x = (double) xAxis.mean();
			}
			if (yAxis!=null) {
				y = (double) yAxis.mean();
			}
		} catch (LiveStreamException e) {
			logger.warn("Failed in get image snapshot, so use default (x,y) = (10, 10)",e);
		}
		CrosshairBean crosshairBean = new CrosshairBean();
		crosshairBean.setCameraName(displayName);
		crosshairBean.setxPosition(x);
		crosshairBean.setyPosition(y);
		return crosshairBean;
	}

	private void writeCrosshairToFile(Map<String, CrosshairBean> beansMap2) {
		try {
			List<CrosshairBean> beans= new ArrayList<>();
			for (CrosshairBean bean : beansMap2.values()) {
				beans.add(bean);
			}
			writeBeansToCsv(beans, Paths.get(fileName));
		} catch (IOException e) {
			logger.error("Write to file {} throws", fileName, e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {

		for (Object value: parameters.values()) {
			if ("Add".equals(String.valueOf(value))) {
				element.setText("Add");
				element.setTooltip("Add crosshair");
				element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_ADD_CROSS_HAIR));
			} else if ("Remove".equals(String.valueOf(value))) {
				element.setText("Remove");
				element.setTooltip("Remove crosshair");
				element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_REMOVE_CROSS_HAIR));
			} else if ("Save".equals(String.valueOf(value))) {
				element.setText("Save");
				element.setTooltip("Save crosshair position");
				element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_SAVE_CROSS_HAIR));
			} else if ("Visible".equals(String.valueOf(value))) {
				element.setText("Visible");
				element.setTooltip("taggle crosshair visible");
				if (crosshairAdded && visible) {
					element.setChecked(true);
				} else {
					element.setChecked(false);
				}
			} else if ("Moveable".equals(String.valueOf(value))) {
				element.setText("Moveable");
				element.setTooltip("taggle crosshair moveable");
				if (crosshairAdded && moveable) {
					element.setChecked(true);
				} else {
					element.setChecked(false);
				}
			}
		}
	}

	/**
	 * Read CSV rows from a file and convert to {@link CrosshairBean}.
	 * The first row of the file should contain the field names (case sensitive) as:
	 * <p>
	 * {@code "cameraName","xPosition","yPosition"}
	 */
	private List<CrosshairBean> readBeansFromCsv(Path path) throws IOException {
		List<CrosshairBean> beans = new ArrayList<>();
		try (CSVParser csvFileParser = new CSVParser(Files.newBufferedReader(path),
				CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withFirstRecordAsHeader())) {
			List<CSVRecord> csvRecords = csvFileParser.getRecords();
			for (CSVRecord rec : csvRecords) {
				Map<String, ? extends Object> mappedFields = rec.toMap();
				CrosshairBean cb = new CrosshairBean();
				// Check that the header names read are valid properties
				if (mappedFields.keySet().stream().anyMatch(f -> !PropertyUtils.isWriteable(cb, f))) {
					logger.warn("Crosshair file: {} contains invalid header names", path);
					return Collections.emptyList();
				}
				BeanUtils.populate(cb, mappedFields);
				beans.add(cb);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Error deserialising Crosshair bean", e);
		}
		return beans;
	}

	private void writeBeansToCsv(List<CrosshairBean> beans, Path path) throws IOException {
		try {
			// First generate a header from the class, TreeSet to ensure consistent ordering
			Map<String, String> prop = BeanUtils.describe(new CrosshairBean());
			Set<String> header = new TreeSet<>(prop.keySet());
			// The class entry is removed to leave the fields only
			header.remove("class");
			try (CSVPrinter pr = new CSVPrinter(Files.newBufferedWriter(path),
					CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withHeader(header.toArray(new String[] {})))) {
				for (CrosshairBean b : beans) {
					Map<String, String> bMap = BeanUtils.describe(b);
					List<String> record = header.stream().map(bMap::get).collect(toList());
					pr.printRecord(record);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("Error serialising Crosshair bean", e);
		}
	}

}
