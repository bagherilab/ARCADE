package arcade.util;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Comparator;
import static arcade.util.Matrix.*;

public class MatrixTest {
	private static final double EPSILON = 1E-10;
	
	private static final double[][] A1D = new double[][] {
		{  1,  2,  3,  4,  5 },
		{  6,  7,  8,  9, 10 },
		{ 11, 12, 13, 14, 15 },
		{ 16, 17, 18, 19, 20 },
		{ 21, 22, 23, 24, 25 }
	};
	
	private static final double[][] A2D = new double[][] {
		{ 2, 3, 0, 1, 2 },
		{ 0, 1, 2, 3, 1 },
		{ 0, 3, 1, 1, 0 },
		{ 0, 1, 2, 1, 1 },
		{ 0, 0, 0, 2, 1 }
	};
	
	private static final double[][] A3D = new double[][] {
		{ 1, 0, 4, 3, 0 },
		{ 6, 2, 0, 2, 2 },
		{ 4, 3, 1, 0, 0 },
		{ 2, 3, 2, 1, 0 },
		{ 3, 1, 3, 2, 1 }
	};
	
	private static final double[] B1D = new double[] { 2, 3, 6, 1, 2 };
	
	private static final ArrayList<Value> A1S = new ArrayList<>();
	
	private static final ArrayList<Value> A2S = new ArrayList<>();
	
	private static final ArrayList<Value> A3S = new ArrayList<>();
	
	private static final ArrayList<Value> B1S = new ArrayList<>();
	
	public static final Comparator<Value> COMPARATOR = (v1, v2) ->
			v1.i != v2.i ? Integer.compare(v1.i, v2.i) :
					v1.j != v2.j ? Integer.compare(v1.j, v2.j) :
							Double.compare(v1.v, v2.v);
	
	@BeforeClass
	public static void setupMatrices() {
		A1S.add(new Value(0, 0, 1));
		A1S.add(new Value(0, 1, 2));
		A1S.add(new Value(0, 2, 3));
		A1S.add(new Value(0, 3, 4));
		A1S.add(new Value(0, 4, 5));
		A1S.add(new Value(1, 0, 6));
		A1S.add(new Value(1, 1, 7));
		A1S.add(new Value(1, 2, 8));
		A1S.add(new Value(1, 3, 9));
		A1S.add(new Value(1, 4, 10));
		A1S.add(new Value(2, 0, 11));
		A1S.add(new Value(2, 1, 12));
		A1S.add(new Value(2, 2, 13));
		A1S.add(new Value(2, 3, 14));
		A1S.add(new Value(2, 4, 15));
		A1S.add(new Value(3, 0, 16));
		A1S.add(new Value(3, 1, 17));
		A1S.add(new Value(3, 2, 18));
		A1S.add(new Value(3, 3, 19));
		A1S.add(new Value(3, 4, 20));
		A1S.add(new Value(4, 0, 21));
		A1S.add(new Value(4, 1, 22));
		A1S.add(new Value(4, 2, 23));
		A1S.add(new Value(4, 3, 24));
		A1S.add(new Value(4, 4, 25));
		
		A2S.add(new Value(0, 0, 2));
		A2S.add(new Value(0, 1, 3));
		A2S.add(new Value(0, 3, 1));
		A2S.add(new Value(0, 4, 2));
		A2S.add(new Value(1, 1, 1));
		A2S.add(new Value(1, 2, 2));
		A2S.add(new Value(1, 3, 3));
		A2S.add(new Value(1, 4, 1));
		A2S.add(new Value(2, 1, 3));
		A2S.add(new Value(2, 2, 1));
		A2S.add(new Value(2, 3, 1));
		A2S.add(new Value(3, 1, 1));
		A2S.add(new Value(3, 2, 2));
		A2S.add(new Value(3, 3, 1));
		A2S.add(new Value(3, 4, 1));
		A2S.add(new Value(4, 3, 2));
		A2S.add(new Value(4, 4, 1));
		
		A3S.add(new Value(0, 0, 1));
		A3S.add(new Value(0, 2, 4));
		A3S.add(new Value(0, 3, 3));
		A3S.add(new Value(1, 0, 6));
		A3S.add(new Value(1, 1, 2));
		A3S.add(new Value(1, 3, 2));
		A3S.add(new Value(1, 4, 2));
		A3S.add(new Value(2, 0, 4));
		A3S.add(new Value(2, 1, 3));
		A3S.add(new Value(2, 2, 1));
		A3S.add(new Value(3, 0, 2));
		A3S.add(new Value(3, 1, 3));
		A3S.add(new Value(3, 2, 2));
		A3S.add(new Value(3, 3, 1));
		A3S.add(new Value(4, 0, 3));
		A3S.add(new Value(4, 1, 1));
		A3S.add(new Value(4, 2, 3));
		A3S.add(new Value(4, 3, 2));
		A3S.add(new Value(4, 4, 1));
		
		B1S.add(new Value(0, 0, 2));
		B1S.add(new Value(0, 1, 3));
		B1S.add(new Value(0, 2, 6));
		B1S.add(new Value(0, 3, 1));
		B1S.add(new Value(0, 4, 2));
	}
	
