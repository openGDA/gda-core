/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;



import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NFSFileMonitor {
	private static final Logger logger=LoggerFactory.getLogger(NFSFileMonitor.class);

	public static void main(String[] args) throws InterruptedException {
		
		FileSystemManager fsManager;
		FileObject listendir;
		try {
			fsManager = VFS.getManager();
			listendir = fsManager.resolveFile("/dls/i11/data/2013/cm5935-5");
			DefaultFileMonitor fm = new DefaultFileMonitor(new CustomFileListener()); 
			fm.setRecursive(true); 
			fm.addFile(listendir); 
			fm.start(); 
			while (true) {
				Thread.sleep(5000);
			}
		} catch (FileSystemException e) {
			logger.error(e.getMessage(), e);
		}
		
		
	}
}
