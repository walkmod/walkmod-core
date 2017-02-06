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

package org.walkmod.writers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.walkmod.ChainWriter;
import org.walkmod.walkers.AbstractWalker;
import org.walkmod.walkers.VisitorContext;

public abstract class AbstractFileWriter implements ChainWriter {

    private String[] excludes;

    private String[] includes;

    private File outputDirectory;

    private String normalizedOutputDirectory;

    private String encoding = "UTF-8";

    private String platform = null;

    private static final String UNIX = "unix";
    private static final String MAC = "mac";
    private static final String WINDOWS = "windows";
    
    private boolean createdEmptyFile = false;

    private static Logger log = Logger.getLogger(AbstractFileWriter.class);

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = new File(outputDirectory);
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdir();
        }
        normalizedOutputDirectory = FilenameUtils.normalize(this.outputDirectory.getAbsolutePath(), true);
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    public abstract File createOutputDirectory(Object o);

    protected boolean isValid(File out) throws IOException {

        boolean write = true;
        if (out != null) {
            String aux = FilenameUtils.normalize(out.getCanonicalPath(), true);
            if (excludes != null) {
                for (int i = 0; i < excludes.length && write; i++) {
                    if (!excludes[i].startsWith(normalizedOutputDirectory)) {
                        excludes[i] = normalizedOutputDirectory + "/" + excludes[i];
                        if (excludes[i].endsWith("\\*\\*")) {
                            excludes[i] = excludes[i].substring(0, excludes[i].length() - 2);
                        }
                    }
                    write = !(excludes[i].startsWith(aux) || FilenameUtils.wildcardMatch(aux, excludes[i]));
                }
            }
            if (includes != null && write) {
                write = false;
                for (int i = 0; i < includes.length && !write; i++) {
                    if (!includes[i].startsWith(normalizedOutputDirectory)) {
                        includes[i] = normalizedOutputDirectory + "/" + includes[i];
                        if (includes[i].endsWith("\\*\\*")) {
                            includes[i] = includes[i].substring(0, includes[i].length() - 2);
                        }
                    }

                    write = includes[i].startsWith(aux) || FilenameUtils.wildcardMatch(aux, includes[i]);
                }
            }
        }
        return write;
    }
    
    public boolean requiresToAppend(VisitorContext vc){
        return vc.get("append") == null || !Boolean.TRUE.equals(vc.get("append"));   
    }
    
    protected Writer getWriter(File out) throws UnsupportedEncodingException, FileNotFoundException{
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), getEncoding()));
    }
    
    protected File getOutputFile(Object n, VisitorContext vc){
        File out = null;
        if (vc != null) {
            out = (File) vc.get(AbstractWalker.ORIGINAL_FILE_KEY);
        }
        if (out == null) {
            log.debug("Creating the target source file. This is not the original source file.");
            out = createOutputDirectory(n);
            createdEmptyFile = true;
        } else {
            log.debug("The system will overwrite the original source file.");
        }
        return out;
    }

    public void write(Object n, VisitorContext vc) throws Exception {

        File out = getOutputFile(n, vc);
        
        if (out != null) {

            if (isValid(out)) {
                Writer writer = null;

                try {
                    vc.put("outFile", out);
                    String content = getContent(n, vc);
                    vc.remove("outFile");
                    if (content != null && !"".equals(content)) {
                        char endLineChar = getEndLineChar(out);
                        writer = getWriter(out);
                        if (requiresToAppend(vc)) {
                            append(content, writer, endLineChar); 
                        } else {
                            write(content, writer, endLineChar);
                        }
                        Summary.getInstance().addFile(out);
                        log.debug(out.getPath() + " written ");
                    }
                } finally {
                    if (writer != null) {
                        writer.close();

                    }
                }
            } else {
                if (createdEmptyFile && out != null && out.isFile()) {
                    out.delete();
                }
                log.debug("skipping " + out.getParent());
            }
        } else {
            log.debug("There is no place where to write.");
        }
    }

    public void write(String content, Writer writer, char endLineChar) throws IOException {
        char[] buffer = content.toCharArray();

        String endLine = "\n";
        if (endLineChar == '\r') {
            endLine = "\r\n";
        }
        if (platform != null) {
            if (platform.equals(UNIX)) {
                endLine = "\n";
            } else if (platform.equals(WINDOWS)) {
                endLine = "\r\n";
            } else if (platform.equals(MAC)) {
                endLine = "\r";
            }
        }
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == '\n') {
                writer.write(endLine);
            } else if (buffer[i] == '\r') {
                writer.write(endLine);
                if (i + 1 < buffer.length && buffer[i + 1] == '\n') {
                    i++;
                }
            } else {
                writer.write(buffer[i]);
            }
        }

    }

    public void append(String content, Writer writer, char endLineChar) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line = reader.readLine();
        String endLine = "\n";
        if (endLineChar == '\r') {
            endLine = "\r\n";
        }
        while (line != null) {
            writer.append(line + endLine);
            line = reader.readLine();
        }
    }

    public char getEndLineChar(File file) throws IOException {
        char endLineChar = '\n';
        if (file.exists()) {
            FileReader reader = new FileReader(file);
            try {
                char[] buffer = new char[150];
                boolean detected = false;
                int bytes = reader.read(buffer);
                char previousChar = '\0';
                while (bytes > 0 && !detected) {
                    for (int i = 0; i < bytes && !detected; i++) {
                        if (buffer[i] == '\r') {
                            endLineChar = '\r';
                            detected = true;
                        }
                        detected = detected || (previousChar == '\n' && buffer[i] != '\r');
                        previousChar = buffer[i];

                    }
                    if (!detected) {
                        bytes = reader.read(buffer);
                    }
                }
            } finally {
                reader.close();
            }
        } else {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().startsWith("windows")) {
                endLineChar = '\r';
            }
        }
        return endLineChar;
    }

    public abstract String getContent(Object n, VisitorContext vc);

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    public void setPath(String path) {
        setOutputDirectory(path);
    }

    public String getPath() {
        return this.outputDirectory.getPath();
    }

    @Override
    public void setExcludes(String[] excludes) {
        if (excludes != null && System.getProperty("os.name").toLowerCase().contains("windows")) {
            for (int i = 0; i < excludes.length; i++) {
                excludes[i] = FilenameUtils.normalize(excludes[i], true);
            }
        }
        this.excludes = excludes;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }

    @Override
    public void setIncludes(String[] includes) {
        if (includes != null && System.getProperty("os.name").toLowerCase().contains("windows")) {
            for (int i = 0; i < includes.length; i++) {
                includes[i] = FilenameUtils.normalize(includes[i], true);
            }
        }
        this.includes = includes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
        log.debug("[encoding]:" + encoding);
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

}
