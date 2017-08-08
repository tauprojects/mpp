package psy.lob.saw.own;

import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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

	binaryTrees = { "trees.lockfree.NonBlockingTorontoBSTMap", 
					"trees.lockbased.LockBasedFriendlyTreeMap",
					"trees.lockbased.LogicalOrderingAVL", 
					"trees.lockbased.LockBasedStanfordTreeMap" },

			hashTables = { "hashmaps.lockfree.NonBlockingFriendlyHashMap",
							"hashmaps.lockfree.NonBlockingCliffHashMap",
							// "hashmaps.lockfree.JavaHashIntSet",
							"hashmaps.lockbased.LockBasedJavaHashMap", 
							"hashmaps.transactional.TransactionalBasicHashSet" },

			linkedLists = { "linkedlists.lockbased.LockCouplingListIntSet", 
							//"linkedlists.lockbased.LazyLinkedListSortedSet",
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

	
	/***************************************************************************/
	
	//function to launch JMH with args
	public double launchBenchmark(String benchfile,String className, 
			String upd, String initSize, int threads, boolean enableGC)
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

		Collection<RunResult> run = runner.run();

		//logic to get result from benchmark
		RunResult[] resArray = run.toArray(new RunResult[run.size()]);
		Collection<BenchmarkResult> benchres = resArray[0].getBenchmarkResults();
		BenchmarkResult[] benchResArray = benchres.toArray(new BenchmarkResult[benchres.size()]);
		return benchResArray[0].getPrimaryResult().getScore();
	}

	//function to print the titles to the csv
	public static void printTitlesCsv(PrintWriter pw,int i){
		String[] titles = {"id","Structure Type","Update Ratio","Initial Size","Class Name", 
				"Threads"};
		 StringBuilder sb = new StringBuilder();
		 for (int ind=0;ind<titles.length;ind++){
			 sb.append(titles[ind]).append(',');
		 }
		 for (int ind=0;ind<i-1;ind++){
			 sb.append("Throuhput").append(ind).append(',');
		 }
		 sb.append("Throuhput").append(i-1).append('\n');
		 pw.write(sb.toString());	        
	}
	
	//function to print the titles to the csv
	public static void printRowCsv(PrintWriter pw,int id, String structName,
			 String u,String i,String b,int t,double[] res){
		 StringBuilder sb = new StringBuilder();
		 sb.append(String.format("%d,%s,%s,%s,%s,%d,",id,structName,u,i,b,t));
		 for (int ind=0;ind<res.length-1;ind++){
			 sb.append(res[ind]).append(',');
		 }
		 sb.append(res[res.length-1]).append('\n');
		 pw.write(sb.toString());	 
		 pw.flush();
	}	
	
	
	public static void main(String[] args) throws Exception {
		
		int argNumber = 0;
		int forceThreads = 0;
		String forceUpd = null,forceInitSize = null, forceClass = null, forceStruct = null;
		int id = 0;
		String benchFile = null;
		int iterations = 5;
		int startfrom = 0;
		
		//parse arguments. wrong arguments will be ignored
		while (argNumber < args.length) {
			String currentArg = args[argNumber++];
			String optionValue = args[argNumber++];
			if (currentArg.equals("-t"))
				forceThreads = Integer.parseInt(optionValue);
			else if (currentArg.equals("-u"))
				forceUpd = optionValue;
			else if (currentArg.equals("-i"))
				forceInitSize = optionValue;
			else if (currentArg.equals("-b"))
				forceClass = optionValue;
			else if (currentArg.equals("-s"))
				forceStruct = optionValue;
			else if (currentArg.equals("-r"))
				iterations = Integer.parseInt(optionValue);
			else if (currentArg.equals("-index"))
				startfrom = Integer.parseInt(optionValue);			
			}
		
		//if all args available run the specified configuration.
		//if there is any kind of error the benchmark will capture it
		if(forceThreads != 0 && forceUpd!=null && 
				forceInitSize != null && forceClass != null)
		{

			System.out.println("\n\n************************************************************************************");
			System.out.format("Running SPECIFIC configuration:     %s,%s,%s,%d\n",
					forceClass,forceUpd,forceInitSize,forceThreads);
			System.out.println("************************************************************************************\n\n");

			
			if (Arrays.asList(linkedLists).contains(forceClass))
				benchFile = "SetBenchmark";
			else
				benchFile = "MapBenchmark";
			new RunBenchmarks().launchBenchmark(
					benchFile,forceClass,forceUpd,forceInitSize,forceThreads,true); 
			System.out.println("\n\nFINISHED SUCCESSFULLY");
			//if not all args available and one of them is not supported abort
		} else {
			boolean error = false;
			if(forceThreads != 0 && forceThreads != 1 && forceThreads != 2 
					&& forceThreads != 4 && forceThreads != 8  
					&& forceThreads != 16 && forceThreads != 32){
				System.err.format("Unsupported thread count %d\n",forceThreads);
				error = true;
			 } if(forceUpd!=null && !Arrays.asList(upd).contains(forceUpd)){
				System.err.format("Unsupported update ratio %s\n",forceUpd);
			 	error = true;
			 } if(forceInitSize != null && !Arrays.asList(initSize).contains(forceInitSize)){
				System.err.format("Unsupported init size %s\n",forceInitSize);
				error = true;
			 } if(forceClass != null && !Arrays.asList(binaryTrees).contains(forceClass)
					&& !Arrays.asList(hashTables).contains(forceClass)
					&& !Arrays.asList(linkedLists).contains(forceClass)
					&& !Arrays.asList(skipLists).contains(forceClass)){
				System.err.format("Unsupported class: %s\n",forceClass);
				error = true;
			 } if(forceStruct != null && !Arrays.asList(structsNames).contains(forceStruct)){
				System.err.format("Unsupported struct type %s\n",forceStruct);
				error = true;
			 } if(iterations <= 0){
					System.err.format("Unsupported iterations count %d\n",iterations);
					error = true;
				 }
			 if (error)
				 return;
		}
		
		
		//create CSV file with this day and time
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH-mm");
		Calendar cal = Calendar.getInstance();
		String date = simpleDateFormat.format(cal.getTime());
		OutputStream out = new FileOutputStream(
				new File(String.format("Tests/JMH/RunResults %s.csv", date)));
		PrintWriter pw = new PrintWriter(out,true);
		
		printTitlesCsv(pw,iterations);
		
		
		
		/** MAIN LOOP **/
		MainLoop:
		for (int structInd=0;structInd<4;structInd++)
		{
			benchFile = structInd == 2 ? "SetBenchmark" : "MapBenchmark";

			if(forceStruct !=null && !structsNames[structInd].equals(forceStruct))
				continue MainLoop;
			
			UpdLoop:
			for (String u : upd)
			{
				if(forceUpd !=null && !u.equals(forceUpd))
					continue UpdLoop;
				
				InitSizeLoop:
				for (String i : initSize)
				{
					if(forceInitSize !=null && !i.equals(forceInitSize))
						continue InitSizeLoop;
					
					ClassLoop:
					for (String b : structs[structInd])
					{
						if(forceClass !=null && !b.equals(forceClass))
							continue ClassLoop;
						
						ThreadLoop:
						for (int t : threads)
						{	
							if(id < startfrom){
								id++;
								continue ThreadLoop;			
							}
							
							if (forceThreads != 0 && t != forceThreads)
								continue ThreadLoop;

							double[] res = new double[iterations];
							
							for (int runtime=0; runtime<iterations;runtime++)
							{
								System.out.println("\n\n************************************************************************************");
								System.out.format("%d.(%d/%d) Running:     %s,%s,%s,%d [run no #%d]\n",
										id,runtime+1,iterations,b,u,i,t,runtime);
								System.out.println("************************************************************************************\n\n");
								res[runtime] = new RunBenchmarks().launchBenchmark(
										benchFile,b,u,i,t,true);
							}
							printRowCsv(pw,id,structsNames[structInd],
									u,i,b,t,res);

							id++;
						}
					}
				}
			}
		}
	}
}
