package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.atomic.AtomicBoolean;

public class MoneyRegisterSerializer {

    private AtomicBoolean isSerialized;

    private static class MoneyRegisterSerializerHolder{
        private static MoneyRegisterSerializer instance = new MoneyRegisterSerializer();
    }

    private MoneyRegisterSerializer(){
        isSerialized = new AtomicBoolean(false);
    }

    public static MoneyRegisterSerializer getInstance() {
        return MoneyRegisterSerializerHolder.instance;
    }

    public void serialize(String fileName, MoneyRegister moneyRegister){
        if(isSerialized.compareAndSet(false,true)){
            Serializer s = new Serializer(fileName, moneyRegister);
            s.serialize();
        }
    }
}
