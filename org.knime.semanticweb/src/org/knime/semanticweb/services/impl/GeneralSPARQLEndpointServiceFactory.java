package org.knime.semanticweb.services.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.util.KnimeEncryption;
import org.knime.semanticweb.services.RDFServiceFactory;

/**
 * @author Lara Gorini
 *
 * @param <F>
 *            a {@link SPARQLEndpointService}.
 */
public abstract class GeneralSPARQLEndpointServiceFactory<F extends SPARQLEndpointService>
		implements RDFServiceFactory<F> {

	private static final String CONTENT_GENERAL = "CONTENT";
	private static final String CFG_ENDPOINT = "endpoint";
	private static final String CFG_USERNAME = "username";
	private static final String CFG_PASSWORD = "password";
	private static final String CFG_CREDENTIAL = "credential";
	private final SecretKey secretKey = KnimeEncryption.createSecretKey("!ThisIsMySecretWhatIsYours?");

	@Override
	public F create(final ExecutionMonitor exec, final ZipInputStream in) throws IOException {
		final ZipEntry ze = in.getNextEntry();
		if (!ze.getName().equals(CONTENT_GENERAL)) {
			throw new IOException("Key \"" + ze.getName() + "\" does not " + " match expected zip entry name \""
					+ CONTENT_GENERAL + "\".");
		}
		final ModelContentRO model = ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
		F service = null;
		try {
			final String endpoint = model.getString(CFG_ENDPOINT);
			final String credential = model.getString(CFG_CREDENTIAL);
			final String username = model.getString(CFG_USERNAME);
			char[] password;
			try {
				password = KnimeEncryption.decrypt(secretKey, model.getString(CFG_PASSWORD)).toCharArray();
			} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
				throw new IOException(e);
			}
			service = createService(endpoint, credential, username, password);
		} catch (final InvalidSettingsException e) {
			throw new IOException(e);
		}
		return service;

	}

	@Override
	public void save(final ExecutionMonitor exec, final ZipOutputStream out, final F service) throws IOException {
		final ModelContent model = new ModelContent(CONTENT_GENERAL);
		model.addString(CFG_ENDPOINT, service.getEndoint());
		model.addString(CFG_CREDENTIAL, service.getCredential());
		model.addString(CFG_USERNAME, service.getUsername() == null ? "" : service.getUsername());
		try {
			model.addString(CFG_PASSWORD, KnimeEncryption.encrypt(secretKey,
					service.getPassword() == null ? new char[0] : service.getPassword()));
		} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
			throw new IOException(e);
		}
		out.putNextEntry(new ZipEntry(CONTENT_GENERAL));
		model.saveToXML(new NonClosableOutputStream.Zip(out));
	}

	protected abstract F createService(final String endpoint, final String credential, final String username,
			char[] password);
}