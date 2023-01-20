package arcade.patch.sim;

import arcade.core.agent.action.Action;
import arcade.core.env.comp.Component;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.patch.agent.action.PatchActionInsert;
import arcade.patch.agent.action.PatchActionRemove;
import arcade.patch.agent.cell.PatchCellFactory;
import arcade.patch.env.comp.PatchComponentSitesGraphRect;
import arcade.patch.env.comp.PatchComponentSitesPatternRect;
import arcade.patch.env.comp.PatchComponentSitesSource;
import arcade.patch.env.lat.PatchLatticeFactory;
import arcade.patch.env.lat.PatchLatticeFactoryRect;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryRect;
import arcade.patch.env.loc.PatchLocationRect;

/**
 * Extension of {@link PatchSimulation} for rectangular geometry.
 */

public final class PatchSimulationRect extends PatchSimulation {
    /**
     * Rectangular simulation for a {@link Series} for given random seed.
     *
     * @param seed  the random seed for random number generator
     * @param series  the simulation series
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
                component = new PatchComponentSitesPatternRect(series, parameters);
                break;
            case "graph_sites":
                component = new PatchComponentSitesGraphRect(series, parameters, random);
                break;
            default:
                break;
        }
        
        return component;
    }
}
