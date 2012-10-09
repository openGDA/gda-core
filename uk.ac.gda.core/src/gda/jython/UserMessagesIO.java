/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.jython;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserMessagesIO {
	
	public static void writeMessages(List<UserMessage> messages, OutputStream os) throws IOException{
		
		// version number
		os.write(1);
		
		DataOutputStream dos = new DataOutputStream(os);
		
		dos.writeInt(messages.size());
		
		for (UserMessage msg : messages) {
			dos.writeInt(msg.sourceClientNumber);
			dos.writeUTF(msg.sourceUsername);
			dos.writeUTF(msg.message);
			dos.writeLong(msg.timestamp);
		}
		
		dos.flush();
	}
	
	public static List<UserMessage> readMessages(InputStream is) throws IOException {
		
		@SuppressWarnings("unused")
		final int version = is.read();
		
		DataInputStream dis = new DataInputStream(is);
		
		final int numMessages = dis.readInt();
		List<UserMessage> messages = new ArrayList<UserMessage>(numMessages);
		
		for (int i=0; i<numMessages; i++) {
			final int sourceClientNumber = dis.readInt();
			final String sourceUsername = dis.readUTF();
			final String message = dis.readUTF();
			final long timestamp = dis.readLong();
			UserMessage um = new UserMessage(sourceClientNumber, sourceUsername, message, timestamp);
			messages.add(um);
		}
		
		return messages;
	}

}
