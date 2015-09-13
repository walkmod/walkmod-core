package org.walkmod.util;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.utils.TestUtils;

import java.io.File;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FileResourceTest {

	private static final String SOURCES_PATH = "src/main/java";

	@Test
	public void testIncludes() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util/FileResource.java";
		fr.setIncludes(new String[] { file });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		assertThat(f.getAbsolutePath(), equalTo(filter.getAbsolutePath()));
		assertThat(it.hasNext(), is(false));
	}

	@Test
	public void testIncludes2() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = new File(new File("src/main/java"), "org/walkmod/util")
				.getAbsolutePath();
		fr.setIncludes(new String[] { file });

		File filter = new File(file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		assertThat(f.getAbsolutePath(), startsWith(filter.getAbsolutePath()));

	}

	@Test
	public void testIncludesWildcard() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util";
		fr.setIncludes(new String[] { file });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		assertThat(f.getAbsolutePath(), startsWith(filter.getAbsolutePath()));

	}

	@Test
	public void testIncludesWildcard2() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util";
		fr.setIncludes(new String[] { file + "/*" });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		assertThat(f.getAbsolutePath(), startsWith(filter.getAbsolutePath()));
	}

	@Test
	public void testIncludesWildcard3() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod";
		fr.setIncludes(new String[] { file + "/**" });

		Iterator<File> it = fr.iterator();
		assertThat(it.hasNext(), is(true));
		File result = it.next();
		String osDependentPath = TestUtils.buildPath("src", "main", "java",
				"org", "walkmod");
		assertThat(result.getAbsolutePath(), containsString(osDependentPath));
	}

	@Test
	public void testExcludes() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util/FileResource.java";
		fr.setExcludes(new String[] { file });

		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		String osDependentPath = TestUtils.buildPath("org", "walkmod", "util",
				"FileResource.java");
		assertThat(f.getAbsolutePath(), not(containsString(osDependentPath)));
	}

	@Test
	public void testExcludesWildcard() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util";
		fr.setExcludes(new String[] { file });
		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		String osDependentPath = TestUtils.buildPath("org", "walkmod", "util");
		assertThat(f.getAbsolutePath(), not(containsString(osDependentPath)));
	}

	@Test
	public void testExcludesWildcard2() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod/util";
		fr.setExcludes(new String[] { file + "/*" });

		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		String osDependentPath = TestUtils.buildPath("org", "walkmod", "util");
		assertThat(f.getAbsolutePath(), not(containsString(osDependentPath)));
	}

	@Test
	public void testExcludesWildcard3() throws Exception {
		FileResource fr = new FileResource();
		fr.setPath(SOURCES_PATH);
		String file = "org/walkmod";
		fr.setExcludes(new String[] { file + "/**" });

		Iterator<File> it = fr.iterator();
		assertThat(it.hasNext(), is(false));
	}

}
