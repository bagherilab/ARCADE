package arcade.potts.agent.process;

import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.agent.process.PottsProcessSecretionTest.makeCell;
import static arcade.potts.agent.process.PottsProcessSensingHypoxicTest.sensing;

public class PottsProcessSensingTest {
    @Test
    public void make_hypoxicVersion_returnsHypoxicProcess() {
        PottsCell cell = makeCell(sensing(1, 12, 4));
        assertTrue(PottsProcessSensing.make(cell, "HYPOXIC") instanceof PottsProcessSensingHypoxic);
    }

    @Test
    public void make_lowerCaseVersion_returnsHypoxicProcess() {
        PottsCell cell = makeCell(sensing(1, 12, 4));
        assertTrue(PottsProcessSensing.make(cell, "hypoxic") instanceof PottsProcessSensingHypoxic);
    }

    @Test
    public void make_unknownVersion_returnsNull() {
        PottsCell cell = makeCell(new MiniBox());
        assertNull(PottsProcessSensing.make(cell, "OTHER"));
        assertNull(PottsProcessSensing.make(cell, ""));
    }
}
