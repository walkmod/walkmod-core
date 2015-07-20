package org.walkmod.util;

import java.io.File;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class FileResourceTest {

	@Test
	public void testIncludes() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util/FileResource.java";
		fr.setIncludes(new String[] { file });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		Assert.assertEquals(filter.getAbsolutePath(), f.getAbsolutePath());
		Assert.assertEquals(false, it.hasNext());
	}

	@Test
	public void testIncludes2() {
		FileResource fr = new FileResource();
		File aux = new File("src/main/java");
		fr.setPath("src/main/java");
		String file = new File(aux, "org/walkmod/util").getAbsolutePath();
		fr.setIncludes(new String[] { file });
		File filter = new File(file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		Assert.assertTrue(f.getAbsolutePath().startsWith(
				filter.getAbsolutePath()));

	}

	@Test
	public void testIncludesWildcard() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util";
		fr.setIncludes(new String[] { file });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		Assert.assertTrue(f.getAbsolutePath().startsWith(
				filter.getAbsolutePath()));

	}

	@Test
	public void testIncludesWildcard2() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util";
		fr.setIncludes(new String[] { file + "/*" });
		File filter = new File("src/main/java/" + file);
		Iterator<File> it = fr.iterator();
		File f = it.next();
		Assert.assertTrue(f.getAbsolutePath().startsWith(
				filter.getAbsolutePath()));
	}

	@Test
	public void testIncludesWildcard3() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod";
		fr.setIncludes(new String[] { file + "/**" });

		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File result = it.next();
		Assert.assertTrue(result.getAbsolutePath().contains(
				"src/main/java/org/walkmod"));
	}

	@Test
	public void testExcludes() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util/FileResource.java";
		fr.setExcludes(new String[] { file });

		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		Assert.assertTrue(!f.getAbsolutePath().contains(
				"org/walkmod/util/FileResource.java"));

	}

	@Test
	public void testExcludesWildcard() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util";
		fr.setExcludes(new String[] { file });
		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		Assert.assertTrue(!f.getAbsolutePath().contains("org/walkmod/util"));

	}

	@Test
	public void testExcludesWildcard2() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod/util";
		fr.setExcludes(new String[] { file + "/*" });

		Iterator<File> it = fr.iterator();
		Assert.assertTrue(it.hasNext());
		File f = it.next();
		Assert.assertTrue(!f.getAbsolutePath().contains("org/walkmod/util"));

	}

	@Test
	public void testExcludesWildcard3() {
		FileResource fr = new FileResource();
		fr.setPath("src/main/java");
		String file = "org/walkmod";
		fr.setExcludes(new String[] { file + "/**" });

		Iterator<File> it = fr.iterator();

		Assert.assertTrue(!it.hasNext());

	}

}
