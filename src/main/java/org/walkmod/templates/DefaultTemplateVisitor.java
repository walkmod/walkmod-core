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
package org.walkmod.templates;

import groovy.io.PlatformLineWriter;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.walkmod.ChainWriter;
import org.walkmod.conf.entities.WriterConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.walkers.AbstractWalker;
import org.walkmod.walkers.ParseException;
import org.walkmod.walkers.Parser;
import org.walkmod.walkers.ParserAware;
import org.walkmod.walkers.VisitorContext;

public class DefaultTemplateVisitor implements TemplatesAware, ParserAware {

	private List<String> templates;
	private List<File> templateFiles;
	private List<String> missingTemplates = new LinkedList<String>();
	private File propertiesFile = null;

	private TemplateEngine templateEngine;
	private String rootLabel = null;
	private String output;
	private Parser<?> parser;
	private static Logger log = Logger.getLogger(DefaultTemplateVisitor.class);
	private File currentTemplate;

	private String suffix = ".result";

	public DefaultTemplateVisitor() {
	}

	public DefaultTemplateVisitor(TemplateEngine engine) {
		setTemplateEngine(engine);
	}

	public synchronized void visit(Object node, VisitorContext context)
			throws Exception {
		if (rootLabel == null) {
			setRootLabel("cu");
		}
		if (propertiesFile == null) {
			setProperties("template.properties");
		}
		context.put(getRootLabel(), node);
		if (templateEngine == null) {
			Object bean = context.getBean(
					"org.walkmod.templates.groovy.GroovyTemplateEngine", null);
			if (bean != null && bean instanceof TemplateEngine) {
				templateEngine = (TemplateEngine) bean;
				log.info("Applying [groovy] as a default template engine");
			} else {
				throw new WalkModException("Template engine not found");
			}
		}

		templateEngine.initialize(context, node);

		if (templateFiles != null && templates != null
				&& templateFiles.size() == templates.size()) {

			for (File template : templateFiles) {

				String templateResult = templateEngine.applyTemplate(template,
						propertiesFile);
				Object producedNode = null;
				currentTemplate = template;

				if (parser != null) {
					try {
						producedNode = parser.parse(templateResult, true);
					} catch (ParseException e) {
						log.warn("Error parsing the template "
								+ template.getAbsolutePath()
								+ ". Dumping contents..");

						doPlainOutput(templateResult, context);
					}

				} else {
					doPlainOutput(templateResult, context);
				}
				if (producedNode != null) {
					log.debug("Template successfuly parsed");
					context.addResultNode(producedNode);
				}
			}
		} else {
			if (!missingTemplates.isEmpty()) {
				for (String missing : missingTemplates) {
					log.error("The template " + missing + " is missing");
				}
			}
			throw new WalkModException(
					"There are missing or unexitent templates.");
		}

	}

	public void setSuffix(String suffix) {
		if (suffix != null) {
			suffix = suffix.trim();
			if (suffix.startsWith(".")) {
				if (suffix.length() > 1) {
					suffix = suffix.substring(1);
				} else {
					throw new IllegalArgumentException(
							"The suffix must have at least one letter");
				}
			}
			else if("".equals(suffix)){
				throw new IllegalArgumentException(
						"The suffix must have at least one letter");
			}
			this.suffix = suffix;
		}
	}

	public void doPlainOutput(String templateResult, VisitorContext context)
			throws Exception {
		WriterConfig writerConfig = context.getArchitectureConfig()
				.getWriterConfig();
		ChainWriter chainWriter = writerConfig.getModelWriter();
		if (output == null) {
			String fileName = currentTemplate.getName();
			if (context.containsKey(AbstractWalker.ORIGINAL_FILE_KEY)) {
				log.debug("Original file path found");
				File originalFile = (File) context
						.get(AbstractWalker.ORIGINAL_FILE_KEY);
				String fullPath = originalFile.getPath();
				String readerPath = context.getArchitectureConfig()
						.getReaderConfig().getPath();
				fileName = fullPath.substring(readerPath.length());
				if(fileName.startsWith(File.separator)){
					fileName = fileName.substring(1);
				}

			} else {
				log.debug("working with the template name");
			}
			int pos = fileName.lastIndexOf(".");

			if (pos != -1) {
				log.debug("Removing the existing suffix");
				fileName = fileName.substring(0, pos);
			}

			log.warn("Setting a default output file! [" + fileName + ".result]");
			VisitorContext auxCtxt = new VisitorContext();
			File defaultOutputFile = new File(writerConfig.getPath(), fileName
					+ "." + suffix);
			if (!defaultOutputFile.exists()) {
				log.info("++" + defaultOutputFile.getAbsolutePath());
				defaultOutputFile.getParentFile().mkdirs();
				defaultOutputFile.createNewFile();
			}
			auxCtxt.put(AbstractWalker.ORIGINAL_FILE_KEY, defaultOutputFile);
			chainWriter.write(templateResult, auxCtxt);

		} else {
			String outputFile = output;

			// validates if it is a template name to reduce
			// computation
			char[] chars = outputFile.toCharArray();
			boolean isGString = false;
			for (int i = 0; i < chars.length && !isGString; i++) {
				isGString = chars[i] == '$' || chars[i] == '<';
			}

			if (isGString) {
				GStringTemplateEngine engine = new GStringTemplateEngine();

				Template templateName = engine.createTemplate(output);
				StringWriter stringWriter = new StringWriter();
				Writer platformWriter = new PlatformLineWriter(stringWriter);
				templateName.make(context).writeTo(platformWriter);
				outputFile = platformWriter.toString();

			}

			File file = new File(outputFile);
			VisitorContext auxCtxt = new VisitorContext();
			auxCtxt.put(AbstractWalker.ORIGINAL_FILE_KEY, file);
			auxCtxt.put("append", Boolean.TRUE);
			if (!file.exists()) {
				log.info("++" + file.getAbsolutePath());
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			chainWriter.write(templateResult, auxCtxt);
		}
	}

	public List<String> getTemplates() {
		return templates;
	}

	public void setTemplates(List<String> templates) {
		this.templates = templates;
		if (templates != null) {
			templateFiles = new LinkedList<File>();

			for (String template : templates) {
				File aux = new File(template);
				if (aux.exists()) {
					templateFiles.add(aux);
				} else {
					missingTemplates.add(template);
				}
			}
		}
	}

	public void setTemplateEngine(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	public TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	@Override
	public String getRootLabel() {
		return rootLabel;
	}

	@Override
	public void setRootLabel(String rootLabel) {
		this.rootLabel = rootLabel;
	}

	public void setProperties(String propertiesFile) {
		File properties = new File(propertiesFile);
		if (properties.exists()) {
			this.propertiesFile = properties;
		}

	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}

	@Override
	public void setParser(Parser<?> parser) {
		this.parser = parser;

	}

	@Override
	public Parser<?> getParser() {
		return parser;
	}

}
