package com.ljh.untils.id;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * id生成器
 * @author ljh
 */
public class IdGenerator {

    /**
     * 起始时间戳
     */
    public static final long START_STAMP = Objects.requireNonNull(DateUtil.get("2023-1-1")).getTime();

    /**
     * 机房号
     */
    public static final long DATA_CENTER = 5L;

    /**
     * 机器号
     */
    public static final long MACHINE = 5L;

    /**
     * 序列号
     */
    public static final long SEQUENCE = 12L;

    /**
     * 机器号最大值
     */
    public static final long MACHINE_MAX = ~(-1L << MACHINE);

    /**
     * 机房最大值
     */
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER);

    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE);



    /**
     * 时间戳左移位数
     */
    public static final long START_STAMP_LEFT = DATA_CENTER + MACHINE + SEQUENCE ;


    /**
     * 机房号左移位数
     */
    public static final long DATA_CENTER_LEFT = MACHINE + SEQUENCE ;


    /**
     * 机器号左移位数
     */
    public static final long MACHINE_LEFT = SEQUENCE ;





    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    private long lastTimeStamp = -1L;


    public IdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){

            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法");

        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }



    public   long getId(){

        long currentTime = System.currentTimeMillis();

        long timeStamp = currentTime - START_STAMP;

        //判断时间回拨
        if (timeStamp < lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");

        }

        if (timeStamp == lastTimeStamp){

            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX){

                timeStamp = getNextTimeStamp();

                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }

        lastTimeStamp = timeStamp;

        long sequence = sequenceId.sum();

        return timeStamp << START_STAMP_LEFT | dataCenterId << DATA_CENTER_LEFT |
                machineId << MACHINE_LEFT | sequence;





    }

    private long getNextTimeStamp() {

        long current = System.currentTimeMillis() - START_STAMP;

        while (current <= lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;

        }

        return current;

    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(2, 1);


        for (int i = 0; i < 1000; i++){

            new Thread(() ->{


                long id = idGenerator.getId();
                System.out.println(id);
            }).start();
        }

    }





}
