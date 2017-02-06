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
package org.walkmod.patches;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class Patches {

	public static String generatePatch(String originalText, String text, String location) {
		List<String> original = Arrays.asList(originalText.split("\n"));
		List<String> revised = Arrays.asList(text.split("\n"));

		Patch<String> patches = DiffUtils.diff(original, revised);
		List<String> unifiedDiffs = DiffUtils.generateUnifiedDiff("a" + File.separator + location, "b" + File.separator
				+ location, original, patches, 4);
		Iterator<String> it = unifiedDiffs.iterator();

		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	public static String applyPatch(String text, String patch) throws PatchFailedException {
		List<String> original = Arrays.asList(text.split("\n"));
		List<String> diff = Arrays.asList(patch.split("\n"));

		Patch<String> generatedPatch = DiffUtils.parseUnifiedDiff(diff);
		Iterator<String> it = DiffUtils.patch(original, generatedPatch).iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}
}
