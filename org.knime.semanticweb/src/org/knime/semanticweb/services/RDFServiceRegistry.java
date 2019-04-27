package org.knime.semanticweb.services;

import java.util.HashMap;
import java.util.Map;

import org.knime.semanticweb.services.impl.InMemoryEndpointServiceFactory;
import org.knime.semanticweb.services.impl.SPARQLEndpointServiceFactory;
import org.knime.semanticweb.services.impl.VirtuosoEndpointServiceFactory;

/**
 * @author Lara Gorini
 *
 */
public final class RDFServiceRegistry {

	private static RDFServiceRegistry INSTANCE;

	/**
	 * @return the the instance of this class.
	 */
	public static RDFServiceRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RDFServiceRegistry();
		}
		return INSTANCE;
	}

	private final Map<String, RDFServiceFactory<? extends RDFService>> m_serviceMap = new HashMap<>();

	private RDFServiceRegistry() {
		addFactory(new InMemoryEndpointServiceFactory());
		addFactory(new SPARQLEndpointServiceFactory());
		addFactory(new VirtuosoEndpointServiceFactory());
	}

	private void addFactory(final RDFServiceFactory<? extends RDFService> factory) {
		final RDFServiceFactory<? extends RDFService> old = m_serviceMap.get(factory.getID());
		if (old != null) {
			throw new IllegalStateException("Factory with id. " + factory.getID() + " already exists: " + old);
		}
		m_serviceMap.put(factory.getID(), factory);
	}

	/**
	 * @param id
	 *            The ID of the factory.
	 * @return the factory.
	 */
	public static RDFServiceFactory<? extends RDFService> getFactory(final String id) {
		return getInstance().m_serviceMap.get(id);
	}
}
