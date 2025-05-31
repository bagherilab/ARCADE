package arcade.patch.env.lattice;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationDecayer;
import arcade.patch.env.operation.PatchOperationDiffuserRect;
import arcade.patch.env.operation.PatchOperationGenerator;
import arcade.patch.util.PatchEnums.Category;

public class PatchLatticeRectTest {
    @Test
    public void constructor_calledWithDiffuser_returnsCorrectDiffuser() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/DIFFUSER", "");

        PatchLatticeRect lattice = new PatchLatticeRect(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.DIFFUSER) instanceof PatchOperationDiffuserRect);
    }

    @Test
    public void constructor_calledWithGeneratorParameter_returnsGenerator() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/GENERATOR", "");

        PatchLatticeRect lattice = new PatchLatticeRect(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.GENERATOR) instanceof PatchOperationGenerator);
    }

    @Test
    public void constructor_calledWithDecayerParameter_returnsDecayer() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/DECAYER", "");

        PatchLatticeRect lattice = new PatchLatticeRect(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.DECAYER) instanceof PatchOperationDecayer);
    }
}
