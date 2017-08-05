package psy.lob.saw.own;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import contention.abstractions.CompositionalMap;
import psy.lob.saw.synchrobench.trees.lockbased.LockBasedFriendlyTreeMap;
import psy.lob.saw.synchrobench.trees.lockbased.LockBasedStanfordTreeMap;
import psy.lob.saw.synchrobench.trees.lockbased.LogicalOrderingAVL;
import psy.lob.saw.synchrobench.trees.lockfree.NonBlockingTorontoBSTMap;

public class MapBenchmark {

	@State(Scope.Benchmark)
	public static class GroupContext {

		@Param({ "trees.lockfree.NonBlockingTorontoBSTMap",
            	 "trees.lockbased.LockBasedFriendlyTreeMap",
            	 "trees.lockbased.LogicalOrderingAVL",
            	 "trees.lockbased.LockBasedStanfordTreeMap"})
		public static String className;
		
		@Param({ "16535", "32768", "65536" })
		public static int initSize = 16535;
		
		@Param({ "0", "50" })
		public int updateRateArg;

		public int updateRate = updateRateArg * 10;


		private static int valueRange = initSize * 2;

		public CompositionalMap<Integer, Integer> map;
		private Random groupRand;

		@Setup(Level.Trial)
		public void doSetup() throws IllegalArgumentException
		{
			
			if(className.equals("trees.lockfree.NonBlockingTorontoBSTMap"))
			{
				map = new NonBlockingTorontoBSTMap<Integer, Integer>();
			} else if(className.equals("trees.lockbased.LockBasedFriendlyTreeMap")){
				map = new LockBasedFriendlyTreeMap<Integer,Integer>();
			} else if(className.equals("trees.lockbased.LogicalOrderingAVL")){
				map = new LogicalOrderingAVL<Integer,Integer>();
			} else if(className.equals("trees.lockbased.LockBasedStanfordTreeMap")){
				map = new LockBasedStanfordTreeMap<Integer,Integer>();
			} else{
				throw new IllegalArgumentException(
						String.format(
								"Unknown classname [%s] in MapBenchmark run [i=%d,u=%d]",
								className,initSize,updateRateArg));
			}	

			groupRand = new Random();

			// fill
			for (int i = 0; i < initSize;) {

				int newInt = groupRand.nextInt(valueRange);

				if (map.putIfAbsent(newInt, newInt) == null)
					i++; // only if successful
			}

		}

		// @TearDown(Level.Trial)
		// public void doTearDown() {
		// System.out.println(total);
		// }

	} // end of group

	@State(Scope.Thread)
	public static class ThreadContext {

		protected int numAdd;
		protected Random rand;
		public boolean res;
		protected int numRemove;
		public int numFailures;

		@Setup(Level.Iteration)
		public void doSetup() {
			numAdd = 0;
			rand = new Random();
			numRemove = 0;
			numFailures = 0;
		}

		// @TearDown(Level.Iteration)
		// public void doTearDown() {
		//
		// }
	}

	@SuppressWarnings("unused")
	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	public void run(GroupContext g, ThreadContext t) {
		Integer newInt = t.rand.nextInt(g.valueRange);
		Integer a, b;
		int coin = t.rand.nextInt(1000);

		if (coin < g.updateRate) { // add
			if ((a = g.map.putIfAbsent((int) newInt, (int) newInt)) == null) {
				a = 0;
			}
		} else { // remove
			if ((a = g.map.remove((int) newInt)) != null) {
				a = 0;
			}
		}
	}
}
