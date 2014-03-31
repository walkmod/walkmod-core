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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.walkmod.ChainWalker;
import org.walkmod.Resource;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.TransformationConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.merger.CollectionUtil;
import org.walkmod.merger.IdentificableNode;
import org.walkmod.merger.MergeEngine;
import org.walkmod.merger.Mergeable;

public abstract class AbstractWalker implements ChainWalker{

	public static final String NAMESPACE_SEPARATOR = "::";

	private List<Object> visitor;

	private Object writer;

	private Resource<?> resource;

	private String rootNamespace;

	private ChainConfig chainConfig;

	private Set<Object> visitedElements;

	private Collection<VisitorMessage> visitorMessages;

	private static Logger log = Logger.getLogger(AbstractWalker.class);
	
	public static final String ORIGINAL_FILE_KEY = "original_file_key";

	public AbstractWalker() {
		this.visitedElements = new HashSet<Object>();
		this.visitorMessages = new LinkedList<VisitorMessage>();
	}

	@Override
	public void setVisitors(List<Object> visitor) {
		this.visitor = visitor;
	}

	protected void visit(Object element, List<Object> visitors,
			List<TransformationConfig> transformations, VisitorContext context)
			throws Exception {
		if (rootNamespace != null && !"".equals(rootNamespace)) {
			String qualifiedName = getResource().getNearestNamespace(element,
					NAMESPACE_SEPARATOR);
			if (rootNamespace.startsWith(qualifiedName)) {
				return;
			}
		}
		int index = 0;
		for (Object visitor : visitors) {
			boolean isMergeable = transformations.get(index).isMergeable();
			String mergePolicy = transformations.get(index).getMergePolicy();
			if (mergePolicy != null) {
				isMergeable = true;
			}
			if (isMergeable && mergePolicy == null) {
				mergePolicy = "default";
			}
			Method[] methods = visitor.getClass().getMethods();
			List<Object> restVisitors = visitors.subList(index + 1,
					visitors.size());
			List<TransformationConfig> restTransformations = transformations
					.subList(index + 1, transformations.size());
			Set<String> visitedTypes = new HashSet<String>();
			for (int j = 0; j < methods.length; j++) {
				if (methods[j].getName().equals("visit")) {
					Class<?> type = methods[j].getParameterTypes()[0];
					if ((!visitedTypes.contains(element.getClass().getName()))
							&& type.isInstance(element)) {
						visitedTypes.add(type.getName());
						int paramsLength = methods[j].getParameterTypes().length;
						Object[] params = new Object[paramsLength];
						params[0] = element;
						VisitorContext args = new VisitorContext(
								getChainConfig());
						if (paramsLength == 2) {
							params[1] = args;
						}
						methods[j].invoke(visitor, params);
						context.getVisitorMessages().addAll(
								args.getVisitorMessages());
						MergeEngine me = null;
						if (isMergeable) {
							me = chainConfig.getConfiguration().getMergeEngine(
									mergePolicy);
						}
						if (args.hasResultNodes()) {

							Iterator<Object> it = args.getResultNodes()
									.iterator();

							while (it.hasNext()) {
								Object currentArg = it.next();
								if (isMergeable) {
									currentArg = merge(currentArg, me, context);
								}
								
								context.addResultNode(currentArg);		
								
								visit(currentArg, restVisitors,
										restTransformations, context);
								return;

							}
						} else {
							context.addResultNode(element);
						}
					}
				}
			}
			index++;
		}
	}

	protected abstract Object getSourceNode(Object targetNode);

	protected void visit(Object element) throws Exception {
		VisitorContext context = new VisitorContext(getChainConfig());
		visit(element, context);
		addVisitorMessages(context);
	}

	protected void visit(Object element, VisitorContext vc) throws Exception {
		Collection<TransformationConfig> colTransformations = getChainConfig()
				.getWalkerConfig().getTransformations();
		List<TransformationConfig> transformations;
		if (colTransformations instanceof List) {
			transformations = (List<TransformationConfig>) getChainConfig()
					.getWalkerConfig().getTransformations();
		} else {
			transformations = new LinkedList<TransformationConfig>(
					colTransformations);
		}
		visit(element, getVisitors(), transformations, vc);
		if (vc.getResultNodes() != null) {
			writeAll(vc.getResultNodes());
		}
	}

	protected void writeAll(Collection<Object> elements) throws Exception {
		if (elements != null) {
			Iterator<Object> it = elements.iterator();
			while (it.hasNext()) {
				write(it.next());
			}
		}
	}

	protected void write(Object element) throws Exception {
		write(element, null);
	}

