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

import org.junit.Test;

import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.doe.RangeInfo;

public class DOEInfoTest {
	
	@Test
	public void testNested1() throws Throwable {

		final TestBean t = new TestBean();
		t.setI("1;3;1");
		t.setJ("1;3;1");
		t.setK("1;3;1");

	    final TestContainer c = new TestContainer();
	    c.setTestBean(t);
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
		System.out.println(RangeInfo.format(os));
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(r);
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(c);
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
	    
		final List<RangeInfo> os = DOEUtils.getInfo(cl);
		if (os.size()!=27) throw new Exception("Three parameters, each with three values should give 27 experiments!");
	    
	}


}
