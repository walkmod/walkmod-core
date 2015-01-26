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
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.walkmod.ChainAdapter;
import org.walkmod.ChainWriter;
import org.walkmod.Resource;
import org.walkmod.ResourceModifier;
import org.walkmod.ChainWalkerAdapter;
import org.walkmod.ChainWalkerInvocation;
import org.walkmod.ChainWalker;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.conf.entities.WalkerConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.walkers.Parser;
import org.walkmod.walkers.ParserAware;

public class DefaultChainWalkerAdapter implements ChainWalkerAdapter {

    private WalkerConfig config;

    private ChainWalker walker;

    private List<Object> visitors = new LinkedList<Object>();

    private String name;

    private ChainWalkerInvocation wi;

    private ChainAdapter ap;

    private Collection<TransformationConfig> transformationConfigs;

    private static final Log LOG = LogFactory.getLog(DefaultChainWalkerAdapter.class);

    @Override
    public Resource<?> getModel() {
        return ap.getResource();
    }

    @Override
    public void setWalker(ChainWalker walker) {
        this.walker = walker;
    }

    @Override
    public ChainWalker getWalker() {
        return walker;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void prepare() throws WalkModException {
        walker.setResource(getModel());
        walker.setRootNamespace(config.getRootNamespace());
        ChainWriter mw = ap.getChainWriter();
        mw.setExcludes(config.getChainConfig().getWriterConfig().getExcludes());
        mw.setIncludes(config.getChainConfig().getWriterConfig().getIncludes());
        walker.setWriter(ap.getChainWriter());
        walker.setChainConfig(config.getChainConfig());
        ChainConfig ac = config.getChainConfig();
        Object visitor;
        Configuration c = ac.getConfiguration();
        Parser parser = null;
        String parserType = config.getParserConfig().getType();
        if (parserType != null) {
            Object parserInstance = c.getBean(parserType, config.getParserConfig().getParameters());
            if (parserInstance != null) {
                if (parserInstance instanceof Parser) {
                    parser = (Parser) parserInstance;
                    walker.setParser(parser);
                } else {
                    throw new WalkModException("The parser " + parserType + " must implement " + Parser.class.getName());
                }
            } else {
                throw new WalkModException("The parser " + parserType + " does not exist.");
            }
        }
        for (TransformationConfig config : getTransformationConfig()) {
            setName(config.getName());
            visitor = config.getVisitorInstance();
            if (visitor == null || "".equals(config.getType())) {
                visitor = c.getBean(config.getType(), config.getParameters());
            }
            if (visitor instanceof ResourceModifier) {
                ((ResourceModifier) visitor).setResource(getModel());
            }
            if (visitor instanceof ParserAware) {
                ((ParserAware) visitor).setParser(parser);
            }
            if (visitor != null) {
                LOG.debug("setting chain[\"" + ac.getName() + "\"].transformation[\"" + getName() + "\"].walker = " + walker.getClass().getName());
                LOG.debug("setting chain[\"" + ac.getName() + "\"].transformation[\"" + getName() + "\"].visitor = " + visitor.getClass().getName());
                visitors.add(visitor);
            } else {
                walker = null;
                visitor = null;
                LOG.debug("Transformation[" + getName() + "] without walker and visitor");
            }
        }
        walker.setVisitors(visitors);
        wi.init(this);
    }

    @Override
    public void execute() throws WalkModException {
        wi.invoke();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setWalkerInvocation(ChainWalkerInvocation wi) {
        this.wi = wi;
    }

    @Override
    public ChainWalkerInvocation getWalkerInvocation() {
        return wi;
    }

    @Override
    public ChainAdapter getArchitecture() {
        return ap;
    }

    @Override
    public void setArchitectureProxy(ChainAdapter ap) {
        this.ap = ap;
        setName(ap.getName());
    }

    @Override
    public void setTransformationConfig(Collection<TransformationConfig> config) {
        this.transformationConfigs = config;
    }

    @Override
    public Collection<TransformationConfig> getTransformationConfig() {
        return transformationConfigs;
    }

    @Override
    public void setWalkerConfig(WalkerConfig config) {
        this.config = config;
        setTransformationConfig(config.getTransformations());
    }

    @Override
    public WalkerConfig getWalkerConfig() {
        return config;
    }
}
