package arcade.patch;

import java.nio.file.Path;
import java.util.ArrayList;
import com.google.gson.Gson;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.LocationContainer;
import arcade.core.sim.output.OutputLoader;
import arcade.patch.sim.output.PatchOutputDeserializer;
import static arcade.core.sim.Simulation.DEFAULT_CELL_TYPE;
import static arcade.core.sim.Simulation.DEFAULT_LOCATION_TYPE;

public final class PatchARCADETestUtilities {
    protected PatchARCADETestUtilities() {
        throw new UnsupportedOperationException();
    }

    public static ArrayList<CellContainer> loadCellsFile(
            Path path, String series, int seed, int tick) {
        String cellsFile =
                String.format(
                        "%s/%s_%04d_%06d.CELLS.json", path.toAbsolutePath(), series, seed, tick);
        Gson gson = PatchOutputDeserializer.makeGSON();
        String cellOutput = OutputLoader.read(cellsFile);
        return gson.fromJson(cellOutput, DEFAULT_CELL_TYPE);
    }

    public static ArrayList<LocationContainer> loadLocationsFile(
            Path path, String series, int seed, int tick) {
        String locsFile =
                String.format(
                        "%s/%s_%04d_%06d.LOCATIONS.json",
                        path.toAbsolutePath(), series, seed, tick);
        Gson gson = PatchOutputDeserializer.makeGSON();
        String locsOutput = OutputLoader.read(locsFile);
        return gson.fromJson(locsOutput, DEFAULT_LOCATION_TYPE);
    }
}
