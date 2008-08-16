package org.javasimon;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * EnabledFactory implements methods called from SimonFactory if the Simon API is enabled.
 *
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @created Aug 16, 2008
 */
class EnabledFactory implements Factory {
	static final Factory INSTANCE = new EnabledFactory();

	private final Map<String, AbstractSimon> allSimons = new HashMap<String, AbstractSimon>();

	private UnknownSimon rootSimon;

	public Simon getSimon(String name) {
		return allSimons.get(name);
	}

	public void destroySimon(String name) {
		AbstractSimon simon = allSimons.remove(name);
		if (simon.getChildren().size() > 0) {
			replaceSimon(simon, UnknownSimon.class);
		} else {
			((AbstractSimon) simon.getParent()).replaceChild(simon, null);
		}
	}

	public void reset() {
		allSimons.clear();
		rootSimon = new UnknownSimon(SimonFactory.ROOT_SIMON_NAME);
		allSimons.put(SimonFactory.ROOT_SIMON_NAME, rootSimon);
	}

	public synchronized Counter getCounter(String name) {
		return (Counter) getOrCreateSimon(name, CounterImpl.class);
	}

	public synchronized Stopwatch getStopwatch(String name) {
		return (Stopwatch) getOrCreateSimon(name, StopwatchImpl.class);
	}

	public String generateName(String suffix, boolean includeMethodName) {
		StackTraceElement stackElement = Thread.currentThread().getStackTrace()[3];
		StringBuilder nameBuilder = new StringBuilder(stackElement.getClassName());
		if (includeMethodName) {
			nameBuilder.append('.').append(stackElement.getMethodName());
		}
		if (suffix != null) {
			nameBuilder.append(suffix);
		}
		return nameBuilder.toString();
	}

	public Simon getRootSimon() {
		return rootSimon;
	}

	public Collection<String> simonNames() {
		return allSimons.keySet();
	}

	private Simon getOrCreateSimon(String name, Class<? extends AbstractSimon> simonClass) {
		AbstractSimon simon = allSimons.get(name);
		if (simon == null) {
			simon = newSimon(name, simonClass);
		} else if (simon instanceof UnknownSimon) {
			simon = replaceSimon(simon, simonClass);
		} else {
			if (!(simonClass.isInstance(simon))) {
				throw new SimonException("Simon named '" + name + "' already exists and its type is '" + simon.getClass().getName() + "' while requested type is '" + simonClass.getName() + "'.");
			}
		}
		return simon;
	}

	private AbstractSimon replaceSimon(AbstractSimon simon, Class<? extends AbstractSimon> simonClass) {
		AbstractSimon newSimon = instantiateSimon(simon.getName(), simonClass);
		newSimon.enabled = simon.enabled;

		// fixes parent link and parent's children list
		((AbstractSimon) simon.getParent()).replaceChild(simon, newSimon);

		// fixes children list and all children's parent link
		for (Simon child : simon.getChildren()) {
			newSimon.addChild((AbstractSimon) child);
			((AbstractSimon) child).setParent(newSimon);
		}

		allSimons.put(simon.getName(), newSimon);
		return newSimon;
	}

	private AbstractSimon newSimon(String name, Class<? extends AbstractSimon> simonClass) {
		AbstractSimon simon = instantiateSimon(name, simonClass);
		if (name != null) {
			addToHierarchy(simon, name);
		}
		return simon;
	}

	private AbstractSimon instantiateSimon(String name, Class<? extends AbstractSimon> simonClass) {
		AbstractSimon simon;
		try {
			Constructor<? extends AbstractSimon> constructor = simonClass.getConstructor(String.class);
			simon = constructor.newInstance(name);
		} catch (NoSuchMethodException e) {
			throw new SimonException(e);
		} catch (InvocationTargetException e) {
			throw new SimonException(e);
		} catch (IllegalAccessException e) {
			throw new SimonException(e);
		} catch (InstantiationException e) {
			throw new SimonException(e);
		}
		return simon;
	}

	private void addToHierarchy(AbstractSimon simon, String name) {
		allSimons.put(name, simon);
		int ix = name.lastIndexOf(SimonFactory.HIERARCHY_DELIMITER);
		AbstractSimon parent = rootSimon;
		if (ix != -1) {
			String parentName = name.substring(0, ix);
			parent = allSimons.get(parentName);
			if (parent == null) {
				parent = new UnknownSimon(parentName);
				addToHierarchy(parent, parentName);
			}
		}
		parent.addChild(simon);
	}
}