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
package org.walkmod.conf.providers;

import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.ExecutionModeEnum;
import org.walkmod.conf.entities.Configuration;

public class ExecutionModeProvider implements ConfigurationProvider {

	private ExecutionModeEnum mode;

	private Configuration configuration;

	public ExecutionModeProvider(ExecutionModeEnum mode) {
		this.mode = mode;
	}

	@Override
	public void init(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void load() throws ConfigurationException {
		configuration.setExecutionMode(mode);
		
	}

}
