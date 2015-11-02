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
package org.walkmod.walkers;

import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChangeLogPrinter {

	private Map<String, Integer> added;

	private Map<String, Integer> deleted;

	private Map<String, Integer> updated;

	private Map<String, Integer> unmodified;

	private static Logger log = Logger.getLogger(ChangeLogPrinter.class);

	public ChangeLogPrinter(Map<String, Integer> added, Map<String, Integer> updated, Map<String, Integer> deleted,
			Map<String, Integer> unmodified) {
		this.added = added;
		this.updated = updated;
		this.deleted = deleted;
		this.unmodified = unmodified;
	}

	private void printChangesByKey() {
		Set<String> keys = added.keySet();
		for (String key : keys) {
			String aux = String.valueOf(key.charAt(0)).toLowerCase() + key.substring(1);
			String[] label = aux.split("(?=\\p{Upper})");
			String printedLabel = "";
			if (label != null) {
				for (int i = 0; i < label.length; i++) {
					if (!"Declaration".equals(label[i])) {
						if (i > 0) {
							printedLabel = printedLabel + " " + label[i].toLowerCase();
						} else {
							printedLabel = label[i].toLowerCase();
						}
					}
				}
			}
			int total = added.get(key) + deleted.get(key) + updated.get(key) + unmodified.get(key);
			String resume = "";
			if (total == unmodified.get(key)) {
				resume = printedLabel + "s : [changes : 0]";
			} else {
				String addedMsg = "0";
				String deletedMsg = "0";
				String updatedMsg = "0";
				int addedItems = added.get(key);
				int deletedItems = deleted.get(key);
				int updatedItems = updated.get(key);
				if (addedItems > 0) {
					addedMsg = addedItems + "/" + total;
				}
				if (deletedItems > 0) {
					deletedMsg = deletedItems + "/" + total;
				}
				if (updatedItems > 0) {
					updatedMsg = updatedItems + "/" + total;
				}
				resume = printedLabel + "s : [ added: " + addedMsg + ", deleted: " + deletedMsg + ", updated: "
						+ updatedMsg + "]";
			}
			log.debug(resume);
		}
	}

	public void print() {
		printChangesByKey();
	}
}
