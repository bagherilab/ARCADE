package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.sim.PottsSimulation.TRANSCRIPTION_FACTORS_TYPE;

/** Custom saver for potts-specific serialization. */
public final class PottsOutputSaver extends OutputSaver {
    /**
     * Creates a {@code PottsOutputSaver} for the series.
     *
     * @param series the simulation series
     */
    public PottsOutputSaver(Series series) {
        super(series);
    }

    /** {@code true} to save transcription factors, {@code false} otherwise. */
    public boolean saveTranscriptionFactors;

    @Override
    protected Gson makeGSON() {
        return PottsOutputSerializer.makeGSON();
    }

    public void saveTranscriptionFactors(int tick) {
        if (sim instanceof PottsSimulation) {
            String json =
                    gson.toJson(
                            ((PottsSimulation) sim).getAllTranscriptionFactors(),
                            TRANSCRIPTION_FACTORS_TYPE);
            String patch = prefix + String.format("_%06d.TRANSCRIPTION_FACTORS.json", tick);
            write(patch, format(json, FORMAT_ELEMENTS));
        }
    }

    @Override
    public void save(int tick) {
        super.save(tick);
        if (saveTranscriptionFactors) {
            saveTranscriptionFactors(tick);
        }
    }
}
