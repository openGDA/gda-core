/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.doe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import uk.ac.gda.doe.DOEUtils;

/**
 * Unit test for DOE algorithm which is recursive and expands out
 * simulation sets using weightings defined in annotations.
 * 
 * If you have a case that does not expand correctly, add it here.
 * Probably a fix to DOEUtils.readAnnoations(...) will be what you
 * then need. 
 */
public class DOETest {
	@Test
	public void testFieldWeightings() throws Throwable {
		final TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		
		final List<?> os = DOEUtils.expand(t);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
		
		// Test order as expected.
		final List<Object> compare = new ArrayList<Object>(27);
		for (int k = 1; k < 4; k++) {
			for (int j = 1; j < 4; j++) {
				for (int i = 1; i < 4; i++) {
					compare.add(new TestBean(i+"" ,j+".0",k+".0"));
				}
			}
		}
		
		if (!os.equals(compare)) throw new Exception("Expansion did not process weightings correctly!");
		
		// Test order another way around.
		compare.clear();
		for (int i = 1; i < 4; i++) {
			for (int j = 1; j < 4; j++) {
				for (int k = 1; k < 4; k++) {
					compare.add(new TestBean(i+"",j+".0",k+".0"));
				}
			}
		}
		
		if (os.equals(compare)) throw new Exception("Expansion did not process weightings correctly!");
	}
	
	@Test
	public void testNested1() throws Throwable {

		final TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");

	    final TestContainer c = new TestContainer();
	    c.setTestBean(t);
	    
		final List<?> os = DOEUtils.expand(c);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNested2() throws Throwable {

		final TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");

	    final TestContainer c = new TestContainer();
	    c.setTestBean(t);
	    
	    final TestContainerContainer r = new TestContainerContainer();
	    r.setTestContainer(c);
	    
		final List<?> os = DOEUtils.expand(r);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNestedList1() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);

		final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	
	@Test
	public void testNestedList2() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);
		
		t = new TestBean();
		t.setI("4.0");
		t.setJ("4.0");
		t.setK("4.0");
		beans.add(t);

