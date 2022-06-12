package PSAA.LoggerPSAA;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoggerPSAA
{
    public class ThreadWrapper implements Runnable
    {
        public ThreadWrapper(int subs)
        {
            subscriber = subs;
        }
        public void run()
        {
            try {
                sendMesToSubscribers(subscriber);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private int subscriber;
    }
	public LoggerPSAA()
    {
        for(int i = 0; i < mem.length; i++)
        {
            mem[i]= new payload();
            mem[i].data = "";
            mem[i].refs = 0;
        }
        for (int i = 0; i < maxsubscribers; i++)
        {
            fifoState[i] = fifoStates.empty;
            cond_p[i] = fifo_m.newCondition();
        }
        compStatus = 0;         // not started state
        subscribers = 0;        // currently subscribed subscribers
    }
	public void stop()
    {
        compStatus = 3;         // stop the trheads
        for(int i = 0; i < subscribers; i++)
        {            
            fifo_m.lock();      // take the lock and send signal
            cond_p[i].signal(); // if any consumer is waiting on condvar wake him up. he will see that stop is requested.
            fifo_m.unlock();    // after sending the signal release the lock, so the thread that waits on condvar can continue
            if (th[i].isAlive())
            {
                try {
                    th[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public void subscribe(ILogger log)
    {
        compStatus = 2; // active state
        if (subscribers < maxsubscribers)
        {
            th[subscribers] = new Thread(new ThreadWrapper(subscribers));
            th[subscribers].start();
            subscribers++;
        }
    }

    // producer calls this function
	public void writeLogMes(String str) throws InterruptedException
    {
        fifo_m.lock();
        try
        {
            // stops, check that the point we have pointed is with 0 refs so we can write there
            while(mem[head].refs != 0)
            {
                cond_c.await();
            }
            // if we are here it means there is a free space in fifo, we can insert at least one element
            mem[head].data = str;
            mem[head].refs = subscribers; // update the refs to the number of consumers. Each consumer when read this cell will decrease the value by 1.
            System.out.println("write mem[" + head + "]: data= " + mem[head].data + "; refs = " + mem[head].refs);
            if (head == fifosize - 1) 
            { 
                head = 0; 
                System.out.println( "Producer moves write pointer to the begining"); 
            }
            else 
            { 
                head++; 
            }
            // after inserting one element, check the fifo state for all consumers, it could be full for some of them
            for (int e = 0; e < subscribers; e++)
            {
                if (fifoState[e] == fifoStates.empty)  // notify the consumer not to wait on condvar only if it has been waiting
                {
                    cond_p[e].signal();
                }
                fifoState[e] = fifoStates.notempty;   // having inserted an element, no consumer can be empty
            }
        }
        finally 
        {
            fifo_m.unlock();
        }
    }

    // threads run this function
	public void sendMesToSubscribers(int consumer) throws InterruptedException 
    {
        while (compStatus == 2) // thread is allive while comp is started
	    {          
            fifo_m.lock();
            try
            {
                while(fifoState[consumer] == fifoStates.empty && compStatus == 2)
                {
                    cond_p[consumer].await();
                }
                if(compStatus == 3)
                {
                    return; // stop signal has been sent
                }
                // if we are here it means there is at least one element in fifo, we can read at least one element
                System.out.println("Consumer" + consumer + " reads mem[" + c[consumer] + "]: data= " + mem[c[consumer]].data + " refs = " + mem[c[consumer]].refs );
                mem[c[consumer]].refs--; // update the read cell
                // update consumer fifo pointer
                if (c[consumer] == fifosize - 1) 
                { 
                    c[consumer] = 0; 
                    System.out.println("Consumer " + consumer + " moves read pointer to the begining"); 
                }
                else 
                { 
                    c[consumer]++; 
                }

                // the producer will be signalled by each consumer that finds empty cell
                if(mem[head].refs == 0) // the producer will be modified only if there is empty element at head position (there could be more empty elemets)
                    cond_c.signal();
                if (c[consumer] == head)               // c catch up p, fifo is empty for this consumer
                    fifoState[consumer] = fifoStates.empty;
                else
                    fifoState[consumer] = fifoStates.notempty;
                m_cb.cb(2); // Calls the last instantiated producer cb.
            }
            finally 
            {
                fifo_m.unlock();
            }
        }
    }

    public void setCallback(ILogger il)
    {       
        m_cb = il;
    }

    private static int maxsubscribers = 10;
    private int fifosize = 10;
    int subscribers;                                         // subscribers count
    private Thread[] th = new Thread[maxsubscribers];        // one thread per subscriber
    private int compStatus;                                  // 0- deinit; 1- init; 2-start; 3- stop
    public ILogger m_cb;                                     // store the consumer which callback to call
    final Lock fifo_m = new ReentrantLock();
    final Condition[] cond_p = new Condition[maxsubscribers];
    final Condition cond_c = fifo_m.newCondition();
	private int[] c = new int[20];                           // pointers in FIFO
	private payload[] mem = new payload[fifosize];
	int head = 0;
	fifoStates[] fifoState = new fifoStates[maxsubscribers]; // fifo can be full for c1 and empty for c2 and partially full for c3
}
