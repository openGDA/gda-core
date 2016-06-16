package gda.util;

import org.springframework.core.env.PropertySource;

import gda.configuration.properties.LocalProperties;

public class LocalPropertiesPropertySource extends PropertySource<LocalProperties> {

	public LocalPropertiesPropertySource() {
		super("LocalProperties", new LocalProperties());
	}

	@Override
	public String getProperty(String name) {
//		System.out.println("getting property " + name);
		final String value = LocalProperties.get(name);
//		System.out.println("returning value " + value);
		return value;
	}

}
