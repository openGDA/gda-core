/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.rcp.client.propertyTesters;

import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.rcp.Application;

import org.eclipse.core.expressions.PropertyTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsStaffPropertyTester extends PropertyTester {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public IsStaffPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equalsIgnoreCase("isStaff")){
			try {
				return AuthoriserProvider.getAuthoriser().isLocalStaff(UserAuthentication.getUsername());
			} catch (ClassNotFoundException e) {
				logger.warn("Exception when trying to identify user as staff", e);
				return false;
			}
		}
		return false;

	}

}
