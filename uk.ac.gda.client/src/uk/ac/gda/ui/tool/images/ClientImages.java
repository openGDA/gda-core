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

package uk.ac.gda.ui.tool.images;

import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Enumerates client's images/icons available from the client/icons folder
 * Used in union with {@link ClientSWTElements#getImage(ClientImages)}, a client component implementation can decouple itself
 * from naming explicitly the location of the icon and consequently harmonising the client layout (same function, same image)
 *
 */
public enum ClientImages {

	// --- GENERAL IMAGES ---
	ADD(ClientImagesGroup.GENERAL, "add-16.png"),
	DELETE(ClientImagesGroup.GENERAL, "delete-16.png"),
	INFO(ClientImagesGroup.GENERAL, "info-16.png"),
	MOVE_TO(ClientImagesGroup.GENERAL, "timing-belt-16.png"),
	NEW(ClientImagesGroup.GENERAL, "new-file-16.png"),
	NO_IMAGE(ClientImagesGroup.GENERAL, "no-image-16.png"),
	OPEN(ClientImagesGroup.GENERAL, "open-16.png"),
	POSITION_PIN(ClientImagesGroup.GENERAL, "position-pin.png"),
	RUN(ClientImagesGroup.GENERAL, "run-16.png"),
	SAVE(ClientImagesGroup.GENERAL, "save-16.png"),
	SELECT_DOCUMENT(ClientImagesGroup.GENERAL, "select_document-16.png"),
	START(ClientImagesGroup.GENERAL, "play-16.png"),
	STOP(ClientImagesGroup.GENERAL, "stop-16.png"),
	TARGET(ClientImagesGroup.GENERAL, "target-16.png"),
	TRIANGLE_DOWN(ClientImagesGroup.GENERAL, "triangle-down-16.png"),
	TRIANGLE_UP(ClientImagesGroup.GENERAL, "triangle-up-16.png"),
	EXCLAMATION_RED(ClientImagesGroup.GENERAL, "exclamation-red.png"),
	LOCK(ClientImagesGroup.GENERAL, "lock.png"),
	UNLOCK(ClientImagesGroup.GENERAL, "lock-unlock.png"),
	EXPAND(ClientImagesGroup.GENERAL, "arrow-out.png"),
	AUTOSCALE(ClientImagesGroup.GENERAL, "AutoScale.png"),

	// --- MAPPING IMAGES ---
	CENTERED_RECTAGLE(ClientImagesGroup.GENERAL, "centred_rectangle.png"),
	LINE(ClientImagesGroup.GENERAL, "line.png"),
	POINT(ClientImagesGroup.GENERAL, "point.png"),
	BEAM_SELECTOR(ClientImagesGroup.GENERAL, "arrow-switch.png"),
	MAP_REDRAW(ClientImagesGroup.GENERAL, "map--pencil.png"),

	CAMERA(ClientImagesGroup.GENERAL, "camera-16.png"),

	// --- ENERGY IMAGES ---
	BEAM_16(ClientImagesGroup.ENERGY, "beam_16.png"),

	// --- STATE IMAGES ---
	STATE_ERROR(ClientImagesGroup.STATE, "unavailable_16.png"),
	STATE_WARNING(ClientImagesGroup.STATE, "warning_16.png"),
	STATE_IDLE(ClientImagesGroup.STATE, "idle_16.png"),
	STATE_ACTIVE(ClientImagesGroup.STATE, "active_16.png"),

	// --- TOMOGRAPHY IMAGES ---
	SINOGRAM_50(ClientImagesGroup.TOMOGRAPHY, "sinogram-50.png");


	private final ClientImagesGroup group;
	private final String fileName;
	private final String imagesPathFormat = "%s%s";

	/**
	 * Defines an enum for an image.
	 * Internally defines the path to the image using a {@link ClientImagesGroup} as root for the image filename.
	 *
	 * @param group the image root path
	 * @param fileName the image name
	 */
	ClientImages(ClientImagesGroup group, String fileName) {
		this.group = group;
		this.fileName = fileName;
	}

	public String getImagePath() {
		return String.format(imagesPathFormat, getGroup().getPath(), getFileName());
	}

	private String getFileName() {
		return fileName;
	}

	private ClientImagesGroup getGroup() {
		return group;
	}

	/**
	 * A set of constant defining a group of images.
	 * Each enum refers to a path having the "icons" folder as implicit root.
	 * Consequently is possible to define both a one level path
	 * <i>tomography</i> or a multilevel one as <i>configuration/robot</i>
	 *
	 * <b>It is discouraged to create levels for a specific beamline.
	 * Please use the existing groups in order to not diverge from a hamonised GDA client.
	 * </b>
	 */
	enum ClientImagesGroup {

		GENERAL(""),
		ENERGY("energy/"),
		STATE("state/"),
		TOMOGRAPHY("tomography/");

		private final String path;

		private final String groupPathFormat = "icons/%s";
		ClientImagesGroup(String path) {
			this.path = path;
		}

		private String getPath() {
			return String.format(groupPathFormat, path);
		}
	}
}
