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
	ADD(ClientImagesGroup.GENERAL, "add.png"),

	// --- ENERGY IMAGES ---
	BEAM_16(ClientImagesGroup.ENERGY, "beam_16.png"),

	// --- TOMOGRAPHY IMAGES ---
	SINOGRAM_50(ClientImagesGroup.TOMOGRAPHY, "sinogram-50.png");


	private final ClientImagesGroup group;
	private final String fileName;
	private final String imagesPathFormat = "%s/%s";

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

		GENERAL(""), ENERGY("energy"), TOMOGRAPHY("tomography");

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
