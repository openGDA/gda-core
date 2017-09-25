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

package gda.rcp.views;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content
 * (like Task List, for example).
 */

public class ViewContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private TreeParent invisibleRoot;
	private IViewSite viewSite;

	public ViewContentProvider(IViewSite viewSite) {
		super();
		this.viewSite = viewSite;
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent.equals(viewSite)) {
			if (invisibleRoot == null)
				initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}



	@Override
	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject) child).getParent();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof TreeParent) {
			return ((TreeParent) parent).getChildren();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent)
			return ((TreeParent) parent).hasChildren();
		return false;
	}

	/*
	 * We will set up a dummy model to initialize tree heararchy. In a real
	 * code, you will connect to a real model and expose its hierarchy.
	 */
	private void initialize() {
		TreeObject to1 = new TreeObject("Leaf 1");
		TreeObject to2 = new TreeObject("Leaf 2");
		TreeObject to3 = new TreeObject("Leaf 3");
		TreeParent p1 = new TreeParent("Parent 1");
		p1.addChild(to1);
		p1.addChild(to2);
		p1.addChild(to3);

		List<String> interfaces = Finder.getInstance().listAllInterfaces();
		TreeParent[] parents = new TreeParent[interfaces.size()];
		int i = 0;
		for (String intfc : interfaces) {
			parents[i] = new TreeParent(intfc);
			Finder.getInstance().listAllNames(intfc);
			List<Findable> objects2 = Finder.getInstance().listAllObjects(intfc);
			TreeObject[] objects = new TreeObject[objects2.size()];
			int j = 0;
			for (Iterator<Findable> iterator = objects2.iterator(); iterator.hasNext();) {
				Findable findable = iterator.next();
				if (findable instanceof Scannable) {
					Scannable scannable = (Scannable) findable;
					objects[j] = new ScannableTreeObject(scannable);
				} else {
					objects[j] = new TreeObject(findable.getName());
				}
				parents[i].addChild(objects[j]);
				j++;
			}
			i++;
		}

		// ArrayList<String> names = Finder.getInstance().listAllNames(null);
		// TreeObject [] objects = new TreeObject[names.size()];
		// int i = 0;
		// for (Iterator iterator = names.iterator(); iterator.hasNext();) {
		// String string = (String) iterator.next();
		// objects[i] = new TreeObject(string);
		// p1.addChild(objects[i]);
		// i++;
		// }

		TreeObject to4 = new TreeObject("Leaf 4");
		TreeParent p2 = new TreeParent("Parent 2");
		p2.addChild(to4);

		TreeParent root = new TreeParent("All Objects");
		// root.addChild(p1);
		// root.addChild(p2);
		for (int j = 0; j < parents.length; j++) {
			root.addChild(parents[j]);
		}

		invisibleRoot = new TreeParent("");
		invisibleRoot.addChild(root);
	}

}
