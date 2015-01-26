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

package org.walkmod.util.location;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationImpl implements Location {

    private final String description;

    private final String URI;

    private final int lineNumber;

    private final int columnNumber;

    public static final LocationImpl UNKNOWN = new LocationImpl(null, null, -1, -1);

    public LocationImpl(String description, String uri) {
        this(description, uri, -1, -1);
    }

    public LocationImpl(String description, String uri, int line, int column) {
        if (uri == null || uri.length() == 0) {
            this.URI = null;
            this.lineNumber = -1;
            this.columnNumber = -1;
        } else {
            this.URI = uri;
            this.lineNumber = line;
            this.columnNumber = column;
        }
        if (description != null && description.length() == 0) {
            description = null;
        }
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getURI() {
        return URI;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public List<String> getSnippet(int padding) {
        List<String> snippet = new ArrayList<String>();
        if (getLineNumber() > 0) {
            try {
                InputStream in = new URL(getURI()).openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                int lineno = 0;
                int errno = getLineNumber();
                String line;
                while ((line = reader.readLine()) != null) {
                    lineno++;
                    if (lineno >= errno - padding && lineno <= errno + padding) {
                        snippet.add(line);
                    }
                }
            } catch (Exception ex) {
            }
        }
        return snippet;
    }
}
