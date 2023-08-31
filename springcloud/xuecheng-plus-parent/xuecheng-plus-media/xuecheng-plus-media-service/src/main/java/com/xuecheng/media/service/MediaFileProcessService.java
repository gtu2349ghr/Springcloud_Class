package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * 处理成功后保存任务信息
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

    /**
     * 查询任务信息
     * @param shardTotal
     * @param shardIndex
     * @param count
     * @return
     */
    public List<MediaProcess> selectPreProcess(int  shardTotal, int shardIndex, int count);

    /**
     * 根据id拿到结果
     * * @param id
     * @return
     */
    public  MediaProcess selectStatus(Long id);

    /**
     * 更新状态
     * @param mediaProcess1
     * @return
     */
    int updateStatus(MediaProcess mediaProcess1);
}
