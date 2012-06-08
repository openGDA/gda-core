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

package uk.ac.gda.pydev.extension.parser;



public interface IGdaJythonPartitions{
    //this is just so that we don't have to break the interface
    public final static String GDA_ALIAS = "__gda_alias";//alias
	public final static String GDA_LS 	= "__gda_ls";//ls
    public final static String GDA_POS  = "__gda_pos";//pos
    public final static String GDA_UPOS = "__gda_upos";//upos
    public final static String GDA_INC = "__gda_inc";//inc
    public final static String GDA_UINC = "__gda_uinc";//uinc
    public final static String GDA_HELP = "__gda_help";//help
    public final static String GDA_LIST_DEFAULTS = "__gda_list_defaults";//list_defaults
    public final static String GDA_ADD_DEFAULT = "__gda_add_default";//add_default
    public final static String GDA_REMOVE_DEFAULT = "__gda_remove_default";//remove_default
    public final static String GDA_LEVEL = "__gda_level";//level
    public final static String GDA_PAUSE = "__gda_pause";//pause
    public final static String GDA_RESET_NAMESPACE = "__gda_reset_namespace";// reset_namespace
    public final static String GDA_RUN = "__gda_run";//run
    public final static String GDA_SCAN = "__gda_scan";//scan
    public final static String GDA_PSCAN = "__gda_pscan";//pscan
    public final static String GDA_CSCAN = "__gda_cscan";//cscan
    public final static String GDA_ZACSCAN = "__gda_zascan";//zacscan
    public final static String GDA_TESTSCAN = "__gda_testscan";//testscan
    public final static String GDA_GSCAN = "__gda_gscan";//gscan
    public final static String GDA_TSCAN = "__gda_tscan";//tscan
    public final static String GDA_TIMESCAN = "__gda_timescan";//timescan
    public final static String GDA_CVSCAN = "__gda_cvscan";//cvscan
    public final static String GDA_ROBOTSCAN = "__gda_robotscan";//robotscan
    public final static String GDA_STAGESCAN = "__gda_stagescan";//stagescan
    public final static String GDA_TEMPSCAN = "__gda_tempscan";//tempscan
    
    public final static String[] types = {GDA_ALIAS, GDA_LS, GDA_POS, GDA_UPOS, GDA_INC, GDA_UINC, 
    	GDA_HELP, GDA_LIST_DEFAULTS, GDA_ADD_DEFAULT, GDA_REMOVE_DEFAULT, GDA_LEVEL, GDA_PAUSE,
    	GDA_RESET_NAMESPACE, GDA_RUN, GDA_SCAN, GDA_PSCAN, GDA_CSCAN, GDA_ZACSCAN, GDA_TESTSCAN,
    	GDA_GSCAN, GDA_TSCAN, GDA_TIMESCAN, GDA_CVSCAN, GDA_ROBOTSCAN, GDA_STAGESCAN, GDA_TEMPSCAN};
    
    public static final String GDA_PARTITION_TYPE = "__GDA_PARTITION_TYPE";

}
