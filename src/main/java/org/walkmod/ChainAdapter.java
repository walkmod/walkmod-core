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

import java.util.Map;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.exceptions.WalkModException;

public interface ChainAdapter {

    public void setName(String name);

    public String getName();

    public void setChainConfig(ChainConfig config);

    public ChainConfig getChainConfig();

    public ChainInvocation getChainInvocation();

    public void setChainInvocation(ChainInvocation ai);

    public Resource<?> getResource();

    public void setResource(Resource<?> model);

    public Map<String, Object> getParams();

    public void setParams(Map<String, Object> params);

    public ChainWalkerAdapter getWalkerAdapter();

    public void setWalkerAdapter(ChainWalkerAdapter walkerAdapter);

    public void setChainWriter(ChainWriter writer);

    public ChainWriter getChainWriter();

    public void prepare() throws WalkModException;

    public void execute() throws WalkModException;
}