	@Test
	public void getUpper_isNotStrictDense_getsMatrix() {
		double[][] E = new double[][] {
				{  1,  2,  3,  4,  5 },
				{  0,  7,  8,  9, 10 },
				{  0,  0, 13, 14, 15 },
				{  0,  0,  0, 19, 20 },
				{  0,  0,  0,  0, 25 }
		};
		assertArrayEquals(E, getUpper(A1D, false));
	}
	
	@Test
	public void getUpper_isNotStrictSparse_getsMatrix() {
		ArrayList<Value> E = new ArrayList<>();
		E.add(new Value(0, 0, 1));
		E.add(new Value(0, 1, 2));
		E.add(new Value(0, 2, 3));
		E.add(new Value(0, 3, 4));
		E.add(new Value(0, 4, 5));
		E.add(new Value(1, 1, 7));
		E.add(new Value(1, 2, 8));
		E.add(new Value(1, 3, 9));
		E.add(new Value(1, 4, 10));
		E.add(new Value(2, 2, 13));
		E.add(new Value(2, 3, 14));
		E.add(new Value(2, 4, 15));
		E.add(new Value(3, 3, 19));
		E.add(new Value(3, 4, 20));
		E.add(new Value(4, 4, 25));
		
		ArrayList<Value> U = getUpper(A1S, false);
		U.sort(COMPARATOR);
		E.sort(COMPARATOR);
		
		assertEquals(E, U);
	}
	
	@Test
	public void getUpper_isStrictDense_getsMatrix() {
		double[][] E = new double[][] {
			{  0,  2,  3,  4,  5 },
			{  0,  0,  8,  9, 10 },
			{  0,  0,  0, 14, 15 },
			{  0,  0,  0,  0, 20 },
			{  0,  0,  0,  0,  0 }
		};
		assertArrayEquals(E, getUpper(A1D, true));
	}
	
	@Test
	public void getUpper_isStrictSparse_getsMatrix() {
		ArrayList<Value> E = new ArrayList<>();
		E.add(new Value(0, 1, 2));
		E.add(new Value(0, 2, 3));
		E.add(new Value(0, 3, 4));
		E.add(new Value(0, 4, 5));
		E.add(new Value(1, 2, 8));
		E.add(new Value(1, 3, 9));
		E.add(new Value(1, 4, 10));
		E.add(new Value(2, 3, 14));
		E.add(new Value(2, 4, 15));
		E.add(new Value(3, 4, 20));
		
		ArrayList<Value> U = getUpper(A1S, true);
		U.sort(COMPARATOR);
		E.sort(COMPARATOR);
		
		assertEquals(E, U);
	}
	
