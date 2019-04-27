package org.knime.semanticweb.nodes.insert;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Lara Gorini
 *
 */
public class InsertRowsNodeFactory extends NodeFactory<InsertRowsNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InsertRowsNodeModel createNodeModel() {
		return new InsertRowsNodeModel();
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
	public NodeView<InsertRowsNodeModel> createNodeView(final int viewIndex, final InsertRowsNodeModel nodeModel) {
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
		return new InsertRowsNodeDialog();
	}

}
