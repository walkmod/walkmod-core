package org.walkmod;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineExecutionTest extends AbstractWalkmodExecutionTest {

	@Test
	public void testScriptExample() throws Exception {
		File tmp = new File("src/test/resources/scripts").getAbsoluteFile();
		String code = "public class Foo { public String bar; }";
		File srcDir = new File(tmp, "src/main/java");
		srcDir.mkdirs();
		FileUtils.write(new File(srcDir, "Foo.java"), code);
		
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		System.setProperty("user.dir", tmp.getAbsolutePath());
		
		try {
			run(new String[] { "apply"});
		} finally {
			System.setProperty("user.dir", userDir);
		}
		String newContent = FileUtils.readFileToString(new File(srcDir, "Foo.java"));
		Assert.assertTrue(newContent.contains("private String"));
	}
}
