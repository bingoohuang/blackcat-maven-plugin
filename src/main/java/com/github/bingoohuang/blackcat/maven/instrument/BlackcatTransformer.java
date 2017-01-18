package com.github.bingoohuang.blackcat.maven.instrument;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.blackcat.javaagent.instrument.BlackcatInstrument;
import com.github.bingoohuang.blackcat.javaagent.utils.Tuple;
import com.github.bingoohuang.blackcat.maven.scan.InstrumentSourceScanner;
import com.github.bingoohuang.blackcat.maven.utils.AntPathMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class BlackcatTransformer {
    static final Set<String> sourceIncludesDefault = Collections.singleton("*");
    static final Set<String> sourceExcludesDefault = Collections.EMPTY_SET;
    static final String wildcardPattern = "*/*";
    static final String sourceFileEnding = ".class";
    static final AntPathMatcher matcher = new AntPathMatcher();
    static final Log log = new SystemStreamLog();

    @SneakyThrows
    public void instrument(File sourceDir,
                           Set<String> sourceIncludes,
                           Set<String> sourceExcludes) {
        val includes = parseSources(sourceIncludes.isEmpty() ? sourceIncludesDefault : sourceIncludes);
        val excludes = parseSources(sourceExcludes == null ? sourceExcludesDefault : sourceExcludes);

        String sourceBasePath = sourceDir.getCanonicalPath() + "/";
        SourceInclusionScanner scanner = new InstrumentSourceScanner(
                includes.keySet(), Collections.EMPTY_SET);
        Set<File> sources = scanner.getIncludedSources(sourceDir, null);
        for (File source : sources) {
            processSource(includes, excludes, sourceBasePath, source);
        }
    }

    private void processSource(Multimap<String, String> includes,
                               Multimap<String, String> excludes,
                               String sourceBasePath,
                               File source) throws IOException {
        String sourceRelativePath = source.getCanonicalPath()
                .substring(sourceBasePath.length());

        Set<String> includesSet = newHashSet();
        for (String includeKey : includes.keySet()) {
            if (!matcher.match(includeKey, sourceRelativePath)) continue;

            includesSet.addAll(includes.get(includeKey));
        }

        Set<String> excludesSet = newHashSet();
        for (String excludeKey : excludes.keySet()) {
            if (!matcher.match(excludeKey, sourceRelativePath)) continue;

            if (excludes.containsEntry(excludeKey, "*")) return;
            excludesSet.addAll(excludes.get(excludeKey));
        }

        doInstrument(source, sourceRelativePath, includesSet, excludesSet);
    }

    private void doInstrument(File source, String sourceRelativePath,
                              Set<String> includesSet, Set<String> excludesSet)
            throws IOException {
        byte[] classfileBuffer = Files.toByteArray(source);
        val blackcatInst = new BlackcatInstrument(classfileBuffer);
        Tuple<Boolean, byte[]> result = blackcatInst.modifyClass();
        if (result.x) {
            Files.write(result.y, source);
            log.info("Instrument class:" + sourceRelativePath
                    + ", include:" + JSON.toJSONString(includesSet)
                    + ", exclude:" + JSON.toJSONString(excludesSet));
        }
    }

    private Multimap<String, String> parseSources(Set<String> sources) {
        Multimap<String, String> result = HashMultimap.create();
        for (String source : sources) {
            String[] split = source.split(":");
            if (Strings.isNullOrEmpty(split[0])) continue;

            String key = split[0].replaceAll("\\.", "/")
                    + ("*".equals(split[0]) || split[0].endsWith("/*")
                    ? wildcardPattern : "") + sourceFileEnding;
            String value = split.length < 2 ? "*" : split[1];

            result.put(key, value);
        }

        return result;
    }
}
