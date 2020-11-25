package arcade.core.agent.cell;

/**
 * Container class for loading a {@link Cell}.
 */
public class CellContainer {
	public final int id;
	public final int pop;
	public final int age;
	
	public CellContainer(int id, int pop, int age) {
		this.id = id;
		this.pop = pop;
		this.age = age;
	}
}