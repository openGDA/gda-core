/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for selection of a space group.
 */
public class BravaisLatticeSelector extends JButton implements MouseListener, ActionListener, IObservable {
	
	private static final Logger logger = LoggerFactory.getLogger(BravaisLatticeSelector.class);
	
	private static String unknown = "unknown";

	private String[] primitiveTriclinic = { "P1" };

	private String[] primitiveMonoclinic = { "P2", "P21" };

	private String[] centeredMonoclinic = { "C2" };

	private String[] primitiveOrthorhombic = { "P222", "P2221", "P21212", "P212121" };

	private String[] ccenteredOrthorhombic = { "C222", "C2221" };

	private String[] icenteredOrthorhombic = { "I222", "I212121" };

	private String[] fcenteredOrthorhombic = { "F222" };

	private String[] primitiveTetragonal = { "P4", "P41", "P42", "P43", "P422", "P4212", "P4122", "P41212", "P4222",
			"P42212", "P4322", "P43212" };

	private String[] icenteredTetragonal = { "I4", "I41", "I422", "I4122" };

	private String[] primitiveTrigonal = { "P3", "P31", "P32", "R3", "P312", "P321", "P3112", "P3121", "P3212", "P3221" };

	private String[] primitiveHexagonal = { "P6", "P61", "P65", "P62", "P64", "P63", "P622", "P6122", "P6522", "P6222",
			"P6422", "P6322" };

	private String[] primitiveRhombohedral = { "R3", "R32" };

	private String[] primitiveCubic = { "P23", "P213", "P432", "P4232", "P4332", "P4132" };

	private String[] icenteredCubic = { "I23", "I213", "I432", "I4132" };

	private String[] fcenteredCubic = { "F23", "F432", "F4132" };

	JPopupMenu popup = new JPopupMenu();

	JMenuItem mi;

	JMenu primitiveTriclinicMenu = new JMenu("primitive triclinic");

	JMenu primitiveMonoclinicMenu = new JMenu("primitive monoclinic");

	JMenu centeredMonoclinicMenu = new JMenu("centered monoclinic");

	JMenu primitiveOrthorhombicMenu = new JMenu("primitive orthorhombic");

	JMenu ccenteredOrthorhombicMenu = new JMenu("C centered orthorhombic");

	JMenu icenteredOrthorhombicMenu = new JMenu("I centered orthorhombic");

	JMenu fcenteredOrthorhombicMenu = new JMenu("F centered orthorhombic");

	JMenu primitiveTetragonalMenu = new JMenu("primitive tetragonal");

	JMenu icenteredTetragonalMenu = new JMenu("I centered tetragonal");

	JMenu primitiveTrigonalMenu = new JMenu("primitive trigonal");

	JMenu primitiveHexagonalMenu = new JMenu("primitive hexagonal");

	JMenu primitiveRhombohedralMenu = new JMenu("primitive rhombohedral");

	JMenu primitiveCubicMenu = new JMenu("primitive cubic");

	JMenu icenteredCubicMenu = new JMenu("I centerd cubic");

	JMenu fcenteredCubicMenu = new JMenu("F centered cubic");

	Dimension size;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Is a popup menu which allows selection of SpaceGroup
	 */
	public BravaisLatticeSelector() {
		super(unknown);
		size = getPreferredSize();
		addMouseListener(this);

		mi = new JMenuItem(unknown);
		mi.setActionCommand(unknown);
		mi.addActionListener(this);
		popup.add(mi);
		makeMenu(primitiveTriclinic, primitiveTriclinicMenu);
		makeMenu(primitiveMonoclinic, primitiveMonoclinicMenu);
		makeMenu(centeredMonoclinic, centeredMonoclinicMenu);
		makeMenu(primitiveOrthorhombic, primitiveOrthorhombicMenu);
		makeMenu(ccenteredOrthorhombic, ccenteredOrthorhombicMenu);
		makeMenu(icenteredOrthorhombic, icenteredOrthorhombicMenu);
		makeMenu(fcenteredOrthorhombic, fcenteredOrthorhombicMenu);
		makeMenu(primitiveTetragonal, primitiveTetragonalMenu);
		makeMenu(icenteredTetragonal, icenteredTetragonalMenu);
		makeMenu(primitiveTrigonal, primitiveTrigonalMenu);
		makeMenu(primitiveHexagonal, primitiveHexagonalMenu);
		makeMenu(primitiveRhombohedral, primitiveRhombohedralMenu);
		makeMenu(primitiveCubic, primitiveCubicMenu);
		makeMenu(icenteredCubic, icenteredCubicMenu);
		makeMenu(fcenteredCubic, fcenteredCubicMenu);
	}

	/**
	 * Make set of MenuItems and add to the JMenu menu then adds the JMenu to JPopupMenu popup
	 * 
	 * @param items
	 *            String names of MenuItems to be added
	 * @param menu
	 *            menu to add MenuItems to
	 */
	private void makeMenu(String[] items, JMenu menu) {
		for (int i = 0; i < items.length; i++) {
			mi = new JMenuItem(items[i]);
			mi.setActionCommand(items[i]);
			mi.addActionListener(this);
			menu.add(mi);
		}
		popup.add(menu);
	}

	// MouseListener

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
		logger.debug("DEBUG: mousePressed");
		popup.show(me.getComponent(), me.getX(), me.getY());
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	// ActionListener

	@Override
	public void actionPerformed(ActionEvent ae) {
		String selection = ae.getActionCommand();
		logger.debug("BravaisLatticeSelector " + this.getName() + " actionPerformed. selection = " + selection);
		setText(selection);
		setPreferredSize(size);
		JMenuItem jmi = (JMenuItem) ae.getSource();
		if (!jmi.getActionCommand().equals(unknown))
			observableComponent.notifyIObservers(this, jmi.getActionCommand());
		else
			observableComponent.notifyIObservers(this, null);
	}

	// IObservable

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}