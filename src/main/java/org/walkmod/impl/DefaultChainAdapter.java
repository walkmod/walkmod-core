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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.walkmod.ChainAdapter;
import org.walkmod.ChainInvocation;
import org.walkmod.ChainReader;
import org.walkmod.ChainWriter;
import org.walkmod.Resource;
import org.walkmod.ChainWalker;
import org.walkmod.ChainWalkerAdapter;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.ReaderConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.exceptions.WalkModException;

public class DefaultChainAdapter implements ChainAdapter {

	private String name;

	private ChainConfig ac;

	private ChainInvocation ai;

	private Resource<?> model;

	private Map<String, Object> params;

	private ChainWalkerAdapter walkerAdapter;

	private ChainWriter modelWriter;

	private static final Log LOG = LogFactory.getLog(DefaultChainAdapter.class);

	public DefaultChainAdapter() {
		params = new HashMap<String, Object>();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setChainConfig(ChainConfig ac) {
		this.ac = ac;
	}

	@Override
	public ChainConfig getChainConfig() {
		return ac;
	}

	@Override
	public ChainInvocation getChainInvocation() {
		return ai;
	}

	@Override
	public void setChainInvocation(ChainInvocation ai) {
		this.ai = ai;
	}

	@Override
	public Resource<?> getResource() {
		return model;
	}

	@Override
	public void setResource(Resource<?> model) {
		this.model = model;
	}

	@Override
	public Map<String, Object> getParams() {
		return params;
	}

	@Override
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public void prepare() throws WalkModException {
		setName(ac.getName());
		ReaderConfig readerConfig = ac.getReaderConfig();
		WriterConfig writerConfig = ac.getWriterConfig();
		String modelName = readerConfig.getPath();
		String modelType = readerConfig.getType();
		ChainReader reader = readerConfig.getModelReader();
		if (reader == null) {
			try {
				reader = (ChainReader) ac.getConfiguration().getBean(modelType,
						readerConfig.getParameters());
			} catch (Exception e2) {
				throw new WalkModException("The model " + modelName
						+ ", whose type is " + modelType
						+ "in the architecture " + getName()
						+ " cannot be loaded ", e2);
			}
		}
		readerConfig.setModelReader(reader);
		reader.setPath(readerConfig.getPath());
		reader.setExcludes(readerConfig.getExcludes());
		reader.setIncludes(readerConfig.getIncludes());
		try {
			setResource(reader.read());
			LOG.debug("Model " + modelName + " loaded");
		} catch (Exception e2) {
			throw new WalkModException("The model " + modelName
					+ ", whose type is " + modelType + "in the architecture "
					+ getName() + " cannot be read ", e2);
		}
		WalkerConfig wc = ac.getWalkerConfig();
		ChainWalkerAdapter wa = new DefaultChainWalkerAdapter();
		setWalkerAdapter(wa);
		ChainWalker walker = wc.getWalker();
		if (walker == null) {
			walker = (ChainWalker) ac.getConfiguration().getBean(wc.getType(),
					wc.getParams());
		}
		wc.setWalker(walker);
		wa.setWalker(walker);
		wa.setWalkerConfig(wc);
		wa.setArchitectureProxy(this);
		wa.setWalkerInvocation(new DefaultChainWalkerInvocation());
		ChainWriter writer = writerConfig.getModelWriter();
		if (writer == null) {
			try {
				writer = (ChainWriter) ac.getConfiguration().getBean(
						writerConfig.getType(), writerConfig.getParams());
			} catch (Exception e2) {
				throw new WalkModException("The writer " + ", whose type is "
						+ writerConfig.getType() + "in the architecture "
						+ getName() + " cannot be read ", e2);
			}
		}
		writerConfig.setModelWriter(writer);
		writer.setPath(writerConfig.getPath());
		setChainWriter(writer);
		wa.prepare();
		ai.init(this);
	}

	@Override
	public void execute() throws WalkModException {
		ai.invoke();
	}

	@Override
	public ChainWalkerAdapter getWalkerAdapter() {
		return walkerAdapter;
	}

	@Override
	public void setWalkerAdapter(ChainWalkerAdapter walkerAdapter) {
		this.walkerAdapter = walkerAdapter;
	}

	@Override
	public void setChainWriter(ChainWriter writer) {
		this.modelWriter = writer;
	}

	@Override
	public ChainWriter getChainWriter() {
		return modelWriter;
	}
}
