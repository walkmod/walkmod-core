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

import java.util.Collection;
import java.util.List;

import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.walkers.ParserAware;
import org.walkmod.walkers.VisitorMessage;

public interface ChainWalker extends ParserAware{

	public void setResource(Resource<?> resource);

	public void setVisitors(List<Object> visitor);

	public void setWriter(Object writer);

	public void setRootNamespace(String namespace);

	public void setChainConfig(ChainConfig ac);

	public String getRootNamespace();

	public List<Object> getVisitors();

	public Object getWriter();

	public void execute() throws Exception;

	public Collection<VisitorMessage> getVisitorMessages();
	
	public int getNumModifications();
	
	public int getNumAdditions();
	
	public int getNumDeletions();
	
	public boolean reportChanges();
	
	public void setReportChanges(boolean reportChanges);
	
	public boolean hasChanges();

}
