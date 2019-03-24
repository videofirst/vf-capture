/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-present, Video First Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
package io.videofirst.capture.service.impl;

import static java.util.stream.Collectors.toMap;

import io.videofirst.capture.configuration.properties.CaptureConfig;
import io.videofirst.capture.configuration.properties.CaptureDefaults;
import io.videofirst.capture.model.info.ConfigInfo;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.model.info.Info;
import io.videofirst.capture.service.InfoService;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the InfoService interface.
 *
 * @author Bob Marks
 */
@Component
public class DefaultInfoService implements InfoService {

    // Injected fields

    private final CaptureConfig captureConfig;
    private final CaptureDefaults captureDefaults;

    // Private fields

    private Info info;

    @Autowired
    public DefaultInfoService(CaptureConfig captureConfig, CaptureDefaults captureDefaults) {
        this.captureConfig = captureConfig;
        this.captureDefaults = captureDefaults;

        init();
    }

    @Override
    public Info getInfo() {
        return info;
    }

    // Private methods

    private void init() {
        info = Info.builder()
            .info(initConfigInfo())
            .defaults(captureDefaults)
            .build();
    }

    private ConfigInfo initConfigInfo() {

        GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
        Rectangle rect = graphicsConfiguration.getBounds();

        ConfigInfo systemInfo = ConfigInfo.builder()
            .started(LocalDateTime.now())
            .storage(captureConfig.getStorage())
            .upload(captureConfig.getUpload())
            .display(DisplayInfo.builder()
                .x(rect.x)
                .y(rect.y)
                .width(rect.width)
                .height(rect.height)
                .build())
            .environment(getEnvironmentFromJavaSystemProps())
            .build();
        return systemInfo;
    }

    private GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfiguration GraphicsConfiguration = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
        return GraphicsConfiguration;
    }

    private Map<String, String> getEnvironmentFromJavaSystemProps() {
        List<String> systemPropsIncludes = captureConfig.getEnvironment();
        Properties props = java.lang.System.getProperties();

        Map<String, String> environment = systemPropsIncludes.stream()
            .filter(props::containsKey)
            .sorted()
            .collect(toMap(s -> s, props::getProperty, (e1, e2) -> e2, LinkedHashMap::new));
        return environment;
    }

}
