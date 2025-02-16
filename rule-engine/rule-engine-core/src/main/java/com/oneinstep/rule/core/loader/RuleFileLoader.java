package com.oneinstep.rule.core.loader;

import com.oneinstep.rule.core.config.RuleConfigConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RuleFileLoader {

    private RuleFileLoader() {
    }

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public static Map<String, String> loadLocalRules(String basePath) {
        Map<String, String> ruleFiles = new HashMap<>();
        try {
            // 加载所有.drl文件
            Resource[] resources = resourceResolver.getResources(
                    "classpath:" + basePath + "/**/*" + RuleConfigConstants.DRL_EXTENSION);

            for (Resource resource : resources) {
                String path = resource.getURL().getPath();
                String relativePath = path.substring(path.indexOf(basePath));
                String content = StreamUtils.copyToString(
                        resource.getInputStream(), StandardCharsets.UTF_8);
                ruleFiles.put(relativePath, content);
                log.info("Loaded local rule file: {}", relativePath);
            }
        } catch (IOException e) {
            log.error("Failed to load local rule files", e);
        }
        return ruleFiles;
    }
} 