package org.walkmod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.walkmod.utils.TestUtils.getValue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
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
		WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options(), null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.getAbsolutePath(), equalTo(new File("walkmod.xml").getAbsolutePath()));

		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade, "configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(nullValue()));
	}

	@Test
	public void facade_with_cfg_file() {
		// Note that the constructor does not validate that the file exists
		String fileName = "test/any_file.xml";
		WalkModFacade facade = new WalkModFacade(new File(fileName), OptionsBuilder.options(), null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);

		assertThat(cfg.getAbsolutePath(), equalTo(new File(fileName).getAbsolutePath()));
		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade, "configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(nullValue()));
	}

	@Test
	public void facade_with_configuration_provider() {
		// Note that the constructor does not validate if the file exists
		String fileName = "test/any_file.xml";
		WalkModFacade facade = new WalkModFacade(new File(fileName), OptionsBuilder.options(),
				new MockConfigurationProvider());
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.exists(), is(false));

		assertThat(cfg.getAbsolutePath(), equalTo(new File(fileName).getAbsolutePath()));
		assertDefaultOptions(facade);

		ConfigurationProvider configProvider = getValue(facade, "configurationProvider", ConfigurationProvider.class);
		assertThat(configProvider, is(not(nullValue())));
		assertThat(configProvider, instanceOf(MockConfigurationProvider.class));
	}

	@Test
	public void facade_with_options() {
		// Note that the constructor does not validate if the file exists
		String[] includes = new String[] { "include1", "include2" };
		String[] excludes = new String[] { "exclude1", "exclude2", "exclude3" };
		OptionsBuilder options = OptionsBuilder.options().includes(includes).excludes(excludes).printErrors(true)
				.throwException(true);

		WalkModFacade facade = new WalkModFacade(null, options, null);
		assertThat(facade, is(not(nullValue())));

		File cfg = getValue(facade, "cfg", File.class);
		assertThat(cfg.getAbsolutePath(), equalTo(new File("walkmod.xml").getAbsolutePath()));

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
		String execDir = System.getProperty("user.dir");
		File executionDir = new File("src/test/resources/testFiles");

		File srcDir = new File(executionDir, "src/main/java");

		srcDir.mkdirs();

		FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");

		String path1 = new File(".").getCanonicalPath();
		WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);

		List<File> result = facade.apply();
		assertThat(result.get(0), Matchers.notNullValue());

		String path = new File(".").getCanonicalPath();
		assertThat(path, Matchers.equalTo(path1));

		result.get(0).delete();
		Assert.assertEquals(execDir, System.getProperty("user.dir"));
	}

	@Test
	public void facadeExecutionLocally() throws Exception {
		String path = System.getProperty("user.dir");
		WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options().printErrors(true), null);
		List<File> result = facade.check();
		assertThat(result, Matchers.notNullValue());
		Assert.assertEquals(path, System.getProperty("user.dir"));
	}

	@Test
	public void testWithExecutionDirSettingTheWalkmodFile() throws Exception {
		String path = System.getProperty("user.dir");
		File executionDir = new File("src/test/resources/testFiles");
		File srcDir = new File(executionDir, "src/main/java");
		srcDir.mkdirs();
		FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");
		File cfg = new File("src/test/resources/testFiles/walkmod.xml");

		WalkModFacade facade = new WalkModFacade(cfg, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);
		List<File> result = facade.apply();
		assertThat(result.get(0), Matchers.notNullValue());
		result.get(0).delete();
		Assert.assertEquals(path, System.getProperty("user.dir"));
	}

	@Test
	public void testMultiModuleCheckExecution() throws Exception {
		
		String path = System.getProperty("user.dir");
		File executionDir = new File("src/test/resources/multimodule");

		File srcDir = new File(executionDir, "module1/src/main/java");
		srcDir.mkdirs();
		FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");

		File srcDir2 = new File(executionDir, "module2/src/main/java");
		srcDir2.mkdirs();
		FileUtils.write(new File(srcDir2, "Foo.java"), "public class Foo { private int foo; }");

		File cfg = new File("src/test/resources/multimodule/walkmod.xml");

		WalkModFacade facade = new WalkModFacade(cfg, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);
		List<File> result = facade.check();
		
		assertThat(result.size(), Matchers.is(2));

		FileUtils.deleteDirectory(new File("module1/src"));
		FileUtils.deleteDirectory(new File("module2/src"));
		FileUtils.deleteDirectory(new File("module1/target"));
		FileUtils.deleteDirectory(new File("module2/target"));
		Assert.assertEquals(path, System.getProperty("user.dir"));

	}

	@Test
	public void testMultiModuleApplyExecution() throws Exception {
		
		String path = System.getProperty("user.dir");
		
		File executionDir = new File("src/test/resources/multimodule");

		File srcDir = new File(executionDir, "module1/src/main/java");
		srcDir.mkdirs();
		FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");

		File srcDir2 = new File(executionDir, "module2/src/main/java");
		srcDir2.mkdirs();
		FileUtils.write(new File(srcDir2, "Foo.java"), "public class Foo { private int foo; }");

		File cfg = new File("src/test/resources/multimodule/walkmod.xml");

		WalkModFacade facade = new WalkModFacade(cfg, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);
		List<File> result = facade.apply();
		// check does not produce changes
		assertThat(result.size(), Matchers.is(2));

		FileUtils.deleteDirectory(new File("module1/src"));
		FileUtils.deleteDirectory(new File("module2/src"));
		FileUtils.deleteDirectory(new File("module1/target"));
		FileUtils.deleteDirectory(new File("module2/target"));
		
		Assert.assertEquals(path, System.getProperty("user.dir"));

	}
	
	@Test
	public void testMultiModuleInstallExecution() throws Exception {
		
		String path = System.getProperty("user.dir");
		
		File executionDir = new File("src/test/resources/multimodule");

		File srcDir = new File(executionDir, "module1/src/main/java");
		srcDir.mkdirs();
		FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");

		File srcDir2 = new File(executionDir, "module2/src/main/java");
		srcDir2.mkdirs();
		FileUtils.write(new File(srcDir2, "Foo.java"), "public class Foo { private int foo; }");

		File cfg = new File("src/test/resources/multimodule/walkmod.xml");

		WalkModFacade facade = new WalkModFacade(cfg, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);
		facade.install();
		
		FileUtils.deleteDirectory(new File("module1/src"));
		FileUtils.deleteDirectory(new File("module2/src"));
		
		Assert.assertTrue(true);
		
		Assert.assertEquals(path, System.getProperty("user.dir"));

	}
	
	@Test
	public void testSimpleTransformationNameExecution() throws Exception {
		
		String path = System.getProperty("user.dir");
		
		File executionDir = new File("src/test/resources/simple");

		

		File cfg = new File("src/test/resources/simple/walkmod.yml");

		WalkModFacade facade = new WalkModFacade(cfg, OptionsBuilder.options().executionDirectory(executionDir)
				.printErrors(true), null);
		facade.check();
		
		Assert.assertTrue(true);
		
		Assert.assertEquals(path, System.getProperty("user.dir"));

	}
	
	
	@Test
   public void facadeWithExecutionDirCheck() throws Exception {
      String execDir = System.getProperty("user.dir");
      File executionDir = new File("src/test/resources/testFiles");

      File srcDir = new File(executionDir, "src/main/java");

      srcDir.mkdirs();

      FileUtils.write(new File(srcDir, "Bar.java"), "public class Bar { private int foo; }");

      String path1 = new File(".").getCanonicalPath();
      WalkModFacade facade = new WalkModFacade(null, OptionsBuilder.options().executionDirectory(executionDir)
            .printErrors(true), null);

      List<File> result = facade.check();
      assertThat(result.get(0), Matchers.notNullValue());

      String path = new File(".").getCanonicalPath();
      assertThat(path, Matchers.equalTo(path1));

      result.get(0).delete();
      Assert.assertEquals(execDir, System.getProperty("user.dir"));
   }
	
	

}
