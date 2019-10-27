/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradleplugins

import spock.lang.Specification
import spock.lang.Subject

@Subject(GradlePluginPortal)
class GradlePluginPortalTest extends Specification {
    def "can page scrap the plugin information"() {
        def portal = GradlePluginPortal.connect(GradlePluginPortalTest.class.getResource("single/search.html"))
        def visitor = new Bob()

        when:
        portal.visit(visitor)

        then:
        noExceptionThrown()
        visitor.plugins.size() == 10
        visitor.plugins.values()*.pluginId == ['nebula.lint', 'com.louiscad.splitties', 'com.opitzconsulting.orcas.orcas-gradle-plugin', 'com.opitzconsulting.orcas.orcas-gradle-plugin-domainextension', 'net.kebernet.xddl', 'com.novoda.static-analysis', 'com.github.vlsi.stage-vote-release', 'com.github.vlsi.license-gather', 'com.github.vlsi.ide', 'com.github.vlsi.crlf']
        visitor.plugins.values()*.latestVersion == ['15.0.1', '0.1.3', '7.1.0', '7.1.0', '0.3.2', '1.1', '1.36.0', '1.36.0', '1.36.0', '1.36.0']
//        plugins*.notation == ["gradle.plugin.bue.akhikhl.wuff:wuff-plugin:0.0.3", "gradle.plugin.com.polidea.cockpit:CockpitPlugin:1.0.3", "gradle.plugin.net.wooga.gradle:atlas-build-unity:0.3.0", "gradle.plugin.org.ysb33r.gradle:operatingsystem-gradle-plugin:0.9", "gradle.plugin.com.rapidminer.gradle:gradle-plugin-rapidminer-code-quality:0.4.5-SNAPSHOT", "gradle.plugin.com.pluribuslabs:artifact-version-plugin:1.1.0", "gradle.plugin.us.ascendtech:gwt-gradle:0.1.4", "gradle.plugin.us.ascendtech:gwt-gradle:0.1.4", "gradle.plugin.com.liferay:gradle-plugins-source-formatter:2.3.188", "com.netflix.nebula:nebula-publishing-plugin:8.0.0"]
    }

    def "can scrap a real life dump of the plugin portal"() {
        def portal = GradlePluginPortal.connect(GradlePluginPortalTest.class.getResource("wget-dump/index.html"))
        def visitor = new Bob()

        when:
        portal.visit(visitor)

        then:
        noExceptionThrown()
    }

    def "can scrap multiple pages"() {
        def portal = GradlePluginPortal.connect(GradlePluginPortalTest.class.getResource("multi/search.html"))

        expect:
        portal.allPluginInformations.size() == 30
    }

    static class Bob implements GradlePluginPortalVisitor {
        def plugins = [:]

        @Override
        void visitPlugin(String pluginId, String description, String latestVersion) {
            plugins.put(pluginId, [pluginId: pluginId, description: description, latestVersion: latestVersion, version: [latestVersion]])

            println("Visiting plugin '${pluginId}' - ${latestVersion}")
        }

        @Override
        void visitPluginVersion(String pluginId, String version, String notation) {
            plugins.get(pluginId).version << version
            println("Visiting version '${pluginId}' - ${version}: ${notation}")
        }
    }
}