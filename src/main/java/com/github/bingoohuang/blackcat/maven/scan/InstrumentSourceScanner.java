package com.github.bingoohuang.blackcat.maven.scan;

import org.codehaus.plexus.compiler.util.scan.AbstractSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InstrumentSourceScanner extends AbstractSourceInclusionScanner {

    private final Set<String> sourceIncludes;

    private final Set<String> sourceExcludes;

    public InstrumentSourceScanner() {
        this(Collections.singleton("**/*"), Collections.<String>emptySet());
    }

    public InstrumentSourceScanner(Set<String> sourceIncludes, Set<String> sourceExcludes) {
        this.sourceIncludes = sourceIncludes;
        this.sourceExcludes = sourceExcludes;
    }

    @Override
    public Set<File> getIncludedSources(File sourceDir, File targetDir) throws InclusionScanException {
        String[] potentialIncludes = scanForSources(sourceDir, sourceIncludes, sourceExcludes);
        Set<File> matchingSources = new HashSet<File>(potentialIncludes != null ? potentialIncludes.length : 0);
        if (potentialIncludes != null) {
            for (String potentialSource : potentialIncludes) {
                matchingSources.add(new File(sourceDir, potentialSource));
            }
        }
        return matchingSources;
    }
}