	@Test
	public void getLower_isNotStrictDense_getsMatrix() {
		double[][] E = new double[][] {
			{  1,  0,  0,  0,  0 },
			{  6,  7,  0,  0,  0 },
			{ 11, 12, 13,  0,  0 },
			{ 16, 17, 18, 19,  0 },
			{ 21, 22, 23, 24, 25 }
		};
		assertArrayEquals(E, getLower(A1D, false));
	}
	
	@Test
	public void getLower_isNotStrictSparse_getsMatrix() {
		ArrayList<Value> E = new ArrayList<>();
		E.add(new Value(0, 0, 1));
		E.add(new Value(1, 0, 6));
		E.add(new Value(1, 1, 7));
		E.add(new Value(2, 0, 11));
		E.add(new Value(2, 1, 12));
		E.add(new Value(2, 2, 13));
		E.add(new Value(3, 0, 16));
		E.add(new Value(3, 1, 17));
		E.add(new Value(3, 2, 18));
		E.add(new Value(3, 3, 19));
		E.add(new Value(4, 0, 21));
		E.add(new Value(4, 1, 22));
		E.add(new Value(4, 2, 23));
		E.add(new Value(4, 3, 24));
		E.add(new Value(4, 4, 25));
		
		ArrayList<Value> L = getLower(A1S, false);
		L.sort(COMPARATOR);
		E.sort(COMPARATOR);
		
		assertEquals(E, L);
	}
	
	@Test
	public void getLower_isStrictDense_getsMatrix() {
		double[][] E = new double[][] {
				{  0,  0,  0,  0,  0 },
				{  6,  0,  0,  0,  0 },
				{ 11, 12,  0,  0,  0 },
				{ 16, 17, 18,  0,  0 },
				{ 21, 22, 23, 24,  0 }
		};
		assertArrayEquals(E, getLower(A1D, true));
	}
	
	@Test
	public void getLower_isStrictSparse_getsMatrix() {
		ArrayList<Value> E = new ArrayList<>();
		E.add(new Value(1, 0, 6));
		E.add(new Value(2, 0, 11));
		E.add(new Value(2, 1, 12));
		E.add(new Value(3, 0, 16));
		E.add(new Value(3, 1, 17));
		E.add(new Value(3, 2, 18));
		E.add(new Value(4, 0, 21));
		E.add(new Value(4, 1, 22));
		E.add(new Value(4, 2, 23));
		E.add(new Value(4, 3, 24));
		
		ArrayList<Value> L = getLower(A1S, true);
		L.sort(COMPARATOR);
		E.sort(COMPARATOR);
		
		assertEquals(E, L);
	}
	
	@Test
	public void multiply_denseRepresentation_calculatesMatrix() {
		double[] A2B1 = new double[] { 18, 20, 16, 18,  4 };
		double[] A3B1 = new double[] { 29, 24, 23, 26, 31 };
		double[][] A2A3 = new double[][] {
			{ 28, 11, 16, 17, 8 },
			{ 23, 18, 11,  7, 3 },
			{ 24, 12,  3,  7, 6 },
			{ 19, 12,  7,  5, 3 },
			{  7,  7,  7,  4, 1 }
		};
		
		assertArrayEquals(multiply(A2D, B1D), A2B1, EPSILON);
		assertArrayEquals(multiply(A3D, B1D), A3B1, EPSILON);
		assertArrayEquals(multiply(A2D, A3D), A2A3);
	}
	
