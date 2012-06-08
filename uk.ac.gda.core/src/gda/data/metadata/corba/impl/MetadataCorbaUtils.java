/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.metadata.corba.impl;

import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.corba.CorbaMetadataEntry;
import gda.device.DeviceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods for converting between a {@link MetadataEntry} and a
 * {@link CorbaMetadataEntry}.
 */
public class MetadataCorbaUtils {

	private static final Logger logger = LoggerFactory.getLogger(MetadataCorbaUtils.class);
	
	protected static CorbaMetadataEntry marshal(IMetadataEntry me) throws DeviceException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(me);
			oos.close();
			return new CorbaMetadataEntry(baos.toByteArray());
		} catch (IOException ioe) {
			final String msg = "Could not marshal MetadataEntry";
			logger.error(msg, ioe);
			throw new DeviceException(msg, ioe);
		}
	}
	
	protected static IMetadataEntry unmarshal(CorbaMetadataEntry cme) throws DeviceException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cme.data));
			IMetadataEntry me = (IMetadataEntry) ois.readObject();
			ois.close();
			return me;
		} catch (Exception e) {
			final String msg = "Could not unmarshal CorbaMetadataEntry";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

}
