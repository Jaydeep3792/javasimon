package org.javasimon.jmx;

import java.beans.ConstructorProperties;

/**
 * Value object for retrieving Simon name and type info via Simon MXBean ({@link SimonManagerMXBean}).
 * This value object make possible to retrieve list of all instantiated Simons together with
 * their types, so no multiple roundtrips are needed.
 * <p/>
 * Example: Following example shows usage of SimonInfo object to find out Simon type through jmx.
 * <pre>
 * System.out.println("List of stopwatch Simons:");
 * for (SimonInfo si : simon.getSimonInfos()) {
 * if (si.getType().equals(SimonInfo.STOPWATCH)) {
 * System.out.println("  " + si.getName());
 * }
 * }</pre>
 *
 * @author Radovan Sninsky
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @see SimonManagerMXBean#getSimonInfos
 * @since 2.0
 */
public final class SimonInfo {

	/** Type identifier for unknown Simon. */
	public static final String UNKNOWN = "Unknown";

	/** Type identifier for Stopwatch. */
	public static final String STOPWATCH = "Stopwatch";

	/** Type identifier for Counter. */
	public static final String COUNTER = "Counter";

	private String name;
	private String type;

	/**
	 * Class constructor due to JMX requirements.
	 *
	 * @param name Simon name
	 * @param type Simon type ({@code 'stopwatch'} or {@code 'counter'})
	 */
	@ConstructorProperties({"name", "type"})
	public SimonInfo(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns fully hierarchical name of Simon.
	 *
	 * @return Simon name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns Simon type, either {@code 'stopwatch'} or {@code 'counter'} strings.
	 *
	 * @return Simon type
	 * @see org.javasimon.jmx.SimonInfo#UNKNOWN
	 * @see org.javasimon.jmx.SimonInfo#STOPWATCH
	 * @see org.javasimon.jmx.SimonInfo#COUNTER
	 */
	public String getType() {
		return type;
	}
}
