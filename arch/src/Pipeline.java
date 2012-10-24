import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

class Instructions {
	int pc, type, rs, rt, rd, mem;
	boolean branch; // outcome of branch

	Instructions() {
		branch = true;
	}
}

public class Pipeline {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		int clock = 0; // no. of clock cycles taken
		//eclipse warning de raha hai, check it out TODO
		int bubbles = 0; // maintain a bubble counter
		Instructions[] pipe = new Instructions[5]; // array of latches in the
													// pipeline..(To form a
													// circular queue or
													// something later) TODO
													// pipeline[0] is for IF [1]
													// for ID [2] for ALU [3]for
													// MEM [4] for WB

		BranchPrediction predictor = new BranchPrediction();
		Cache cache = new Cache();
		boolean dont_read = false;

		BufferedReader ex = new BufferedReader(new FileReader("exec_trace.txt")); // for
																					// execution
																					// trace
		BufferedReader in = new BufferedReader(new FileReader("inst_trace.txt")); // for
																					// instruction
																					// file
		final int TABLE_SIZE = Integer.MAX_VALUE / 16; // MAX_VALUE/16 was the
														// best my machine could
														// support, anyone knows
														// how to increase the
														// length of the array??
														// TODO
		LinkedList<Instructions> hashTable[] = (LinkedList<Instructions>[]) new LinkedList[TABLE_SIZE]; // hash
																										// table
																										// to
																										// store
																										// all
																										// instructions
																										// in
																										// the
																										// instruction
																										// file
		// preprocessing of the inst file
		for (int i = 0; i < TABLE_SIZE; i++)
			hashTable[i] = new LinkedList<Instructions>();
		while (true) {
			String s = in.readLine();
			if (s == null)
				break;
			StringTokenizer st = new StringTokenizer(s);
			Instructions ins = new Instructions();
			ins.pc = (int) Long.parseLong(st.nextToken(), 16);
			int key = (ins.pc % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE;
			ins.type = Integer.parseInt(st.nextToken());
			ins.rs = Integer.parseInt(st.nextToken());
			ins.rt = Integer.parseInt(st.nextToken());
			ins.rd = Integer.parseInt(st.nextToken());
			hashTable[key].add(ins);
		}
		in.close();
		while (true) {//check termination condition TODO dekh lo kab terminate karna hai loop ko
			// maintain a loop running through the entire ins file
			Instructions latest = null;

			if (bubbles != 0)
				; // if bubbles !=0 insert bubble
			// don't read

			else if (dont_read) // set when a load-use hazard occured in the
								// previous cycle
				latest = pipe[0];

			// reading the file only when bubbles == 0 && dont_read == false
			else {
				StringTokenizer st = new StringTokenizer(ex.readLine());
				int pc = (int) Long.parseLong(st.nextToken(), 16);
				int key = (pc % TABLE_SIZE + TABLE_SIZE) % TABLE_SIZE;
				Iterator<Instructions> i = hashTable[key].iterator();
				while (i.hasNext()) {
					Instructions next = i.next();
					if (next.pc == pc)
						latest = next;
				}
				latest.mem = (int) Long.parseLong(st.nextToken(), 16); // mem
																		// from
																		// trace
																		// file
				int result = Integer.parseInt(st.nextToken());
				latest.branch = (result == 0) ? false : true;// branch outcome
																// from trace
																// file...true
																// for taken
			}
			ex.close();
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
				if ((pipe[0].type == 2 && (pipe[0].rs == pipe[1].rd || pipe[0].rt == pipe[1].rd)) // alu
						|| (pipe[0].type == 3 && pipe[0].rs == pipe[1].rd)) // branch
					dont_read = true;
			}
			// in IF stage, if ins is branch ..ask for branch prediction
			if (!dont_read && pipe[0] != null && pipe[0].type == 3)
				if (pipe[0].branch != predictor.predict(pipe[0].pc,
						pipe[0].branch))
					bubbles += 2;

			/******* ALU *******/
			// in ALU stage, if ins is branch then train the predictor
			if (pipe[2] != null && pipe[2].type == 3)
				predictor.train(pipe[2].pc, pipe[2].branch);

			/******* MEM *******/
			if (pipe[3] != null && pipe[3].type < 2)
				// ? Load : Store
				clock += pipe[3].type == 0 ? cache.access(pipe[3].mem, false)
						: cache.access(pipe[3].mem, true);
			else
				clock++; // cycle anyways complete
		}

	}
}
