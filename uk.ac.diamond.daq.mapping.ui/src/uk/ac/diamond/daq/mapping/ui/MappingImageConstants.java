/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import java.util.List;

/**
 * A non-instantiable class consisting of constants for the names of the icons used
 * in the Mapping UI.
 */
public class MappingImageConstants {

	public static final String IMG_FOLDER_IMPORT = "folder-import.png";
	public static final String IMG_FOLDER_EXPORT = "folder-export.png";
	public static final String IMG_TICKED = "ticked.png";
	public static final String IMG_UNTICKED = "unticked.gif";
	public static final String IMG_PLUS = "plus.png";
	public static final String IMG_CROSS = "cross.png";
	public static final String IMG_PENCIL = "pencil.png";
	public static final String IMG_SAVE = "save.png";
	public static final String IMG_COPY = "copy.png";
	public static final String IMG_CAMERA = "camera.png";
	public static final String IMG_OPEN = "open.png";
	public static final String IMG_STOP = "stop.png";
	public static final String IMG_MAP_PENCIL = "map--pencil.png";
	public static final String IMG_MAP_PIN = "map-pin.png";
	public static final String IMG_GEAR = "gear.png";
	public static final String IMG_PAGE_REFRESH = "page_refresh.png";
	public static final String IMG_NEXUS = "nexus.png";
	public static final String IMG_DATABASE_ARROW = "database--arrow.png";
	public static final String IMG_DATABASE_PLUS = "database--plus.png";
	public static final String IMG_YELLOW = "yellow.png";
	public static final String IMG_GREY = "grey.png";
	public static final String IMG_RED = "red.png";
	public static final String IMG_GREEN = "green.png";
	public static final String IMG_WARNING = "warning.png";
	public static final String IMG_PLAY = "play.png";

	public static final List<String> IMAGE_NAMES = List.of(
			IMG_FOLDER_IMPORT,
			IMG_FOLDER_EXPORT,
			IMG_TICKED,
			IMG_UNTICKED,
			IMG_PLUS,
			IMG_CROSS,
			IMG_PENCIL,
			IMG_SAVE,
			IMG_COPY,
			IMG_CAMERA,
			IMG_OPEN,
			IMG_STOP,
			IMG_MAP_PENCIL,
			IMG_MAP_PIN,
			IMG_GEAR,
			IMG_PAGE_REFRESH,
			IMG_NEXUS,
			IMG_DATABASE_ARROW,
			IMG_DATABASE_PLUS,
			IMG_YELLOW,
			IMG_GREY,
			IMG_RED,
			IMG_GREEN,
			IMG_WARNING,
			IMG_PLAY
	);

	private MappingImageConstants() {
		// private constructor to prevent instantiation
	}

}
