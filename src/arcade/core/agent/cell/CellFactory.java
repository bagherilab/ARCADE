package arcade.core.agent.cell;

import java.util.ArrayList;
import arcade.core.sim.Series;
import arcade.core.env.loc.Location;
import static arcade.core.agent.cell.Cell.State;

public interface CellFactory {
	/**
	 * Container class for loading a {@link Cell}.
	 */
	class CellContainer {
		public final int id;
		public final int pop;
		public final int age;
		public final Cell.State state;
		
		public CellContainer(int id, int pop, int age, State state) {
			this.id = id;
			this.pop = pop;
			this.age = age;
			this.state = state;
		}
	}
	
	/**
	 * Container class for loading into a {@link CellFactory}.
	 */
	class CellFactoryContainer {
		/** List of loaded cell containers */
		public final ArrayList<CellContainer> cells;
		
		/** Creates an empty {@link CellFactory} container. */
		public CellFactoryContainer() { cells = new ArrayList<>(); }
	}
	
	/**
	 * Initializes the factory for the given series.
	 * 
	 * @param series  the simulation series
	 */
	void initialize(Series series);
	
	/**
	 * Loads cell containers into the factory container.
	 *
	 * @param series  the simulation series
	 */
	void loadCells(Series series);
	
	/**
	 * Creates cell containers from population settings.
	 * 
	 * @param series  the simulation series
	 */
	void createCells(Series series);
	
	/**
	 * Create a {@link arcade.core.agent.cell.Cell} object.
	 *
	 * @param cellContainer  the cell container
	 * @param location  the cell location
	 * @return  a {@link arcade.core.agent.cell.Cell} object
	 */
	Cell make(CellContainer cellContainer, Location location);
}