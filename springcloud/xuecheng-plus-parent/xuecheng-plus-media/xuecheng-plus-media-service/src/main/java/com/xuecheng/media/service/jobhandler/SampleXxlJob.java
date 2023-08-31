package com.xuecheng.media.service.jobhandler;

import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.utils.Mp4VideoUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class SampleXxlJob {
    @Autowired
    MediaFileProcessService mediaFileProcessService;
    @Resource
    RedissonClient redisson;
    @Autowired
    MediaFileService mediaFileService;
    public  static  String lockName="lock";
    private static Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);


    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        System.out.println("任务开始执行啦————————————————————————————————————————————————————————");
//        XxlJobHelper.log("XXL-JOB, Hello World.");
//
//        for (int i = 0; i < 5; i++) {
//            XxlJobHelper.log("beat at:" + i);
//            TimeUnit.SECONDS.sleep(2);
//        }
        // default success
    }


    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        System.out.println("shardIndex="+shardIndex+"shardTotal="+shardTotal+"_____________________________");
//
//        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
//
//        // 业务逻辑
//        for (int i = 0; i < shardTotal; i++) {
//            if (i == shardIndex) {
//                XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
//            } else {
//                XxlJobHelper.log("第 {} 片, 忽略", i);
//            }
//        }
        //业务逻辑
        //查询内核数量
        int count = Runtime.getRuntime().availableProcessors();
        //先查询任务
        log.info("执行查询任务");
        List<MediaProcess> mediaProcesses = mediaFileProcessService.selectPreProcess(shardTotal, shardIndex, count);
        //拿到任务的数量
        int size = mediaProcesses.size();
        log.info("拿到的任务数量："+size);
        if(size<=0){
            return;
        }
        //在这里进行计数器的计算
        CountDownLatch latch=new CountDownLatch(size);
        //然后创建线程池执行任务
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //然后去执行逻辑
        mediaProcesses.forEach(mediaProcess -> {
            executorService.execute(()->{
                //fileid
                String fileId = mediaProcess.getFileId();
                //任务id
                Long taskid = mediaProcess.getId();
                //在这里执行任务
                //首先要更新任务状态，这里加redission判断
                RLock lock = redisson.getLock(lockName);
                lock.lock();
                try{
                    //先拿到状态看是否已经有其他的执行器去执行他
                    //先根据id拿到结果，然后结果不等于4开始更新
                    MediaProcess mediaProcess1 = mediaFileProcessService.selectStatus(taskid);
                    if(mediaProcess1.getStatus().equals("4")||mediaProcess1.getFailCount()>=3){
                        return ;
                    }
                    mediaProcess1.setStatus("4");
                    //否则就更新
                    int i = mediaFileProcessService.updateStatus(mediaProcess1);
                    if(i<=0){
                        log.info("更新数据库状态失败，执行器过程id:{}",fileId);
                        return ;
                    }else{
                        log.info("更新数据库成功，执行器过程id:{}",fileId);
                    }
                }catch (Exception e){
                    e.printStackTrace();

                }finally {
                    lock.unlock();
                }
                //然后就是更新完毕了，开始处理视频
                //从minio中下载
                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                File file = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                if(file==null){
                    log.info("minio中文件不存在下载失败，桶:{},objectName:{}",bucket,filePath);
                    //这里失败后进行保存
                    mediaFileProcessService.saveProcessFinishStatus(taskid,"3",fileId,null,"minio文件下载失败");
                //最后临时文件一定要删除
                    file.delete();
                    return ;
                }
                //然后创建一个临时文件
                File minio=null;
                String mp4_path1 = null;
                try {
                    minio= File.createTempFile("minio", ".mp4");

                } catch (IOException e) {
                    e.printStackTrace();
                    log.debug("创建临时文件异常");
                }
                mp4_path1 = minio.getAbsolutePath();
                //源文件的地址（下载的时候也在本地创建了一个临时文件）
                String absolutePath = file.getAbsolutePath();
                //然后处理视频
                //ffmpeg的路径
                String ffmpeg_path = "D:\\BBB\\ffmpeg\\bin\\ffmpeg.exe";//ffmpeg的安装位置
                //源avi视频的路径
                String video_path = absolutePath;
                //转换后mp4文件的名称
                String mp4_name = fileId+".mp4";
                //转换后mp4文件的路径
                String mp4_path = mp4_path1;
                //创建工具类对象
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                //开始视频转换，成功将返回success
                String s = videoUtil.generateMp4();
                if(!s.equals("success")){
                    log.debug("视频转码失败，原因:{},bucket:{},objectName:{}",s,bucket,filePath);
                    mediaFileProcessService.saveProcessFinishStatus(taskid,"3",fileId,null,s);
                    minio.delete();
          return;
                }

                       //成功之后要进行上传

                String mimeType = mediaFileService.getMimeType(".mp4");
                String objetName=getmergerPath(fileId,".mp4");
                boolean b = mediaFileService.addMediaFilesToMinIO(mp4_path, mimeType, bucket,objetName);
                //然后更新状态
                if(!b){
                    //上传失败了
                    log.debug("视频上传失败，taskId:{}",taskid);
                    mediaFileProcessService.saveProcessFinishStatus(taskid,"3",fileId,null,"minio上传视频失败");
             minio.delete();
               return;
                }
                //上传成功然后就更新状态，并且设置url
                String url = getmergerPath(fileId, ".mp4");
                mediaFileProcessService.saveProcessFinishStatus(taskid,"2",fileId,url,"成功");
                log.info("全部完成");
                minio.delete();
                //这里计数器减一
                latch.countDown();
            });
        });
        //在循环结束之后进行计数器等待不让主线程结束
        latch.await();

    }
    public String getmergerPath(String fileMd5,String ext){
        return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+ext;
    }
