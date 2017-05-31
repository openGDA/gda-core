/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.closeactions.contactinfo;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import gda.configuration.properties.LocalProperties;

public class ISPyBJdbcTemplate {

	private final String ispybUrl = "gda.px.contactinfo.ispyb.url";
	private final String ispybUser = "gda.px.contactinfo.ispyb.user";
	private final String ispybPass = "gda.px.contactinfo.ispyb.password";

	private JdbcTemplate template;

	public JdbcTemplate template() throws Exception {
		if (template == null) {
			final String url = LocalProperties.get(ispybUrl);
			final String username = LocalProperties.get(ispybUser);
			final String password = LocalProperties.get(ispybPass);

			MariaDbDataSource ds = new MariaDbDataSource();
			ds.setUrl(url);
			ds.setUserName(username);
			ds.setPassword(password);

			template = new JdbcTemplate(ds);
		}
		return template;
	}
}
