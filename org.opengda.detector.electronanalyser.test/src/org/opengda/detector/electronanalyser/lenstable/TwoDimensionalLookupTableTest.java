/**
 *
 */
package org.opengda.detector.electronanalyser.lenstable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Table;

/**
 * @author fy65
 *
 */
public class TwoDimensionalLookupTableTest {
	TwoDimensionalLookupTable table;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		table= new TwoDimensionalLookupTable();
		table.createTable("./../../gda-diamond.git/configurations/i09-config/lookupTables/low_energy_table.txt");
		}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.opengda.detector.electronanalyser.lenstable.TwoDimensionalLookupTable#createTable(java.lang.String)}.
	 */
	@Test
	public void testCreateTable() {
		Table<String, String, String> table2 = table.getTable();
		assertTrue(!table2.isEmpty());
		assertTrue(table2.contains("Angular45", "50"));
		assertTrue(table2.containsValue("625-4397"));
		for (String rowkey : table2.rowKeySet()) {
			for (String colkey : table2.columnKeySet()) {
				System.out.println(String.format("%s\t%s\t%s",rowkey, colkey, table2.get(rowkey,  colkey)));
			}
		}

	}

	/**
	 * Test method for {@link org.opengda.detector.electronanalyser.lenstable.TwoDimensionalLookupTable#getValue(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetValue() {
		String expected="625-4397";
		assertArrayEquals(table.getValue("Angular45","500").getBytes(), (expected).getBytes());
	}

}
