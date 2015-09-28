package org.walkmod;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Test;

public class WalkmodDispatcherTest {


	@Test
	public void testNoArgs() throws Exception {
		Assert.assertTrue(run(null).contains("walkmod [options] [command] [command options]"));
	}

	@Test
	public void testUsage() throws Exception {
		Assert.assertTrue(run(new String[] { "--help" }).contains("walkmod [options] [command] [command options]"));
	}

	@Test
	public void testApply() throws Exception {
		Assert.assertTrue(run(new String[] { "apply" }).contains("TRANSFORMATION CHAIN SUCCESS"));
	}

	@Test
	public void testApplyWithParams() throws Exception {
		Assert.assertTrue(run(
				new String[] { "apply", "--includes",
						new File("src/main/java/org/walkmod/WalkmodFacade.java").getAbsolutePath() }).contains(
				"TRANSFORMATION CHAIN SUCCESS"));
	}

	@Test
	public void testInstall() throws Exception {
		Assert.assertTrue(run(new String[] { "install" }).contains("PLUGIN INSTALLATION COMPLETE"));

	}

	@Test
	public void testCheck() throws Exception {
		Assert.assertTrue(run(new String[] { "check" }).contains("TRANSFORMATION CHAIN SUCCESS"));

	}

	@Test
	public void testCheckWithParams() throws Exception {
		Assert.assertTrue(run(
				new String[] { "check", "--includes",
						new File("src/main/java/org/walkmod/WalkmodFacade.java").getAbsolutePath() }).contains(
				"TRANSFORMATION CHAIN SUCCESS"));
	}

	@Test
	public void testVersion() throws Exception {
		Assert.assertTrue(run(new String[] { "--version" }).contains("Walkmod version"));
	}

	@Test
	public void testInvalidGoal() throws Exception {
		Assert.assertTrue(run(new String[] { "foo" }).contains("Expected a command, got foo"));
	}

	@Test
	public void testApplyWithInvalidArgs() throws Exception {
		Assert.assertTrue(run(new String[] { "apply", "-F" }).contains("Unknown option: -F"));
	}

	private String run(String[] args) throws Exception {

		ByteArrayOutputStream mem = new ByteArrayOutputStream();
		BufferedOutputStream stream = new BufferedOutputStream(mem);

		PrintStream ps = new PrintStream(stream);
		PrintStream old = System.out;
		System.setOut(ps);
		
		WalkModFacade.log = Logger.getLogger(WalkModFacade.class.getName());
		WalkModFacade.log.removeAllAppenders();
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
		appender.setName("stdout");
		WalkModFacade.log.addAppender(appender);

		String result = "";
		try {
			WalkModDispatcher.main(args);
			stream.flush();
			result = mem.toString();

		} finally {
			System.setOut(old);
			ps.close();
			stream.close();
			mem.close();
		}

		return result;
	}

}
