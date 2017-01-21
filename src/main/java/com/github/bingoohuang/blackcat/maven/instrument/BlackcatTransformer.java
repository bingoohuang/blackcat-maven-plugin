package com.github.bingoohuang.blackcat.maven.instrument;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.blackcat.javaagent.instrument.BlackcatInstrument;
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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singleton;

public class BlackcatTransformer {
    static final AntPathMatcher matcher = new AntPathMatcher();
    static final Log log = new SystemStreamLog();

    @SneakyThrows
    public void instrument(File sourceDir,
                           Set<String> srcIncludes,
                           Set<String> srcExcludes) {
        val incSet = srcIncludes.isEmpty() ? singleton("*") : srcIncludes;
        val includes = parseSources(incSet);
        val excSet = srcExcludes == null ? (Set<String>) EMPTY_SET : srcExcludes;
        val excludes = parseSources(excSet);

        val sourceBasePath = sourceDir.getCanonicalPath() + "/";
        val scanner = new InstrumentSourceScanner(includes.keySet(), EMPTY_SET);
        val sources = scanner.getIncludedSources(sourceDir, null);
        for (val source : sources) {
            processSource(includes, excludes, sourceBasePath, source);
        }
    }

    private void processSource(Multimap<String, String> includes,
                               Multimap<String, String> excludes,
                               String sourceBasePath,
                               File source) throws IOException {
        String canonicalPath = source.getCanonicalPath();
        int srcBasePathLen = sourceBasePath.length();
        String srcRelativePath = canonicalPath.substring(srcBasePathLen);

        Set<String> includesSet = newHashSet();
        for (String includeKey : includes.keySet()) {
            if (!matcher.match(includeKey, srcRelativePath)) continue;

            includesSet.addAll(includes.get(includeKey));
        }

        Set<String> excludesSet = newHashSet();
        for (String excludeKey : excludes.keySet()) {
            if (!matcher.match(excludeKey, srcRelativePath)) continue;

            if (excludes.containsEntry(excludeKey, "*")) return;
            excludesSet.addAll(excludes.get(excludeKey));
        }

        doInstrument(source, srcRelativePath, includesSet, excludesSet);
    }

    private void doInstrument(File source,
                              String srcRelativePath,
                              Set<String> includesSet,
                              Set<String> excludesSet)
            throws IOException {
        byte[] classfileBuffer = Files.toByteArray(source);
        val blackcatInst = new BlackcatInstrument(classfileBuffer);
        val result = blackcatInst.modifyClass();
        if (result.x) {
            Files.write(result.y, source);
            log.info("Instrument class:" + srcRelativePath
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
                    ? "*/*" : "") + ".class";
            String value = split.length < 2 ? "*" : split[1];

            result.put(key, value);
        }

        return result;
    }
}
