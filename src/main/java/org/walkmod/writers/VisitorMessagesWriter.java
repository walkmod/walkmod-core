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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.walkmod.ChainWriter;
import org.walkmod.walkers.AbstractWalker;
import org.walkmod.walkers.VisitorContext;

public class VisitorMessagesWriter 
		implements ChainWriter {

	private static Logger log = Logger.getLogger(VisitorMessagesWriter.class);

	private String[] includes;

	private String[] excludes;



	@Override
	public void close() throws IOException {
		log.info("WRITE SUCCESSFUL");
	}

	
	@Override
	public void flush() throws IOException {
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public void setPath(String path) {
		
	}

	public void write(Object n, VisitorContext vc) {

		File out = null;
		if (vc != null) {
			out = (File) vc.get(AbstractWalker.ORIGINAL_FILE_KEY);
		}
	
		boolean write = true;

		if (out != null) {
			if (excludes != null) {
				for (int i = 0; i < excludes.length && write; i++) {
					write = !(FilenameUtils.wildcardMatch(out.getPath(),
							excludes[i]));
				}
			}

			if (includes != null && write) {
				write = false;
				for (int i = 0; i < includes.length && !write; i++) {
					write = FilenameUtils.wildcardMatch(out.getPath(),
							includes[i]);
				}
			}
			if (write) {
				Collection<String> messages = vc.getVisitorMessages();
				if (messages != null) {
					for (String message : messages) {
						log.info(message);
					}
				}
			}
		}

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
}
