public class Cache
{
	public class Set
	{
		int assoc;
		int[] arr;
		int tagLength;
		public Set(int assoc, int tag)
		{
			this.assoc = assoc;
			arr = new int[assoc];
			tagLength = tag;
		}
		public boolean matchTag(int Tag)
		{
			for (int i=0; i<assoc; i++)
				if (arr[i] % (1<<assoc) == Tag)
					return true;
			return false;
		}	
	}
	//public static void 
}
