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

import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ParserConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

public class AbstractChainConfigurationProvider {

	public void addDefaultReaderConfig(ChainConfig ac) {
		ReaderConfig readerConfig = new ReaderConfig();
		readerConfig.setPath(null);
		readerConfig.setType(null);
		ac.setReaderConfig(readerConfig);
	}

	public void addDefaultWalker(ChainConfig ac) {
		WalkerConfig wc = new WalkerConfigImpl();
		wc.setType(null);
		wc.setParserConfig(new ParserConfigImpl());
		ac.setWalkerConfig(wc);
	}

	public void addDefaultWriterConfig(ChainConfig ac) {
		WriterConfig wc = new WriterConfigImpl();
		wc.setPath(ac.getReaderConfig().getPath());
		wc.setType(null);
		ac.setWriterConfig(wc);
	}
}
