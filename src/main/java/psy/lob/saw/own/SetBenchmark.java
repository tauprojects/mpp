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

import contention.abstractions.CompositionalIntSet;

public class SetBenchmark {

	@State(Scope.Benchmark)
	public static class GroupContext {

		@Param({ "linkedlists.lockfree.NonBlockingLinkedListSetRTTI",
            	 "linkedlists.lockbased.LockCouplingListIntSet",
            	 "linkedlists.lockbased.LazyLinkedListSortedSet",
            	 "linkedlists.transactional.ElasticLinkedListIntSet",
            	 "linkedlists.transactional.ReusableLinkedListIntSet"})
		
		public static String className;
		
		@Param({ "16535", "32768", "65536" })
		public static int initSize = 16535;
		
		@Param({ "0", "50" })
		public int updateRateArg;

		private int updateRate;
		private int valueRange;

		public CompositionalIntSet set;
		private Random groupRand;

		@SuppressWarnings("unchecked")
		@Setup(Level.Trial)
		public void doSetup() throws Exception
		{
			Class<CompositionalIntSet> benchClass = 
					(Class<CompositionalIntSet>) Class
					.forName("psy.lob.saw.synchrobench." + className);
			Constructor<CompositionalIntSet> c = benchClass
					.getConstructor();
			set = (CompositionalIntSet) c.newInstance();
		
			updateRate = updateRateArg * 10;
			valueRange = initSize * 2;

			
			groupRand = new Random();

			// fill
			for (int i = 0; i < initSize;) {

				int newInt = groupRand.nextInt(valueRange);

				if (set.addInt(newInt))
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
	public void run(GroupContext g, ThreadContext t) {
		Integer newInt = t.rand.nextInt(g.valueRange);
		boolean a;
		int coin = t.rand.nextInt(1000);
		
		if (coin < g.updateRate) {
			if (coin < g.updateRate /2) { // add
				if (a = g.set.addInt((int) newInt)) {
					a = false;
				}
			} else { // remove
				if (a = g.set.removeInt((int) newInt)) {
					a = false;
				}
			}
		} else {
			if (a = g.set.containsInt((int) newInt)) {
				a = false;
			}
		}
	}
}
