/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.excalibur.config.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.gda.excalibur.config.model.AnperModel;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Anper Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getPreamp <em>Preamp</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getIkrum <em>Ikrum</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getShaper <em>Shaper</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getDisc <em>Disc</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getDiscls <em>Discls</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThresholdn <em>Thresholdn</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getDacPixel <em>Dac Pixel</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getDelay <em>Delay</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getTpBufferIn <em>Tp Buffer In</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getTpBufferOut <em>Tp Buffer Out</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getRpz <em>Rpz</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getGnd <em>Gnd</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getTpref <em>Tpref</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getFbk <em>Fbk</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getCas <em>Cas</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getTprefA <em>Tpref A</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getTprefB <em>Tpref B</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold0 <em>Threshold0</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold1 <em>Threshold1</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold2 <em>Threshold2</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold3 <em>Threshold3</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold4 <em>Threshold4</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold5 <em>Threshold5</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold6 <em>Threshold6</em>}</li>
 *   <li>{@link uk.ac.gda.excalibur.config.model.impl.AnperModelImpl#getThreshold7 <em>Threshold7</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AnperModelImpl extends EObjectImpl implements AnperModel {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * The default value of the '{@link #getPreamp() <em>Preamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPreamp()
	 * @generated
	 * @ordered
	 */
	protected static final int PREAMP_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPreamp() <em>Preamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPreamp()
	 * @generated
	 * @ordered
	 */
	protected int preamp = PREAMP_EDEFAULT;

	/**
	 * This is true if the Preamp attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean preampESet;

	/**
	 * The default value of the '{@link #getIkrum() <em>Ikrum</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIkrum()
	 * @generated
	 * @ordered
	 */
	protected static final int IKRUM_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getIkrum() <em>Ikrum</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIkrum()
	 * @generated
	 * @ordered
	 */
	protected int ikrum = IKRUM_EDEFAULT;

	/**
	 * This is true if the Ikrum attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean ikrumESet;

	/**
	 * The default value of the '{@link #getShaper() <em>Shaper</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShaper()
	 * @generated
	 * @ordered
	 */
	protected static final int SHAPER_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getShaper() <em>Shaper</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShaper()
	 * @generated
	 * @ordered
	 */
	protected int shaper = SHAPER_EDEFAULT;

	/**
	 * This is true if the Shaper attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean shaperESet;

	/**
	 * The default value of the '{@link #getDisc() <em>Disc</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDisc()
	 * @generated
	 * @ordered
	 */
	protected static final int DISC_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDisc() <em>Disc</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDisc()
	 * @generated
	 * @ordered
	 */
	protected int disc = DISC_EDEFAULT;

	/**
	 * This is true if the Disc attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean discESet;

	/**
	 * The default value of the '{@link #getDiscls() <em>Discls</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDiscls()
	 * @generated
	 * @ordered
	 */
	protected static final int DISCLS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDiscls() <em>Discls</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDiscls()
	 * @generated
	 * @ordered
	 */
	protected int discls = DISCLS_EDEFAULT;

	/**
	 * This is true if the Discls attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean disclsESet;

	/**
	 * The default value of the '{@link #getThresholdn() <em>Thresholdn</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdn()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLDN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThresholdn() <em>Thresholdn</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThresholdn()
	 * @generated
	 * @ordered
	 */
	protected int thresholdn = THRESHOLDN_EDEFAULT;

	/**
	 * This is true if the Thresholdn attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean thresholdnESet;

	/**
	 * The default value of the '{@link #getDacPixel() <em>Dac Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacPixel()
	 * @generated
	 * @ordered
	 */
	protected static final int DAC_PIXEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDacPixel() <em>Dac Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDacPixel()
	 * @generated
	 * @ordered
	 */
	protected int dacPixel = DAC_PIXEL_EDEFAULT;

	/**
	 * This is true if the Dac Pixel attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean dacPixelESet;

	/**
	 * The default value of the '{@link #getDelay() <em>Delay</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDelay()
	 * @generated
	 * @ordered
	 */
	protected static final int DELAY_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDelay() <em>Delay</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDelay()
	 * @generated
	 * @ordered
	 */
	protected int delay = DELAY_EDEFAULT;

	/**
	 * This is true if the Delay attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean delayESet;

	/**
	 * The default value of the '{@link #getTpBufferIn() <em>Tp Buffer In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpBufferIn()
	 * @generated
	 * @ordered
	 */
	protected static final int TP_BUFFER_IN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTpBufferIn() <em>Tp Buffer In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpBufferIn()
	 * @generated
	 * @ordered
	 */
	protected int tpBufferIn = TP_BUFFER_IN_EDEFAULT;

	/**
	 * This is true if the Tp Buffer In attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tpBufferInESet;

	/**
	 * The default value of the '{@link #getTpBufferOut() <em>Tp Buffer Out</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpBufferOut()
	 * @generated
	 * @ordered
	 */
	protected static final int TP_BUFFER_OUT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTpBufferOut() <em>Tp Buffer Out</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpBufferOut()
	 * @generated
	 * @ordered
	 */
	protected int tpBufferOut = TP_BUFFER_OUT_EDEFAULT;

	/**
	 * This is true if the Tp Buffer Out attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tpBufferOutESet;

	/**
	 * The default value of the '{@link #getRpz() <em>Rpz</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRpz()
	 * @generated
	 * @ordered
	 */
	protected static final int RPZ_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getRpz() <em>Rpz</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRpz()
	 * @generated
	 * @ordered
	 */
	protected int rpz = RPZ_EDEFAULT;

	/**
	 * This is true if the Rpz attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean rpzESet;

	/**
	 * The default value of the '{@link #getGnd() <em>Gnd</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGnd()
	 * @generated
	 * @ordered
	 */
	protected static final int GND_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGnd() <em>Gnd</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGnd()
	 * @generated
	 * @ordered
	 */
	protected int gnd = GND_EDEFAULT;

	/**
	 * This is true if the Gnd attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean gndESet;

	/**
	 * The default value of the '{@link #getTpref() <em>Tpref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpref()
	 * @generated
	 * @ordered
	 */
	protected static final int TPREF_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTpref() <em>Tpref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTpref()
	 * @generated
	 * @ordered
	 */
	protected int tpref = TPREF_EDEFAULT;

	/**
	 * This is true if the Tpref attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tprefESet;

	/**
	 * The default value of the '{@link #getFbk() <em>Fbk</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFbk()
	 * @generated
	 * @ordered
	 */
	protected static final int FBK_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFbk() <em>Fbk</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFbk()
	 * @generated
	 * @ordered
	 */
	protected int fbk = FBK_EDEFAULT;

	/**
	 * This is true if the Fbk attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean fbkESet;

	/**
	 * The default value of the '{@link #getCas() <em>Cas</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCas()
	 * @generated
	 * @ordered
	 */
	protected static final int CAS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCas() <em>Cas</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCas()
	 * @generated
	 * @ordered
	 */
	protected int cas = CAS_EDEFAULT;

	/**
	 * This is true if the Cas attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean casESet;

	/**
	 * The default value of the '{@link #getTprefA() <em>Tpref A</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTprefA()
	 * @generated
	 * @ordered
	 */
	protected static final int TPREF_A_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTprefA() <em>Tpref A</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTprefA()
	 * @generated
	 * @ordered
	 */
	protected int tprefA = TPREF_A_EDEFAULT;

	/**
	 * This is true if the Tpref A attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tprefAESet;

	/**
	 * The default value of the '{@link #getTprefB() <em>Tpref B</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTprefB()
	 * @generated
	 * @ordered
	 */
	protected static final int TPREF_B_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTprefB() <em>Tpref B</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTprefB()
	 * @generated
	 * @ordered
	 */
	protected int tprefB = TPREF_B_EDEFAULT;

	/**
	 * This is true if the Tpref B attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean tprefBESet;

	/**
	 * The default value of the '{@link #getThreshold0() <em>Threshold0</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold0()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD0_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold0() <em>Threshold0</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold0()
	 * @generated
	 * @ordered
	 */
	protected int threshold0 = THRESHOLD0_EDEFAULT;

	/**
	 * This is true if the Threshold0 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold0ESet;

	/**
	 * The default value of the '{@link #getThreshold1() <em>Threshold1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold1()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD1_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold1() <em>Threshold1</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold1()
	 * @generated
	 * @ordered
	 */
	protected int threshold1 = THRESHOLD1_EDEFAULT;

	/**
	 * This is true if the Threshold1 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold1ESet;

	/**
	 * The default value of the '{@link #getThreshold2() <em>Threshold2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold2()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD2_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold2() <em>Threshold2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold2()
	 * @generated
	 * @ordered
	 */
	protected int threshold2 = THRESHOLD2_EDEFAULT;

	/**
	 * This is true if the Threshold2 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold2ESet;

	/**
	 * The default value of the '{@link #getThreshold3() <em>Threshold3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold3()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD3_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold3() <em>Threshold3</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold3()
	 * @generated
	 * @ordered
	 */
	protected int threshold3 = THRESHOLD3_EDEFAULT;

	/**
	 * This is true if the Threshold3 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold3ESet;

	/**
	 * The default value of the '{@link #getThreshold4() <em>Threshold4</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold4()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD4_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold4() <em>Threshold4</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold4()
	 * @generated
	 * @ordered
	 */
	protected int threshold4 = THRESHOLD4_EDEFAULT;

	/**
	 * This is true if the Threshold4 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold4ESet;

	/**
	 * The default value of the '{@link #getThreshold5() <em>Threshold5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold5()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD5_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold5() <em>Threshold5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold5()
	 * @generated
	 * @ordered
	 */
	protected int threshold5 = THRESHOLD5_EDEFAULT;

	/**
	 * This is true if the Threshold5 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold5ESet;

	/**
	 * The default value of the '{@link #getThreshold6() <em>Threshold6</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold6()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD6_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold6() <em>Threshold6</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold6()
	 * @generated
	 * @ordered
	 */
	protected int threshold6 = THRESHOLD6_EDEFAULT;

	/**
	 * This is true if the Threshold6 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold6ESet;

	/**
	 * The default value of the '{@link #getThreshold7() <em>Threshold7</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold7()
	 * @generated
	 * @ordered
	 */
	protected static final int THRESHOLD7_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getThreshold7() <em>Threshold7</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold7()
	 * @generated
	 * @ordered
	 */
	protected int threshold7 = THRESHOLD7_EDEFAULT;

	/**
	 * This is true if the Threshold7 attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean threshold7ESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AnperModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExcaliburConfigPackage.Literals.ANPER_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getPreamp() {
		return preamp;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPreamp(int newPreamp) {
		int oldPreamp = preamp;
		preamp = newPreamp;
		boolean oldPreampESet = preampESet;
		preampESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__PREAMP, oldPreamp, preamp, !oldPreampESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetPreamp() {
		int oldPreamp = preamp;
		boolean oldPreampESet = preampESet;
		preamp = PREAMP_EDEFAULT;
		preampESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__PREAMP, oldPreamp, PREAMP_EDEFAULT, oldPreampESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetPreamp() {
		return preampESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getIkrum() {
		return ikrum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIkrum(int newIkrum) {
		int oldIkrum = ikrum;
		ikrum = newIkrum;
		boolean oldIkrumESet = ikrumESet;
		ikrumESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__IKRUM, oldIkrum, ikrum, !oldIkrumESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetIkrum() {
		int oldIkrum = ikrum;
		boolean oldIkrumESet = ikrumESet;
		ikrum = IKRUM_EDEFAULT;
		ikrumESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__IKRUM, oldIkrum, IKRUM_EDEFAULT, oldIkrumESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetIkrum() {
		return ikrumESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getShaper() {
		return shaper;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShaper(int newShaper) {
		int oldShaper = shaper;
		shaper = newShaper;
		boolean oldShaperESet = shaperESet;
		shaperESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__SHAPER, oldShaper, shaper, !oldShaperESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetShaper() {
		int oldShaper = shaper;
		boolean oldShaperESet = shaperESet;
		shaper = SHAPER_EDEFAULT;
		shaperESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__SHAPER, oldShaper, SHAPER_EDEFAULT, oldShaperESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetShaper() {
		return shaperESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDisc() {
		return disc;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDisc(int newDisc) {
		int oldDisc = disc;
		disc = newDisc;
		boolean oldDiscESet = discESet;
		discESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__DISC, oldDisc, disc, !oldDiscESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetDisc() {
		int oldDisc = disc;
		boolean oldDiscESet = discESet;
		disc = DISC_EDEFAULT;
		discESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__DISC, oldDisc, DISC_EDEFAULT, oldDiscESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetDisc() {
		return discESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDiscls() {
		return discls;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDiscls(int newDiscls) {
		int oldDiscls = discls;
		discls = newDiscls;
		boolean oldDisclsESet = disclsESet;
		disclsESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__DISCLS, oldDiscls, discls, !oldDisclsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetDiscls() {
		int oldDiscls = discls;
		boolean oldDisclsESet = disclsESet;
		discls = DISCLS_EDEFAULT;
		disclsESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__DISCLS, oldDiscls, DISCLS_EDEFAULT, oldDisclsESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetDiscls() {
		return disclsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThresholdn() {
		return thresholdn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThresholdn(int newThresholdn) {
		int oldThresholdn = thresholdn;
		thresholdn = newThresholdn;
		boolean oldThresholdnESet = thresholdnESet;
		thresholdnESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN, oldThresholdn, thresholdn, !oldThresholdnESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThresholdn() {
		int oldThresholdn = thresholdn;
		boolean oldThresholdnESet = thresholdnESet;
		thresholdn = THRESHOLDN_EDEFAULT;
		thresholdnESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN, oldThresholdn, THRESHOLDN_EDEFAULT, oldThresholdnESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThresholdn() {
		return thresholdnESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDacPixel() {
		return dacPixel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDacPixel(int newDacPixel) {
		int oldDacPixel = dacPixel;
		dacPixel = newDacPixel;
		boolean oldDacPixelESet = dacPixelESet;
		dacPixelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL, oldDacPixel, dacPixel, !oldDacPixelESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetDacPixel() {
		int oldDacPixel = dacPixel;
		boolean oldDacPixelESet = dacPixelESet;
		dacPixel = DAC_PIXEL_EDEFAULT;
		dacPixelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL, oldDacPixel, DAC_PIXEL_EDEFAULT, oldDacPixelESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetDacPixel() {
		return dacPixelESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDelay(int newDelay) {
		int oldDelay = delay;
		delay = newDelay;
		boolean oldDelayESet = delayESet;
		delayESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__DELAY, oldDelay, delay, !oldDelayESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetDelay() {
		int oldDelay = delay;
		boolean oldDelayESet = delayESet;
		delay = DELAY_EDEFAULT;
		delayESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__DELAY, oldDelay, DELAY_EDEFAULT, oldDelayESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetDelay() {
		return delayESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTpBufferIn() {
		return tpBufferIn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTpBufferIn(int newTpBufferIn) {
		int oldTpBufferIn = tpBufferIn;
		tpBufferIn = newTpBufferIn;
		boolean oldTpBufferInESet = tpBufferInESet;
		tpBufferInESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN, oldTpBufferIn, tpBufferIn, !oldTpBufferInESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTpBufferIn() {
		int oldTpBufferIn = tpBufferIn;
		boolean oldTpBufferInESet = tpBufferInESet;
		tpBufferIn = TP_BUFFER_IN_EDEFAULT;
		tpBufferInESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN, oldTpBufferIn, TP_BUFFER_IN_EDEFAULT, oldTpBufferInESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTpBufferIn() {
		return tpBufferInESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTpBufferOut() {
		return tpBufferOut;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTpBufferOut(int newTpBufferOut) {
		int oldTpBufferOut = tpBufferOut;
		tpBufferOut = newTpBufferOut;
		boolean oldTpBufferOutESet = tpBufferOutESet;
		tpBufferOutESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT, oldTpBufferOut, tpBufferOut, !oldTpBufferOutESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTpBufferOut() {
		int oldTpBufferOut = tpBufferOut;
		boolean oldTpBufferOutESet = tpBufferOutESet;
		tpBufferOut = TP_BUFFER_OUT_EDEFAULT;
		tpBufferOutESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT, oldTpBufferOut, TP_BUFFER_OUT_EDEFAULT, oldTpBufferOutESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTpBufferOut() {
		return tpBufferOutESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getRpz() {
		return rpz;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRpz(int newRpz) {
		int oldRpz = rpz;
		rpz = newRpz;
		boolean oldRpzESet = rpzESet;
		rpzESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__RPZ, oldRpz, rpz, !oldRpzESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetRpz() {
		int oldRpz = rpz;
		boolean oldRpzESet = rpzESet;
		rpz = RPZ_EDEFAULT;
		rpzESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__RPZ, oldRpz, RPZ_EDEFAULT, oldRpzESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetRpz() {
		return rpzESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGnd() {
		return gnd;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGnd(int newGnd) {
		int oldGnd = gnd;
		gnd = newGnd;
		boolean oldGndESet = gndESet;
		gndESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__GND, oldGnd, gnd, !oldGndESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetGnd() {
		int oldGnd = gnd;
		boolean oldGndESet = gndESet;
		gnd = GND_EDEFAULT;
		gndESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__GND, oldGnd, GND_EDEFAULT, oldGndESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetGnd() {
		return gndESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTpref() {
		return tpref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTpref(int newTpref) {
		int oldTpref = tpref;
		tpref = newTpref;
		boolean oldTprefESet = tprefESet;
		tprefESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__TPREF, oldTpref, tpref, !oldTprefESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTpref() {
		int oldTpref = tpref;
		boolean oldTprefESet = tprefESet;
		tpref = TPREF_EDEFAULT;
		tprefESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__TPREF, oldTpref, TPREF_EDEFAULT, oldTprefESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTpref() {
		return tprefESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getFbk() {
		return fbk;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFbk(int newFbk) {
		int oldFbk = fbk;
		fbk = newFbk;
		boolean oldFbkESet = fbkESet;
		fbkESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__FBK, oldFbk, fbk, !oldFbkESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetFbk() {
		int oldFbk = fbk;
		boolean oldFbkESet = fbkESet;
		fbk = FBK_EDEFAULT;
		fbkESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__FBK, oldFbk, FBK_EDEFAULT, oldFbkESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetFbk() {
		return fbkESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCas() {
		return cas;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCas(int newCas) {
		int oldCas = cas;
		cas = newCas;
		boolean oldCasESet = casESet;
		casESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__CAS, oldCas, cas, !oldCasESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetCas() {
		int oldCas = cas;
		boolean oldCasESet = casESet;
		cas = CAS_EDEFAULT;
		casESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__CAS, oldCas, CAS_EDEFAULT, oldCasESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetCas() {
		return casESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTprefA() {
		return tprefA;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTprefA(int newTprefA) {
		int oldTprefA = tprefA;
		tprefA = newTprefA;
		boolean oldTprefAESet = tprefAESet;
		tprefAESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__TPREF_A, oldTprefA, tprefA, !oldTprefAESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTprefA() {
		int oldTprefA = tprefA;
		boolean oldTprefAESet = tprefAESet;
		tprefA = TPREF_A_EDEFAULT;
		tprefAESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__TPREF_A, oldTprefA, TPREF_A_EDEFAULT, oldTprefAESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTprefA() {
		return tprefAESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTprefB() {
		return tprefB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTprefB(int newTprefB) {
		int oldTprefB = tprefB;
		tprefB = newTprefB;
		boolean oldTprefBESet = tprefBESet;
		tprefBESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__TPREF_B, oldTprefB, tprefB, !oldTprefBESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetTprefB() {
		int oldTprefB = tprefB;
		boolean oldTprefBESet = tprefBESet;
		tprefB = TPREF_B_EDEFAULT;
		tprefBESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__TPREF_B, oldTprefB, TPREF_B_EDEFAULT, oldTprefBESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetTprefB() {
		return tprefBESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold0() {
		return threshold0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold0(int newThreshold0) {
		int oldThreshold0 = threshold0;
		threshold0 = newThreshold0;
		boolean oldThreshold0ESet = threshold0ESet;
		threshold0ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0, oldThreshold0, threshold0, !oldThreshold0ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold0() {
		int oldThreshold0 = threshold0;
		boolean oldThreshold0ESet = threshold0ESet;
		threshold0 = THRESHOLD0_EDEFAULT;
		threshold0ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0, oldThreshold0, THRESHOLD0_EDEFAULT, oldThreshold0ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold0() {
		return threshold0ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold1() {
		return threshold1;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold1(int newThreshold1) {
		int oldThreshold1 = threshold1;
		threshold1 = newThreshold1;
		boolean oldThreshold1ESet = threshold1ESet;
		threshold1ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1, oldThreshold1, threshold1, !oldThreshold1ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold1() {
		int oldThreshold1 = threshold1;
		boolean oldThreshold1ESet = threshold1ESet;
		threshold1 = THRESHOLD1_EDEFAULT;
		threshold1ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1, oldThreshold1, THRESHOLD1_EDEFAULT, oldThreshold1ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold1() {
		return threshold1ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold2() {
		return threshold2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold2(int newThreshold2) {
		int oldThreshold2 = threshold2;
		threshold2 = newThreshold2;
		boolean oldThreshold2ESet = threshold2ESet;
		threshold2ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2, oldThreshold2, threshold2, !oldThreshold2ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold2() {
		int oldThreshold2 = threshold2;
		boolean oldThreshold2ESet = threshold2ESet;
		threshold2 = THRESHOLD2_EDEFAULT;
		threshold2ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2, oldThreshold2, THRESHOLD2_EDEFAULT, oldThreshold2ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold2() {
		return threshold2ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold3() {
		return threshold3;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold3(int newThreshold3) {
		int oldThreshold3 = threshold3;
		threshold3 = newThreshold3;
		boolean oldThreshold3ESet = threshold3ESet;
		threshold3ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3, oldThreshold3, threshold3, !oldThreshold3ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold3() {
		int oldThreshold3 = threshold3;
		boolean oldThreshold3ESet = threshold3ESet;
		threshold3 = THRESHOLD3_EDEFAULT;
		threshold3ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3, oldThreshold3, THRESHOLD3_EDEFAULT, oldThreshold3ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold3() {
		return threshold3ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold4() {
		return threshold4;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold4(int newThreshold4) {
		int oldThreshold4 = threshold4;
		threshold4 = newThreshold4;
		boolean oldThreshold4ESet = threshold4ESet;
		threshold4ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4, oldThreshold4, threshold4, !oldThreshold4ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold4() {
		int oldThreshold4 = threshold4;
		boolean oldThreshold4ESet = threshold4ESet;
		threshold4 = THRESHOLD4_EDEFAULT;
		threshold4ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4, oldThreshold4, THRESHOLD4_EDEFAULT, oldThreshold4ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold4() {
		return threshold4ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold5() {
		return threshold5;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold5(int newThreshold5) {
		int oldThreshold5 = threshold5;
		threshold5 = newThreshold5;
		boolean oldThreshold5ESet = threshold5ESet;
		threshold5ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5, oldThreshold5, threshold5, !oldThreshold5ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold5() {
		int oldThreshold5 = threshold5;
		boolean oldThreshold5ESet = threshold5ESet;
		threshold5 = THRESHOLD5_EDEFAULT;
		threshold5ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5, oldThreshold5, THRESHOLD5_EDEFAULT, oldThreshold5ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold5() {
		return threshold5ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold6() {
		return threshold6;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold6(int newThreshold6) {
		int oldThreshold6 = threshold6;
		threshold6 = newThreshold6;
		boolean oldThreshold6ESet = threshold6ESet;
		threshold6ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6, oldThreshold6, threshold6, !oldThreshold6ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold6() {
		int oldThreshold6 = threshold6;
		boolean oldThreshold6ESet = threshold6ESet;
		threshold6 = THRESHOLD6_EDEFAULT;
		threshold6ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6, oldThreshold6, THRESHOLD6_EDEFAULT, oldThreshold6ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold6() {
		return threshold6ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getThreshold7() {
		return threshold7;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setThreshold7(int newThreshold7) {
		int oldThreshold7 = threshold7;
		threshold7 = newThreshold7;
		boolean oldThreshold7ESet = threshold7ESet;
		threshold7ESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7, oldThreshold7, threshold7, !oldThreshold7ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetThreshold7() {
		int oldThreshold7 = threshold7;
		boolean oldThreshold7ESet = threshold7ESet;
		threshold7 = THRESHOLD7_EDEFAULT;
		threshold7ESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7, oldThreshold7, THRESHOLD7_EDEFAULT, oldThreshold7ESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetThreshold7() {
		return threshold7ESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExcaliburConfigPackage.ANPER_MODEL__PREAMP:
				return getPreamp();
			case ExcaliburConfigPackage.ANPER_MODEL__IKRUM:
				return getIkrum();
			case ExcaliburConfigPackage.ANPER_MODEL__SHAPER:
				return getShaper();
			case ExcaliburConfigPackage.ANPER_MODEL__DISC:
				return getDisc();
			case ExcaliburConfigPackage.ANPER_MODEL__DISCLS:
				return getDiscls();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN:
				return getThresholdn();
			case ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL:
				return getDacPixel();
			case ExcaliburConfigPackage.ANPER_MODEL__DELAY:
				return getDelay();
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN:
				return getTpBufferIn();
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT:
				return getTpBufferOut();
			case ExcaliburConfigPackage.ANPER_MODEL__RPZ:
				return getRpz();
			case ExcaliburConfigPackage.ANPER_MODEL__GND:
				return getGnd();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF:
				return getTpref();
			case ExcaliburConfigPackage.ANPER_MODEL__FBK:
				return getFbk();
			case ExcaliburConfigPackage.ANPER_MODEL__CAS:
				return getCas();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_A:
				return getTprefA();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_B:
				return getTprefB();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0:
				return getThreshold0();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1:
				return getThreshold1();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2:
				return getThreshold2();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3:
				return getThreshold3();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4:
				return getThreshold4();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5:
				return getThreshold5();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6:
				return getThreshold6();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7:
				return getThreshold7();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ExcaliburConfigPackage.ANPER_MODEL__PREAMP:
				setPreamp((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__IKRUM:
				setIkrum((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__SHAPER:
				setShaper((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DISC:
				setDisc((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DISCLS:
				setDiscls((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN:
				setThresholdn((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL:
				setDacPixel((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DELAY:
				setDelay((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN:
				setTpBufferIn((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT:
				setTpBufferOut((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__RPZ:
				setRpz((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__GND:
				setGnd((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF:
				setTpref((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__FBK:
				setFbk((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__CAS:
				setCas((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_A:
				setTprefA((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_B:
				setTprefB((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0:
				setThreshold0((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1:
				setThreshold1((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2:
				setThreshold2((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3:
				setThreshold3((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4:
				setThreshold4((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5:
				setThreshold5((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6:
				setThreshold6((Integer)newValue);
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7:
				setThreshold7((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ExcaliburConfigPackage.ANPER_MODEL__PREAMP:
				unsetPreamp();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__IKRUM:
				unsetIkrum();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__SHAPER:
				unsetShaper();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DISC:
				unsetDisc();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DISCLS:
				unsetDiscls();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN:
				unsetThresholdn();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL:
				unsetDacPixel();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__DELAY:
				unsetDelay();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN:
				unsetTpBufferIn();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT:
				unsetTpBufferOut();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__RPZ:
				unsetRpz();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__GND:
				unsetGnd();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF:
				unsetTpref();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__FBK:
				unsetFbk();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__CAS:
				unsetCas();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_A:
				unsetTprefA();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_B:
				unsetTprefB();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0:
				unsetThreshold0();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1:
				unsetThreshold1();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2:
				unsetThreshold2();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3:
				unsetThreshold3();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4:
				unsetThreshold4();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5:
				unsetThreshold5();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6:
				unsetThreshold6();
				return;
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7:
				unsetThreshold7();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ExcaliburConfigPackage.ANPER_MODEL__PREAMP:
				return isSetPreamp();
			case ExcaliburConfigPackage.ANPER_MODEL__IKRUM:
				return isSetIkrum();
			case ExcaliburConfigPackage.ANPER_MODEL__SHAPER:
				return isSetShaper();
			case ExcaliburConfigPackage.ANPER_MODEL__DISC:
				return isSetDisc();
			case ExcaliburConfigPackage.ANPER_MODEL__DISCLS:
				return isSetDiscls();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLDN:
				return isSetThresholdn();
			case ExcaliburConfigPackage.ANPER_MODEL__DAC_PIXEL:
				return isSetDacPixel();
			case ExcaliburConfigPackage.ANPER_MODEL__DELAY:
				return isSetDelay();
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_IN:
				return isSetTpBufferIn();
			case ExcaliburConfigPackage.ANPER_MODEL__TP_BUFFER_OUT:
				return isSetTpBufferOut();
			case ExcaliburConfigPackage.ANPER_MODEL__RPZ:
				return isSetRpz();
			case ExcaliburConfigPackage.ANPER_MODEL__GND:
				return isSetGnd();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF:
				return isSetTpref();
			case ExcaliburConfigPackage.ANPER_MODEL__FBK:
				return isSetFbk();
			case ExcaliburConfigPackage.ANPER_MODEL__CAS:
				return isSetCas();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_A:
				return isSetTprefA();
			case ExcaliburConfigPackage.ANPER_MODEL__TPREF_B:
				return isSetTprefB();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD0:
				return isSetThreshold0();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD1:
				return isSetThreshold1();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD2:
				return isSetThreshold2();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD3:
				return isSetThreshold3();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD4:
				return isSetThreshold4();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD5:
				return isSetThreshold5();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD6:
				return isSetThreshold6();
			case ExcaliburConfigPackage.ANPER_MODEL__THRESHOLD7:
				return isSetThreshold7();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (preamp: ");
		if (preampESet) result.append(preamp); else result.append("<unset>");
		result.append(", ikrum: ");
		if (ikrumESet) result.append(ikrum); else result.append("<unset>");
		result.append(", shaper: ");
		if (shaperESet) result.append(shaper); else result.append("<unset>");
		result.append(", disc: ");
		if (discESet) result.append(disc); else result.append("<unset>");
		result.append(", discls: ");
		if (disclsESet) result.append(discls); else result.append("<unset>");
		result.append(", thresholdn: ");
		if (thresholdnESet) result.append(thresholdn); else result.append("<unset>");
		result.append(", dacPixel: ");
		if (dacPixelESet) result.append(dacPixel); else result.append("<unset>");
		result.append(", delay: ");
		if (delayESet) result.append(delay); else result.append("<unset>");
		result.append(", tpBufferIn: ");
		if (tpBufferInESet) result.append(tpBufferIn); else result.append("<unset>");
		result.append(", tpBufferOut: ");
		if (tpBufferOutESet) result.append(tpBufferOut); else result.append("<unset>");
		result.append(", rpz: ");
		if (rpzESet) result.append(rpz); else result.append("<unset>");
		result.append(", gnd: ");
		if (gndESet) result.append(gnd); else result.append("<unset>");
		result.append(", tpref: ");
		if (tprefESet) result.append(tpref); else result.append("<unset>");
		result.append(", fbk: ");
		if (fbkESet) result.append(fbk); else result.append("<unset>");
		result.append(", cas: ");
		if (casESet) result.append(cas); else result.append("<unset>");
		result.append(", tprefA: ");
		if (tprefAESet) result.append(tprefA); else result.append("<unset>");
		result.append(", tprefB: ");
		if (tprefBESet) result.append(tprefB); else result.append("<unset>");
		result.append(", threshold0: ");
		if (threshold0ESet) result.append(threshold0); else result.append("<unset>");
		result.append(", threshold1: ");
		if (threshold1ESet) result.append(threshold1); else result.append("<unset>");
		result.append(", threshold2: ");
		if (threshold2ESet) result.append(threshold2); else result.append("<unset>");
		result.append(", threshold3: ");
		if (threshold3ESet) result.append(threshold3); else result.append("<unset>");
		result.append(", threshold4: ");
		if (threshold4ESet) result.append(threshold4); else result.append("<unset>");
		result.append(", threshold5: ");
		if (threshold5ESet) result.append(threshold5); else result.append("<unset>");
		result.append(", threshold6: ");
		if (threshold6ESet) result.append(threshold6); else result.append("<unset>");
		result.append(", threshold7: ");
		if (threshold7ESet) result.append(threshold7); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //AnperModelImpl
