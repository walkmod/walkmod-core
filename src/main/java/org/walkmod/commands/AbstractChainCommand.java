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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.walkmod.OptionsBuilder;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public class AbstractChainCommand {

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help;

    @Parameter(names = "--offline", description = "Resolves the walkmod plugins and their dependencies in offline mode")
    private boolean offline = false;

    @Parameter(names = { "-e",
            "--verbose" }, description = "Prints the stacktrace of the produced error during the execution")
    private Boolean showException = null;

    @Parameter(names = { "-i", "--includes" }, description = "Defines a subset of files of the reader path to include")
    private ArrayList<String> includes = null;

    @Parameter(names = { "-x", "--excludes" }, description = "Defines a subset of files of the reader path to exclude")
    private ArrayList<String> excludes = null;

    @Parameter(names = { "-p", "--path" }, description = "Overrides the reader and writer paths of the selected chains")
    private String path = null;

    @DynamicParameter(names = "-D", description = "Dynamic parameters")
    private Map<String, String> dynamicParams = new HashMap<String, String>();

    @Parameter(description = "[chains to execute. If the chain does not exists, it dynamically creates one with a transformation with the same name]")
    private List<String> parameters = new ArrayList<String>();

    public boolean isHelpNeeded() {
        return help;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public Boolean getShowException() {
        return showException;
    }

    public void setShowException(Boolean showException) {
        this.showException = showException;
    }

    public ArrayList<String> getIncludes() {
        return includes;
    }

    public void setIncludes(ArrayList<String> includes) {
        this.includes = includes;
    }

    public ArrayList<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(ArrayList<String> excludes) {
        this.excludes = excludes;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getDynamicParams() {
        return dynamicParams;
    }

    public void setDynamicParams(Map<String, String> dynamicParams) {
        this.dynamicParams = dynamicParams;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public OptionsBuilder buildOptions() {
        String[] includesArray = null;
        String[] excludesArray = null;

        if (includes != null && !includes.isEmpty()) {
            includesArray = new String[includes.size()];
            includes.toArray(includesArray);
        }
        if (excludes != null && !excludes.isEmpty()) {
            excludesArray = new String[excludes.size()];
            excludes.toArray(excludesArray);
        }

        offline = (offline == true);
        showException = showException != null && (showException == true);
        Map<String, Object> dynamicArgs = new HashMap<String, Object>();
        dynamicArgs.putAll(dynamicParams);

        return OptionsBuilder.options().verbose(true).offline(offline).printErrors(showException)
                .includes(includesArray).excludes(excludesArray).dynamicArgs(dynamicArgs).path(path);
        
     

    }

}
