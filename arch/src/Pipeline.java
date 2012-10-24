import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

class Instructions {

	int		type, rs, rt, rd;
	long	pc, mem;
	boolean	branch	= true;	// outcome of branch

}

public class Pipeline {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		int clock = 0; // no. of clock cycles taken
		int bubbles = 0; // maintain a bubble counter

		Instructions[] pipe = new Instructions[5];

		BranchPrediction predictor = new BranchPrediction();
		Cache cache = new Cache();
		boolean dont_read = false;

		BufferedReader ex = new BufferedReader(new FileReader("exec_trace.txt")); // for execution trace
		BufferedReader in = new BufferedReader(new FileReader("inst_trace.txt")); // for instruction file

		final int TABLE_SIZE = 10000019;
		// hash table to store all instructions in the instruction file
		LinkedList<Instructions> hashTable[] = (LinkedList<Instructions>[]) new LinkedList[TABLE_SIZE];
		
		// preprocessing of the inst file
		for (int i = 0; i < TABLE_SIZE; i++)
			hashTable[i] = new LinkedList<Instructions>();
		
		System.err.println("Array initialised!");
		
		String s;
		while ((s = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(s);
			Instructions ins = new Instructions();
			ins.pc = Long.parseLong(st.nextToken().substring(2), 16);
			int key = (int) (ins.pc % TABLE_SIZE);
			ins.type = Integer.parseInt(st.nextToken());
			ins.rs = Integer.parseInt(st.nextToken());
			ins.rt = Integer.parseInt(st.nextToken());
			ins.rd = Integer.parseInt(st.nextToken());
			hashTable[key].add(ins);
		}
		in.close();

		System.err.println("Instructions hashed!");
		
		long last = 0, dinst = 0;
		while (last < 4) {
			// maintain a loop running through the entire ins file
			if (last > 0)
				last++;
			Instructions latest = null;

			if (bubbles != 0)
				bubbles--; // if bubbles !=0 insert bubble don't read

			else if (dont_read) // set when a load-use hazard occured in the previous cycle
				latest = pipe[0];

			// reading the file only when bubbles == 0 && dont_read == false
			else {
				String line = ex.readLine();
				if (line == null && last == 0)
					last++;
				else if (line != null) {
					dinst++;
					StringTokenizer st = new StringTokenizer(line);
					long pc = Long.parseLong(st.nextToken().substring(2), 16);
					int key = (int) (pc % TABLE_SIZE);
					Iterator<Instructions> i = hashTable[key].iterator();
					while (i.hasNext()) {
						Instructions next = i.next();
						if (next.pc == pc)	{
							latest = next;
							break;
						}
					}
					latest.mem = Long.parseLong(st.nextToken().substring(2), 16); // mem from trace file
					latest.branch = (Integer.parseInt(st.nextToken()) == 0) ? false : true;// branch outcome from trace file...true for taken
				}
			}
			// move the pipeline 1 step forward
			for (int j = 4; j > 0; j--)
				pipe[j] = pipe[j - 1];
			pipe[0] = latest;

			if (dont_read) {
				pipe[1] = null;
				dont_read = false;
			}

			/******* IF ********/
			// Load-Use hazard : stall if same register, except when the new
			// instr. itself is load/store
			if (pipe[0] != null && pipe[1] != null && pipe[1].type == 0) {
				if (((pipe[0].type & 1) == 0 && (pipe[0].rs == pipe[1].rd || pipe[0].rt == pipe[1].rd)) // alu and load
						|| (pipe[0].type == 3 && pipe[0].rs == pipe[1].rd) // branch
						|| (pipe[0].type == 1 && (pipe[0].rs == pipe[1].rd || pipe[0].rd == pipe[1].rd) )) // store
					dont_read = true;
			}
			// in IF stage, if ins is branch ..ask for branch prediction
			if (!dont_read && pipe[0] != null && pipe[0].type == 3)
				if (pipe[0].branch != predictor.predict(pipe[0].pc, pipe[0].branch))
					bubbles += 2;

			/******* ALU *******/
			// in ALU stage, if ins is branch then train the predictor
			if (pipe[2] != null && pipe[2].type == 3)
				predictor.train(pipe[2].pc, pipe[2].branch);

			/******* MEM *******/
			if (pipe[3] != null && pipe[3].type < 2)
				// ? Load : Store
				clock += pipe[3].type == 0 ? cache.access(pipe[3].mem, false) : cache.access(pipe[3].mem, true);
			else clock++; // cycle anyways complete

			if(dinst % 100000 == 0)
				System.err.println("No. of dynamic instructions processed: " + dinst + "; last: " + last);
		}

		ex.close();
		System.out.printf("%.2f %.2f%% %.2f%% %.10f%%",((double) dinst / (double) clock), cache.L1LocalMiss(), cache.L2LocalMiss(), predictor.meter.givAcc());

	}
}