	@Test
	public void multiply_sparseRepresentation_calculatesMatrix() {
		ArrayList<Value> E = new ArrayList<>();
		E.add(new Value(0, 0, 28));
		E.add(new Value(0, 1, 11));
		E.add(new Value(0, 2, 16));
		E.add(new Value(0, 3, 17));
		E.add(new Value(0, 4, 8));
		E.add(new Value(1, 0, 23));
		E.add(new Value(1, 1, 18));
		E.add(new Value(1, 2, 11));
		E.add(new Value(1, 3, 7));
		E.add(new Value(1, 4, 3));
		E.add(new Value(2, 0, 24));
		E.add(new Value(2, 1, 12));
		E.add(new Value(2, 2, 3));
		E.add(new Value(2, 3, 7));
		E.add(new Value(2, 4, 6));
		E.add(new Value(3, 0, 19));
		E.add(new Value(3, 1, 12));
		E.add(new Value(3, 2, 7));
		E.add(new Value(3, 3, 5));
		E.add(new Value(3, 4, 3));
		E.add(new Value(4, 0, 7));
		E.add(new Value(4, 1, 7));
		E.add(new Value(4, 2, 7));
		E.add(new Value(4, 3, 4));
		E.add(new Value(4, 4, 1));
		
		ArrayList<Value> M = multiply(A2S, A3S);
		M.sort(COMPARATOR);
		E.sort(COMPARATOR);
		
		assertEquals(E, M);
	}
	
	@Test
	public void invertUpper_givenUpper_calculatesInversion() {
		double[][] A2_IU = new double[][]{
			{ 0.5, -1.5,   3,  1, -0.5 },
			{   0,    1,  -2, -1,   0 },
			{   0,    0,   1, -1,   1 },
			{   0,    0,   0,  1,  -1 },
			{   0,    0,   0,  0,   1 }
		};
		
		double[][] A3_IU = new double[][]{
			{ 1,   0, -4, -3,  0 },
			{ 0, 0.5,  0, -1, -1 },
			{ 0,   0,  1,  0,  0 },
			{ 0,   0,  0,  1,  0 },
			{ 0,   0,  0,  0,  1 }
		};
		
		assertArrayEquals(invertUpper(A2D), A2_IU);
		assertArrayEquals(invertUpper(A3D), A3_IU);
	}
	
	@Test
	public void invertLower_givenLower_calculatesInversion() {
		double[][] A2_IL = new double[][]{
				{ 0.5,   0,  0,  0, 0 },
				{   0,   1,  0,  0, 0 },
				{   0,  -3,  1,  0, 0 },
				{   0,   5, -2,  1, 0 },
				{   0, -10,  4, -2, 1 }
		};
		
		double[][] A3_IL = new double[][]{
				{  1,    0,  0,  0, 0 },
				{ -3,  0.5,  0,  0, 0 },
				{  5, -1.5,  1,  0, 0 },
				{ -3,  1.5, -2,  1, 0 },
				{ -9,    1,  1, -2, 1 }
		};
		
		assertArrayEquals(invertLower(A2D), A2_IL);
		assertArrayEquals(invertLower(A3D), A3_IL);
	}
	
	@Test
	public void forwardSubstitution_denseGivenLower_solvesEquation() {
		double[] A1B1_MI = multiply(invertLower(A1D), B1D);
		double[] A1B1_FS = forwardSubstitution(A1D, B1D);
		assertArrayEquals(A1B1_MI, A1B1_FS, EPSILON);
	}
	
	@Test
	public void forwardSubstitution_denseGivenCoefficients_solvesEquation() {
		double[][] A1_MI = multiply(invertLower(A1D), getUpper(A1D, true));
		double[][] A1_FS = forwardSubstitution(A1D);
		for (int i = 0; i < A1_MI.length; i++) {
			assertArrayEquals(A1_MI[i], A1_FS[i], EPSILON);
		}
	}
	
	@Test
	public void forwardSubstitution_sparseGivenLower_solvesEquation() {
		double[] A1B1_MI = multiply(invertLower(A1D), B1D);
		double[] A1B1_FSD = forwardSubstitution(A1S, B1D);
		assertArrayEquals(A1B1_MI, A1B1_FSD, EPSILON);
	}
	
	@Test
	public void forwardSubstitution_sparseGivenCoefficients_solvesEquation() {
		double[][] A1_MI = multiply(invertLower(A1D), getUpper(A1D, true));
		double[][] A1_FSD = toDense(forwardSubstitution(A1S));
		for (int i = 0; i < A1_MI.length; i++) {
			assertArrayEquals(A1_MI[i], A1_FSD[i], EPSILON);
		}
	}
}
