package psy.lob.saw.own;

import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

public class SimpleBenchmark {

    @State(Scope.Thread)
    public static class MyState {

        @Setup(Level.Trial)
        public void doSetup() {
            list = new OptimisticList<Integer>();
            rand = new Random();
//            System.out.println("Setup - Instance List");
        }
        
        @Setup(Level.Invocation)
        public void doRand() {
        	a = rand.nextInt();
            //System.out.println("Setup - Random Calc");
        }
        
        @TearDown(Level.Trial)
        public void doTearDown() {
//            System.out.println("Do TearDown");
        }

        public int a = 1;
        Random rand;
        public OptimisticList<Integer> list;
        public boolean res;
    }

    @Benchmark 
    @BenchmarkMode(Mode.Throughput)
    public void add(MyState state) {
    	state.res = state.list.add(state.a);
    }
    
    @Benchmark 
    @BenchmarkMode(Mode.Throughput)
    public void remove(MyState state) {
    	state.res = state.list.add(state.a);
    }
}
	
	

