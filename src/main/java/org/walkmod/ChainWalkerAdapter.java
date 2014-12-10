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
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.exceptions.WalkModException;

public interface ChainWalkerAdapter {

	public String getName();

	public void setName(String name);

	public void setTransformationConfig(Collection<TransformationConfig> config);

	public Collection<TransformationConfig> getTransformationConfig();

	public Resource<?> getModel();

	public void setWalker(ChainWalker walker);

	public ChainWalker getWalker();

	public void setWalkerInvocation(ChainWalkerInvocation ta);

	public ChainWalkerInvocation getWalkerInvocation();

	public ChainAdapter getArchitecture();

	public void setArchitectureProxy(ChainAdapter ap);

	public void prepare() throws WalkModException;

	public void execute() throws WalkModException;

	public void setWalkerConfig(WalkerConfig config);

	public WalkerConfig getWalkerConfig();
}
