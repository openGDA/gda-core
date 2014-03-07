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

package gda.device.detector.uview;

import java.io.IOException;

import gda.device.DeviceException;
import gda.io.socket.SocketBidiAsciiCommunicator;

public class UViewTcpSocketConnection extends SocketBidiAsciiCommunicator {
	
	public byte[] readBinary(int length) throws DeviceException {
		lock.lock();
		byte read[] = new byte[length];
		try {
			connectIfRequired();
			reader.read(read);
			return read;
		} catch (IOException e) {
			throw new DeviceException("Error reading output bytes", e);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected void connectIfRequired() throws DeviceException {
		if ( writer == null || reader == null ) {
			super.connectIfRequired();
			this.send("asc");
		}
	}
}
