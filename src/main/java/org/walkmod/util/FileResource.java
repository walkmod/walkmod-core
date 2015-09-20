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

package org.walkmod.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.walkmod.Resource;

public class FileResource implements Resource<File> {

	private File file;

	private String[] extensions;

	private String[] includes;

	private String[] excludes;

	public File getFile() {
		return file;
	}

	public void setFile(File file) throws Exception {
		this.file = file.getCanonicalFile();
	}

	public void setPath(String path) throws Exception {
		File f = new File(path);
		setFile(f);
	}

	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}

	private static String[] toSuffixes(String[] extensions) {
		String[] suffixes = new String[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			suffixes[i] = "." + extensions[i];
		}
		return suffixes;
	}

	private boolean matches(String fileName, String filter) {
		
		return filter.startsWith(fileName) || FilenameUtils.wildcardMatch(fileName, filter)
				|| fileName.startsWith(filter);

	}

	@Override
	public Iterator<File> iterator() {
		String fileNormalized = FilenameUtils.normalize(file.getAbsolutePath(), true);
		if (includes != null) {
			for (int i = 0; i < includes.length; i++) {

				if (!includes[i].startsWith(fileNormalized)) {

					includes[i] = fileNormalized + "/" + includes[i];

				}
				if (includes[i].endsWith("**")) {
					includes[i] = includes[i].substring(0, includes[i].length() - 3);
				}
			}
		}
		if (excludes != null) {
			for (int i = 0; i < excludes.length; i++) {

				if (!excludes[i].startsWith(fileNormalized)) {
					excludes[i] = fileNormalized + "/" + excludes[i];

				}
				if (excludes[i].endsWith("**")) {
					excludes[i] = excludes[i].substring(0, excludes[i].length() - 3);
				}
			}
		}

		if (file.isDirectory()) {

			IOFileFilter filter = null;

			IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
			if (excludes != null || includes != null) {

				directoryFilter = new IOFileFilter() {

					@Override
					public boolean accept(File dir, String name) {

						boolean excludesEval = false;
						boolean includesEval = false;
						String aux = FilenameUtils.normalize(name, true);
						if (excludes != null) {
							for (int i = 0; i < excludes.length && !excludesEval; i++) {
								excludesEval = (FilenameUtils.wildcardMatch(aux, excludes[i])
										|| dir.getAbsolutePath().startsWith(excludes[i]));
							}
						}
						if (includes != null) {
							for (int i = 0; i < includes.length && !includesEval; i++) {
								includesEval = matches(aux, includes[i]);
							}
						} else {
							includesEval = true;
						}
						return (includesEval && !excludesEval) || (includes == null && excludes == null);

					}

					@Override
					public boolean accept(File file) {
						boolean excludesEval = false;
						boolean includesEval = false;

						String aux = FilenameUtils.normalize(file.getAbsolutePath(), true);
						if (excludes != null) {

							for (int i = 0; i < excludes.length && !excludesEval; i++) {
								excludesEval = (FilenameUtils.wildcardMatch(aux, excludes[i])
										|| file.getParentFile().getAbsolutePath().startsWith(excludes[i]));
							}
						}
						if (includes != null) {
							for (int i = 0; i < includes.length && !includesEval; i++) {
								includesEval = matches(aux, includes[i]);
							}
						} else {
							includesEval = true;
						}
						boolean result = (includesEval && !excludesEval) || (includes == null && excludes == null);

						return result;

					}
				};
				if (extensions == null) {
					filter = directoryFilter;

				} else {
					String[] suffixes = toSuffixes(extensions);
					filter = new SuffixFileFilter(suffixes);
				}

			} else {
				if (extensions == null) {
					filter = TrueFileFilter.INSTANCE;
				} else {
					String[] suffixes = toSuffixes(extensions);
					filter = new SuffixFileFilter(suffixes);
				}
			}

			return FileUtils.listFiles(file, filter, directoryFilter).iterator();
		}
		Collection<File> aux = new LinkedList<File>();
		if (extensions == null) {
			aux.add(file);
		}
		return aux.iterator();
	}

	@Override
	public String getNearestNamespace(Object element, String regexSeparator) {
		if (element instanceof File) {
			return ((File) element).getParentFile().getPath().replaceAll("/", regexSeparator);
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String getOwnerNamespace(Object element, String regexSeparator) {
		if (element instanceof File) {
			return ((File) element).getParent().replaceAll("/", regexSeparator);
		}
		throw new IllegalArgumentException();
	}

	public String[] getIncludes() {
		return includes;
	}

	public void setIncludes(String[] includes) {
		if (includes != null  && System.getProperty("os.name").toLowerCase().contains("windows")) {
			for (int i = 0; i < includes.length; i++) {
				includes[i] = FilenameUtils.normalize(includes[i], true);
			}
		}
		this.includes = includes;

	}

	public String[] getExcludes() {
		return excludes;
	}

	public void setExcludes(String[] excludes) {
		if (excludes != null && System.getProperty("os.name").toLowerCase().contains("windows")) {
			for (int i = 0; i < excludes.length; i++) {
				excludes[i] = FilenameUtils.normalize(excludes[i], true);
			}
		}
		this.excludes = excludes;
	}
}
