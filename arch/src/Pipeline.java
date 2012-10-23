import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

class latch {
	int type,		// 0 for Load,1 for Store,2 for ALU,3 for Branch
		rs, rd, rt, pc;
	boolean branch;// branch outcome
}

public class Pipeline {
	
	public static void main(String[] args) throws FileNotFoundException {
		int clock = 0;					// no. of clock cycles taken
		int bubbles = 0;				// maintain a bubble counter
		latch[] pipe = new latch[5];	// array of latches in the pipeline..(To form a circular queue or something later) TODO
										// pipeline[0] is for IF [1] for ID [2] for ALU [3]for MEM [4] for WB
		
		BranchPrediction predictor = new BranchPrediction();
		Cache cache = new Cache();
		boolean dont_read = false;
		
		BufferedReader ex = new BufferedReader(new FileReader("exec_trace.txt")); //for execution trace
		BufferedReader in = new BufferedReader(new FileReader("inst_trace.txt")); //for instruction file
		
		while (true) {// maintain a loop running through the entire ins file
			latch latest = null;
			
			if (bubbles != 0); 			// if bubbles !=0 insert bubble
			// don't read
			
			else if (dont_read) 		// set when a load-use hazard occured in the previous cycle
				latest = pipe[0];
			
			// reading the file only when bubbles == 0 && dont_read == false
			else	{
				latest = new latch();
				latest.type = 0;	// type of ins from trace file and from ins file.. TODO
				latest.rs = 0;		// register source from ins file
				latest.rt = 0;		// register target from ins file
				latest.rd = 0;		// register destination from ins file
				latest.pc = 0;		// TODO pc is in hex in the file..
				latest.branch = true;// branch outcome from trace file...true for taken
			}
			
			// move the pipeline 1 step forward
			for (int j = 4; j > 0; j--)
				pipe[j] = pipe[j - 1];
			pipe[0] = latest;
			
			if(dont_read)	{
				pipe[1] = null;
				dont_read = false;
			}

			/*******IF********/
			// Load-Use hazard : stall if same register, except when the new instr. itself is load/store
			if (pipe[0] != null && pipe[1] != null && pipe[1].type == 0)	{
				if ( (pipe[0].type == 2 && (pipe[0].rs == pipe[1].rd || pipe[0].rt == pipe[1].rd)) // alu
						|| (pipe[0].type == 3 && pipe[0].rs == pipe[1].rd)) //branch
					dont_read = true;
			}
			// in IF stage, if ins is branch ..ask for branch prediction
			if (!dont_read && pipe[0] != null && pipe[0].type == 3)
				if (pipe[0].branch != predictor.predict(pipe[0].pc, pipe[0].branch))
					bubbles += 2;
			
			/*******ALU*******/
			// in ALU stage, if ins is branch then train the predictor
			if (pipe[2] != null && pipe[2].type == 3)
				predictor.train(pipe[2].pc, pipe[2].branch);

			/*******MEM*******/
			if (pipe[3] != null && pipe[3].type < 2)
				// ? Load : Store
				clock += pipe[3].type == 0 ? cache.access(pipe[3].rs + pipe[3].rt, false) : cache.access(pipe[3].rs + pipe[3].rd, true);
			else
				clock ++;		// cycle anyways complete
		}

	}
}
