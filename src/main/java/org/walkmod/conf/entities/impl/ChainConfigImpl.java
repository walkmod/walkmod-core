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
package org.walkmod.conf.entities.impl;

import java.util.List;
import java.util.Map;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;

public class ChainConfigImpl implements ChainConfig {

	private String name;

	private ReaderConfig model;

	private Map<String, Object> parameters;

	private List<TransformationConfig> transformations;

	private Configuration configuration;

	private WriterConfig writerConfig;

	private WalkerConfig walkerConfig;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TransformationConfig> getTransformations() {
		return transformations;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setTransformations(List<TransformationConfig> transformations) {
		this.transformations = transformations;
	}

	public ReaderConfig getReaderConfig() {
		return model;
	}

	public void setReaderConfig(ReaderConfig model) {
		this.model = model;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setWalkerConfig(WalkerConfig wc) {
		wc.setChainConfig(this);
		this.walkerConfig = wc;
	}

	@Override
	public WalkerConfig getWalkerConfig() {
		return walkerConfig;
	}

	@Override
	public void setWriterConfig(WriterConfig wc) {
		this.writerConfig = wc;
	}

	@Override
	public WriterConfig getWriterConfig() {
		return writerConfig;
	}
}
