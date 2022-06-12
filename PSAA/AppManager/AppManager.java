package PSAA.AppManager;

import PSAA.LoggerCon.LoggerCon;
import PSAA.LoggerPSAA.LoggerPSAA;
import PSAA.Producer.Producer;

public class AppManager
{
    public static void main(String args[])
    {
        Producer prod1   = new Producer();   // producer
        Producer prod2   = new Producer();   // producer
        LoggerPSAA log   = new LoggerPSAA(); // service
        LoggerCon logCon = new LoggerCon();  // consumer
        LoggerCon logSer = new LoggerCon();  // consumer
        prod1.setService(log);               // telling the service about the producer
        prod2.setService(log);               // telling the service about the producer
        log.subscribe(logCon);               // to call writeLogMessage of the consumer
        log.subscribe(logSer);               // to call writeLogMessage of the consumer

        for (int i = 0; i < 20; i++)
        {
            prod1.writeLogMes("abc");
            prod2.writeLogMes("def");
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.stop();
        System.out.println("Program done");
    }
}
