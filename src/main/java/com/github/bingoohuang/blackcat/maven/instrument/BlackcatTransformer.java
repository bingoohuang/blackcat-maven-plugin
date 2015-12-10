package com.github.bingoohuang.blackcat.maven.instrument;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.blackcat.javaagent.instrument.BlackcatInstrument;
import com.github.bingoohuang.blackcat.javaagent.utils.Tuple;
import com.github.bingoohuang.blackcat.maven.scan.InstrumentSourceScanner;
import com.github.bingoohuang.blackcat.maven.utils.AntPathMatcher;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class BlackcatTransformer {
    private static final Set<String> sourceIncludesDefault = Collections.singleton("*");
    private static final Set<String> sourceExcludesDefault = Collections.EMPTY_SET;
    private static final String wildcardPattern = "*/*";
    private static final String sourceFileEnding = ".class";
    private static final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Instance logger
     */
    private Log log;

    public void instrument(File sourceDir, Set<String> sourceIncludes, Set<String> sourceExcludes) {
        Map<String, Set<String>> includes = parseSources(sourceIncludes.isEmpty()
                ? sourceIncludesDefault : sourceIncludes);
        Map<String, Set<String>> excludes = parseSources(sourceExcludes == null
                ? sourceExcludesDefault : sourceExcludes);

        try {
            String sourceBasePath = sourceDir.getCanonicalPath() + "/";
            SourceInclusionScanner scanner = new InstrumentSourceScanner(
                    includes.keySet(), Collections.EMPTY_SET);
            Set<File> sources = scanner.getIncludedSources(sourceDir, null);

            for (File source : sources) {
                String sourceRelativePath = source.getCanonicalPath()
                        .substring(sourceBasePath.length());

                Set<String> includesSet = newHashSet();
                for (String includeKey : includes.keySet()) {
                    if (!matcher.match(includeKey, sourceRelativePath))
                        continue;
                    includesSet.addAll(includes.get(includeKey));
                }

                Set<String> excludesSet = newHashSet();
                for (String excludeKey : excludes.keySet()) {
                    if (!matcher.match(excludeKey, sourceRelativePath))
                        continue;

                    Set<String> tempSet = excludes.get(excludeKey);
                    if (tempSet.contains("*")) {
                        excludesSet = null;
                        break;
                    }
                    excludesSet.addAll(tempSet);
                }

                doInstrument(source, sourceRelativePath, includesSet, excludesSet);
            }
        } catch (InclusionScanException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doInstrument(File source, String sourceRelativePath,
                              Set<String> includesSet, Set<String> excludesSet) throws IOException {
        if (excludesSet == null) return;

        try {
            byte[] classfileBuffer = Files.toByteArray(source);
            BlackcatInstrument blackcatInst = new BlackcatInstrument(classfileBuffer);
            Tuple<Boolean, byte[]> result = blackcatInst.modifyClass();
            if (result.x) {
                Files.write(result.y, source);
                getLog().info("Instrument class:" + sourceRelativePath
                        + ", include:" + JSON.toJSONString(includesSet)
                        + ", exclude:" + JSON.toJSONString(excludesSet));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw Throwables.propagate(e);
        }
    }

    private Map<String, Set<String>> parseSources(Set<String> sources) {
        Map<String, Set<String>> result = newHashMap();
        for (String source : sources) {
            String[] split = source.split(":");
            if (Strings.isNullOrEmpty(split[0])) continue;
            String key = split[0].replaceAll("\\.", "/")
                    + ("*".equals(split[0]) || split[0].endsWith("/*")
                    ? wildcardPattern : "") + sourceFileEnding;
            String value = split.length < 2 ? "*" : split[1];

            if (result.get(key) == null) result.put(key, new HashSet<String>());
            result.get(key).add(value);
        }
        return result;
    }

    public Log getLog() {
        if (log == null) log = new SystemStreamLog();
        return log;
    }
}
