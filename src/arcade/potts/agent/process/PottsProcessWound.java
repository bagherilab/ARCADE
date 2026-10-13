package arcade.potts.agent.process;

import java.util.List;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Domain;
import arcade.potts.util.PottsEnums.State;

/**
 * Wound-healing phenotype process.
 *
 * <p>Each tick, a cell senses mean PDGF, ECM and INF, switches phenotype (fibroblast,
 * myofibroblast, senescent, macrophage) and sets its secretion rates for the tick. Steps before
 * {@link PottsProcessSecretion}.
 */
public class PottsProcessWound extends PottsProcess {
    /** Cell phenotypes. */
    public enum Phenotype {
        /** Fibroblast. */
        FIBROBLAST,

        /** Myofibroblast. */
        MYOFIBROBLAST,

        /** Senescent fibroblast. */
        SENESCENT,

        /** Macrophage. */
        MACROPHAGE,

        /** Generic tissue (parenchyma) cell. */
        TISSUE
    }

    /** Neighbour offsets in x. */
    private static final int[] DX = {1, -1, 0, 0};

    /** Neighbour offsets in y. */
    private static final int[] DY = {0, 0, 1, -1};

    /** Current phenotype. */
    Phenotype phenotype;

    /** Age as a myofibroblast [ticks]. */
    int myofibroblastAge;

    /** Age as a senescent cell [ticks]. */
    int senescentAge;

    /** True if the cell is currently gated into proliferation. */
    boolean proliferating;

    /** Macrophage population code; -1 if none, null if not yet resolved. */
    private Integer macrophageCode;

    /**
     * Creates a wound process for the cell.
     *
     * @param cell the cell
     */
    public PottsProcessWound(PottsCell cell) {
        super(cell);
        Parameters parameters = cell.getParameters();
        String role = parameters == null ? "FIBROBLAST" : parameters.getString("wound/ROLE");
        phenotype = Phenotype.valueOf(role);
        if (phenotype != Phenotype.FIBROBLAST
                && phenotype != Phenotype.MACROPHAGE
                && phenotype != Phenotype.TISSUE) {
            throw new IllegalArgumentException("invalid wound ROLE [ " + role + " ]");
        }
    }

    /**
     * Gets the phenotype.
     *
     * @return the phenotype
     */
    public Phenotype getPhenotype() {
        return phenotype;
    }

    /**
     * Gets the myofibroblast age.
     *
     * @return the age [ticks]
     */
    public int getMyofibroblastAge() {
        return myofibroblastAge;
    }

    /**
     * Gets the senescent age.
     *
     * @return the age [ticks]
     */
    int getSenescentAge() {
        return senescentAge;
    }

    /**
     * Gets whether the cell is gated into proliferation.
     *
     * @return {@code true} if proliferating
     */
    boolean isProliferating() {
        return proliferating;
    }

    /**
     * Gets the phenotype label for output; senescent cells are split by phase.
     *
     * @return the label
     */
    public String getPhenotypeLabel() {
        if (phenotype == Phenotype.SENESCENT) {
            return senescentAge <= param("T_NIS") ? "SENESCENT_FIBROGENIC" : "SENESCENT_FIBROLYTIC";
        }
        return phenotype.name();
    }

    /**
     * Gets the wound ages.
     *
     * @return {myofibroblastAge, senescentAge} [ticks]
     */
    public int[] getAges() {
        return new int[] {myofibroblastAge, senescentAge};
    }

    /**
     * Restores phenotype and ages, e.g. for a same-population daughter.
     *
     * @param label the phenotype label (senescent labels carry a phase suffix)
     * @param ages {myofibroblastAge, senescentAge}, or {@code null} for zero
     */
    public void restore(String label, int[] ages) {
        phenotype = Phenotype.valueOf(label.startsWith("SENESCENT") ? "SENESCENT" : label);
        myofibroblastAge = ages == null ? 0 : ages[0];
        senescentAge = ages == null ? 0 : ages[1];
    }

    /**
     * Gets a wound process parameter.
     *
     * @param id the parameter id
     * @return the parameter value
     */
    private double param(String id) {
        return cell.getParameters().getDouble("wound/" + id);
    }

