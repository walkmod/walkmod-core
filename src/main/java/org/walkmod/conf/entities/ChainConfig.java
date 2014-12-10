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
package org.walkmod.conf.entities;

import java.util.Map;

public interface ChainConfig {

	public String getName();

	public void setName(String name);

	public void setParameters(Map<String, Object> parameters);

	public Map<String, Object> getParameters();

	public ReaderConfig getReaderConfig();

	public void setReaderConfig(ReaderConfig model);

	public Configuration getConfiguration();

	public void setConfiguration(Configuration configuration);

	public void setWalkerConfig(WalkerConfig wc);

	public WalkerConfig getWalkerConfig();

	public void setWriterConfig(WriterConfig wc);

	public WriterConfig getWriterConfig();
}
