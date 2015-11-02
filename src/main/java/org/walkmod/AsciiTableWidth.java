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
package org.walkmod;

import org.apache.commons.lang3.ArrayUtils;

import de.vandermeer.asciitable.commons.ArrayTransformations;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_Width;
import de.vandermeer.asciitable.v2.row.ContentRow;
import de.vandermeer.asciitable.v2.row.V2_Row;

public class AsciiTableWidth implements V2_Width {

	private int[] minWidths = new int[0];
	private int[] maxWidths = new int[0];

	private int maxWidth = 100;

	public AsciiTableWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 * Creates a new width object.
	 * @param minWidth minimum column width as number of characters
	 * @param maxWidth maximum column width as number of characters
	 * @return self to allow for chaining
	 */
	public AsciiTableWidth add(final int minWidth, final int maxWidth) {
		this.minWidths = ArrayUtils.add(this.minWidths, minWidth);
		this.maxWidths = ArrayUtils.add(this.maxWidths, maxWidth);
		return this;
	}

	@Override
	public int[] getColumnWidths(V2_AsciiTable table) {
		int cols = table.getColumnCount();
		int[] resultWidths = new int[cols];

		// apply min width settings
		System.arraycopy(minWidths, 0, resultWidths, 0, minWidths.length > cols ? cols : minWidths.length);

		// iterate over all rows
		for (V2_Row row : table.getTable()) {
			if (row instanceof ContentRow) {
				ContentRow crow = (ContentRow) row;
				Object[] cells = crow.getColumns();

				// iterate over all cells in the row
				for (int i = 0; i < cells.length; i++) {
					String[] lines = ArrayTransformations.PROCESS_CONTENT(cells[i]);
					if (lines != null) {
						// measuring the width of each line within a cell
						for (String line : lines) {
							int lineWidth = line.length() + 2 * crow.getPadding()[i];
							if (lineWidth > maxWidth) {
								lineWidth = maxWidth;
							}
							if (lineWidth > resultWidths[i]) {
								int maxWidth = (maxWidths.length > i) ? maxWidths[i] : 0;
								if (maxWidth < 1 || lineWidth < maxWidth) {
									resultWidths[i] = lineWidth;
								} else {
									resultWidths[i] = maxWidth;
								}
							}
						}
					}
				}
			}
		}
		return resultWidths;
	}

}
