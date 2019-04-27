package org.knime.semanticweb.port;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * @author Lara Gorini
 *
 */
public class SemanticWebPortObjectSpec extends AbstractSimplePortObjectSpec {

	private static final String CFG_TYPE = "type";
	private String m_type;

	/**
	 * @noreference This class is not intended to be referenced by clients.
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<SemanticWebPortObjectSpec> {
	}

	/**
	 * Constructor.
	 */
	public SemanticWebPortObjectSpec() {
		this("");
	}

	/**
	 * @param type
	 *            type
	 */
	public SemanticWebPortObjectSpec(final String type) {
		m_type = type;
	}


	/**
	 * @param model
	 * @throws InvalidSettingsException
	 */
	public SemanticWebPortObjectSpec(final ModelContentRO model) throws InvalidSettingsException {
		load(model);
	}

	@Override
	protected void save(final ModelContentWO model) {
		model.addString(CFG_TYPE, m_type);
	}

	@Override
	protected void load(final ModelContentRO model) throws InvalidSettingsException {
		m_type = model.getString(CFG_TYPE);
	}

}
