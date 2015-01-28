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

package org.walkmod.walkers;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.walkmod.conf.entities.ChainConfig;

public class VisitorContext extends HashMap<String, Object> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2667087124958777943L;

	private static final String KEY_AST_RESULT_NODES = "result";

	private Collection<String> visitorMessages = new LinkedList<String>();

	private ChainConfig ac = null;

	public VisitorContext() {
	}

	public VisitorContext(ChainConfig ac) {
		this();
		this.ac = ac;
	}

	public boolean addResultNode(Object node) {
		if (!super.containsKey(KEY_AST_RESULT_NODES)) {
			super.put(KEY_AST_RESULT_NODES, new LinkedList<Object>());
		}
		@SuppressWarnings("unchecked")
		Collection<Object> collection = (Collection<Object>) get(KEY_AST_RESULT_NODES);
		Iterator<Object> it = collection.iterator();
		while (it.hasNext()) {
			if (it.next() == node) {
				it.remove();
			}
		}
		return collection.add(node);
	}

	public boolean addAllResultNodes(Collection<Object> nodes) {
		if (!super.containsKey(KEY_AST_RESULT_NODES)) {
			super.put(KEY_AST_RESULT_NODES, new LinkedList<Object>());
		}
		Collection<Object> collection = (Collection<Object>) get(KEY_AST_RESULT_NODES);
		boolean added = false;
		for (Object o : nodes) {
			added = addResultNode(o) || added;
		}
		return added;
	}

	@Override
	public Object remove(Object key) {
		if (!KEY_AST_RESULT_NODES.equals(key)) {
			return super.remove(key);
		}
		return null;
	}

	@Override
	public Object put(String key, Object value) {
		if (!KEY_AST_RESULT_NODES.equals(key)) {
			return super.put(key, value);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Object> getResultNodes() {
		Collection<Object> result;
		if (super.containsKey(KEY_AST_RESULT_NODES)) {
			result = (Collection<Object>) get(KEY_AST_RESULT_NODES);
		} else {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	public boolean hasResultNodes() {
		return !getResultNodes().isEmpty();
	}

	public ChainConfig getArchitectureConfig() {
		return ac;
	}

	public void addTransformationMessage(String message) {
		this.visitorMessages.add(message);
	}

	public Collection<String> getVisitorMessages() {
		return visitorMessages;
	}

	public Object getBean(String name, Map<?, ?> parameters) {
		return getArchitectureConfig().getConfiguration().getBean(name,
				parameters);
	}

	public ClassLoader getClassLoader() {
		// for unitary test proposal
		if (getArchitectureConfig() == null) {
			return Thread.currentThread().getContextClassLoader();
		}
		return getArchitectureConfig().getConfiguration().getClassLoader();
	}

	public URI getResource(String resource) {
		URL url = getClassLoader().getResource(resource);
		if (url == null) {
			File file = new File(resource);
			if (file.exists()) {
				return file.toURI();
			} else {
				return null;
			}
		} else {
			try {
				return url.toURI();
			} catch (URISyntaxException e) {
				return null;
			}
		}
	}
}
