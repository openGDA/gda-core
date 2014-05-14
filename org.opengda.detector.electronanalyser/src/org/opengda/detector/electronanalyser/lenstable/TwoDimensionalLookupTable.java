package org.opengda.detector.electronanalyser.lenstable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.io.Files;

public class TwoDimensionalLookupTable {
	private static final Logger logger=LoggerFactory.getLogger(TwoDimensionalLookupTable.class);
	private Table<String, String, String> table=HashBasedTable.create();

	//read from table file, populate the table object
	public Table<String, String, String> createTable(String filename) {
		File file=new File(filename);
		return createTable(file);
	}
	public Table<String, String, String> createTable(File file) {
		try {
			List<String> readlines=Files.readLines(file, Charsets.UTF_8);
			Splitter splitter=Splitter.on("\t");
			readlines=removeCommentlines(readlines);

			//first row is column header - gives column keys
			List<String> passenergy = splitter.splitToList(readlines.get(0));
			// first column is row header - gives row keys
			for (int i =1; i<readlines.size(); i++) {
				List<String> energyranges = splitter.splitToList(readlines.get(i));
				for (int j=1; j<energyranges.size(); j++) {
//					System.out.println(String.format("%s\t%s\t%s",energyranges.get(0), passenergy.get(j), energyranges.get(j)) );
					table.put(energyranges.get(0), passenergy.get(j), energyranges.get(j));
				}
			}
		} catch (IOException e) {
			logger.error("Cannot read the lookup table : "+file.getAbsolutePath(),e);
			e.printStackTrace();
		}
		return table;
	}
	private List<String> removeCommentlines(List<String> readlines) {
		List<String> lines=Lists.newArrayList();
		for (String line : readlines) {
			if (!line.startsWith("#")) {
				lines.add(line);
			}
		}
		return lines;
	}
	
	public String getValue(String rowKey, String columnKey) {
		return table.get(rowKey, columnKey);
	}

	public Table<String, String, String> getTable() {
		return table;
	}

}