    /**
     * Gets the mean layer value over the cell's voxels.
     *
     * @param sim the simulation
     * @param layer the layer id
     * @return the mean value, or 0 if the layer does not exist
     */
    private double mean(Simulation sim, String layer) {
        PottsLattice lattice = (PottsLattice) sim.getLattice(layer);
        return lattice == null ? 0 : lattice.getAverageValue(location);
    }

    /**
     * Gets the population code of the macrophage population, resolved on first use.
     *
     * @param sim the simulation
     * @return the population code, or -1 if there is no macrophage population
     */
    private int resolveMacrophageCode(Simulation sim) {
        if (macrophageCode == null) {
            macrophageCode = -1;
            String id = cell.getParameters().getString("wound/MACROPHAGE");
            if (id != null && !id.equals("NONE")) {
                MiniBox pop = sim.getSeries().populations.get(id);
                if (pop != null) {
                    macrophageCode = pop.getInt("CODE");
                }
            }
        }
        return macrophageCode;
    }

    /**
     * Gets the empty, in-domain boundary voxels and the total boundary size.
     *
     * @param sim the simulation
     * @param boundarySize one-element array that receives the total boundary size
     * @return the free boundary voxels
     */
    private List<Voxel> freeBoundaryVoxels(Simulation sim, int[] boundarySize) {
        int[][][] ids = ((PottsSimulation) sim).getPotts().ids;
        PottsProcessSecretion secretion = (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
        List<Voxel> boundary =
                secretion != null ? secretion.getBoundaryVoxels(ids) : boundaryVoxels(ids);
        boundarySize[0] = boundary.size();
        List<Voxel> free = new java.util.ArrayList<>();
        for (Voxel v : boundary) {
            // Potts border padding (index 0 and length - 1) is always id 0 but is not part of
            // the domain: padding voxels stay in the denominator and count as occupied.
            boolean inside =
                    v.x >= 1
                            && v.y >= 1
                            && v.x <= ids[v.z].length - 2
                            && v.y <= ids[v.z][0].length - 2;
            if (inside && ids[v.z][v.x][v.y] == 0) {
                free.add(v);
            }
        }
        return free;
    }

    /**
     * Gets the fraction of boundary voxels that are empty and inside the domain.
     *
     * @param sim the simulation
     * @return the free fraction, or 0 if the cell has no boundary voxels
     */
    private double freeFraction(Simulation sim) {
        int[] size = new int[1];
        List<Voxel> free = freeBoundaryVoxels(sim, size);
        return size[0] == 0 ? 0 : (double) free.size() / size[0];
    }

    /**
     * Checks whether the mean ECM over the free boundary voxels reaches the tissue growth gate.
     *
     * @param sim the simulation
     * @return {@code true} if the gate is off, or the mean free-space ECM is at least the gate
     */
    private boolean freeEcmPermitsGrowth(Simulation sim) {
        double gate = param("ECM_TISSUE_GROWTH");
        if (gate <= 0) {
            return true;
        }
        PottsLattice lattice = (PottsLattice) sim.getLattice("ECM");
        List<Voxel> free = freeBoundaryVoxels(sim, new int[1]);
        if (lattice == null || free.isEmpty()) {
            return false;
        }
        double[][][] field = lattice.getField();
        double sum = 0;
        for (Voxel v : free) {
            sum += field[v.z][v.x][v.y];
        }
        return sum / free.size() >= gate;
    }

    /**
     * Checks whether the cell touches a macrophage.
     *
     * @param sim the simulation
     * @return {@code true} if a boundary voxel belongs to a macrophage
     */
    private boolean touchesMacrophage(Simulation sim) {
        int code = resolveMacrophageCode(sim);
        if (code < 0) {
            return false;
        }
        PottsSimulation pottsSim = (PottsSimulation) sim;
        int[][][] ids = pottsSim.getPotts().ids;
        PottsProcessSecretion secretion = (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
        List<Voxel> boundary =
                secretion != null ? secretion.getBoundaryVoxels(ids) : boundaryVoxels(ids);
        for (Voxel v : boundary) {
            int id = ids[v.z][v.x][v.y];
            if (id > 0 && pottsSim.getPotts().getCell(id).getPop() == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the cell touches a senescent cell.
     *
     * @param sim the simulation
     * @return {@code true} if a boundary voxel belongs to a senescent cell
     */
    private boolean touchesSenescent(Simulation sim) {
        PottsSimulation pottsSim = (PottsSimulation) sim;
        int[][][] ids = pottsSim.getPotts().ids;
        PottsProcessSecretion secretion = (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
        List<Voxel> boundary =
                secretion != null ? secretion.getBoundaryVoxels(ids) : boundaryVoxels(ids);
        for (Voxel v : boundary) {
            int id = ids[v.z][v.x][v.y];
            if (id <= 0) {
                continue;
            }
            PottsProcessWound other =
                    (PottsProcessWound) pottsSim.getPotts().getCell(id).getProcess(Domain.WOUND);
            if (other != null && other.phenotype == Phenotype.SENESCENT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the aging senescence routes to a fibroblast or myofibroblast.
     *
     * <p>Draws are only made for routes with a non-zero rate, so defaults leave the random stream
     * unchanged.
     *
     * @param random the random number generator
     * @param sim the simulation
     * @param inf the mean INF over the cell
     * @return {@code true} if the cell became senescent
     */
    private boolean checkSenescenceRoutes(MersenneTwisterFast random, Simulation sim, double inf) {
        int age = -1;
        double spontaneous = param("SPONTANEOUS_SENESCENCE");
        double contact = param("BYSTANDER_CONTACT");
        double paracrine = param("BYSTANDER_PARACRINE");
        boolean tissue = phenotype == Phenotype.TISSUE;
        if ((phenotype == Phenotype.FIBROBLAST || tissue)
                && spontaneous > 0
                && random.nextDouble() < spontaneous) {
            age = (int) param("T_NIS") + 1;
        } else if (contact > 0 && touchesSenescent(sim) && random.nextDouble() < contact) {
            age = tissue ? (int) param("T_NIS") + 1 : 0;
        } else if (paracrine > 0
                && inf > param("INF_BYSTANDER")
                && random.nextDouble() < paracrine) {
            age = tissue ? (int) param("T_NIS") + 1 : 0;
        }
        if (age < 0) {
            return false;
        }
        phenotype = Phenotype.SENESCENT;
        senescentAge = age;
        myofibroblastAge = 0;
        proliferating = false;
        cell.setState(State.QUIESCENT);
        if (age == 0) {
            secrete("PDGF", param("SEN_PDGF"), "ECM", param("SEN_ECM"));
        } else {
            secrete("INF", param("SEN_INF"), "MMP", param("SEN_MMP"));
        }
        return true;
    }

    /**
     * Gets the 2D boundary voxels, used when the cell has no secretion process.
     *
     * @param ids the potts array of cell ids
     * @return the list of unique boundary voxels within the array
     */
    private List<Voxel> boundaryVoxels(int[][][] ids) {
        java.util.LinkedHashSet<Voxel> boundary = new java.util.LinkedHashSet<>();
        int self = cell.getID();
        for (Voxel v : location.getVoxels()) {
            for (int n = 0; n < DX.length; n++) {
                int x = v.x + DX[n];
                int y = v.y + DY[n];
                if (x < 0 || y < 0 || x >= ids[v.z].length || y >= ids[v.z][0].length) {
                    continue;
                }
                if (ids[v.z][x][y] != self) {
                    boundary.add(new Voxel(x, y, v.z));
                }
            }
        }
        return new java.util.ArrayList<>(boundary);
    }

    /**
     * Sets this tick's secretion rates; layers not listed are set to zero.
     *
     * @param amounts alternating layer id and rate [amount/tick]
     */
    private void secrete(Object... amounts) {
        PottsProcessSecretion secretion = (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
        if (secretion == null) {
            return;
        }
        for (String layer : secretion.rates.keySet()) {
            secretion.setRate(layer, 0);
        }
        for (int i = 0; i < amounts.length; i += 2) {
            String layer = (String) amounts[i];
            if (secretion.rates.containsKey(layer)) {
                secretion.setRate(layer, (Double) amounts[i + 1]);
            }
        }
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        if (cell.getState() == State.APOPTOTIC) {
            return;
        }

        if (phenotype == Phenotype.MACROPHAGE) {
            if (cell.getState() != State.QUIESCENT) {
                cell.setState(State.QUIESCENT);
            }
            return;
        }

        double pdgf = mean(sim, "PDGF");
        double ecm = mean(sim, "ECM");
        double inf = mean(sim, "INF");

        if (phenotype == Phenotype.SENESCENT) {
            if (cell.getState() != State.QUIESCENT) {
                cell.setState(State.QUIESCENT);
            }
            senescentAge++;
            boolean fibrogenic = senescentAge <= param("T_NIS");

            double basal = param("BASAL_CLEARANCE");
            if (basal > 0 && random.nextDouble() < basal) {
                cell.setState(State.APOPTOTIC);
                return;
            }

            if (!fibrogenic && touchesMacrophage(sim) && random.nextDouble() < param("CLEARANCE")) {
                cell.setState(State.APOPTOTIC);
                return;
            }

            double kill = param("DRUG_KILL");
            if (kill > 0) {
                double tick =
                        sim.getSchedule().getTime() + ((PottsSeries) sim.getSeries()).tickOffset;
                double start = param("DRUG_START");
                if (tick >= start
                        && tick < start + param("DRUG_DURATION")
                        && random.nextDouble() < kill) {
                    cell.setState(State.APOPTOTIC);
                    return;
                }
            }

            if (fibrogenic) {
                secrete("PDGF", param("SEN_PDGF"), "ECM", param("SEN_ECM"));
            } else {
                secrete("INF", param("SEN_INF"), "MMP", param("SEN_MMP"));
            }
            return;
        }

        double minFree = param("CONTACT_FREE_FRACTION");
        if (phenotype == Phenotype.TISSUE) {
            double free = freeFraction(sim);
            boolean blocked = minFree > 0 && free < minFree;
            boolean matrix = freeEcmPermitsGrowth(sim);
            if (cell.getState() == State.QUIESCENT && !blocked && free > 0 && matrix) {
                cell.setState(State.PROLIFERATIVE);
            } else if (cell.getState() == State.PROLIFERATIVE
                    && (blocked || free == 0 || !matrix)) {
                cell.setState(State.QUIESCENT);
            }
            proliferating = cell.getState() == State.PROLIFERATIVE;
            if (!checkSenescenceRoutes(random, sim, inf)) {
                secrete();
            }
            return;
        }

        boolean blocked = minFree > 0 && freeFraction(sim) < minFree;
        double gate = param("PDGF_PROLIFERATION");
        if (cell.getState() == State.QUIESCENT && pdgf > gate && !blocked) {
            cell.setState(State.PROLIFERATIVE);
        } else if (cell.getState() == State.PROLIFERATIVE && (pdgf < gate / 2 || blocked)) {
            cell.setState(State.QUIESCENT);
        }
        proliferating = cell.getState() == State.PROLIFERATIVE;

        if (phenotype == Phenotype.FIBROBLAST) {
            if (checkSenescenceRoutes(random, sim, inf)) {
                return;
            }
            if (pdgf > param("PDGF_DIFFERENTIATION")
                    && ecm > param("ECM_DIFFERENTIATION")
                    && random.nextDouble() < param("DIFFERENTIATION_RATE")) {
                phenotype = Phenotype.MYOFIBROBLAST;
                myofibroblastAge = 0;
            }
        } else {
            myofibroblastAge++;
            if (inf > param("INF_APOPTOSIS")) {
                cell.setState(State.APOPTOTIC);
                return;
            }
            if (checkSenescenceRoutes(random, sim, inf)) {
                return;
            }
            if (myofibroblastAge > param("MATURATION")) {
                if (ecm < param("ECM_TENSION")) {
                    cell.setState(State.APOPTOTIC);
                    return;
                } else if (random.nextDouble() < param("SENESCENCE_RATE")) {
                    phenotype = Phenotype.SENESCENT;
                    senescentAge = 0;
                    cell.setState(State.QUIESCENT);
                    proliferating = false;
                }
            }
        }

        switch (phenotype) {
            case MYOFIBROBLAST:
                secrete("ECM", param("MYOF_ECM"), "PDGF", param("MYOF_PDGF"));
                break;
            case SENESCENT:
                // Newly senescent (age 0): fibrogenic.
                secrete("PDGF", param("SEN_PDGF"), "ECM", param("SEN_ECM"));
                break;
            default:
                secrete("ECM", param("FIB_ECM"));
        }
    }
}
