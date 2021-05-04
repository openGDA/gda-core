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
import static java.util.stream.Collectors.toCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.fail;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
	private static final String SPLIT_PACKAGE_RESOLVER_NAME = "uk.ac.diamond.daq.splitpackagesresolver";

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
	 * If manifest corresponds to a valid bundle, cache in the static map
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
	 * Ensure that no new split packages are introduced and check exports of split packages
	 * to verify that all split packages are exported by the split package resolver
	 *
	 */
	@Test
	public void checkSplitPackageExports() {

		// Packages that we are currently allowing to be split but not in resolver
		Set<String> exceptions = Set.of("com.swtdesigner");

		for (var pkg : packagesExportedByMoreThanOneBundle.entrySet()) {
			String pkgName = pkg.getKey();
			Set<String> exportingBundles = pkg.getValue();
			if (exceptions.contains(pkgName)) {
				continue;
			}
			if (exportingBundles.size() == 2 && exportingBundles.contains(SPLIT_PACKAGE_RESOLVER_NAME)) {
				fail("Package: " + pkgName + " is no longer split - remove from splitpackage resolver");
			}
			if (exportingBundles.size() > 1 && !exportingBundles.contains(SPLIT_PACKAGE_RESOLVER_NAME)) {
				fail(String.format("New split package detected: %s Refactor change or add to splitpackage resolver",
						pkgName));
			}
		}
	}

    /**
     * Verify that all bundles exporting split packages are required by the
     * split package resolver
     */
    @Test
	public void checkSplitPackageResolverBundles() throws BundleException {
		Manifest splitPackageResolver = allBundleManifests.get(SPLIT_PACKAGE_RESOLVER_NAME);
		String rbValue = splitPackageResolver.getMainAttributes().getValue(RB_ATTRIBUTE);
		ManifestElement[] rbEntries = ManifestElement.parseHeader(RB_ATTRIBUTE.toString(), rbValue);

		// Plugins required by the split package resolver
		Set<String> resolverRequiredBundles = stream(rbEntries).map(ManifestElement::getValue)
				.collect(toCollection(TreeSet::new));

		// Plugins contributing split packages
		Set<String> actualSplitProviders = packagesExportedByMoreThanOneBundle.values().stream()
				.flatMap(Collection::stream).collect(toCollection(TreeSet::new));

		actualSplitProviders.remove(SPLIT_PACKAGE_RESOLVER_NAME);

		assertThat("New bundle contributing split package - add to split package resolver", actualSplitProviders,
				everyItem(isIn(resolverRequiredBundles)));
		assertThat(
				"Split package resolver requires a bundle which is no longer providing a split package - remove from resolver",
				resolverRequiredBundles, everyItem(isIn(actualSplitProviders)));
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
