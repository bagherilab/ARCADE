package arcade.patch.env.lattice;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationDecayer;
import arcade.patch.env.operation.PatchOperationDiffuserTri;
import arcade.patch.env.operation.PatchOperationGenerator;
import arcade.patch.util.PatchEnums.Category;

public class PatchLatticeTriTest {
    @Test
    public void makeOperation_calledWithDiffuser_returnsCorrectDiffuser() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/DIFFUSER", "");

        PatchLatticeTri lattice = new PatchLatticeTri(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.DIFFUSER) instanceof PatchOperationDiffuserTri);
    }

    @Test
    public void makeOperation_calledWithGenerator_returnsGenerator() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/GENERATOR", "");

        PatchLatticeTri lattice = new PatchLatticeTri(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.GENERATOR) instanceof PatchOperationGenerator);
    }

    @Test
    public void makeOperation_calledWithDecayer_returnsDecayer() {
        MiniBox parameters = new MiniBox();
        parameters.put("(OPERATION)/DECAYER", "");

        PatchLatticeTri lattice = new PatchLatticeTri(3, 3, 3, 1, 1, parameters);

        assert (lattice.getOperation(Category.DECAYER) instanceof PatchOperationDecayer);
    }
}
