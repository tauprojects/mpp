package psy.lob.saw.own;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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

	binaryTrees = { "trees.lockfree.NonBlockingTorontoBSTMap", 
					"trees.lockbased.LockBasedFriendlyTreeMap",
					"trees.lockbased.LogicalOrderingAVL", 
					"trees.lockbased.LockBasedStanfordTreeMap" },

			hashTables = { "hashmaps.lockfree.NonBlockingFriendlyHashMap",
							"hashmaps.lockfree.NonBlockingCliffHashMap",
							// "hashmaps.lockfree.JavaHashIntSet",
							"hashmaps.lockbased.LockBasedJavaHashMap", 
							"hashtables.transactional.TransactionalBasicHashSet" },

			linkedLists = { "linkedlists.lockbased.LockCouplingListIntSet", 
							"linkedlists.lockbased.LazyLinkedListSortedSet",
							"linkedlists.transactional.ElasticLinkedListIntSet",
							"linkedlists.transactional.ReusableLinkedListIntSet" },

			skipLists = { "skiplists.lockfree.NonBlockingFriendlySkipListMap",
						  "skiplists.lockfree.NonBlockingJavaSkipListMap" };

	private final static String[][] structs = { 
			binaryTrees, 
			hashTables,  
			linkedLists,
			skipLists 
			};
	private final static String[] structsNames = { 
			"binaryTrees", 
			"hashTables", 
			"linkedLists", 
			"skipLists" 
			};
	
	private final static int[] threads = { 1, 2, 4, 8, 16, 32 };

	private final static String[] initSize = { "16384", "32768", "65536" };

	private final static String[] upd = { "0", "50" };

	public double launchBenchmark(String benchfile,String className, String upd, String initSize, int threads, boolean enableGC)
			throws Exception {

		Options opt = new OptionsBuilder()
				.include(benchfile+".*")
				.mode(Mode.Throughput)
				.timeUnit(TimeUnit.SECONDS)
				.warmupIterations(0)
				.measurementTime(TimeValue.seconds(5))
				.measurementIterations(1)
				.threads(threads)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(enableGC)
				.param("updateRateArg", upd)
				.param("initSize", initSize)
				.param("className",className)
				//.output("jmhOutput")
				.build();

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

	public static void printTitlesCsv(PrintWriter pw){
		String[] titles = {"id","Structure Type","Update Ratio","Initial Size","Class Name", 
				"Threads","Throughput0","Throughput1","Throughput2","Throughput3","Throughput4"};
		 StringBuilder sb = new StringBuilder();
		 for (int ind=0;ind<titles.length-1;ind++){
			 sb.append(titles[ind]).append(',');
		 }
		 sb.append(titles[titles.length-1]).append('\n');
		 pw.write(sb.toString());	        
	}
	
	public static void main(String[] args) throws Exception {
		
		int id = 0;
		String benchFile = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH-mm");
		String date = simpleDateFormat.format(new Date());
		OutputStream out = new FileOutputStream(new File(String.format("Tests/JMH/RunResults %s.csv", date)));
		PrintWriter pw = new PrintWriter(out,true);
		
		printTitlesCsv(pw);
		
		for (int structInd=0;structInd<4;structInd++) //TODO: change from 1
		{
			benchFile = structInd == 2 ? "SetBenchmark" : "MapBenchmark";
			
			for (String u : upd)
			{
				for (String i : initSize)
				{
					for (String b : structs[structInd])
					{
						for (int t : threads)
						{
							double[] res = new double[5];
							
							for (int runtime=0; runtime<5;runtime++)
							{
								System.out.println("\n\n******************************************");
								System.out.format("Running:     %s,%s,%s,%d [run no #%d]\n",
													b,u,i,t,runtime);
								System.out.println("******************************************\n\n");
								res[runtime] = new RunBenchmarks().launchBenchmark(
										benchFile,b,u,i,t,true); 
							}
							
							pw.write(String.format("%s,%s,%s,%s,%s,%d,%f,%f,%f,%f,%f\n", 
									 String.valueOf(id),
									 structsNames[structInd],
									 u,i,b,t,res[0],res[1],res[2],res[3],res[4]));
							pw.flush();
							id++;
						}
					}
				}
			}
		}
		pw.close();
	}
}
