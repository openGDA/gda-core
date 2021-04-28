/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.osgi.util.ManifestElement;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

public class SplitPackageReportTest {

	private static final Logger logger = LoggerFactory.getLogger(SplitPackageReportTest.class);

	private static final Attributes.Name BSN_ATTRIBUTE = new Attributes.Name("Bundle-SymbolicName");
	private static final Attributes.Name RB_ATTRIBUTE = new Attributes.Name("Require-Bundle");
	private static final Attributes.Name EP_ATTRIBUTE = new Attributes.Name("Export-Package");

	// Caches so that multiple tests don't have to re-read files
	private static final SetMultimap<String, String> packageMap = LinkedHashMultimap.create();
	private static final Map<String, Manifest> allBundleManifests = new HashMap<>();

	/** package name -> list of bundles exporting it */
	private static final Map<String, Set<String>> packagesExportedByMoreThanOneBundle = new TreeMap<>();

	@BeforeClass
	public static void readManifestsAndRecordSplitPackages() throws Exception {
		// Just in case this were to be called multiple times
		packageMap.clear();
		allBundleManifests.clear();
		packagesExportedByMoreThanOneBundle.clear();
		final Path workspace_git = Paths.get("..", "..").toAbsolutePath().normalize();
		findAllManifests(workspace_git).stream().map(SplitPackageReportTest::readManifest).forEach(SplitPackageReportTest::storeBundle);
		for (var bEntry : allBundleManifests.entrySet()) {
			Manifest manifest = bEntry.getValue();
			if (manifest.getMainAttributes().containsKey(EP_ATTRIBUTE)) {
				final String exportedPackagesValue = manifest.getMainAttributes().getValue(EP_ATTRIBUTE);
				final ManifestElement[] exportedPackages = ManifestElement.parseHeader(EP_ATTRIBUTE.toString(),
						exportedPackagesValue);
				for (ManifestElement element : exportedPackages) {
					final String packageName = element.getValue();
					packageMap.put(packageName, bEntry.getKey());
				}
			}

		}
		for (String pkg : packageMap.keySet()) {
			final Set<String> exportingBundles = packageMap.get(pkg);
			if (exportingBundles.size() > 1) {
				packagesExportedByMoreThanOneBundle.put(pkg, exportingBundles);
			}
		}
	}

