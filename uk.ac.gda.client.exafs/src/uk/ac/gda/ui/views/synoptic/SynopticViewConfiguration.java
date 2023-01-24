/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import uk.ac.gda.client.livecontrol.LiveControl;

public class SynopticViewConfiguration extends FindableBase {
	private static final Logger logger = LoggerFactory.getLogger(SynopticViewConfiguration.class);

	// LiveControl object, Point with x,y coordinate of where the LiveControl widget should be placed in the View
	private Map<LiveControl, Point> controls = new HashMap<>();
	private String backgroundImage = "";
	private Point imageStart = new Point(0,0);
	private double imageScaleFactor = 1.0;

	private String viewName = "";
	private boolean showCoordinates = false;

	public void setControls(Map<LiveControl, Point> controls) {
		this.controls = new HashMap<>(controls);
	}

	public void setControlsList(Map<LiveControl, String> controlWithString) {
		this.controls =	controlWithString.entrySet()
				.stream()
				.collect(Collectors.toMap(Entry::getKey, ent -> getPointFromString(ent.getValue())));
	}

	private Point getPointFromString(String pointString) {
		String[] numbers = pointString.replace(","," ").split("\\s+");
		Point point = new Point(0,0);
		if (numbers.length <2 ) {
			logger.warn("Could not extract 2 numbers from string {}", pointString);
		} else {
			point.x = Integer.parseInt(numbers[0]);
			point.y = Integer.parseInt(numbers[1]);
		}
		return point;
	}

	public void setImageStart(String imageStartString) {
		imageStart = getPointFromString(imageStartString);
	}

	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public void setShowCoordinates(boolean showCoordinates) {
		this.showCoordinates = showCoordinates;
	}

	public Map<LiveControl, Point> getControls() {
		return controls;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public Point getImageStart() {
		return imageStart;
	}

	public double getImageScaleFactor() {
		return imageScaleFactor;
	}

	public void setImageScaleFactor(double imageScaleFactor) {
		this.imageScaleFactor = imageScaleFactor;
	}

	public boolean isShowCoordinates() {
		return showCoordinates;
	}
}
