import java.util.LinkedList;

class accuracyMeter {
	int correct, total;

	public void update(boolean precision) {
		if (precision)
			correct++;
		total++;
	}

	public float givAcc() {
		return (float) correct / (float) total;
	}
}

public class BranchPrediction {

	public class twoBit {
		int state;

		public boolean predict() {
			return state > 1;
		}

		public void train(boolean outcome) {
			if (outcome && state < 3)
				state++;
			else if (!outcome && state > 0)
				state--;
		}

		/** for tournament predictor **/
		public void train(boolean outcome, boolean change) {
			if (change) {
				if (outcome && state < 3)
					state++;
				else if (!outcome && state > 0)
					state--;
			}
		}
	}

	public class bimodal {
		final static int n = 10;
		static final int size = 1 << n;
		private twoBit arr[];
		public accuracyMeter meter = new accuracyMeter();

		public bimodal() {
			arr = new twoBit[size];
			for (int i = 0; i < size; ++i)
				arr[i] = new twoBit();
		}

		public boolean predict(int pc, boolean outcome) {
			pc >>= 2; // testing TODO
			pc %= size;
			boolean prediction = arr[pc].predict();
			meter.update(prediction == outcome);
			return prediction;
		}

		public void train(int pc, boolean outcome) {
			pc >>= 2; // testing TODO
			pc %= size;
			arr[pc].train(outcome);
		}
	}

	public class gShare {
		final static int k = 12;
		static final int size = 1 << k;
		int bhr;
		private twoBit arr[];
		public accuracyMeter meter = new accuracyMeter();

		public gShare() {
			arr = new twoBit[size];
			for (int i = 0; i < size; ++i)
				arr[i] = new twoBit();
		}

		public boolean predict(int pc, boolean outcome) {
			// pc >>= 2; // testing TODO
			pc %= size;
			int r = pc ^ bhr;
			boolean prediction = arr[r].predict();
			meter.update(prediction == outcome);
			return prediction;
		}

		public void train(int pc, boolean outcome) {
			// pc >>= 2; // testing TODO
			pc %= size;
			int r = pc ^ bhr;
			if (outcome)
				bhr += (1 << 6);
			bhr >>= 1;
			arr[r].train(outcome);
		}
	}

	/*********** tournament predictor ***********/
	final static int m = 10;
	static final int size = 1 << m;

	public class tournamentObject {
		public twoBit c = new twoBit();
		public gShare g = new gShare();
		public bimodal b = new bimodal();
	}

	private tournamentObject arr[];
	public accuracyMeter meter = new accuracyMeter();

	/** constructor **/
	public BranchPrediction() {
		arr = new tournamentObject[size];
		for (int i = 0; i < size; ++i)
			arr[i] = new tournamentObject();
	}

	private LinkedList<Boolean> gResultQ = new LinkedList<Boolean>(),
			bResultQ = new LinkedList<Boolean>();

	public boolean predict(int pc, boolean outcome) {
		// pc >>= 2; //testing TODO
		pc %= size;
		boolean gpred = arr[pc].g.predict(pc, outcome), // predictor 1
		bpred = arr[pc].b.predict(pc, outcome); // predictor 2
		gResultQ.add(gpred);
		bResultQ.add(bpred);
		boolean prediction = arr[pc].c.predict() ? bpred : gpred;
		meter.update(prediction == outcome);
		return prediction;
	}

	public void train(int pc, boolean outcome) {
		// pc >>= 2; //testing TODO
		pc %= size;
		arr[pc].g.train(pc, outcome);
		arr[pc].b.train(pc, outcome);

		boolean bpred = bResultQ.pop();
		arr[pc].c.train(bpred, bpred ^ gResultQ.pop());
	}

}