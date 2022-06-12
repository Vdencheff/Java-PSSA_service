package PSAA.FifoQueue;
import PSAA.Queue.Queue;

public class FifoQueue 
{
    FifoQueue()
    {
        for(int i = 0; i < buffer.length; i++)
        {
            buffer[i]= new Queue();
            buffer[i].str = "";
            buffer[i].isFull = 0;
        }
    }
    public Queue[] buffer =  new Queue[20];
}
