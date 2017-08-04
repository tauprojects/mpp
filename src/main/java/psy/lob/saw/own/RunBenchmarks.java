package psy.lob.saw.own;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class RunBenchmarks {

	private final static String[]

	binaryTrees = { "trees.lockfree.NonBlockingTorontoBSTMap", "trees.lockbased.LockBasedFriendlyTreeMap",
			"trees.lockbased.LogicalOrderingAVL", "trees.lockbased.LockBasedStanfordTreeMap" },

			hashTables = { "hashtables.lockfree.NonBlockingFriendlyHashMap",
					"hashtables.lockfree.NonBlockingCliffHashMap",
					// "hashtables.lockfree.JavaHashIntSet",
					"hashtables.lockbased.LockBasedJavaHashMap", "hashtables.transactional.TransactionalBasicHashSet" },

			linkedLists = { "linkedlists.lockfree.NonBlockingLinkedListSetRTTI",
					"linkedlists.lockbased.LockCouplingListIntSet", "linkedlists.lockbased.LazyLinkedListSortedSet",
					"linkedlists.transactional.ElasticLinkedListIntSet",
					"linkedlists.transactional.ReusableLinkedListIntSet" },

			skipLists = { "skiplists.lockfree.NonBlockingFriendlySkipListMap",
					"skiplists.lockfree.NonBlockingJavaSkipListMap" };

	private final static String[][] structs = { binaryTrees, hashTables, linkedLists, skipLists };

	private final static int[] threads = { 1, 2, 4, 8, 16, 32 };

	private final static String[] initSize = { "16384", "32768", "65536" };

	private final static String[] upd = { "0", "50" };

	public double launchBenchmark(String className, String upd, String initSize, int threads, boolean enableGC)
			throws Exception {

		Options opt = new OptionsBuilder().include(className + ".*").mode(Mode.Throughput).timeUnit(TimeUnit.SECONDS)
				.warmupIterations(0).measurementTime(TimeValue.seconds(5)).measurementIterations(1).threads(threads)
				.forks(1).shouldFailOnError(true).shouldDoGC(enableGC).param("updateRateArg", upd)
				.param("initSize", initSize).build();

		Runner runner = new Runner(opt);

		// disable prints temporarily
		PrintStream originalStream = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));

		Collection<RunResult> run = runner.run();

		// return prints
		System.setOut(originalStream);

		RunResult[] resArray = run.toArray(new RunResult[run.size()]);
		Collection<BenchmarkResult> benchres = resArray[0].getBenchmarkResults();
		BenchmarkResult[] benchResArray = benchres.toArray(new BenchmarkResult[benchres.size()]);
		return benchResArray[0].getPrimaryResult().getScore();
	}

	public static void main(String[] args) throws Exception {
		// Class.forName("psy.lob.saw.synchrobench.trees.lockfree.NonBlockingTorontoBSTMap")
		// .getConstructor().newInstance();

		double res = new RunBenchmarks().launchBenchmark("psy.lob.saw.own.MapBenchmark", "0", "16384", 2, true);

		System.out.format("\n\nRESULT: %f\n\n",res);

	}

}
