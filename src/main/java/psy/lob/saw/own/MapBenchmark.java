package psy.lob.saw.own;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import contention.abstractions.CompositionalMap;

public class MapBenchmark {

	@State(Scope.Benchmark)
	public static class GroupContext {

		@Param({ "trees.lockfree.NonBlockingTorontoBSTMap",
            	 "trees.lockbased.LockBasedFriendlyTreeMap",
            	 "trees.lockbased.LogicalOrderingAVL",
            	 "trees.lockbased.LockBasedStanfordTreeMap",
            	 
            	 "hashmaps.lockfree.NonBlockingFriendlyHashMap",
                 "hashmaps.lockfree.NonBlockingCliffHashMap",
                 //"hashmaps.lockfree.JavaHashIntSet",
                 "hashmaps.lockbased.LockBasedJavaHashMap",
                 "hashmaps.transactional.TransactionalBasicHashSet",
                 
                 "skiplists.lockfree.NonBlockingFriendlySkipListMap",
                 "skiplists.lockfree.NonBlockingJavaSkipListMap"})
		
		public static String className;
		
		@Param({ "16535", "32768", "65536" })
		public static int initSize = 16535;
		
		@Param({ "0", "50" })
		public int updateRateArg;

		private int updateRate;
		private int valueRange;

		public CompositionalMap<Integer, Integer> map;
		private Random groupRand;

		@SuppressWarnings("unchecked")
		@Setup(Level.Trial)
		public void doSetup() throws Exception
		{
			//only here updateRate and valueRange are available
			updateRate = updateRateArg * 10;
			valueRange = initSize * 2;
			
			//create class from name dynamically
			Class<CompositionalMap<Integer, Integer>> benchClass = 
					(Class<CompositionalMap<Integer, Integer>>) Class
					.forName("psy.lob.saw.synchrobench." + className);
			Constructor<CompositionalMap<Integer, Integer>> c = benchClass
					.getConstructor();
			map = (CompositionalMap<Integer, Integer>) c.newInstance();
			
			groupRand = new Random();

			// fill
			for (int i = 0; i < initSize;) {

				int newInt = groupRand.nextInt(valueRange);
				if (map.putIfAbsent(newInt, newInt) == null)
					i++; // only if successful
			}
		}
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
	}

	@SuppressWarnings("unused")
	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	public void run(GroupContext g, ThreadContext t
		//	,Blackhole bh
			) {
		Integer newInt = t.rand.nextInt(g.valueRange);
		Integer a, b;
		int coin = t.rand.nextInt(1000);

		if (coin < g.updateRate){
			if (coin < g.updateRate / 2) { // add
				if ((a = g.map.putIfAbsent((int) newInt, (int) newInt)) == null) {
					//bh.consume(a);
					a = 0;
				}
			} else { // remove
				if ((a = g.map.remove((int) newInt)) != null) {
					//bh.consume(a);
					a = 0;
				}
			}
		} else {
			if ((a = g.map.get((int) newInt)) == null) {
				//bh.consume(a);
				a = 0;
			}
		}
	}
}

