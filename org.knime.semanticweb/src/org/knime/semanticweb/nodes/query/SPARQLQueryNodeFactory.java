package org.knime.semanticweb.nodes.query;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Lara Gorini
 */
public class SPARQLQueryNodeFactory extends NodeFactory<SPARQLQueryNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SPARQLQueryNodeModel createNodeModel() {
		return new SPARQLQueryNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<SPARQLQueryNodeModel> createNodeView(final int viewIndex, final SPARQLQueryNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new SPARQLQueryNodeDialog();
	}

}