//
//
//    /**
//     * 3、命令行任务
//     */
//    @XxlJob("commandJobHandler")
//    public void commandJobHandler() throws Exception {
//        String command = XxlJobHelper.getJobParam();
//        int exitValue = -1;
//
//        BufferedReader bufferedReader = null;
//        try {
//            // command process
//            ProcessBuilder processBuilder = new ProcessBuilder();
//            processBuilder.command(command);
//            processBuilder.redirectErrorStream(true);
//
//            Process process = processBuilder.start();
//            //Process process = Runtime.getRuntime().exec(command);
//
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
//            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));
//
//            // command log
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                XxlJobHelper.log(line);
//            }
//
//            // command exit
//            process.waitFor();
//            exitValue = process.exitValue();
//        } catch (Exception e) {
//            XxlJobHelper.log(e);
//        } finally {
//            if (bufferedReader != null) {
//                bufferedReader.close();
//            }
//        }
//
//        if (exitValue == 0) {
//            // default success
//        } else {
//            XxlJobHelper.handleFail("command exit value("+exitValue+") is failed");
//        }
//
//    }
//
//
//    /**
//     * 4、跨平台Http任务
//     *  参数示例：
//     *      "url: http://www.baidu.com\n" +
//     *      "method: get\n" +
//     *      "data: content\n";
//     */
//    @XxlJob("httpJobHandler")
//    public void httpJobHandler() throws Exception {
//
//        // param parse
//        String param = XxlJobHelper.getJobParam();
//        if (param==null || param.trim().length()==0) {
//            XxlJobHelper.log("param["+ param +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//
//        String[] httpParams = param.split("\n");
//        String url = null;
//        String method = null;
//        String data = null;
//        for (String httpParam: httpParams) {
//            if (httpParam.startsWith("url:")) {
//                url = httpParam.substring(httpParam.indexOf("url:") + 4).trim();
//            }
//            if (httpParam.startsWith("method:")) {
//                method = httpParam.substring(httpParam.indexOf("method:") + 7).trim().toUpperCase();
//            }
//            if (httpParam.startsWith("data:")) {
//                data = httpParam.substring(httpParam.indexOf("data:") + 5).trim();
//            }
//        }
//
//        // param valid
//        if (url==null || url.trim().length()==0) {
//            XxlJobHelper.log("url["+ url +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//        if (method==null || !Arrays.asList("GET", "POST").contains(method)) {
//            XxlJobHelper.log("method["+ method +"] invalid.");
//
//            XxlJobHelper.handleFail();
//            return;
//        }
//        boolean isPostMethod = method.equals("POST");
//
//        // request
//        HttpURLConnection connection = null;
//        BufferedReader bufferedReader = null;
//        try {
//            // connection
//            URL realUrl = new URL(url);
//            connection = (HttpURLConnection) realUrl.openConnection();
//
//            // connection setting
//            connection.setRequestMethod(method);
//            connection.setDoOutput(isPostMethod);
//            connection.setDoInput(true);
//            connection.setUseCaches(false);
//            connection.setReadTimeout(5 * 1000);
//            connection.setConnectTimeout(3 * 1000);
//            connection.setRequestProperty("connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
//
//            // do connection
//            connection.connect();
//
//            // data
//            if (isPostMethod && data!=null && data.trim().length()>0) {
//                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
//                dataOutputStream.write(data.getBytes("UTF-8"));
//                dataOutputStream.flush();
//                dataOutputStream.close();
//            }
//
//            // valid StatusCode
//            int statusCode = connection.getResponseCode();
//            if (statusCode != 200) {
//                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
//            }
//
//            // result
//            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
//            StringBuilder result = new StringBuilder();
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                result.append(line);
//            }
//            String responseMsg = result.toString();
//
//            XxlJobHelper.log(responseMsg);
//
//            return;
//        } catch (Exception e) {
//            XxlJobHelper.log(e);
//
//            XxlJobHelper.handleFail();
//            return;
//        } finally {
//            try {
//                if (bufferedReader != null) {
//                    bufferedReader.close();
//                }
//                if (connection != null) {
//                    connection.disconnect();
//                }
//            } catch (Exception e2) {
//                XxlJobHelper.log(e2);
//            }
//        }
//
//    }
//
//    /**
//     * 5、生命周期任务示例：任务初始化与销毁时，支持自定义相关逻辑；
//     */
//    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
//    public void demoJobHandler2() throws Exception {
//        XxlJobHelper.log("XXL-JOB, Hello World.");
//    }
//    public void init(){
//        logger.info("init");
//    }
//    public void destroy(){
//        logger.info("destroy");
//    }
//

}
