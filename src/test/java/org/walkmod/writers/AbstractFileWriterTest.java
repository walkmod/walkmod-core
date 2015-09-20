package org.walkmod.writers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.walkmod.walkers.VisitorContext;

public class AbstractFileWriterTest {

	AbstractFileWriter writer = new AbstractFileWriter() {

		@Override
		public File createOutputDirectory(Object o) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContent(Object n, VisitorContext vc) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Test
	public void testEndLineChar() throws IOException {
		File fileWithCR = File.createTempFile("test", "withCR.txt");
		FileWriter fw = new FileWriter(fileWithCR);
		fw.write("\r\n");
		fw.close();
		File fileWithoutCR = File.createTempFile("test", "withoutCR.txt");
		fw = new FileWriter(fileWithoutCR);
		fw.write("test\n");
		fw.close();
		char endLine = writer.getEndLineChar(fileWithoutCR);
		Assert.assertEquals('\n', endLine);
		endLine = writer.getEndLineChar(fileWithCR);
		Assert.assertEquals('\r', endLine);
		File nonExistent = new File("test");
		endLine = writer.getEndLineChar(nonExistent);
		char expected = '\n';
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			expected = '\r';
		}
		Assert.assertEquals(expected, endLine);
	}

	@Test
	public void testWriteExactlyTheSame() throws IOException {
		File fileWithCR = File.createTempFile("test", "withCR.txt");
		FileWriter fw = new FileWriter(fileWithCR);
		String content = "test\r\n";
		fw.write(content);
		fw.close();
		char aux = writer.getEndLineChar(fileWithCR);
		FileWriter fw2 = new FileWriter(fileWithCR);
		writer.write("test\n", fw2, aux);
		fw2.flush();
		fw2.close();
		FileReader reader = new FileReader(fileWithCR);
		char[] buf = new char[content.length()];
		reader.read(buf);
		reader.close();
		Assert.assertEquals('\r', buf[4]);
	}
}
