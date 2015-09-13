package org.walkmod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.walkmod.utils.TestUtils.FILE_SEPARATOR;
import static org.walkmod.utils.TestUtils.getValue;
import static org.walkmod.utils.TestUtils.isWindows;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.Configuration;

/**
 * Tests for {@link org.walkmod.Options} and {@link org.walkmod.OptionsBuilder}
 */
public class WalkmodFacadeTest {

	@Test(expected = NullPointerException.class)
	public void facade_does_not_support_null_optionsBuilder() {
		WalkModFacade facade = new WalkModFacade(null, null, null);
	}

	@Test
	public void facade_minimum_parameters() {
		WalkModFacade facade = new WalkModFacade(null,
				OptionsBuilder.options(), null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.getPath(), equalTo("walkmod.xml"));

		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade,
				"configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(nullValue()));
	}

	@Test
	public void facade_with_cfg_file() {
		// Note that the constructor does not validate that the file exists
		String fileName = "test/any_file.xml";
		WalkModFacade facade = new WalkModFacade(new File(fileName),
				OptionsBuilder.options(), null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		if (isWindows()) {
			assertThat(cfg.getPath(),
					equalTo(fileName.replace("/", FILE_SEPARATOR)));
		} else {
			assertThat(cfg.getPath(), equalTo(fileName));
		}
		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade,
				"configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(nullValue()));
	}

	@Test
	public void facade_with_configuration_provider() {
		// Note that the constructor does not validate if the file exists
		String fileName = "test/any_file.xml";
		WalkModFacade facade = new WalkModFacade(new File(fileName),
				OptionsBuilder.options(), new MockConfigurationProvider());
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.exists(), is(false));
		if (isWindows()) {
			assertThat(cfg.getPath(),
					equalTo(fileName.replace("/", FILE_SEPARATOR)));
		} else {
			assertThat(cfg.getPath(), equalTo(fileName));
		}
		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade,
				"configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(not(nullValue())));
		assertThat(configProvider, instanceOf(MockConfigurationProvider.class));
	}

	@Test
	public void facade_with_options() {
		// Note that the constructor does not validate if the file exists
		String[] includes = new String[] { "include1", "include2" };
		String[] excludes = new String[] { "exclude1", "exclude2", "exclude3" };
		OptionsBuilder options = OptionsBuilder.options().includes(includes)
				.excludes(excludes).printErrors(true).throwException(true);

		WalkModFacade facade = new WalkModFacade(null, options, null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.getPath(), equalTo("walkmod.xml"));

		Options finalOpts = getValue(facade, "options", Options.class);
		assertThat(finalOpts, Matchers.notNullValue());

	}

	/**
	 * Empty implementation of a ConfigurationProvider
	 */
	class MockConfigurationProvider implements ConfigurationProvider {
		@Override
		public void init(Configuration configuration) {
		}

		@Override
		public void load() throws ConfigurationException {
		}
	}

	/**
	 * Asserts the default values for all options
	 */
	private void assertDefaultOptions(WalkModFacade facade) {

		Options options = getValue(facade, "options", Options.class);

		Boolean offline = options.isOffline();
		assertThat(offline.booleanValue(), is(false));

		Boolean verbose = options.isVerbose();
		assertThat(verbose.booleanValue(), is(true));

		Boolean printError = options.isPrintErrors();
		assertThat(printError.booleanValue(), is(false));

		Boolean throwsException = options.isThrowException();
		assertThat(throwsException.booleanValue(), is(false));

		Object includes = options.getIncludes();
		assertThat(includes, is(nullValue()));

		Object excludes = options.getExcludes();
		assertThat(excludes, is(nullValue()));
	}

	@Test
	public void facadeWithExecutionDir() throws Exception {

		File executionDir = new File("src/test/resources/testFiles");

		File srcDir = new File(executionDir, "src/main/java");

		srcDir.mkdirs();

		FileUtils.write(new File(srcDir, "Bar.java"),
				"public class Bar { private int foo; }");

		String path1 = new File(".").getCanonicalPath();
		WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options()
				.executionDirectory(executionDir).printErrors(true), null);

		List<File> result = facade.apply();
		assertThat(result.get(0), Matchers.notNullValue());

		String path = new File(".").getCanonicalPath();
		assertThat(path, Matchers.equalTo(path1));

		result.get(0).delete();

	}

	@Test
	public void facadeExecutionLocally() throws Exception {

		WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options()
				.printErrors(true).offline(true), null);

		List<File> result = facade.check();
		assertThat(facade, Matchers.notNullValue());

	}

}
