package org.knime.semanticweb.services.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.config.Config;
import org.knime.semanticweb.services.RDFServiceFactory;

/**
 * @author Lara Gorini
 *
 */
public class InMemoryEndpointServiceFactory implements RDFServiceFactory<InMemoryEndpointService> {

	/** The ID of this Service */
	public static final String ID = "MEMORY_ENDPOINT";

	private static final String LANGUAGE = "Turtle";

	private static final String CONTENT_MEMORY = "CONTENT_MEMORY";

	private static final String CFG_GRAPH_NAME = "graph_name";

	private static final String CFG_KEY_NAME = "key";

	private static final String CFG_URI_NAME = "uri";

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public InMemoryEndpointService create(final ExecutionMonitor exec, final ZipInputStream in) throws IOException {
		InMemoryEndpointService service = null;
		final Dataset dataset = new DatasetImpl(ModelFactory.createDefaultModel());
		final LinkedHashMap<String, String> graphNames = new LinkedHashMap<>();

		ZipEntry ze = in.getNextEntry();
		try {
			while (ze != null) {
				if (ze.getName().matches("(.*)" + CONTENT_MEMORY + "(.*)")) {
					final ModelContentRO modelContent = ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
					for (int i = 0; i < modelContent.getChildCount(); i++) {
						final Config subConfig = modelContent.getConfig(CFG_GRAPH_NAME + i);
						graphNames.put(subConfig.getString(CFG_KEY_NAME), subConfig.getString(CFG_URI_NAME));
					}
				} else {
					final String uriName = graphNames.get(ze.getName());
					final Model model = ModelFactory.createDefaultModel();
					model.read(new NonClosableInputStream.Zip(in), null, LANGUAGE);
					if (ze.getName().equals(InMemoryEndpointService.DEFAULT)) {
						dataset.setDefaultModel(model);
					} else {
						dataset.addNamedModel(uriName, model);
					}
				}
				ze = in.getNextEntry();
			}

			service = new InMemoryEndpointService(dataset, graphNames);
		} catch (final InvalidSettingsException e) {
			throw new IOException(e);
		}
		return service;
	}

	@Override
	public void save(final ExecutionMonitor exec, final ZipOutputStream out, final InMemoryEndpointService service)
			throws IOException {

		final Dataset dataset = service.getDataset();
		final Set<Entry<String, String>> graphNames = service.getNames();

		final ModelContent modelContent = new ModelContent(CONTENT_MEMORY);

		int id = 0;
		for (final Entry<String, String> entry : graphNames) {
			final Config subConfig = modelContent.addConfig(CFG_GRAPH_NAME + id);
			subConfig.addString(CFG_KEY_NAME, entry.getKey());
			subConfig.addString(CFG_URI_NAME, entry.getValue());
			id++;
		}
		out.putNextEntry(new ZipEntry(CONTENT_MEMORY));
		modelContent.saveToXML(new NonClosableOutputStream.Zip(out));

		for (final Entry<String, String> entry : graphNames) {
			// save model
			final Model model;
			out.putNextEntry(new ZipEntry(entry.getKey()));
			// key corresponds to default model
			if (entry.getKey().equals(InMemoryEndpointService.DEFAULT)) {
				model = dataset.getDefaultModel();
			} else {
				model = dataset.getNamedModel(entry.getValue());
			}
			model.write(new NonClosableOutputStream.Zip(out), LANGUAGE, null);

		}

	}

}
