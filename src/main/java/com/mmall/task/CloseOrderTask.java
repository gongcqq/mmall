package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    @PreDestroy
    public void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

    }

//    @Scheduled(cron="0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
//        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

//    @Scheduled(cron="0 */1 * * * ?")
    /*public void closeOrderTaskV2(){
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }*/

    @Scheduled(cron="0 */1 * * * ?")
    public void closeOrderTaskV3(){
        log.info("关闭订单定时任务启动");
        //lockTimeout指的是锁的有效期，也就是给锁设置一个过期时间，以便能够释放锁
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        //如果设置成功，setnxResult就等于1，设置失败就等于0，设置失败就说明锁还没有被释放
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        //如果这一步判断成立，就说明锁已经被释放啦，那么就可以获取到锁啦
        if(setnxResult != null && setnxResult.intValue() == 1){
            //获取到锁之后就可以执行关闭订单的方法啦
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //走到这里说明未获取到锁，继续判断，判断时间戳，看是否需要重置并重新获取锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            //lockValueStr不为null说明锁还没有被释放，而现在的时间大于lockValueStr，说明锁已经过了有效期了
            //锁已经过了有效期，但是却没有被释放，说明在执行释放锁的代码前出现了故障，导致锁没能被释放
            if(lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                //如果只有一个tomcat的话，getSetResult和lockValueStr就是相等的，但是在集群环境下会出现并发访问的情况，所以它俩是有可能不相等的，所以下面还要判断一下
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                //如果getSetResult为null，说明从从获取到lockValueStr到获取到getSetResult这段时间内，由于并发访问，锁已经被删除了
                //如果锁没被删除，而且getSetResult的值和lockValueStr的值还是相等的话，就说明从获取到lockValueStr到获取到getSetResult这段时间内，锁没有被其他人获取过
                if(getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))){
                    //经过上面两层判断还能进到这一步，才算真正获取到锁，才能执行以下关闭订单的方法
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else{
                    log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else{
                log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }


//    @Scheduled(cron="0 */1 * * * ?")
    public void closeOrderTaskV4(){
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if(getLock = lock.tryLock(0,50, TimeUnit.SECONDS)){
                log.info("Redisson获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
//                iOrderService.closeOrder(hour);
            }else{
                log.info("Redisson没有获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常",e);
        } finally {
            if(!getLock){
                return;
            }
            lock.unlock();
            log.info("Redisson分布式锁释放锁");
        }
    }


    private void closeOrder(String lockName){
        RedisShardedPoolUtil.expire(lockName,5);//有效期5秒，防止死锁
        log.info("获取{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("===============================");
    }

}

