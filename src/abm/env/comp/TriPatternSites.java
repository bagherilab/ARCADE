package abm.env.comp;

import abm.util.MiniBox;

/**
 * Pattern based generator sites for triangular grid. Previously TriGenerator,
 * TriPatternGenerator.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.12
 * @since   2.0
 */

public class TriPatternSites extends PatternSites {
	private static final long serialVersionUID = 0;
	
	// CONSTRUCTOR.
	public TriPatternSites(MiniBox component) { super(component); }
	
	// METHOD: calcOffset. Calculates offset based on layer.
	private int calcOffset(int k) { return (DEPTH - k/2 - 1)%3; }
	
	// METHOD: calcCol. Calculates column based on offset and index.
	private int calcCol(int i, int offset) { return (i + 6*offset)%9; }
	
	// METHOD: calcRow. Calculates row based on offset and index.
	private int calcRow(int i, int j, int offset) { return (j + (((i + 6*offset)/9 & 1) == 0 ? 0 : 3))%6; }
	
	// METHOD: calcAvg. Calculates average change in concentration between the
	// two triangles bordering a capillary.
	double calcAvg(int i, int j, int k, double[][] delta) {
		double avg = delta[i][j];
		int offset = calcOffset(k);
		double site = sites[k][i][j];
		
		switch (calcCol(i, offset)) {
			case 0: case 7: case 5:
				if (j != WIDTH - 1) {
					avg += delta[i][j + (site == 1 ? 1 : -1)];
					avg /= 2;
				}
				break;
			case 1: case 2: case 3: case 4:
				if (i != LENGTH - 1 && !(i == 0 && site == 2)) {
					avg += delta[i + (site == 1 ? 1 : -1)][j];
					avg /= 2;
				}
				break;
		}
		
		return avg;
	}
	
	// METHOD: calcFlow. Calculates final change in source concentration based
	// on change in the previous blood vessel (left neighbors).
	public void calcFlow(int i, int j, int k, int[] borders, double[][] delta, double[][] flow) {
		double total = 0;
		double val = calcAvg(i, j, k, delta);
		int offset = calcOffset(k);
		
		switch (calcCol(i, offset)) {
			case 0: case 7:
				if (i == 0 || i == 1) { total = val; }
				else { total = flow[i - 2][j] + val; }
				flow[i][j + borders[BOTTOM]] = total;
				break;
			case 1:
				total = flow[i - 1][j] + val;
				flow[i + borders[RIGHT]][j] = total;
				break;
			case 5:
				total = (flow[i - 1][j + (1 - borders[TOP])] +
					flow[i - 1][j + (j > WIDTH - 3 ? 0 : 1)])/2 + val;
				flow[i][j + borders[BOTTOM]] = total;
				break;
			case 2: case 3:
				switch (calcRow(i, j, offset)) {
					case 1: case 2:
						if (j != WIDTH - 1 && i != 0) { total = flow[i - 1][j + 1] + val; }
						break;
					case 0: case 5:
						if (j != 0 && i != 0) { total = flow[i - 1][j - 1] + val; }
						break;
				}
				flow[i + borders[RIGHT]][j] = total;
				break;
		}
		
		flow[i][j] = total;
	}
	
	// METHOD: calcDamage. Calculates total damage between the two triangles
	// bordering a capillary.
	public void calcDamage(int i, int j, int k, int[] borders) {
		double total = damageSingle[k][i][j];
		int offset = calcOffset(k);
		
		switch (calcCol(i, offset)) {
			case 0: case 7: case 5:
				if (j != WIDTH - 1) {
					total += damageSingle[k][i][j + 1];
					total /= 2;
					damageTotal[k][i][j + 1] = total;
				}
				break;
			case 1: case 2: case 3:
				if (i != LENGTH - 1) {
					total += damageSingle[k][i + 1][j];
					total /= 2;
					damageTotal[k][i + 1][j] = total;
				}
				break;
		}
		
		damageTotal[k][i][j] = total;
	}
	
	// METHOD: makeSites. Create double array indicating position of
	// capillaries where values of 1 or 2 indicate a capillary border.
	void makeSites() {
		byte[][] pattern = {
			{0, 0, 0, 1, 2, 1, 0, 1, 0},
			{0, 0, 0, 1, 2, 2, 0, 2, 0},
			{0, 0, 1, 2, 0, 0, 0, 0, 0},
			{1, 1, 2, 0, 0, 0, 0, 0, 0},
			{2, 1, 2, 0, 0, 0, 0, 0, 0},
			{0, 0, 1, 2, 0, 0, 0, 0, 0}
		};
		
		for (int k = 0; k < DEPTH; k += 2) {
			int offset = calcOffset(k);
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					int col = calcCol(i, offset);
					int row = calcRow(i, j, offset);
					sites[k][i][j] = pattern[row][col];
				}
			}
		}
	}
}