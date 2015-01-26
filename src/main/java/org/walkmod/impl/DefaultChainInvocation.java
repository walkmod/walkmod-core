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

package org.walkmod.impl;

import org.walkmod.ChainAdapter;
import org.walkmod.ChainInvocation;
import org.walkmod.ChainWalkerAdapter;
import org.walkmod.ChainWalkerInvocation;
import org.walkmod.exceptions.WalkModException;

public class DefaultChainInvocation implements ChainInvocation {

    private ChainAdapter chainAdapter;

    @Override
    public void init(ChainAdapter chainAdapter) {
        this.chainAdapter = chainAdapter;
    }

    @Override
    public void invoke() throws WalkModException {
        ChainWalkerAdapter wa = chainAdapter.getWalkerAdapter();
        ChainWalkerInvocation wi = new DefaultChainWalkerInvocation();
        wi.init(wa);
        wi.invoke();
    }
}
