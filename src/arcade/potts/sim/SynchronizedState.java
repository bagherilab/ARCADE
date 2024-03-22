package arcade.potts.sim;

import sim.engine.SimState;

class SynchronizedState {

    private SimState simState;
    private int[][][] regions;
    private int[][][] ids;

    public SynchronizedState(SimState simState, int[][][] regions, int[][][] ids) {
        this.simState = simState;
        this.regions = regions;
        this.ids = ids;
    }

    public synchronized double nextDouble() {
        return simState.random.nextDouble();
    }

    public synchronized int nextInt(int upperBound) {
        return simState.random.nextInt(upperBound);
    }

    public synchronized int getRegion(int x, int y, int z) {
        return regions[x][y][z];
    }

    public synchronized int getId(int z, int y, int x) {
        return ids[z][y][x];
    }
}
