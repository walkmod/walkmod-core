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

import java.io.Closeable;
import java.io.Flushable;

import org.walkmod.walkers.VisitorContext;

public interface ChainWriter extends Closeable, Flushable {

	public String getPath();

	public void setPath(String path);
	
	public void setExcludes(String[] excludes);
	
	public String[] getExcludes();
	
	public void setIncludes(String[] includes);
	
	public String[] getIncludes();
	
	public void write(Object n, VisitorContext vc) throws Exception;
}
