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

/**
 * Bootstrap for launching a module. The path to a module relative to the module home
 * must be provided via the "module" system property or "MODULE" environment variable.
 * The module home itself may be provided via the "module.home" system property or
 * "MODULE_HOME" environment variable. The default module home is: /opt/spring/modules 
 *
 * @author Mark Fisher
 */
public class ModuleLauncher {

	private static final String DEFAULT_MODULE_HOME = "/opt/spring/modules";

	public static void main(String[] args) throws Exception {
		String module = System.getProperty("module");
		if (module == null) {
			module = System.getenv("MODULE");
		}
		if (module == null) {
			System.err.println("Either the 'module' system property or 'MODULE' environment variable is required.");
			System.exit(1);
		}
		String moduleHome = System.getProperty("module.home");
		if (moduleHome == null) {
			moduleHome = System.getenv("MODULE_HOME");
		}
		if (moduleHome == null) {
			moduleHome = DEFAULT_MODULE_HOME;
		}
		File file = new File(moduleHome + "/" + module + ".jar");
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", file.getAbsolutePath());
		builder.inheritIO();
		final Process process = builder.start();
		Runnable hook = new Runnable() {

			@Override
			public void run() {
				process.destroy();
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(hook));
		process.waitFor();
	}
}
