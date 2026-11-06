package arcade.potts.agent.process;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.agent.process.PottsProcessDepositionECMTest.deposition;
import static arcade.potts.agent.process.PottsProcessSecretionTest.makeCell;

public class PottsProcessDepositionTest {
    @Test
    public void make_ecmVersion_returnsEcmProcess() {
        PottsCell cell = makeCell(deposition(1, 0, 0));
        assertTrue(PottsProcessDeposition.make(cell, "ECM") instanceof PottsProcessDepositionECM);
    }

    @Test
    public void make_lowerCaseVersion_returnsEcmProcess() {
        PottsCell cell = makeCell(deposition(1, 0, 0));
        assertTrue(PottsProcessDeposition.make(cell, "ecm") instanceof PottsProcessDepositionECM);
    }

    @Test
    public void make_unknownVersion_returnsNull() {
        PottsCell cell = makeCell(new MiniBox());
        assertNull(PottsProcessDeposition.make(cell, "COLLAGEN"));
        assertNull(PottsProcessDeposition.make(cell, ""));
    }
}
