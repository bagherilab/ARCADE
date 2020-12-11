package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.*;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Container class for loading a {@link PottsCell}.
 */
public final class PottsCellContainer implements CellContainer {
    public final int id;
    public final int pop;
    public final int age;
    public final State state;
    public final Phase phase;
    public final int voxels;
    public final EnumMap<Region, Integer> regionVoxels;
    public final double targetVolume;
    public final double targetSurface;
    public final EnumMap<Region, Double> regionTargetVolume;
    public final EnumMap<Region, Double> regionTargetSurface;
    
    public PottsCellContainer(int id, int pop, int voxels) {
        this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels,
                null, 0, 0, null, null);
    }
    
    public PottsCellContainer(int id, int pop, int voxels, EnumMap<Region, Integer> regionVoxels) {
        this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels,
                regionVoxels, 0, 0, null, null);
    }
    
    public PottsCellContainer(int id, int pop, int age, State state, Phase phase,
                              int voxels, double targetVolume, double targetSurface) {
        this(id, pop, age, state, phase, voxels, null, targetVolume, targetSurface, null, null);
    }
    
    public PottsCellContainer(int id, int pop, int age, State state, Phase phase, int voxels,
                              EnumMap<Region, Integer> regionVoxels,
                              double targetVolume, double targetSurface,
                              EnumMap<Region, Double> regionTargetVolume,
                              EnumMap<Region, Double> regionTargetSurface) {
        this.id = id;
        this.pop = pop;
        this.age = age;
        this.state = state;
        this.phase = phase;
        this.voxels = voxels;
        this.regionVoxels = regionVoxels;
        this.targetVolume = targetVolume;
        this.targetSurface = targetSurface;
        this.regionTargetVolume = regionTargetVolume;
        this.regionTargetSurface = regionTargetSurface;
    }
    
    @Override
    public int getID() { return id; }
    
    @Override
    public Cell convert(CellFactory factory, Location location) {
        return convert((PottsCellFactory) factory, location);
    }
    
    private Cell convert(PottsCellFactory factory, Location location) {
        // Get copies of critical, lambda, and adhesion values.
        MiniBox parameters = factory.popToParameters.get(pop);
        EnumMap<Term, Double> criticals = factory.popToCriticals.get(pop).clone();
        EnumMap<Term, Double> lambdas = factory.popToLambdas.get(pop).clone();
        double[] adhesion = factory.popToAdhesion.get(pop).clone();
        
        // Make cell.
        PottsCell cell;
        
        if (factory.popToRegions.get(pop)) {
            // Initialize region arrays.
            EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = new EnumMap<>(Region.class);
            EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = new EnumMap<>(Region.class);
            EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = new EnumMap<>(Region.class);
            
            // Get copies of critical, lambda, and adhesion values.
            for (Region region : location.getRegions()) {
                criticalsRegion.put(region,
                        factory.popToRegionCriticals.get(pop).get(region).clone());
                lambdasRegion.put(region,
                        factory.popToRegionLambdas.get(pop).get(region).clone());
                adhesionRegion.put(region,
                        factory.popToRegionAdhesion.get(pop).get(region).clone());
            }
            
            cell = new PottsCell(id, pop, state, age, location, true, parameters, adhesion,
                    criticals, lambdas, criticalsRegion, lambdasRegion, adhesionRegion);
        } else {
            cell = new PottsCell(id, pop, state, age, location, false, parameters, adhesion,
                    criticals, lambdas, null, null, null);
        }
        
        // Update cell targets.
        cell.setTargets(targetVolume, targetSurface);
        if (regionTargetVolume != null && regionTargetSurface != null) {
            for (Region region : location.getRegions()) {
                cell.setTargets(region,
                        regionTargetVolume.get(region),
                        regionTargetSurface.get(region));
            }
        }
        
        // Update cell module.
        PottsModule module = (PottsModule) cell.getModule();
        if (module != null) { module.setPhase(phase); }
        
        return cell;
    }
}
