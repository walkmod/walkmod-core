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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.walkmod.conf.ConfigurationException;
import org.walkmod.conf.ConfigurationProvider;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.PluginConfig;

public class IvyConfigurationProvider implements ConfigurationProvider {

	private Configuration configuration;

	private boolean isOffLine;

	public IvyConfigurationProvider() {
		this(false);
	}

	public IvyConfigurationProvider(boolean isOffLine) {
		setOffLine(isOffLine);
	}

	@Override
	public void init(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void load() throws ConfigurationException {
		Collection<PluginConfig> plugins = configuration.getPlugins();
		PluginConfig plugin = null;
		Collection<File> jarsToLoad = new LinkedList<File>();
		try {
			if (plugins != null) {
				Iterator<PluginConfig> it = plugins.iterator();
				while (it.hasNext()) {
					plugin = it.next();
					Collection<File> dependencies = resolveArtifact(
							plugin.getGroupId(), plugin.getArtifactId(),
							plugin.getVersion());
					jarsToLoad.addAll(dependencies);
				}
				URL[] urls = new URL[jarsToLoad.size()];
				int i = 0;
				for (File jar : jarsToLoad) {
					urls[i] = jar.toURI().toURL();
					i++;
				}
				URLClassLoader childClassLoader = new URLClassLoader(urls,
						configuration.getClassLoader());
				configuration.setClassLoader(childClassLoader);
			}
		} catch (Exception e) {
			throw new ConfigurationException("Unable to resolve the plugin: "
					+ plugin.getGroupId() + " : " + plugin.getArtifactId()
					+ " : " + plugin.getVersion(), e);
		}
	}

	public Collection<File> resolveArtifact(String groupId, String artifactId,
			String version) throws Exception {
		// creates clear ivy settings
		IvySettings ivySettings = new IvySettings();
		File settings = new File("ivysettings.xml");
		if (!settings.exists()) {
			settings = new File(ClassLoader
					.getSystemResource("ivysettings.xml").toURI());
		}
		ivySettings.load(settings);
		// creates an Ivy instance with settings
		Ivy ivy = Ivy.newInstance(ivySettings);
		File ivyfile = File.createTempFile("ivy", ".xml");
		ivyfile.deleteOnExit();
		String[] dep = null;
		dep = new String[] { groupId, artifactId, version };
		DefaultModuleDescriptor md = DefaultModuleDescriptor
				.newDefaultInstance(ModuleRevisionId.newInstance(dep[0], dep[1]
						+ "-caller", "working"));
		DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
				ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false,
				false, true);
		md.addDependency(dd);
		ExcludeRule er = new DefaultExcludeRule(new ArtifactId(new ModuleId(
				"org.walkmod", "walkmod-core"), PatternMatcher.ANY_EXPRESSION,
				PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION),
				ExactPatternMatcher.INSTANCE, null);
		dd.addExcludeRule(null, er);
		// creates an ivy configuration file
		XmlModuleDescriptorWriter.write(md, ivyfile);
		String[] confs = new String[] { "default" };
		ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs);
		if (isOffLine) {
			resolveOptions = resolveOptions.setUseCacheOnly(true);
		} else {
			Map<String, Object> params = configuration.getParameters();
			if (params != null) {
				String offlineOpt = params.get("offline").toString();
				if (offlineOpt != null) {
					boolean offline = Boolean.parseBoolean(offlineOpt);
					if (offline) {
						resolveOptions = resolveOptions.setUseCacheOnly(true);
					}
				}
			}
		}
		// init resolve report
		ResolveReport report = ivy.resolve(ivyfile.toURL(), resolveOptions);
		ArtifactDownloadReport[] artifacts = report.getAllArtifactsReports();
		Collection<File> result = new LinkedList<File>();
		int i = 0;
		for (ArtifactDownloadReport item : artifacts) {
			result.add(item.getLocalFile());
			i++;
		}
		return result;
	}

	public void setOffLine(boolean isOffLine) {
		this.isOffLine = isOffLine;
	}

	public boolean isOffLine() {
		return isOffLine;
	}
}
