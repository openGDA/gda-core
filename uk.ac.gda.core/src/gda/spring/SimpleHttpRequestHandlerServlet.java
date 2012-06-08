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

package gda.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

/**
 * A {@link HttpServlet} that delegates to a {@link HttpRequestHandler}.
 * 
 * <p>Unlike  Spring's {@link HttpRequestHandlerServlet}, the target request
 * handler must be specified when this class is instantiated: it does not
 * retrieve the request handler from a Spring application context.
 */
public class SimpleHttpRequestHandlerServlet extends HttpServlet {

	private HttpRequestHandler target;
	
	/**
	 * Creates a servlet that delegates to the specified request handler.
	 * 
	 * @param target the request handler
	 */
	public SimpleHttpRequestHandlerServlet(HttpRequestHandler target) {
		this.target = target;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		target.handleRequest(request, response);
	}

}
