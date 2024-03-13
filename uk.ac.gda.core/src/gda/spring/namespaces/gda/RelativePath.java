/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.spring.namespaces.gda;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

/**
 * Bean parser to allow paths relative to spring xml files to be used as properties.
 * </br>
 * The path that should be made relative to current file should be wrapped in the custom tag
 * associated with this parser, eg for `gda:relative-path`;
 * <pre>
 * {@code
 * <property name="path">
 *     <gda:relative-path path="../../scripts" />
 * </property>
 * }
 * </pre>
 */
public class RelativePath implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(RelativePath.class);

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String path = element.getAttribute("path");
		if (path == null) {
			logger.warn("No 'path' attribute given for relative path");
			return null;
		}

		var resource = parserContext.getReaderContext().getResource();
		try {
			Resource relative = resource.createRelative(path);
			var filePath = relative.getFile().toString();
			var bean = new GenericBeanDefinition();
			bean.setBeanClass(String.class);
			var args = new ConstructorArgumentValues();
			args.addGenericArgumentValue(filePath);
			bean.setConstructorArgumentValues(args);
			return bean;
		} catch (IOException e) {
			logger.error("Couldn't resolve relative path {}", path, e);
		}
		return null;
	}

}
