/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cameltooling.idea.util;

import com.github.cameltooling.idea.extension.CamelIdeaUtilsExtension;
import com.github.cameltooling.idea.reference.endpoint.CamelEndpoint;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public final class CamelIdeaEndpointUtil {
    public static final String[] CAMEL_FILE_EXTENSIONS = {"java", "xml", "yaml", "yml"};
    public static final String BEAN_INJECT_ANNOTATION = "org.apache.camel.BeanInject";

    private final List<CamelIdeaUtilsExtension> enabledExtensions;

    private CamelIdeaEndpointUtil() {
        enabledExtensions = Arrays.stream(CamelIdeaUtilsExtension.EP_NAME.getExtensions())
                .filter(CamelIdeaUtilsExtension::isExtensionEnabled)
                .toList();
    }

    public static CamelIdeaEndpointUtil getService() {
        return ApplicationManager.getApplication().getService(CamelIdeaEndpointUtil.class);
    }

    /**
     * Is the given element from a consumer endpoint used in a route from a <tt>from</tt>, <tt>fromF</tt>,
     * <tt>interceptFrom</tt>, or <tt>pollEnrich</tt> pattern.
     */
    public boolean isConsumerEndpoint(PsiElement element) {
        return enabledExtensions.stream()
                .anyMatch(extension -> extension.isConsumerEndpoint(element));
    }

    /**
     * Is the given element from a producer endpoint used in a route from a <tt>to</tt>, <tt>toF</tt>,
     * <tt>interceptSendToEndpoint</tt>, <tt>wireTap</tt>, or <tt>enrich</tt> pattern.
     */
    public boolean isProducerEndpoint(PsiElement element) {
        return enabledExtensions.stream()
                .anyMatch(extension -> extension.isProducerEndpoint(element));
    }

    /**
     * Could an endpoint uri be present at this location?
     */
    public boolean isPlaceForEndpointUri(PsiElement element) {
        return enabledExtensions.stream()
                .anyMatch(extension -> extension.isPlaceForEndpointUri(element));
    }
    /**
     * Certain elements should be skipped for endpoint validation such as ActiveMQ brokerURL property and others.
     */
    public boolean skipEndpointValidation(PsiElement element) {
        return enabledExtensions.stream()
                .anyMatch(extension -> extension.skipEndpointValidation(element));
    }

    /**
     * Is the given element from a method call named <tt>fromF</tt> or <tt>toF</tt>, or <tt>String.format</tt> which supports the
     * {@link String#format(String, Object...)} syntax and therefore we need special handling.
     */
    public boolean isFromStringFormatEndpoint(PsiElement element) {
        return enabledExtensions.stream()
                .anyMatch(extension -> extension.isFromStringFormatEndpoint(element));
    }


    public List<PsiElement> findEndpointUsages(Module module, CamelEndpoint endpoint) {
        return findEndpointUsages(module, endpoint::baseUriMatches);
    }

    public List<PsiElement> findEndpointUsages(Module module, Predicate<String> uriCondition) {
        return enabledExtensions.stream()
                .map(e -> e.findEndpointUsages(module, uriCondition))
                .flatMap(List::stream)
                .toList();
    }

    public List<PsiElement> findEndpointDeclarations(Module module, CamelEndpoint endpoint) {
        return findEndpointDeclarations(module, endpoint::baseUriMatches);
    }

    public List<PsiElement> findEndpointDeclarations(Module module, Predicate<String> uriCondition) {
        return enabledExtensions.stream()
                .map(e -> e.findEndpointDeclarations(module, uriCondition))
                .flatMap(List::stream)
                .toList();
    }
}
