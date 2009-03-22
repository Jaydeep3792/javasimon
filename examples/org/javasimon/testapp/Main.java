package org.javasimon.testapp;

import org.javasimon.testapp.test.Runner;
import org.javasimon.testapp.mm.AppMXBean;
import org.javasimon.jmx.SimonMXBeanImpl;
import org.javasimon.jmx.SimonMXBean;
import org.javasimon.jmx.JdbcMXBean;
import org.javasimon.jmx.JdbcMXBeanImpl;
import org.javasimon.SimonManager;
import org.h2.tools.RunScript;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.JMException;
import java.lang.management.ManagementFactory;
import java.sql.DriverManager;
import java.sql.Connection;

/**
 * Class Main.
 *
 * @author Radovan Sninsky
 * @version $Revision$ $ Date: $
 * @created 19.3.2009 12:55:56
 * @since 2.0
 */
public class Main {

	private Runner runner;
	private Connection connection;

	public class AppMXBeanImpl implements AppMXBean {

		public AppMXBeanImpl() {
		}

		public void shutdown() {
			if (runner != null) {
				runner.stop();
			}
		}
	}

	private void setupDatabase() throws Exception {
		RunScript.execute("jdbc:h2:file:testappdb", "sa", "sa", "testapp.db.sql", null, false);

		Class.forName("org.javasimon.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:simon:h2:file:testappdb;simon_prefix=org.javasimon.testapp.jdbc", "sa", "sa");
	}

	private void closeDatabase() throws Exception {
		connection.close();
	}

	private void setupRunner() {
		WeightController controller = new WeightController();
		controller.addAction(new InsertAction(new RandomNumberDataProvider(700), connection), 34);
		controller.addAction(new InsertBatchAction(connection), 12);
		controller.addAction(new UpdateAction(new RandomNumberDataProvider(400), connection), 28);
		controller.addAction(new DeleteAction(new RandomNumberDataProvider(600), connection), 26);

		UniformRandomTimer timer = new UniformRandomTimer(7100, 900);

		runner = new Runner(controller, timer);
	}

	private void setupJmx() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			AppMXBean app = new AppMXBeanImpl();
			mbs.registerMBean(app, new ObjectName("org.javasimon.testapp:type=App"));
			System.out.println("AppMXBean registerd");

			SimonMXBean simon = new SimonMXBeanImpl(SimonManager.manager());
			mbs.registerMBean(simon, new ObjectName("org.javasimon.testapp:type=Simon"));
			System.out.println("SimonMXBean registerd");

			JdbcMXBean jdbc = new JdbcMXBeanImpl(SimonManager.manager(), "org.javasimon.testapp.jdbc");
			mbs.registerMBean(jdbc, new ObjectName("org.javasimon.testapp:type=Jdbc"));
			System.out.println("JdbcMXBean registerd");
		} catch (JMException e) {
			System.out.println("JMX beans registration failed!\n"+e);
		}
	}

	private void run() {
		runner.run();
	}

	public static void main(String[] args) throws Exception {
		Main m = new Main();
		m.setupDatabase();
		m.setupRunner();
		m.setupJmx();

		m.run();

		m.closeDatabase();
	}
}