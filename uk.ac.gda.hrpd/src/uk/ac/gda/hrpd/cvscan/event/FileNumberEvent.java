/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.hrpd.cvscan.event;

import java.io.Serializable;

public class FileNumberEvent implements Serializable {
	

	private String filename;
	private Long collectionNumber;

	public FileNumberEvent(String currentFileName, long collectionNumber2) {
		setFilename(currentFileName);
		setCollectionNumber(collectionNumber2);
	}

	public String getFilename() {
		return filename;
	}

	public Long getCollectionNumber() {
		return collectionNumber;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setCollectionNumber(Long collectionNumber) {
		this.collectionNumber = collectionNumber;
	}

}
