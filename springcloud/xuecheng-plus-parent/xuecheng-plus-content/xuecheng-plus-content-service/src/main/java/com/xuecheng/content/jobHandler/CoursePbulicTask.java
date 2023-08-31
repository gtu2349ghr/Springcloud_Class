package com.xuecheng.content.jobHandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class CoursePbulicTask extends MessageProcessAbstract {

    //在这里定义一个调度的任务方法
    @XxlJob("executeTask")
    public void executeTask() throws Exception{
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //调用执行方法
        //执行器id，总数，消息类型，每次处理消息数量，超时时间
        process(shardIndex,shardTotal,"course_public",30,60);
    }
    /**
     * \执行任务逻辑，mq的process方法执行任务没执行一次会掉一次这个方法
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        //这个拿到的是课程的id
        //因为是任务调度所以为了达到分片的目的
        String courseId = mqMessage.getBusinessKey1();
        //下面这写都抽象成方法去实现
        //先上传静态页面
        //写索引
        //写redis
   //只有当所有的任务都完成然后返回true
        return false;
    }

    /**
     * 上传静态页面数据到miniio
     * @param mqMessage
     * @param courseId
     */
    private  void PublishMinio(MqMessage mqMessage,Long courseId){
        Long id = mqMessage.getId();
        //这里拿到任务的id
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(courseId);
        //先拿到阶段一的id
        if(stageOne>0){
            return ;
        }
        //否则就执行业务逻辑


        // 执行完之后写入状态
        int i = mqMessageService.completedStageOne(id);
        if(i<=0){
            XueChengPlusException.cast("任务完成，写入数据库失败:{}");
        }


    }
    private  void saveCourseIndex(MqMessage mqMessage,Long courseId){
        Long id = mqMessage.getId();
        //这里拿到任务的id
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(courseId);
        //先拿到阶段一的id
        if(stageTwo>0){
            return ;
        }
        //否则就执行业务逻辑


        // 执行完之后写入状态
        int i = mqMessageService.completedStageTwo(id);
        if(i<=0){
            XueChengPlusException.cast("写入索引任务完成，写入数据库失败:{}");
        }
        return ;
    }
    private  void insertRedis(MqMessage mqMessage,Long courseId){
        Long id = mqMessage.getId();
        //这里拿到任务的id
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(courseId);
        //先拿到阶段一的id
        if(stageThree>0){
            return ;
        }
        //否则就执行业务逻辑


        // 执行完之后写入状态
        int i = mqMessageService.completedStageThree(id);
        if(i<=0){
            XueChengPlusException.cast("写入redis任务完成，写入数据库失败:{}");
        }
        return ;
    }
}