	private static List<Path> findAllManifests(final Path root) throws IOException {

		final Path manifestPath = Paths.get("META-INF", "MANIFEST.MF");
		final List<Path> manifests = new ArrayList<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (attrs.isRegularFile() && file.endsWith(manifestPath)) {
					manifests.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// Skip Maven build output which can contain expanded dependency bundles
				if (dir.endsWith("target")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return manifests;
	}

	private static Manifest readManifest(Path manifestPath) {
		try (FileInputStream fis = new FileInputStream(manifestPath.toFile())) {
			return new Manifest(fis);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * If manifest corresponds to a valid bundle,
	 * cache in the static map
	 */
	private static void storeBundle(Manifest manifest) {
		if (manifest.getMainAttributes().containsKey(BSN_ATTRIBUTE)) {
			try {
				String bsnValue = manifest.getMainAttributes().getValue(BSN_ATTRIBUTE);
				String bundleName = ManifestElement.parseHeader(BSN_ATTRIBUTE.toString(), bsnValue)[0].getValue();
				allBundleManifests.put(bundleName, manifest);
			} catch (BundleException e) {
				throw new RuntimeException("Bundle problem", e);
			}
		}
	}


	@Test
	public void reportSplitPackages() throws Exception {
		logger.info("{} unique bundle names", allBundleManifests.size());
		logger.info("{} exported packages", packageMap.size());
		logger.info("");
		logger.info("{} packages are exported by more than one bundle:", packagesExportedByMoreThanOneBundle.size());
		int count = 0;
		for (String pkg : packagesExportedByMoreThanOneBundle.keySet()) {
			final int num = ++count;
			final Set<String> exportingBundles = packagesExportedByMoreThanOneBundle.get(pkg);
			logger.info("");
			logger.info("\t{}. {}", num, pkg);
			for (String bundle : exportingBundles) {
				logger.info("\t\t{}", bundle);
			}
		}
	}

	/**
	 * Ensure that no new split packages are introduced
	 * @throws Exception
	 */
	@Test
	public void checkStatus() throws Exception {
		Set<String> knownSplitPackages = Set.of(
				"com.swtdesigner",
				"gda.analysis.io",
				"gda.beamline.health",
				"gda.commandqueue",
				"gda.data",
				"gda.data.metadata",
				"gda.data.metadata.icat",
				"gda.data.nexus",
				"gda.data.scan.datawriter",
				"gda.device",
				"gda.device.adc",
				"gda.device.attenuator",
				"gda.device.continuouscontroller",
				"gda.device.controlpoint",
				"gda.device.currentamplifier",
				"gda.device.detector",
				"gda.device.detector.analyser",
				"gda.device.detector.areadetector",
				"gda.device.detector.countertimer",
				"gda.device.detector.multichannelscaler",
				"gda.device.detector.mythen",
				"gda.device.detector.nxdetector.plugin",
				"gda.device.detector.odccd",
				"gda.device.detector.pilatus",
				"gda.device.detector.xmap",
				"gda.device.enumpositioner",
				"gda.device.frelon",
				"gda.device.monitor",
				"gda.device.motor",
				"gda.device.robot",
				"gda.device.scannable",
				"gda.device.scannable.scannablegroup",
				"gda.device.temperature",
				"gda.device.zebra",
				"gda.exafs.scan",
				"gda.exafs.validation",
				"gda.factory",
				"gda.images.camera",
				"gda.jython",
				"gda.jython.accesscontrol",
				"gda.jython.authenticator",
				"gda.jython.authoriser",
				"gda.jython.batoncontrol",
				"gda.jython.commandinfo",
				"gda.jython.scriptcontroller",
				"gda.jython.scriptcontroller.logging",
				"gda.jython.translator",
				"gda.observable",
				"gda.px.detector",
				"gda.px.sampleChanger",
				"gda.px.stac.bridge",
				"gda.rcp.mx.views",
				"gda.rcp.util",
				"gda.scan",
				"gda.spring",
				"gda.util",
				"gda.util.converters",
				"org.opengda.detector.electronanalyser.utils",
				"uk.ac.diamond.scisoft.analysis",
				"uk.ac.diamond.scisoft.analysis.io",
				"uk.ac.diamond.scisoft.analysis.optimize",
				"uk.ac.diamond.scisoft.analysis.plotserver",
				"uk.ac.diamond.scisoft.analysis.rcp",
				"uk.ac.diamond.scisoft.analysis.rcp.editors",
				"uk.ac.diamond.scisoft.analysis.rcp.plotting",
				"uk.ac.diamond.scisoft.analysis.rcp.util",
				"uk.ac.diamond.scisoft.analysis.rcp.views",
				"uk.ac.diamond.scisoft.analysis.rcp.views.plot",
				"uk.ac.gda.beans",
				"uk.ac.gda.client.experimentdefinition",
				"uk.ac.gda.client.experimentdefinition.ui.handlers",
				"uk.ac.gda.devices.detector.xspress3.controllerimpl",
				"uk.ac.gda.exafs.experiment.ui.data",
				"uk.ac.gda.exafs.ui",
				"uk.ac.gda.exafs.ui.data",
				"uk.ac.gda.jython",
				"uk.ac.gda.mx.model",
				"uk.ac.gda.server.exafs.scan",
				"uk.ac.gda.server.exafs.scan.iterators",
				"uk.ac.gda.server.exafs.scan.preparers",
				"uk.ac.gda.server.ncd.subdetector",
				"uk.ac.gda.ui.viewer"
				);

		Set<String> actualSplitPackages = packagesExportedByMoreThanOneBundle.keySet();

		knownSplitPackages.stream().forEach(
				sp -> assertThat("Package no longer split (remove package from this test): " + sp, actualSplitPackages, hasItem(sp)));
		actualSplitPackages.stream()
				.forEach(sp -> assertThat("New split package detected: " + sp, knownSplitPackages, hasItem(sp)));
	}

	/**
	 * This test checks that any plugins with a dependency on Spring express this via
	 * an Import-Package directive rather than Require-Bundle. This is to aid with
	 * decoupling Spring usage from the name of the provider (particularly for moving
	 * Spring to the target platform)
	 * @see <a href="https://jira.diamond.ac.uk/browse/DAQ-3468">DAQ-3468</a>
	 */
	@Test
	public void testNoRequireBundleForSpring() throws IOException, BundleException {
		Set<String> bundlesRequiringSpring = new HashSet<>();
		for (var bEntry : allBundleManifests.entrySet()) {
			Manifest manifest = bEntry.getValue();
			String bundleName = bEntry.getKey();
			if (manifest.getMainAttributes().containsKey(RB_ATTRIBUTE)) {
				String rbValue = manifest.getMainAttributes().getValue(RB_ATTRIBUTE);
				ManifestElement[] rbEntries = ManifestElement.parseHeader(RB_ATTRIBUTE.toString(), rbValue);
				stream(rbEntries).map(ManifestElement::getValue)
						.filter(r -> r.equals("uk.ac.diamond.org.springframework")).findAny()
						.ifPresent(r -> bundlesRequiringSpring.add(bundleName));
			}
		}
		assertThat("DAQ-3468 Please use Import-Package for Spring dependencies", bundlesRequiringSpring, is(empty()));
	}
}
