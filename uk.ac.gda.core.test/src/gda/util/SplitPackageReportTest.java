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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.osgi.util.ManifestElement;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

public class SplitPackageReportTest {

	private static final Logger logger = LoggerFactory.getLogger(SplitPackageReportTest.class);

	@Test
	public void listSplitPackages() throws Exception {

		final Path workspace_git = Paths.get("..", "..").toAbsolutePath().normalize();

		final List<Path> manifests = findAllManifests(workspace_git);
		logger.info("Found " + manifests.size() + " manifests");

		logger.info("");

		final Attributes.Name bsnAttribute = new Attributes.Name("Bundle-SymbolicName");
		final Attributes.Name exportPackageAttribute = new Attributes.Name("Export-Package");

		final SetMultimap<String, String> packageMap = LinkedHashMultimap.<String, String>create();

		final Set<String> bundleNames = new HashSet<>();

		logger.info("Packages exported by each bundle:");
		for (ListIterator<Path> it = manifests.listIterator(); it.hasNext(); ) {

			logger.info("");

			final int num = (it.nextIndex() + 1);
			final Path manifestPath = it.next();

			final Path projectDir = workspace_git.relativize(manifestPath.resolve(Paths.get("..", "..")).normalize());

			final Manifest manifest = readManifest(manifestPath);

			if (manifest.getMainAttributes().containsKey(bsnAttribute)) {

				final String bsnValue = manifest.getMainAttributes().getValue(bsnAttribute);
				final String bundleName = ManifestElement.parseHeader(bsnAttribute.toString(), bsnValue)[0].getValue();

				logger.info("    " + num + ". " + bundleName + " (" + projectDir + ")");
				bundleNames.add(bundleName);

				if (manifest.getMainAttributes().containsKey(exportPackageAttribute)) {

					final String exportedPackagesValue = manifest.getMainAttributes().getValue(exportPackageAttribute);
					final ManifestElement[] exportedPackages = ManifestElement.parseHeader(exportPackageAttribute.toString(), exportedPackagesValue);

					for (ManifestElement element : exportedPackages) {
						final String packageName = element.getValue();
						logger.info("        " + packageName);
						packageMap.put(packageName, bundleName);
					}
				}
			}

			else {
				logger.info("    " + num + ". " + manifestPath);
				logger.info("        not a bundle");
			}
		}

		logger.info("");

		logger.info(bundleNames.size() + " unique bundle names");
		logger.info(packageMap.size() + " exported packages");

		logger.info("");

		final List<String> packagesExportedByMoreThanOneBundle = new ArrayList<>();
		for (String pkg : packageMap.keySet()) {
			final Set<String> exportingBundles = packageMap.get(pkg);
			if (exportingBundles.size() > 1) {
				packagesExportedByMoreThanOneBundle.add(pkg);
			}
		}

		Collections.sort(packagesExportedByMoreThanOneBundle);

		logger.info(packagesExportedByMoreThanOneBundle.size() + " packages are exported by more than one bundle:");
		for (ListIterator<String> it = packagesExportedByMoreThanOneBundle.listIterator(); it.hasNext(); ) {

			final int num = (it.nextIndex() + 1);
			final String pkg = it.next();

			final Set<String> exportingBundles = packageMap.get(pkg);
			logger.info("");
			logger.info("    " + num + ". " + pkg);
			for (String bundle : exportingBundles) {
				logger.info("        " + bundle);
			}
		}
	}

	private List<Path> findAllManifests(final Path root) throws IOException {

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
		});

		return manifests;
	}

	private Manifest readManifest(Path manifestPath) throws Exception {
		try (FileInputStream fis = new FileInputStream(manifestPath.toFile())) {
			final Manifest manifest = new Manifest(fis);
			return manifest;
		}
	}
}
