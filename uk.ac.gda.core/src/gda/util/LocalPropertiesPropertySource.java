package gda.util;

import org.springframework.core.env.PropertySource;

import gda.configuration.properties.LocalProperties;

public class LocalPropertiesPropertySource extends PropertySource<LocalProperties> {

	public LocalPropertiesPropertySource() {
		super("LocalProperties");
	}

	@Override
	public String getProperty(String name) {
		return LocalProperties.get(name);
	}

}