	protected void write(Object element, VisitorContext vc) throws Exception {
		Method[] methods = writer.getClass().getMethods();
		for (int j = 0; j < methods.length; j++) {
			if (methods[j].getName().equals("write")) {
				Class<?> type = methods[j].getParameterTypes()[0];
				int paramsLength = methods[j].getParameterTypes().length;
				Object[] params = new Object[paramsLength];
				params[0] = element;
				if (paramsLength == 2) {
					params[1] = vc;
				}
				if (type.isInstance(element)) {
					methods[j].invoke(writer, params);
				}
			}
		}
	}

	public boolean isVisitable(Object element) throws Exception {
		if (rootNamespace != null && !"".equals(rootNamespace)) {
			String qualifiedName = getResource().getNearestNamespace(element,
					NAMESPACE_SEPARATOR);
			if (!qualifiedName.startsWith(rootNamespace)) {
				return false;
			}
		}
		return visitedElements.add(element);
	}

	@Override
	public List<Object> getVisitors() {
		return visitor;
	}

	@Override
	public void setResource(Resource<?> resource) {
		this.resource = resource;
	}

	public Resource<?> getResource() {
		return resource;
	}

	public Set<Object> getVisitedElements() {
		return visitedElements;
	}

	public void setVisitedElements(Set<Object> visitedElements) {
		this.visitedElements = visitedElements;
	}

	@Override
	public void setRootNamespace(String namespace) {
		this.rootNamespace = namespace;
	}

	public String getRootNamespace() {
		return rootNamespace;
	}

	public void walk(Object element) throws Exception {
		if (element != null) {
			Collection<java.lang.Class<?>> types = new LinkedList<Class<?>>();
			types.add(element.getClass());
			Queue<java.lang.Class<?>> interfaces = new ConcurrentLinkedQueue<java.lang.Class<?>>(
					types);
			Collection<java.lang.Class<?>> visitedTypes = new LinkedList<java.lang.Class<?>>();
			while (interfaces.size() > 0) {
				java.lang.Class<?> type = interfaces.poll();
				if (visitedTypes.add(type)) {
					try {
						Method m = this.getClass().getMethod("accept", type);
						m.invoke(this, element);
						interfaces.addAll(Arrays.asList(type.getInterfaces()));
					} catch (NoSuchMethodException e) {
					}
				}
			}
		}
	}

	@Override
	public void execute() throws Exception {
		Iterator<?> it = getResource().iterator();
		while (it.hasNext()) {
			Object current = it.next();
			try {
				walk(current);
			} catch (WalkModException e) {
				log.error(e.getMessage());
			}
		}
	}

	@Override
	public void setWriter(Object writer) {
		this.writer = writer;
	}

	public Object getWriter() {
		return writer;
	}

	public ChainConfig getChainConfig() {
		return chainConfig;
	}

	public void setChainConfig(ChainConfig chainConfig) {
		this.chainConfig = chainConfig;
	}

	public Collection<VisitorMessage> getVisitorMessages() {
		return visitorMessages;
	}

	protected void addVisitorMessages(VisitorContext ctx) {
		Collection<String> messages = ctx.getVisitorMessages();
		String location = getLocation(ctx);
		for (String message : messages) {
			VisitorMessage m = new VisitorMessage(location, message);
			this.visitorMessages.add(m);
		}
	}

	protected abstract String getLocation(VisitorContext ctx);

	protected Object merge(Object object, MergeEngine mergeEngine,
			VisitorContext vc) {

		Object local = null;
		Collection<Object> rnodes = vc.getResultNodes();
		boolean previousResult = false;
		if (object instanceof IdentificableNode) {
			local = CollectionUtil.findObject(rnodes, object,
					((IdentificableNode) object).getIdentityComparator());
		} else {
			Iterator<Object> it = rnodes.iterator();
			while (it.hasNext() && local == null) {
				Object current = it.next();
				if (current.equals(object)) {
					local = current;
				}
			}
		}
		previousResult = local != null;
		if (!previousResult) {
			local = getSourceNode(object);
		}

		if (local != null) {
			if (object instanceof Mergeable) {
				((Mergeable) local).merge(object, mergeEngine);
				if (!previousResult) {
					vc.addResultNode(local);
				}
				return local;
			} else {
				if (!previousResult) {
					vc.addResultNode(object);
				}
				return object;
			}
		}
		if (!previousResult) {
			vc.addResultNode(local);
		}
		return local;
	}
	
	@Override
	public boolean hasChanges() {
		return !(getNumModifications() == 0 && getNumAdditions() == 0
				&& getNumDeletions() == 0);
	}
}