	    final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=2) throw new Exception("There should only be 2 items in the TestList object.");
		}
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNestedList2a() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);
		
		t = new TestBean();
		t.setI("4.0");
		t.setJ("4.0");
		t.setK("4.0");
		beans.add(t);

		t = new TestBean();
		t.setI("1.0");
		t.setJ("2.0");
		t.setK("3.0");
		beans.add(t);

		final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=3) throw new Exception("There should only be 2 items in the TestList object.");
		}
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNestedList2b() throws Throwable {
		
		final TestList c = new TestList();
		c.add(new TestBean(0));
		c.add(new TestBean(1));
		
		TestBean t = new TestBean();
		t.setK("100;200;1");
		c.add(t);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=3) throw new Exception("There should only be 3 items in the TestList object.");
		}
		if (os.size()!=101) throw new Exception("Should give 101 experiments!");
	    
	}

	
	@Test
	public void testNestedList2c() throws Throwable {
		
		final TestListOneAnnotation c = new TestListOneAnnotation();
		c.add(new TestDimsData(0));
		c.add(new TestDimsData(1));
		
		TestDimsData t = new TestDimsData(2);
		t.setSliceRange("100;200;1");
		c.add(t);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestListOneAnnotation tl = (TestListOneAnnotation)object;
			if (tl.getTestBeans().size()!=3) throw new Exception("There should only be 3 items in the TestList object.");
		}
		if (os.size()!=101) throw new Exception("Should give 101 experiments!");
	    
	}

	@Test
	public void testNestedList2d() throws Throwable {
		
		final TestDimsDataList c = new TestDimsDataList();
		c.add(new TestDimsData(0));
		c.add(new TestDimsData(1));
		
		TestDimsData t = new TestDimsData(2);
		t.setSliceRange("100;200;1");
		c.add(t);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestDimsDataList tl = (TestDimsDataList)object;
			if (tl.size()!=3) throw new Exception("There should only be 3 items in the TestList object.");
		}
		if (os.size()!=101) throw new Exception("Should give 101 experiments!");
	    
	}

	@Test
	public void testNestedList3() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);
		
		t = new TestBean();
		t.setI("4.0;4.5;1.0"); // Only one value.
		t.setJ("4.0;4.5;1.0"); // Only one value.
		t.setK("4.0;4.5;1.0"); // Only one value.
		beans.add(t);

	    final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=2) throw new Exception("There should only be 2 items in the TestList object.");
			
			for (TestBean tb : tl.getTestBeans()) {
				if (DOEUtils.isDOE(tb.getI())) {
					throw new Exception("TestBean found not to have expanded range! "+tb);
				}
				if (DOEUtils.isDOE(tb.getJ())) {
					throw new Exception("TestBean found not to have expanded range! "+tb);
				}
				if (DOEUtils.isDOE(tb.getK())) {
					throw new Exception("TestBean found not to have expanded range! "+tb);
				}
			}
		}
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNestedList4() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);
		
		t = new TestBean();
		t.setI("4.0;5;1.0");
		t.setJ("4.0;5;1.0"); 
		t.setK("4.0;5;1.0");
		beans.add(t);

	    final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=2) throw new Exception("There should only be 2 items in the TestList object.");
		}
		if (os.size()!=216) throw new Exception("Three parameters, each with three values should give 216 experiments!");
	    
	}

	@Test
	public void testNestedList5() throws Throwable {

		final List<TestBean> beans = new ArrayList<TestBean>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");
		beans.add(t);
		
		t = new TestBean();
		t.setI("4.0");
		t.setJ("4.0");
		t.setK("4.0");
		beans.add(t);

		t = new TestBean();
		t.setI("5.0");
		t.setJ("5.0");
		t.setK("5.0");
		beans.add(t);

		final TestList c = new TestList();
	    c.setTestBeans(beans);
	    
		final List<?> os = DOEUtils.expand(c);
		for (Object object : os) {
			final TestList tl = (TestList)object;
			if (tl.getTestBeans().size()!=3) throw new Exception("There should only be 3 items in the TestList object.");
		}
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@Test
	public void testNestedListContainer() throws Throwable {

		final List<TestContainer> tcl = new ArrayList<TestContainer>(2);
		TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");

	    TestContainer c = new TestContainer();
	    c.setTestBean(t);
	    tcl.add(c);
	    
	    TestContainerList cl = new TestContainerList();
	    cl.setTestContainers(tcl);
	    
		final List<?> os = DOEUtils.expand(cl);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testInt() throws Throwable {

		final TestBean t = new TestBean();
		t.setI("1;3;1"); // Is an int
		t.setJ("1;3;1");
		t.setK("1;3;1");
	    
		final List<TestBean> os = (List<TestBean>)DOEUtils.expand(t);
		final TestBean b = os.get(0);
	    
		if (b.getI().equals("1.0")) throw new Exception("i is marked as an integer and it should be '1' not '1.0' for the first bean.");
	}

	
	@Test
	public void testPatterns() throws Throwable {
		
		Pattern pattern = DOEUtils.getListPattern(5, null);
		if (pattern.matcher("3.22").matches()) throw new Exception();
		if (!pattern.matcher("3.22,4.33").matches()) throw new Exception();
		if (!pattern.matcher("295.0, 300.12").matches()) throw new Exception();
		if(!pattern.matcher("3.22,4.33,5.44").matches()) throw new Exception();
		if(!pattern.matcher("3,4.3,5.4454").matches()) throw new Exception();
		if(!pattern.matcher("3, 4.3, 5.4454, 4").matches()) throw new Exception();
  	
		pattern = DOEUtils.getListPattern(5, "fred");
		if(pattern.matcher("3.22 fred").matches()) throw new Exception();
		if(!pattern.matcher("3.22,4.33 fred").matches()) throw new Exception();
		if(!pattern.matcher("3.22,4.33,5.44 fred").matches()) throw new Exception();
		if(!pattern.matcher("3,4.3,5.4454 fred").matches()) throw new Exception();
		if(!pattern.matcher("3, 4.3, 5.4454, 4 fred").matches()) throw new Exception(); 
		if(!pattern.matcher("3.22,4.33 fred").matches()) throw new Exception();
		if (!DOEUtils.removeUnit("3.22,4.33 fred", "fred").trim().equals("3.22,4.33")) throw new Exception();

		pattern = DOEUtils.getRangePattern(5, "fred");
		if(pattern.matcher("3.22 fred").matches()) throw new Exception();
		if(pattern.matcher("3.22,4.33 fred").matches()) throw new Exception();
		if(pattern.matcher("3.22,4.33,5.44 fred").matches()) throw new Exception();
		if(!pattern.matcher("4.3;5.4454;1 fred").matches())throw new Exception();
		if(!pattern.matcher("3; 4.3; 0.1 fred").matches())throw new Exception();

	}
}
