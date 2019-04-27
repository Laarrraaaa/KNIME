package org.knime.semanticweb.services.impl;

/**
 * @author Lara Gorini
 *
 */
public class VirtuosoEndpointServiceFactory extends GeneralSPARQLEndpointServiceFactory<VirtuosoEndpointService> {

	/** The ID of this Service */
	public static final String ID = "VIRTUOSO_ENDPOINT";

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected VirtuosoEndpointService createService(final String endpoint, final String credential,
			final String username, final char[] password) {
		return new VirtuosoEndpointService(endpoint, username, password, credential);
	}
}
