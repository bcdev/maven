package com.bc.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.model.FileSet;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.text.MessageFormat;

// todo - Look at this interesting API:
// import org.apache.maven.model.FileSet;
// import org.apache.maven.model.PatternSet;
// import org.apache.maven.project.MavenProjectHelper;
// import org.apache.maven.project.MavenProject;


/**
 * Creates output files from a set of {@link Template templates}.
 *
 * @goal template
 * @phase process-sources
 */
public class TemplateMojo extends AbstractMojo {

//    /**
//     * Project instance, to which we want to add an attached artifact.
//     *
//     * @parameter expression=”${project}”
//     * @required
//     * @readonly
//     */
//    private MavenProject project;
//
//    /**
//     * This helper class makes adding an artifact attachment simpler.
//     *
//     * @component
//     */
//    private MavenProjectHelper helper;

    /**
     * Output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Files to process.
     *
     * @parameter
     * @required
     */
    private Template[] templates;
    private static final String LF = "\n";
    private static final String CRLF = "\r\n";


    public void execute() throws MojoExecutionException {
        checkConfig();
        for (Template template : templates) {
            processTemplate(template);
        }
    }

    private void processTemplate(Template template) throws MojoExecutionException {
        File sourceTemplate = template.getSource();
        if (!sourceTemplate.exists()) {
            getLog().warn("Source template not found: " + sourceTemplate.getPath());
            return;
        }
        File outputDirectory = getOutputDirectory(template);
        String outputFileNamePattern = template.getOutputFileNamePattern();

        File[] filterFiles = getFilters(template);
        for (File filterFile : filterFiles) {
            Properties properties = getProperties(filterFile);
            String filterFileName = filterFile.getName();
            properties.put("filter", filterFileName);
            properties.put("filterBaseName", getBaseName(filterFileName));
            properties.put("filterExtension", getExtension(filterFileName));
            String sourceFileName = sourceTemplate.getName();
            properties.put("source", sourceFileName);
            properties.put("sourceBaseName", getBaseName(sourceFileName));
            properties.put("sourceExtension", getExtension(sourceFileName));
            String outputFileName = createOutpuFileName(outputFileNamePattern, properties);
            File outputFile = new File(outputDirectory, outputFileName);
            process(sourceTemplate, outputFile, template.getLineEnding(), properties);
        }
    }

    private static String createOutpuFileName(String outputFileNamePattern, Properties properties) {
        TemplateReader templateReader = new TemplateReader(new StringReader(outputFileNamePattern), properties);
        templateReader.setKeyIndicator('@');
        try {
            return templateReader.readAll();
        } catch (IOException e) {
            return outputFileNamePattern;
        }
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) {
            return name.substring(i + 1);
        }
        return "";

    }

    private static String getBaseName(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) {
            return name.substring(0, i);
        }
        return name;
    }

    private static File[] getFilters(Template template) throws MojoExecutionException {
        FileSet filterSet = template.getFilterSet();
        String directory = filterSet.getDirectory();
        if (!new File(directory).exists()) {
            throw new MojoExecutionException("Filter directory does not exist: " + directory);
        }

        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.addDefaultExcludes();
        directoryScanner.setBasedir(directory);
        directoryScanner.setIncludes((String[]) filterSet.getIncludes().toArray(new String[0]));
        directoryScanner.setExcludes((String[]) filterSet.getExcludes().toArray(new String[0]));
        directoryScanner.scan();
        String[] filePaths = directoryScanner.getIncludedFiles();
        File[] files = new File[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            files[i] = new File(directory, filePaths[i]);
        }
        return files;
    }

    private void process(File source, File outputFile, String lineEnding, Properties properties) throws MojoExecutionException {
        getLog().debug("Creating " + outputFile);
        FileReader fr = null;
        FileWriter fw = null;
        try {
            fr = new FileReader(source);
            TemplateReader templateReader = new TemplateReader(fr, properties);
            fw = new FileWriter(outputFile);
            String text = templateReader.readAll();
            if ("dos".equals(lineEnding)) {
                text = text.replace(CRLF, LF).replace(LF, CRLF);
            } else if ("unix".equals(lineEnding)) {
                text = text.replace(CRLF, LF);
            }
            fw.write(text);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + outputFile, e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private File getOutputDirectory(Template template) {
        File outputDirectory = this.outputDirectory;
        if (template.getOutputDirectory() != null) {
            outputDirectory = template.getOutputDirectory();
        }
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        return outputDirectory;
    }

    private static Properties getProperties(File filter) {
        Properties p = new Properties(System.getProperties());
        if (filter != null) {
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(filter);
                p.load(inStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return p;
    }

    private void checkConfig() throws MojoExecutionException {
        if (templates == null) {
            throw new MojoExecutionException("Missing <templates>");
        }
        if (templates.length == 0) {
            throw new MojoExecutionException("At least one <templates>/<template> must be specified");
        }
        if (outputDirectory == null) {
            throw new MojoExecutionException("Missing <outputDirectory>");
        }

        final File cwd = new File(".").getAbsoluteFile();
        for (Template template : templates) {
            final File source = template.getSource();
            if (source == null) {
                throw new MojoExecutionException("Missing <templates>/<template>/<source>");
            }
            if (!source.exists()) {
                throw new MojoExecutionException(MessageFormat.format("Template source not found: {0} (cwd={1})", source, cwd));
            }
            if (template.getFilterSet() == null) {
                throw new MojoExecutionException("Missing <templates>/<template>/<filterSet>");
            }
            final File dir = new File(template.getFilterSet().getDirectory());
            if (!dir.exists()) {
                throw new MojoExecutionException(MessageFormat.format("Filters directory not found: {0} (cwd={1})", dir, cwd));
            }
        }
    }
}
