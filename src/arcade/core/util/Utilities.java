package arcade.core.util;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.ListIterator;

public final class Utilities {
	/**
	 * Copies the contents on one 3D array to another 3D array.
	 *
	 * The {@code clone} method only works at the one-dimensional level.
	 * Otherwise, we would have shallow cloning.
	 *
	 * @param fromArray  the array to copy from
	 * @param toArray  the array to copy to
	 */
	public static void copyArray(double[][][] fromArray, double[][][] toArray) {
		for (int k = 0; k < fromArray.length; k++) {
			for (int i = 0; i < fromArray[k].length; i++) {
				toArray[k][i] = fromArray[k][i].clone();
			}
		}
	}
	
	/**
	 * Shuffles the given list using a seeded random number generator.
	 * <p>
	 * The list is shuffled in placed.
	 * Based on {@code java.util.Collections} and adapted to use the seeded
	 * random number generator.
	 *
	 * @param list  the list to be shuffled
	 * @param rng  the random number generator
	 */
	public static void shuffleList(ArrayList<?> list, MersenneTwisterFast rng) {
		int size = list.size();
		Object[] arr = list.toArray();
		for (int i = size; i > 1; i--) { swap(arr, i - 1, rng.nextInt(i)); }
		ListIterator it = list.listIterator();
		for (int i = 0; i < size; i++) {
			it.next();
			it.set(arr[i]);
		}
	}
	
	/**
	 * Swaps two objects in an array in place.
	 *
	 * @param arr  the array containing the objects
	 * @param i  the index of the first object
	 * @param j  the index of the second object
	 */
	static void swap(Object[] arr, int i, int j) {
		Object temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}
}