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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.walkmod.ChainAdapter;
import org.walkmod.ChainAdapterFactory;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;

public class DefaultChainAdapterFactory implements ChainAdapterFactory {

	private static final Log LOG = LogFactory
			.getLog(DefaultChainAdapterFactory.class);

	@Override
	public ChainAdapter createChainProxy(Configuration configuration,
			String chain) {

		Collection<ChainConfig> acs = configuration.getChainConfigs();
		Iterator<ChainConfig> it = acs.iterator();
		boolean end = false;
		ChainConfig acfg = null;
		while (it.hasNext() && !end) {
			acfg = it.next();
			end = chain.equals(acfg.getName());
		}
		if (end) {
			LOG.debug("Chain " + chain + " found");
			ChainAdapter ap = new DefaultChainAdapter();
			ap.setChainConfig(acfg);
			ap.setChainInvocation(new DefaultChainInvocation());
			ap.prepare();
			return ap;
		}
		return null;
	}
}
