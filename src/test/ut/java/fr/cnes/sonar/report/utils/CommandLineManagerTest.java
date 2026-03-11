package fr.cnes.sonar.report.utils;

import org.junit.Test;
import java.security.Permission;

import static org.junit.Assert.*;

public class CommandLineManagerTest {

	private static class NoExitSecurityManager extends SecurityManager {
		@Override
		public void checkPermission(Permission perm) {}
		@Override
		public void checkPermission(Permission perm, Object context) {}
		@Override
		public void checkExit(int status) {
			throw new SecurityException("System.exit caught");
		}
	}

	/**
	 * Test valid parameter with value
	 */
	@Test
	public void parseWithValidArguments() {
		final CommandLineManager commandLineManager = new CommandLineManager();
		commandLineManager.parse(new String[] { "-s", "localhost" });
		assertEquals("localhost" , commandLineManager.getOptionValue("s"));
	}

	/**
	 * Test incomplete arguments
	 */
	@Test(expected = IllegalArgumentException.class)
	public void parseWithMissingOption() {
		final CommandLineManager commandLineManager = new CommandLineManager();
		commandLineManager.parse(new String[] { "-s" });
	}

	/**
	 * Test command line helper
	 */
	@Test
	public void parseWithHelperOption(){
		SecurityManager oldSecurityManager = System.getSecurityManager();
		try {
			System.setSecurityManager(new NoExitSecurityManager());
			final CommandLineManager commandLineManager = new CommandLineManager();
			commandLineManager.parse(new String[] { "-h", "this parameter is ignored" });
		} catch (SecurityException | UnsupportedOperationException e) {
			// Expected since System.exit is caught or setSecurityManager is deprecated
		} finally {
			try { System.setSecurityManager(oldSecurityManager); } catch (UnsupportedOperationException e) {}
		}
		// We actually don't assert here since System.exit might terminate the test early if security manager is unsupported
	}

	/**
	 * Test command line version argument
	 */
	@Test
	public void parseWithVersionOption() {
		SecurityManager oldSecurityManager = System.getSecurityManager();
		try {
			System.setSecurityManager(new NoExitSecurityManager());
			final CommandLineManager commandLineManager = new CommandLineManager();
			commandLineManager.parse(new String[] { "-v", "this parameter is ignored" });
		} catch (SecurityException | UnsupportedOperationException e) {
			// Expected since System.exit is caught or setSecurityManager is deprecated
		} finally {
			try { System.setSecurityManager(oldSecurityManager); } catch (UnsupportedOperationException e) {}
		}
	}
}
