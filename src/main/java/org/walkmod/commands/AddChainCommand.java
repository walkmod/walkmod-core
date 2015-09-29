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
package org.walkmod.commands;

import java.util.LinkedList;
import java.util.List;

import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.conf.entities.impl.ChainConfigImpl;
import org.walkmod.conf.entities.impl.TransformationConfigImpl;
import org.walkmod.conf.entities.impl.WalkerConfigImpl;
import org.walkmod.conf.entities.impl.WriterConfigImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;

@Parameters(separators = "=", commandDescription = "Adds an empty chain in the walkmod configuration file.")
public class AddChainCommand implements Command {

	@Parameter(names = "--name", description = "The chain identifier")
	private String name = null;

	@Parameter(names = { "-d", "--path", "--reader.path" }, description = "Source code location.")
	private String readerPath = DEFAULT_PATH;

	private static final String DEFAULT_PATH = "src/main/java";

	@Parameter(names = "--reader", description = "Reader JSON object definition", converter = JSONConverter.class)
	private JSONObject reader = null;

	@Parameter(names = "--writer", description = "Writer JSON object definition", converter = JSONConverter.class)
	private JSONObject writer = null;

	@Parameter(names = "--walker", description = "Walker JSON object definition", converter = JSONConverter.class)
	private JSONObject walker = null;

	@Override
	public void execute() throws Exception {
		ChainConfig chainCfg = new ChainConfigImpl();
		chainCfg.setName(name);

		ObjectMapper mapper = new ObjectMapper();
		if (reader != null) {
			ReaderConfig readerCfg = mapper.readValue(reader.toJSONString(), ReaderConfig.class);
			chainCfg.setReaderConfig(readerCfg);
		} else {
			if (!readerPath.equals(DEFAULT_PATH)) {
				ReaderConfig readerCfg = new ReaderConfig();
				readerCfg.setPath(readerPath);
				chainCfg.setReaderConfig(readerCfg);
			}
		}
		if (writer != null) {
			WriterConfig writerCfg = mapper.readValue(writer.toJSONString(), WriterConfigImpl.class);
			chainCfg.setWriterConfig(writerCfg);
		} else {
			if (!readerPath.equals(DEFAULT_PATH)) {
				WriterConfig writerCfg = new WriterConfigImpl();
				writerCfg.setPath(readerPath);
				chainCfg.setWriterConfig(writerCfg);
			}
		}

		if (walker != null) {
			WalkerConfig walkerCfg = new WalkerConfigImpl();
			if (walker.containsKey("transformations")) {
				JSONArray array = walker.getJSONArray("transformations");
				int limit = array.size();
				List<TransformationConfig> transformations = new LinkedList<TransformationConfig>();

				for (int i = 0; i < limit; i++) {
					TransformationConfig tconfig = mapper.readValue(array.getJSONObject(i).toJSONString(),
							TransformationConfigImpl.class);
					transformations.add(tconfig);
				}

				walkerCfg.setTransformations(transformations);

			}
			if (walker.containsKey("type")) {
				walkerCfg.setType(walker.getString("type"));
			}
			if (walker.containsKey("params")) {
				walkerCfg.setParams(walker.getJSONObject("params"));
			}

			chainCfg.setWalkerConfig(walkerCfg);
		} else {
			chainCfg.setWalkerConfig(new WalkerConfigImpl());
		}

		WalkModFacade facade = new WalkModFacade(OptionsBuilder.options());
		facade.addChainConfig(chainCfg);

	}

}
