/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.itest.karaf;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.io.File;

import javax.inject.Inject;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OinkProxyIT extends OinkKarafITSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkProxyIT.class);

	@Inject
	protected BundleContext bundleContext;
	
	@Test
	public void checkProxyHasASingleConfigPidAssociatedInTheFeaturesRepo() throws Exception {
		checkAdapterHasASingleConfigPidAssociatedInTheFeaturesRepo("proxy");
	}
	
	@Test
	public void checkProxyContextFailsWithoutCfg() throws Exception {
		checkAdapterContextFailsWithoutCfg("proxy");
	}
	
	@Test
	public void checkProxyContextDoesntFailWithCfg() throws Exception {
		checkAdapterContextDoesntFailWithCfg("proxy", "it.proxy.config");
	}	
		
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*,org.springframework.osgi.context.event.*;status=provisional");
	    return probe;
	}

	@Configuration
	public Option[] config() {
		return standardConfig();
	}
	
}
