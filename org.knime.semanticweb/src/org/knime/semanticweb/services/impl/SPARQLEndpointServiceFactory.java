package org.knime.semanticweb.services.impl;

/**
 * @author Lara Gorini
 *
 */
public class SPARQLEndpointServiceFactory extends GeneralSPARQLEndpointServiceFactory<SPARQLEndpointService> {

	/** The ID of this Service */
	public static final String ID = "SPARQL_ENDPOINT";

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected SPARQLEndpointService createService(final String endpoint, final String credential, final String username,
			final char[] password) {
		return new SPARQLEndpointService(endpoint, username, password, credential);
	}

}
