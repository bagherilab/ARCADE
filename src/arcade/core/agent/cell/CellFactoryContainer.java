package arcade.core.agent.cell;

import java.util.ArrayList;

/**
 * Container class for loading into a {@link CellFactory}.
 */
public class CellFactoryContainer {
	/** List of loaded cell containers */
	public final ArrayList<CellContainer> cells;
	
	/** Creates an empty {@link CellFactory} container. */
	public CellFactoryContainer() { cells = new ArrayList<>(); }
}