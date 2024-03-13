/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.configuration.test.source;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.configuration.source.CliOptions;
import uk.ac.diamond.daq.configuration.source.CliOptions.CliArgs;

/** Testing the underlying CLI parsing used by {@link CliOptions} */
class CliArgsTest {
	@Test
	void singleCharacterFlags() {
		var opts = parse("-a -b");
		assertThat(opts.options().entrySet(), is(emptyIterable()));
		assertThat(opts.args(), is(empty()));
		assertThat(opts.flags(), contains("a", "b"));
	}

	@Test
	void combinedSingleCharacterFlags() {
		var opts = parse("-ab");
		assertThat(opts.options().entrySet(), is(emptyIterable()));
		assertThat(opts.args(), is(empty()));
		assertThat(opts.flags(), is(Set.of("a", "b")));
	}

	@Test
	void wordFlag() {
		var args = parse("--flag");
		assertThat(args.flags(), contains("flag"));
	}

	@Test
	void negativeFlag() {
		var args = parse("--no-flag", Set.of("flag"), emptySet(), emptyMap());
		assertThat(args.flags(), not(contains("flag")));
	}

	@Test
	void defaultFlag() {
		var args = parse("", Set.of("flag"), emptySet(), emptyMap());
		assertThat(args.flags(), contains("flag"));
	}

	@Test
	void redundantFlags() {
		var args = parse("--foo --no-foo");
		assertThat(args.flags(), is(empty()));
		args = parse("--no-foo --foo");
		assertThat(args.flags(), contains("foo"));
	}

	@Test
	void duplicateFlags() {
		var args = parse("--foo --foo");
		assertThat(args.flags(), contains("foo"));
	}

	@Test
	void singleCharacterOption() {
		var args = parse("-o value -p value2");
		assertThat(args.options(), is(Map.of("o", "value", "p", "value2")));
	}

	@Test
	void wordOption() {
		var args = parse("--key value --key2 value2");
		assertThat(args.options(), is(Map.of("key", "value", "key2", "value2")));
	}

	@Test
	void duplicateOptions() {
		var args = parse("--key one --key two");
		assertThat(args.options().get("key"), is("two"));
	}

	@Test
	void duplicateVarOptions() {
		var args = parse("--key one --key two", emptySet(), Set.of("key"), emptyMap());
		assertThat(args.varOptions().get("key"), is(List.of("one", "two")));
		assertThat(args.options(), is(emptyMap()));
	}

	@Test
	void freeArgs() {
		var opts = parse("free1 free2");
		assertThat(opts.args(), contains("free1", "free2"));
	}

	@Test
	void freeArgsAfterEndOfArgs() {
		var opts = parse("-- --free1 --free2");
		assertThat(opts.args(), contains("--free1", "--free2"));
		assertThat(opts.flags(), is(empty()));
		assertThat(opts.options().entrySet(), is(empty()));
	}

	@Test
	void aliasedFlags() {
		var args =
				parse("-a -b --long --flag", emptySet(), emptySet(), Map.of("a", "foo", "long", "even_longer"));
		assertThat(args.flags(), containsInAnyOrder("foo", "b", "even_longer", "flag"));

		args =
				parse(
						"-A --clean",
						Set.of("a", "default-values"),
						emptySet(),
						Map.of("A", "no-a", "clean", "no-default-values"));
		assertThat(args.flags(), is(empty()));
	}

	@Test
	void aliasedOptions() {
		var args = parse("-a short --key long", emptySet(), emptySet(), Map.of("a", "foo", "key", "bar"));
		assertThat(args.options(), is(Map.of("foo", "short", "bar", "long")));
	}

	private CliArgs parse(String argv) {
		return new CliArgs(emptySet(), emptySet(), emptyMap()).parse(argv.split(" "));
	}

	private CliArgs parse(String argv, Set<String> defaultFlags, Set<String> multiValue, Map<String, String> aliases) {
		return new CliArgs(defaultFlags, multiValue, aliases).parse(argv.split(" "));
	}
}
