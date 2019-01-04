/**
 * This package contains the components required to configure a fully-automated DIAD experiment.
 * The basic blocks are:
 * <ul>
 * <li> {@link IPlan}: the centralised object which coordinates the experiment
 * <li> {@link ISampleEnvironmentVariable} (SEV): Samples an external signal and broadcasts the sample
 * 		(unless it hasn't changed) to registered listeners (segments and triggers)
 * <li> {@link ISegment}: The experiment consists of a series of segments which run sequentially.
 * 		When active, these blocks enable and/or disable triggers.
 * <li> {@link ITrigger}: Triggers a job at a specified point (a function of time or SEV signal)
 * </ul>
 */
package uk.ac.diamond.daq.experiment.api.plan;