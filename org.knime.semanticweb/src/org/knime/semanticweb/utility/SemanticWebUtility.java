package org.knime.semanticweb.utility;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.semanticweb.services.RDFService;

/**
 * @author Lara Gorini
 *
 */
public class SemanticWebUtility {

	/**
	 * @param row
	 *            The row of {@link BufferedDataTable}
	 * @param graphName_idx
	 *            The index of column containing names of graphs.
	 * @param uri
	 *            The global URI.
	 * @param service
	 *            The {@link RDFService}.
	 * @param globalGraphName
	 *            The name of global graph.
	 * @return the name of graph which is used. If name is null, it is the
	 *         default graph (if service permits one).
	 * @throws InvalidSettingsException
	 *             if service doesn't permit a default graph.
	 */
	public static String getGraphName(final DataRow row, final int graphName_idx, final String uri,
			final RDFService service, final String globalGraphName) throws InvalidSettingsException {

		String localGraphName = null;
		if (graphName_idx >= 0) {
			if (!row.getCell(graphName_idx).isMissing()) {
				localGraphName = getResourceName(uri, ((StringCell) row.getCell(graphName_idx)).getStringValue());
			} else {
				if (globalGraphName.trim().isEmpty()) {
					localGraphName = service.getDefaultGraphName();
				} else {
					localGraphName = globalGraphName;
				}
			}
		} else {
			if (globalGraphName.trim().isEmpty()) {
				localGraphName = service.getDefaultGraphName();
			} else {
				localGraphName = globalGraphName;
			}
		}
		return localGraphName;
	}

	/**
	 * @param defaultUri
	 *            Given default URI.
	 * @param resource
	 *            Resource name.
	 * @return a valid URI for a given resource.
	 */
	public static String getResourceName(final String defaultUri, final String resource) {
		String result = resource;
		if (!resource.startsWith("http://")) {
			result = defaultUri + resource;
		}
		return result;
	}

	/**
	 * @param colIdx
	 *            Index of column.
	 * @param row
	 *            Row if {@link BufferedDataTable}.
	 * @param ignoreMissing
	 *            TRUE if missing cells shall be ignored.
	 * @return DataCell.
	 */
	public static DataCell getCheckCell(final int colIdx, final DataRow row, final boolean ignoreMissing) {
		final DataCell cell;
		if (colIdx < 0) {
			cell = DataType.getMissingCell();
		} else {
			cell = row.getCell(colIdx);
			if (!ignoreMissing && cell.isMissing()) {
				throw new IllegalArgumentException("Missing value found in row with id " + row.getKey());
			}
		}
		return cell;
	}

}
