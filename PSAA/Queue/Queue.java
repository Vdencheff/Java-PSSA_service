package PSAA.Queue;

public class Queue 
{
    public String str;
	// from 0 to num subscribers. Each subscriber when reads the message will decrease this value
	// producer when produce entry, will make this value equal to number of subscribers
	public int isFull;
}
