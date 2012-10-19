public class Cache
{
	public class Set
	{
		int assoc;
		Integer[] arr;
		boolean fifo;
		
		public Set(int assoc, boolean fifo)
		{
			this.assoc = assoc;
			arr = new Integer[assoc];
			this.fifo = fifo;
		}
		
		public boolean matchTag(int tag)
		{
			for (int i=0; i<assoc; i++)
				if (arr[i] != null && (arr[i] >> 1) == tag)	{
					if(!fifo)	{
						Integer a = arr[i];
						for(int j = i-1; j >= 0; --j)
							arr[j+1] = arr[j];
						arr[0] = a;
					}
					return true;
				}
			return false;
		}
		
		public boolean replace(int tag)	{
			if(matchTag(tag))
				return false;
			for(int j = assoc-2; j >= 0; --j)
				arr[j+1] = arr[j];
			arr[0] = tag << 1;
			return true;
		}
	}
}
