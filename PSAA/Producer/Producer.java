package PSAA.Producer;

import PSAA.LoggerPSAA.ILogger;
import PSAA.LoggerPSAA.LoggerPSAA;

public class Producer implements ILogger
{
	public void setService(LoggerPSAA log)
    {
        m_log = log;
        log.setCallback(this); // service must know about Producer to be able to call the callback
         
    }

	public void writeLogMes(String str)
    {
        if(m_log != null)
        {
            try {
                m_log.writeLogMes(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void cb(int a) // callback
    {
        System.out.println("Producer callback "+ a);
    }

	LoggerPSAA m_log; // keep the service instance
}
