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

package uk.ac.gda.common.entity.filter;

/**
 * Builds a DocumentFilter instance.
 *
 * @author Maurizio Nagni
 */
public class DocumentFilterBuilder {

	private String fileExtension;

	/**
	 * Returns the expected extension, typically a file extension, associated with a persisted document
	 * @return the document extension
	 */
	public DocumentFilterBuilder setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
		return this;
	}

	public DocumentFilter build() {
		return new InnerDocumentFilter(fileExtension);
	}

	private class InnerDocumentFilter implements DocumentFilter {
		private final String fileExtension;

		public InnerDocumentFilter(String fileExtension) {
			super();
			this.fileExtension = fileExtension;
		}

		@Override
		public String getFileExtension() {
			return fileExtension;
		}
	}
}
