class latch {
	int type;// 0 for Load,1 for Store,2 for ALU,3 for Branch
	int rs;// Source Register
	int rd;// Destination Register
	int rt;// Target Register
	int pc;// PC
	boolean branch;// branch outcome
}

public class Pipeline {

	public static void main(String[] args) {
		int noofinstructions = 10;// No. of instructions to read from
									// instructions file TODO
		int clock = 0;// clock for calculating the time taken
		int bubble_counter = 0;// maintain a bubble counter
		latch[] pipeline = new latch[5];// array of latches in the pipeline..(To
										// form a circular queue later) TODO
										// pipeline[0]=IF [1]=ID [2]=ALU [3]=MEM
										// [4]=WB
		BranchPrediction predictor = new BranchPrediction();
		Cache cache = new Cache();
		for (int i = 0; i < noofinstructions; i++) {// maintain a loop running
													// through the entire ins
													// file

			latch latest = new latch();
			latest.type = 0;// type of ins from trace file and from ins
							// file.. TODO
			latest.rs = 0;// register source from ins file
			latest.rt = 0;// register target from ins file
			latest.rd = 0;// register destination from ins file
			latest.pc = 0;// TODO pc is in hex in the file..
			latest.branch = true;// branch outcome from trace file...true for
									// taken
			for (int shift = 4; shift > 0; shift--) {// So when new instruction
														// come, we need to
														// shift the things
														// i.e.pipeline[0] to
														// [1],[1] to [2]
														// because next
														// instruction has
														// arrived
				pipeline[shift] = pipeline[shift - 1];// not sure if this will
														// work when everything
														// is null
			}
			if (bubble_counter != 0) {// if bubble_counter !=0 insert bubble to
										// stall
				latest = null;
				i--;// we have to start from same i as we are basically not
					// reading the file when counter!=0
			}
			pipeline[0] = latest;

			if (pipeline[1] != null && pipeline[1].type == 0) {// Load Use
																// Hazard..Stall
																// always except
																// consecutive
																// load
																// load/store
																// case
				if (pipeline[0] != null
						&& (pipeline[0].type != 0 || pipeline[0].type != 1)
						&& (pipeline[0].rd == pipeline[1].rs || pipeline[0].rd == pipeline[1].rt)) {
					pipeline[0]=null;
					bubble_counter+=1;
					i--;
				}

			}
			if (pipeline[0] != null && pipeline[0].type == 3) {// in IF stage
																// ,if ins is
																// branch ..ask
																// for branch
																// prediction
				if (pipeline[0].branch = predictor.predict(pipeline[0].pc,
						pipeline[0].branch))// if prediction is correct,chill
											// hain else increase bubble counter
											// by 2
					bubble_counter += 2;
			}
			if (pipeline[2] != null && pipeline[2].type == 3) {// in ALU stage,
																// if ins is
																// branch then
																// train the
																// predictor
				predictor.train(pipeline[2].pc, pipeline[2].branch);

			}
			if (pipeline[3] != null
					&& (pipeline[3].type == 0 || pipeline[3].type == 1)) {
				int time = 0;
				if (pipeline[3].type == 0) {// Load
					time = cache.access(pipeline[3].rs + pipeline[3].rt, true);
				} else if (pipeline[3].type == 1) {// Store
					time = cache.access(pipeline[3].rs + pipeline[3].rd, false);
				}
				clock += time;
				clock = clock - 1;
			} else if (pipeline[3] != null && pipeline[3].type != 3
					&& pipeline[3].type != 3) {

			}

			clock += 1;// adding 1 to clock at each iteration
		}

	}
}
