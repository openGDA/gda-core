package org.opengda.detector.electronanalyser.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameUtil {

	private static final Logger logger=LoggerFactory.getLogger(FilenameUtil.class);
	private static String prefix;

	public FilenameUtil(String filepath) {
		// TODO Auto-generated constructor stub
	}

	public static String convertSeparator(String filepath) {
		if (OsUtil.isUnix()) {
			filepath = FilenameUtils.separatorsToUnix(filepath);
		}
		if (OsUtil.isWindows()) {
			filepath = FilenameUtils.separatorsToWindows(filepath);
			if (getPrefix()==null) {
				logger.error("file path prefix is not set explicitly. set to default C:\\");
				setPrefix("C:\\");
			}
			filepath = getPrefix() + filepath;
		}
		return filepath;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		FilenameUtil.prefix = prefix;
	}

}
