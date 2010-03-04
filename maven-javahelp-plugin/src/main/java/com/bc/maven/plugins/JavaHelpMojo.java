/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus

 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.bc.maven.plugins;

import com.sun.java.help.search.Indexer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Goal which invokes the Javahelp indexer. This will create a search index database for your javahelp
 * set. Note that currently this goal can only be invoked on a directory but all html files under that directory
 * will be indexed recursively.
 *
 * @author Chad Lyon
 * @author Marco Zuehlke
 * @goal javahelp-indexer
 * @phase process-resources
 */
public class JavaHelpMojo extends AbstractMojo {

    private static Indexer indexer = new Indexer();

    private static final String DEFAULT_INCLUDE = "**/*.html";

    private static final String DEFAULT_EXCLUDE = "**/.svn";

    /**
     * List of files to include. Specified as fileset patterns.
     *
     * @parameter
     */
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns.
     *
     * @parameter
     */
    private String[] excludes;

    /**
     * @parameter
     */
    private String locale;

    /**
     * The location of the output JavaSearchIndex database.
     *
     * @parameter default-value="${project.build.outputDirectory}/doc/help/JavaHelpSearch"
     */
    private File dataBase;

    /**
     * The directory containing the Java Help set.
     *
     * @parameter default-value="${basedir}/src/main/resources/doc/help"
     */
    private File sourcePath;

    /**
     * @parameter
     */
    private File logFile;

    /**
     * @parameter
     */
    private File configFile;

    /**
     * @parameter
     */
    private boolean verbose;

    /**
     * @parameter
     */
    private boolean noStopWords;

    /**
     * @parameter expression="${javahelp.indexer.skip}" 
     */
    private boolean skip;

    public void execute()
            throws MojoExecutionException {
        /*
         * Get the arguments...
         */
        ArrayList<String> args = new ArrayList<String>();

        if (!skip) {
            if (locale != null) {
                args.add("-locale");
                args.add(locale);
            }

            if (dataBase != null) {
                args.add("-db");
                args.add(dataBase.getAbsolutePath());
            }

            if (logFile != null) {
                args.add("-logfile");
                args.add(logFile.getAbsolutePath());
            }

            if (configFile != null) {
                args.add("-c");
                args.add(configFile.getAbsolutePath());
            }

            if (verbose) {
                args.add("-verbose");
            }

            if (noStopWords) {
                args.add("-nostopwords");
            }

            if (sourcePath != null) {
                args.add("-sourcepath");
                args.add(sourcePath.getAbsolutePath() + File.separator);

                List<String> files = getFilesToIndex(sourcePath);
                args.addAll(files);
            }

            try {
                indexer.compile(args.toArray(new String[args.size()]));
            }
            catch (Exception e) {
                throw new MojoExecutionException(
                        "Java Help indexing exception, a full search database may not have been created.",
                        e);
            }

            getLog().info("Javahelp Search Database written to " + dataBase.getAbsolutePath());
        } else {
            getLog().info("Skipping Javahelp search indexing.");
        }
    }

    private List<String> getFilesToIndex(File basedir) {
        ArrayList<String> files = new ArrayList<String>();

        DirectoryScanner scanner = new DirectoryScanner();
        if (excludes == null || excludes.length == 0) {
            scanner.setExcludes(new String[]{DEFAULT_EXCLUDE});
        } else {
            scanner.setExcludes(excludes);
        }

        if (includes == null || includes.length == 0) {
            scanner.setIncludes(new String[]{DEFAULT_INCLUDE});
        } else {
            scanner.setIncludes(includes);
        }

        scanner.setBasedir(basedir);
        scanner.scan();

        for (String file : scanner.getIncludedFiles()) {
            getLog().debug("Indexing: " + file);
            files.add(file);
        }

        return files;
    }
}
