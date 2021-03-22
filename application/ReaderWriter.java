package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.OrderResult;

public abstract class ReaderWriter<T> {
    protected abstract int doRead(T obj);
    protected abstract OrderResult doWrite(T obj);
    protected int activeReaders = 0;
    protected int activeWriters = 0;
    protected int waitingWriters = 0;

    public int read(T obj){
        beforeRead();
        int price = doRead(obj);
        afterRead();
        return price;
    }
    public OrderResult write(T obj){
        beforeWrite();
        OrderResult or = doWrite(obj);
        afterWrite();
        return or;
    }
    protected synchronized void beforeRead(){
        while(!allowRead()){
            try{
                wait();
            }
            catch (InterruptedException e){}
        }
        activeReaders++;
    }
    protected synchronized void beforeWrite(){
        while(!allowWrite()){
            waitingWriters++;
            try{
                wait();
            }
            catch (InterruptedException e){}
            waitingWriters--;
        }
        activeWriters++;
    }
    protected synchronized void afterRead(){
        activeReaders--;
        notifyAll();
    }
    protected synchronized void afterWrite(){
        activeWriters--;
        notifyAll();
    }
    protected boolean allowRead(){
        return activeWriters == 0 && waitingWriters == 0;
    }
    protected boolean allowWrite(){
        return activeReaders == 0 && activeWriters == 0;
    }
}
