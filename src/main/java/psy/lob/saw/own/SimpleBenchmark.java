package psy.lob.saw.own;


import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import psy.lob.saw.synchrobench.trees.lockfree.NonBlockingTorontoBSTMap;


public class SimpleBenchmark {
	
	private final static int updateRate = 50;
	private final static boolean forcedUpdate = true;
	private final static int initSize = 16535;
	private final static int valueRange = 16535*2;
	private static AtomicInteger totalAdds;
	private static AtomicInteger totalRemoves;	
	private static AtomicInteger totalFailures;	
	
    @State(Scope.Benchmark)
    public static class GroupContext {

    	
    	public NonBlockingTorontoBSTMap<Integer,Integer> map;
        private Random groupRand;
        
    	@Setup(Level.Trial)
        public void doSetup() {
            map = new NonBlockingTorontoBSTMap<Integer,Integer>();
            totalAdds = new AtomicInteger(0);
            totalRemoves =  new AtomicInteger(0);
            totalFailures =  new AtomicInteger(0);
            
            groupRand = new Random();
            
    		//fill
    		for(int i=0;i<initSize;){
    			
    			int newInt = groupRand.nextInt(valueRange);
    			
    			if(map.putIfAbsent(newInt, newInt) == null)
    				i++; //only if successful
    		}
            
        }
        
        @TearDown(Level.Trial)
        public void doTearDown() {
        	int total=totalAdds.get();
        	total = total + totalRemoves.get();
        	total = total + totalFailures.get();
        	System.out.println(total);
        }

    } //end of group
    
    
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
		
        @TearDown(Level.Iteration)
        public void doTearDown() {
//        	totalAdds.addAndGet(numAdd);
//        	totalRemoves.addAndGet(numRemove);
//        	totalFailures.addAndGet(numFailures);
        	
        }
	}
    
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void run(GroupContext g, ThreadContext t) {
    	Integer newInt = t.rand.nextInt(valueRange);
		Integer a, b;
		int coin = t.rand.nextInt(100);

		if (coin < updateRate) { // add
			if ((a = g.map.putIfAbsent((int) newInt, (int) newInt)) == null) {
//				t.numAdd++;
				totalAdds.incrementAndGet();
			} else {
//				t.numFailures++;
				totalFailures.incrementAndGet();
				
			}
		} else { // remove
			if ((a = g.map.remove((int) newInt)) != null) {
				totalRemoves.incrementAndGet();
//				t.numRemove++;
			} else
				totalFailures.incrementAndGet();
//				t.numFailures++;
		}	
    }
}
	
	

