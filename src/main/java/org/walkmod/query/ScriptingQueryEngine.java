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

package org.walkmod.query;

import groovy.lang.GroovyClassLoader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.scripting.ScriptProcessor;
import org.walkmod.walkers.VisitorContext;

public class ScriptingQueryEngine implements QueryEngine {

    private String language = "groovy";

    // default scripting language
private ScriptEngine engine = null;

    private VisitorContext context = null;

    private static Logger log = Logger.getLogger(ScriptProcessor.class);

    private Bindings bindings;

    private Object rootNode = null;

    private List<String> includes;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(VisitorContext context) {
        initialize(context, Collections.EMPTY_MAP);
    }

    @Override
    public void initialize(VisitorContext context, Map<String, Object> parameters) {
        if (engine == null) {
            ScriptEngineManager factory = new ScriptEngineManager(context.getClassLoader());
            engine = factory.getEngineByName(language);
            if (engine instanceof GroovyScriptEngineImpl) {
                ((GroovyScriptEngineImpl) engine).setClassLoader(new GroovyClassLoader(context.getClassLoader(), new CompilerConfiguration()));
            }
        }
        this.context = context;
        bindings = engine.createBindings();
        Set<String> keys = parameters.keySet();
        if (keys != null) {
            for (String key : keys) {
                Object value = parameters.get(key);
                if (key.equals("node")) {
                    rootNode = value;
                }
                bindings.put(key, value);
            }
        }
    }

    @Override
    public Object resolve(String query) {
        return resolve(rootNode, query);
    }

    @Override
    public Object resolve(Object context, String query) {
        if (context == null) {
            context = rootNode;
        }
        bindings.put("node", context);
        bindings.put("root", rootNode);
        bindings.put("context", this.context);
        try {
            if (query == null) {
                return null;
            }
            if (includes != null) {
                for (String include : includes) {
                    URI uri = this.context.getResource(include);
                    if (uri != null) {
                        String aux = includeExpression(uri.getPath()) + "\n";
                        query = aux + query;
                    }
                }
            }
            return engine.eval(query, bindings);
        } catch (ScriptException e) {
            log.error("The query: [" + query + "] has an error");
            throw new WalkModException(e);
        }
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public String includeExpression(String include) {
        if ("groovy".equals(language)) {
            return "evaluate(new File(\"" + include + "\"));";
        }
        return "";
    }
}
