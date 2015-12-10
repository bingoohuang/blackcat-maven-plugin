package com.github.bingoohuang.blackcat.maven;

import com.github.bingoohuang.blackcat.maven.instrument.BlackcatTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Instrument Compiles application sources
 *
 * @goal instrument
 */
public class InstrumentCompilerMojo extends AbstractMojo {

    /**
     * The directory for compiled classes.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * A list of instrument inclusion filters for the compiler.
     *
     * @parameter
     */
    private Set<String> instrumentIncludes = new HashSet<String>();

    /**
     * A list of instrument exclusion filters for the compiler.
     *
     * @parameter
     */
    private Set<String> instrumentExcludes = new HashSet<String>();

    public void execute() throws MojoExecutionException {
        new BlackcatTransformer()
                .instrument(outputDirectory, instrumentIncludes, instrumentExcludes);
    }
}
