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

package gda.spring.remoting.http;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that provides the list of available remote objects.
 */
public class RemoteObjectListerServlet extends HttpServlet {

	private static final String CONTENT_TYPE = "text/plain";
	
	private Map<String, String> objects;
	
	/**
	 * Creates a lister servlet that will return the specified list of objects.
	 * 
	 * @param objects the objects
	 */
	public RemoteObjectListerServlet(Map<String, String> objects) {
		this.objects = objects;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(CONTENT_TYPE);
		ServletOutputStream outStream = resp.getOutputStream();
		for (Map.Entry<String, String> object : objects.entrySet()) {
			String servletName = object.getKey();
			String className = object.getValue();
			outStream.println(servletName + "|" + className);
		}
		outStream.close();
	}

}
