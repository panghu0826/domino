package com.jule.robot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTime {
    private final static Logger logger = LoggerFactory.getLogger(RunTime.class);
    private long createTime;
    private String programName;

    public RunTime(String _programName){
        this.createTime = System.currentTimeMillis();
        this.programName = _programName;
    }

    public long getSecsBuyAlreadyRun(){
        return getSecsBuyAlreadyRun(2);
    }

    /**
     * 获得已经执行的秒数
     * @param limitSec 限制秒数，如果超过此秒数，将打印 ERROR 日志。
     * @return
     */
    public long getSecsBuyAlreadyRun(int limitSec){
        long secNumber = -1;
        try {
            secNumber = (System.currentTimeMillis() - this.createTime) / 1000; //得到相差的秒数
            if(secNumber > limitSec){
                logger.error("程序执行时间过长, programName->{}, runSecNumber->{}", programName, secNumber);
            }
        }catch (Exception ex){
            logger.error("获得程序执行时长Exception. programName->{}", programName, ex);
        }
        return secNumber;
    }
}
