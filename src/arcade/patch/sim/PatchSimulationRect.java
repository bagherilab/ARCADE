package arcade.patch.sim;

import arcade.core.agent.action.Action;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.agent.action.PatchActionConvert;
import arcade.patch.agent.action.PatchActionInsert;
import arcade.patch.agent.action.PatchActionRemove;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.component.PatchComponentCycle;
import arcade.patch.env.component.PatchComponentDegrade;
import arcade.patch.env.component.PatchComponentPulse;
import arcade.patch.env.component.PatchComponentRemodel;
import arcade.patch.env.component.PatchComponentSitesGraphRect;
import arcade.patch.env.component.PatchComponentSitesPatternRect;
import arcade.patch.env.component.PatchComponentSitesSource;
import arcade.patch.env.lattice.PatchLatticeFactory;
import arcade.patch.env.lattice.PatchLatticeFactoryRect;
import arcade.patch.env.location.PatchLocationFactory;
import arcade.patch.env.location.PatchLocationFactoryRect;
import arcade.patch.env.location.PatchLocationRect;

/** Extension of {@link PatchSimulation} for rectangular geometry. */
public final class PatchSimulationRect extends PatchSimulation {
    /**
     * Rectangular simulation for a {@link Series} for given random seed.
     *
     * @param seed the random seed for random number generator
     * @param series the simulation series
     */
    public PatchSimulationRect(long seed, Series series) {
        super(seed, series);
        PatchLocationRect.updateConfigs((PatchSeries) series);
    }

    @Override
    public PatchLocationFactory makeLocationFactory() {
        return new PatchLocationFactoryRect();
    }

    @Override
    public PatchCellFactory makeCellFactory() {
        return new PatchCellFactory();
    }

    @Override
    public PatchLatticeFactory makeLatticeFactory() {
        return new PatchLatticeFactoryRect();
    }

    @Override
    public Action makeAction(String actionClass, MiniBox parameters) {
        switch (actionClass) {
            case "insert":
                return new PatchActionInsert(series, parameters);
            case "remove":
                return new PatchActionRemove(series, parameters);
            case "convert":
                return new PatchActionConvert(series, parameters);
            default:
                return null;
        }
    }

    @Override
    public Component makeComponent(String componentClass, MiniBox parameters) {
        switch (componentClass) {
            case "source_sites":
                return new PatchComponentSitesSource(series, parameters);
            case "pattern_sites":
                return new PatchComponentSitesPatternRect(series, parameters);
            case "graph_sites_simple":
                return new PatchComponentSitesGraphRect.Simple(series, parameters, random);
            case "graph_sites_complex":
                return new PatchComponentSitesGraphRect.Complex(series, parameters, random);
            case "pulse":
                return new PatchComponentPulse(series, parameters);
            case "cycle":
                return new PatchComponentCycle(series, parameters);
            case "degrade":
                return new PatchComponentDegrade(series, parameters);
            case "remodel":
                return new PatchComponentRemodel(series, parameters);
            default:
                return null;
        }
    }
}
