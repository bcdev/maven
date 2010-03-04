package com.bc.maven.plugins;

import org.apache.maven.model.FileSet;

import java.io.File;
import java.util.Properties;

/**
 * A template from which output files are created. 
 */
public class Template {
    /**
     * Source template file.
     *
     * @parameter
     * @required
     */
    private File source;

    /**
     * Output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Output file name pattern. The '@' indicates a reference to a property originating
     * from the property files in the  <code>filterSet</code>. Additionally the following
     * references are available:
     * <code>@{filter}</code>,
     * <code>@{filterBaseName}</code>,
     * <code>@{filterExtension}</code>,
     * <code>@{source}</code>,
     * <code>@{sourceBaseName}</code>,
     * <code>@{sourceExtension}</code>.
     *
     * @parameter expression="@{filterBaseName}.@{sourceExtension}"
     * @required
     */
    private String outputFileNamePattern;

    /**
     * Fileset which defines the filters (Java property files).
     *
     * @parameter
     */
    private FileSet filterSet;


    /**
     * Line ending. Can be one of "keep", "dos", "unix".
     *
     * @parameter expression="keep"
     * @required
     */
    private String lineEnding;


    public File getSource() {
        return source;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getOutputFileNamePattern() {
        return outputFileNamePattern;
    }

    public FileSet getFilterSet() {
        return filterSet;
    }

    public String getLineEnding() {
        return lineEnding;
    }
}
