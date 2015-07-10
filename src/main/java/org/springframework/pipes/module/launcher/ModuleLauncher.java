/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.pipes.module.launcher;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.pipes.module.classloader.ParentLastURLClassLoader;
import org.springframework.util.StringUtils;

/**
 * Bootstrap for launching one or more modules. The module path(s), relative to the module home, must be provided via
 * the "modules" system property or "MODULES" environment variable as a comma-delimited list. The module home directory
 * itself may be provided via the "module.home" system property or "MODULE_HOME" environment variable. The default
 * module home directory is: /opt/spring/modules
 *
 * @author Mark Fisher
 * @author Ilayaperumal Gopinathan
 */
public class ModuleLauncher {

	private static final String DEFAULT_MODULE_HOME = "/opt/spring/modules";

	public static void main(String[] args) throws Exception {
		String modules = System.getProperty("modules");
		if (modules == null) {
			modules = System.getenv("MODULES");
		}
		if (modules == null) {
			System.err.println("Either the 'modules' system property or 'MODULES' environment variable is required.");
			System.exit(1);
		}
		String moduleHome = System.getProperty("module.home");
		if (moduleHome == null) {
			moduleHome = System.getenv("MODULE_HOME");
		}
		if (moduleHome == null) {
			moduleHome = DEFAULT_MODULE_HOME;
		}
		launchModules(new File(moduleHome), StringUtils.tokenizeToStringArray(modules, ","));
	}

	private static void launchModules(File moduleHome, String... modules) {
		Executor executor = Executors.newFixedThreadPool(modules.length);
		for (String module : modules) {
			module = (module.endsWith(".jar")) ? module : module + ".jar";
			executor.execute(new ModuleLaunchTask(new File(moduleHome, module)));
		}
	}

	private static class ModuleLaunchTask implements Runnable {

		private final File file;

		ModuleLaunchTask(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			try {
				JarFileArchive jarFileArchive = new JarFileArchive(file);
				ParentLastURLClassLoader classLoader = new ParentLastURLClassLoader(new URL[] { jarFileArchive.getUrl() },
						Thread.currentThread().getContextClassLoader());
				Thread.currentThread().setContextClassLoader(classLoader);
				new SpringApplicationBuilder(jarFileArchive.getMainClass())
						.resourceLoader(new DefaultResourceLoader(classLoader))
						.run("--spring.jmx.default-domain=module-" + new Random().nextInt());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
