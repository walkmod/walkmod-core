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
}
