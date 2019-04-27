package org.knime.semanticweb.nodes.writefile;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author Lara Gorini
 *
 */
public class WriteToFileNodeFactory extends NodeFactory<WriteToFileNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WriteToFileNodeModel createNodeModel() {
		return new WriteToFileNodeModel();
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
	public NodeView<WriteToFileNodeModel> createNodeView(final int viewIndex, final WriteToFileNodeModel nodeModel) {
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
		return new WriteToFileNodeDialog();
	}

}
