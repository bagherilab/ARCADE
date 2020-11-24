package arcade.potts;

import static arcade.potts.agent.module.PottsModule.Phase;

public final class PottsTestUtilities {
	public static Phase randomPhase() { return Phase.values()[(int)(Math.random()*Phase.values().length - 1) + 1]; }
}

