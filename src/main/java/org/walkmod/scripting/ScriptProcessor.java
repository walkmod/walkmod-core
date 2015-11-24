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
package org.walkmod.scripting;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.query.QueryEngine;
import org.walkmod.query.QueryEngineAware;
import org.walkmod.walkers.VisitorContext;

public class ScriptProcessor implements QueryEngineAware {

	private String language = "groovy";

	private ScriptEngine engine = null;

	private String location;

	private String content;

	private static Logger log = Logger.getLogger(ScriptProcessor.class);

	private QueryEngine queryEngine;

	public void initialize(VisitorContext context, Object node) {
		if (engine == null) {
			ScriptEngineManager factory = new ScriptEngineManager(context.getClassLoader());
			engine = factory.getEngineByName(language);
			if (engine instanceof GroovyScriptEngineImpl) {
				((GroovyScriptEngineImpl) engine).setClassLoader(new GroovyClassLoader(context.getClassLoader(),
						new CompilerConfiguration()));
			}
		}

		if (queryEngine == null) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("language", "groovy");
			List<String> includes = new LinkedList<String>();
			includes.add("query.alias.groovy");
			parameters.put("includes", includes);

			Object bean = context.getBean("org.walkmod.query.ScriptingQueryEngine", parameters);
			if (bean != null) {
				if (bean instanceof QueryEngine) {
					queryEngine = (QueryEngine) bean;
				}

			} else {
				throw new WalkModException("Query Engine not found");
			}
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("node", node);
		queryEngine.initialize(context, params);
	}

	public void visit(Object node, VisitorContext ctx) {
		initialize(ctx, node);

		Bindings bindings = engine.createBindings();
		bindings.put("node", node);
		bindings.put("context", ctx);
		bindings.put("query", getQueryEngine());
		if (content != null) {
			try {
				engine.eval(content, bindings);
			} catch (ScriptException e) {
				log.error("The file " + e.getFileName() + " has an error at line: " + e.getLineNumber() + ", column: "
						+ e.getColumnNumber());
				throw new WalkModException(e);
			}
		} else {
			if (location != null) {
				Reader reader = null;
				File file = new File(location).getAbsoluteFile();
				if (file.exists()) {
					try {
						reader = new FileReader(file);
					} catch (FileNotFoundException e) {
						throw new WalkModException(e);
					}
				} else {
					URL uri = ctx.getClassLoader().getResource(location);
					if (uri != null) {
						try {
							reader = new FileReader(new File(uri.getFile()));
						} catch (FileNotFoundException e) {
							throw new WalkModException(e);
						}
					}
				}
				if (reader != null) {
					try {
						engine.eval(reader, bindings);
					} catch (ScriptException e) {
						throw new WalkModException(e);
					}
				}
			}
		}
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setQueryEngine(QueryEngine queryEngine) {
		this.queryEngine = queryEngine;
	}

	public QueryEngine getQueryEngine() {
		return queryEngine;
	}

	public Object query(String query) {
		if (queryEngine != null) {
			return queryEngine.resolve(query);
		}
		return null;
	}

	public Object query(Object context, String query) {
		if (queryEngine != null) {
			return queryEngine.resolve(context, query);
		}
		return null;
	}

}
