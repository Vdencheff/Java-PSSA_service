package PSAA.LoggerCon;

import PSAA.LoggerPSAA.ILogger;

public class LoggerCon implements ILogger
{
    public void writeLogMes(String str)
    {
        System.out.println("LoggerCon writes: " + str);
    }

     // this function is not needed. Just implement the interface.
    public void cb(int a){}
}
