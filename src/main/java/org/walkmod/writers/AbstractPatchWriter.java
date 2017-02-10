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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.walkmod.walkers.VisitorContext;

public abstract class AbstractPatchWriter extends AbstractFileWriter {

	private boolean patchPerChange = true;

	private boolean patchPerFile = true;

	private String patchFormat = "json";

	private String cause = "walkmod";

	@Override
	public File createOutputDirectory(Object o) {
		return null;
	}

	@Override
	public boolean requiresToAppend(VisitorContext vc) {
		return true;
	}

	@Override
	protected Writer getWriter(File out) throws Exception {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("walkmod.patch").getCanonicalFile(), true), getEncoding()));
	}

	public void setPatchPerChange(boolean patchPerChange) {
		this.patchPerChange = patchPerChange;
	}

	public void setPatchPerFile(boolean patchPerFile) {
		this.patchPerFile = patchPerFile;
	}

	public void setPatchFormat(String format) {
		this.patchFormat = format;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public boolean isPatchPerChange() {
		return patchPerChange;
	}

	public boolean isPatchPerFile() {
		return patchPerFile;
	}

	public String getPatchFormat() {
		return patchFormat;
	}

}
