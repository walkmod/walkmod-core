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
package org.walkmod.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DomConstants {
    public final static Map<String, String> WALKMOD_DTD_MAPPINGS;

    static {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("-//WALKMOD//DTD", "walkmod-1.1.dtd");
        mappings.put("-//WALKMOD//DTD//1.0", "walkmod-1.0.dtd");
        mappings.put("-//WALKMOD//DTD//1.1", "walkmod-1.1.dtd");
        WALKMOD_DTD_MAPPINGS = Collections.unmodifiableMap(mappings);
    }
}
