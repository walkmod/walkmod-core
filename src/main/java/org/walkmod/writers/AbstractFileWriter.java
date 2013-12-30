/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/

package org.walkmod.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.walkmod.ChainWriter;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.walkers.AbstractWalker;
import org.walkmod.walkers.VisitorContext;

public abstract class AbstractFileWriter implements ChainWriter {

	private String[] excludes;

	private String[] includes;

	private File outputDirectory;

	private String normalizedOutputDirectory;
	
	private String encoding = "UTF-8";
	
	private static Logger log = Logger.getLogger(AbstractFileWriter.class);

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = new File(outputDirectory);
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdir();
		}
		normalizedOutputDirectory = FilenameUtils.normalize(
				this.outputDirectory.getPath(), true);
	}

	public File getOutputDirectory() {
		return this.outputDirectory;
	}
	
	public abstract void createOutputDirectory(Object o);

	public void write(Object n, VisitorContext vc) throws Exception {
		File out = null;
		if (vc != null) {
			out = (File) vc.get(AbstractWalker.ORIGINAL_FILE_KEY);
		}
		if (out == null) {
			createOutputDirectory(n);
		}
		boolean write = true;
		if (out != null) {
			String aux = FilenameUtils.normalize(out.getPath(), true);
			if (excludes != null) {
				for (int i = 0; i < excludes.length && write; i++) {
					if (!excludes[i].startsWith(normalizedOutputDirectory)) {
						excludes[i] = normalizedOutputDirectory + "/"
								+ excludes[i];
						if (excludes[i].endsWith("\\*\\*")) {
							excludes[i] = excludes[i].substring(0,
									excludes[i].length() - 2);
						}
					}
					write = !(excludes[i].startsWith(aux) || FilenameUtils
							.wildcardMatch(aux, excludes[i]));
				}
			}
			if (includes != null && write) {
				write = false;
				for (int i = 0; i < includes.length && !write; i++) {
					if (!includes[i].startsWith(normalizedOutputDirectory)) {
						includes[i] = normalizedOutputDirectory + "/"
								+ includes[i];
						if (includes[i].endsWith("\\*\\*")) {
							includes[i] = includes[i].substring(0,
									includes[i].length() - 2);
						}
					}
					write = includes[i].startsWith(aux)
							|| FilenameUtils.wildcardMatch(aux, includes[i]);
				}
			}
			if (write) {
				Writer writer = null;
				
				try {
					
					String content = getContent(n, vc);
					if(content != null && !"".equals(content)){
						
						writer = new BufferedWriter(new OutputStreamWriter(
							    new FileOutputStream(out), getEncoding()));
						writer.write(content);
						log.debug(out.getPath()+" written ");
					}
					else{
						log.error(out.getPath()+" does not have valid content");
						throw new WalkModException("blank code is returned");
					}					
				} 
				finally{
					if(writer != null){						
						writer.close();
						
					}
				}
			}
			else{
				log.debug("skipping "+out.getParent());
			}
		}
	}

	public abstract String getContent(Object n, VisitorContext vc);

	@Override
	public void close() throws IOException {
	}

	

	@Override
	public void flush() throws IOException {
	}

	

	public void setPath(String path) {
		setOutputDirectory(path);
	}

	public String getPath() {
		return this.outputDirectory.getPath();
	}

	@Override
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	@Override
	public String[] getExcludes() {
		return excludes;
	}

	@Override
	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	@Override
	public String[] getIncludes() {
		return includes;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
		log.debug("[encoding]:"+encoding);
	}
	
	
}
