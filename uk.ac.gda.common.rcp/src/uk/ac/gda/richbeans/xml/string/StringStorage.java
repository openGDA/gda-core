/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans.xml.string;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;

public class StringStorage implements IStorage {

	private String string;
	private String name;

	public StringStorage(String input) {
		this(input, null);
	}

	public StringStorage(String input, String name) {
		this.string = input;
		this.name = name;
	}
	
	@Override
	public InputStream getContents() throws CoreException {
		try {
			return new ByteArrayInputStream(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			CoreException c = new CoreException(Status.CANCEL_STATUS);
			c.setStackTrace(e.getStackTrace());
			throw c;
		}
	}

	@Override
	public IPath getFullPath() {
		return null;
	}

	// Suppress warning on implemented method signature
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public String getName() {
		if( name != null){
			return name;
		}
		final String[] lines = string.split("\n");
		final String tag = lines[1].trim();
		return tag.substring(1, tag.length() - 2);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

}
