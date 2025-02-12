/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ops4j.pax.web.itest.common;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.web.itest.base.client.HttpTestClientFactory;

public abstract class AbstractWarJsfCdiIntegrationTest extends ITestBase {

    // 1.0.0.RC2 misses pax-cdi-servlet
    // 1.0.0.RC1 has requirement: (&(osgi.wiring.package=org.ops4j.pax.web.service)(version>=3.0.0)(!(version>=5.0.0)))
    private static final String VERSION_PAX_CDI = "1.0.0";

    protected static Option[] configureJsfAndCdi() {
        return options(
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),
                // API
                mavenBundle("javax.annotation", "javax.annotation-api").version("1.2"),
                mavenBundle("javax.el", "javax.el-api").version("3.0.0"),
                mavenBundle("javax.enterprise", "cdi-api").version("1.2"),
                mavenBundle("javax.interceptor", "javax.interceptor-api").version("1.2"),
                mavenBundle("javax.validation", "validation-api").version("1.1.0.Final"),
                mavenBundle("commons-codec", "commons-codec").version("1.10"),
                mavenBundle("commons-beanutils", "commons-beanutils").version("1.9.3"),
                mavenBundle("commons-collections", "commons-collections").version("3.2.2"),
                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-digester").version("1.8_4"),

                // JSF
                mavenBundle().groupId("org.ops4j.pax.web").artifactId("pax-web-jsp").version(asInProject()),
                mavenBundle("org.apache.myfaces.core", "myfaces-api").version("2.2.12"),
                mavenBundle("org.apache.myfaces.core", "myfaces-impl").version("2.2.12"),
                // Weld
                mavenBundle("org.jboss.classfilewriter", "jboss-classfilewriter").version("1.1.2.Final"),
                mavenBundle("org.jboss.weld", "weld-osgi-bundle").version("2.4.5.Final")
        );
    }

    @Before
    public void setUp() throws Exception {
        // Pax-CDI started later, because order is important
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-api").version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-spi").version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-extender").version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-extension").version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-web").version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId(cdiWebBundleArtifact()).version(VERSION_PAX_CDI).getURL());
        installAndStartBundle(mavenBundle().groupId("org.ops4j.pax.cdi").artifactId("pax-cdi-weld").version(VERSION_PAX_CDI).getURL());
    }

    protected abstract String cdiWebBundleArtifact();

    @Test
//    @Ignore
    public void testCdi() throws Exception {
        // prepare Bundle
        initWebListener();
        installAndStartBundle(mavenBundle()
                .groupId("org.ops4j.pax.web.samples")
                .artifactId("war-jsf22-cdi")
                .versionAsInProject()
                .type("war")
                .getURL());

        waitForWebListener();
        // Test
        String containerSpecificResponseFragment = containerIdentification();
		HttpTestClientFactory.createDefaultTestClient()
				.withResponseAssertion("Response must contain '" + containerSpecificResponseFragment + "'",
						resp -> resp.contains(containerSpecificResponseFragment))
				.doGETandExecuteTest("http://127.0.0.1:8181/war-jsf22-cdi/");
    }

    protected abstract String containerIdentification();

}
