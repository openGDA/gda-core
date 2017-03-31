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

package gda.device.detector.uviewnew;

import java.io.IOException;

import gda.device.DeviceException;
import gda.io.socket.SocketBidiAsciiCommunicator;

public class UViewTcpSocketConnection extends SocketBidiAsciiCommunicator {

	public byte[] readBinary(final int length) throws DeviceException {
		lock.lock();
		byte read[] = new byte[length];
		int readIndex = 0;
		try {
			connectIfRequired();
			while ( readIndex < length ) {
				int bytesRead = reader.read(read, readIndex, length - readIndex);
				if ( bytesRead == -1 ) {
					throw new DeviceException( String.format("Expected {0} bytes, received {1}", length, readIndex) );
				}
				readIndex += bytesRead;
			}
			if ( logger.isDebugEnabled() ) {
				String strOut = new String(read, 0, Math.min(length, 30));
				logger.debug("data out = " + strOut);
			}
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
	
	@Override
	public String send(String cmd) throws DeviceException {
		lock.lock();
		try {
			logger.info("sent: "+ cmd);
			String response = super.send(cmd);
			logger.info("received: " + response);
			return response;
		} finally {
			lock.unlock();
		}
	}
}
