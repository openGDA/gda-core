/**
 * P2R is the torsion rig supplied by Manchester University
 * 
 * We talk to it over telnet. The commands are
 * 
 * M<m1>,M<m2><CR><LF>  - drive motor 1 to m1 and motor 2 to m2. Reply is the message sent or ERROR or INVALID 
 * S<CR><LF> - report status. Reply is <BUSY><m1><m2><m3><CR><LF> where <busy> is state of motor moving either T or F and 
 * m1, m2 and m3 are the actual motor positions. Note m3 is read-only and represents the force.
 * 
 */
/**
 *
 */
package uk.ac.gda.tomography.devices.p2r;