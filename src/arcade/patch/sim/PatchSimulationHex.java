package arcade.patch.sim;

import arcade.core.agent.action.Action;
import arcade.core.env.comp.Component;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.agent.action.PatchActionInsert;
import arcade.patch.agent.action.PatchActionRemove;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.comp.PatchComponentSitesGraphTri;
import arcade.patch.env.comp.PatchComponentSitesPatternTri;
import arcade.patch.env.comp.PatchComponentSitesSource;
import arcade.patch.env.lat.PatchLatticeFactory;
import arcade.patch.env.lat.PatchLatticeFactoryTri;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryHex;
import arcade.patch.env.loc.PatchLocationHex;

/**
 * Extension of {@link PatchSimulation} for hexagonal geometry.
 */

public final class PatchSimulationHex extends PatchSimulation {
    /**
     * Hexagonal simulation for a {@link Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
     */
    public PatchSimulationHex(long seed, Series series) {
        super(seed, series);
        PatchLocationHex.updateConfigs((PatchSeries) series);
    }
    
    @Override
    public PatchLocationFactory makeLocationFactory() {
        return new PatchLocationFactoryHex();
    }
    
    @Override
    public PatchCellFactory makeCellFactory() {
        return new PatchCellFactory();
    }
    
    @Override
    public PatchLatticeFactory makeLatticeFactory() {
        return new PatchLatticeFactoryTri();
    }
    
    @Override
    public Action makeAction(String actionClass, MiniBox parameters) {
        Action action = null;
        
        switch (actionClass) {
            case "insert":
                action = new PatchActionInsert(series, parameters);
                break;
            case "remove":
                action = new PatchActionRemove(series, parameters);
                break;
            default:
                break;
        }
        
        return action;
    }
    
    @Override
    public Component makeComponent(String componentClass, MiniBox parameters) {
        Component component = null;
        
        switch (componentClass) {
            case "source_sites":
                component = new PatchComponentSitesSource(series, parameters);
                break;
            case "pattern_sites":
                component = new PatchComponentSitesPatternTri(series, parameters);
                break;
            case "graph_sites":
                component = new PatchComponentSitesGraphTri(series, parameters, random);
                break;
            default:
                break;
        }
        
        return component;
    }
}
