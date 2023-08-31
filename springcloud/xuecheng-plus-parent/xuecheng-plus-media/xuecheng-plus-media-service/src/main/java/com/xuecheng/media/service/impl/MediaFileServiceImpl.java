package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //根据扩展名获取mimeType
    public String getMimeType(String extension){
        if(extension == null){
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    /**
     * 将文件上传到minio
     * @param localFilePath 文件本地路径
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 对象名
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶
                    .filename(localFilePath) //指定本地文件路径
                    .object(objectName)//对象名 放在子目录下
                    .contentType(mimeType)//设置媒体文件类型
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{},错误信息:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
           e.printStackTrace();
           log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }
    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName1) {


        //文件名
        String filename = uploadFileParamsDto.getFilename();
        //先得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        //得到mimeType
        String mimeType = getMimeType(extension);

        //子目录
        String defaultFolderPath = getDefaultFolderPath();
        //文件的md5值
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName=null;
        if(objectName1==null){
           objectName = defaultFolderPath+fileMd5+extension;
        }else{
            objectName = objectName1;
        }

        //上传文件到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if(!result){
            XueChengPlusException.cast("上传文件失败");
        }
        //入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if(mediaFiles==null){
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);

        return uploadFileResultDto;

    }


    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
                     //预处理表创建完然保存数据库
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert<=0){
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}",bucket,objectName);
                return null;
            }
            //待处理表
            addProcess(mediaFiles);
            return mediaFiles;

        }else{
            MediaFiles mediaFiles1 = mediaFilesMapper.selectById(fileMd5);
            //桶
            mediaFiles1.setBucket(bucket);
            //file_path
            mediaFiles1.setFilePath(objectName);
            //file_id
            mediaFiles1.setFileId(fileMd5);
            //url
            mediaFiles1.setUrl("/"+bucket+"/"+objectName);
            //修改时间
            mediaFiles1.setChangeDate(LocalDateTime.now());
            int i = mediaFilesMapper.updateById(mediaFiles1);
            if(i<=0){
                log.info("向数据库修改信息失败，bucket:{},objectName:{}",bucket,objectName);
                return null;
            }else{
                //待处理表
                addProcess(mediaFiles);
                return mediaFiles;
            }
        }

    }
private  void addProcess(MediaFiles mediaFiles){
        log.info("进入预处理表阶段");
        //判断类型
    String filename = mediaFiles.getFilename();
    String substring = filename.substring(filename.lastIndexOf("."));
    log.info("后缀名:{}",substring);
    //截取完之后获取类型minioType
    String mimeType = getMimeType(substring);
    if(substring.equals(".avi")){
        //然后进行插入
        MediaProcess mediaProcess = new MediaProcess();
        BeanUtils.copyProperties(mediaFiles,mediaProcess);
        mediaProcess.setStatus("1");//1是未处理
        mediaProcess.setCreateDate(LocalDateTime.now());
        mediaProcess.setFailCount(0);
        int insert = mediaProcessMapper.insert(mediaProcess);
        if(insert<=0){
            log.info("创建预处理表失败");
            XueChengPlusException.cast("创建预处理表失败");
        }else{
            log.info("创建预处理表成功");
        }

    }
}
    @Override
    /**
     * 检查文件有没有上传
     */
    public RestResponse<Boolean> cheakFile(String fileMd5) {
        //先查数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles!=null){
            //如果数据库有值的话再去查minio
            String bucket=mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            //将流设置为null
            InputStream stream=null;
            try {
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build());
                if(stream!=null){
                    //如果流不为null则返回true
           log.info("在minio中已经存在文件数据");
                    return RestResponse.success(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    /**
     * 检查分块有没有创建
     * @param fileMd5
     * @param chunkIndex 分块的序号
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //拿到分块的路径
        String chunkPath = getChunkPath(fileMd5);
        //如果数据库有值的话再去查minio
        //将流设置为null
        InputStream stream=null;
        try {
            stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(chunkPath+chunkIndex)
                            .build());
            if(stream!=null){
                //如果流不为null则返回true
                log.info("在minio中已经存在分块数据:{}",chunkIndex);
                return RestResponse.success(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    /**
     * 这个是拿到分块所在的目录
     * @param fileMd5
     * @return
     */
    public String getChunkPath(String fileMd5){
        return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+"chunk"+"/";
    }

    /**
     * 上传分块文件
     * @param fileMde MD5
     * @param localFilePath 当地分块文件路劲
     * @param chunk 分块的序号
     * @return
     */
    public RestResponse uploadChunk(String fileMde,String localFilePath,int chunk){
        //先根据MD5拿到上传到的路径(也就是桶后面的完整名字)
             String path= getChunkPath(fileMde)+chunk;
             //然后获取minioType
        String mimeType = getMimeType(null);
        //调用上传方法
        boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_video, path);
        if(!b){
            return RestResponse.validfail(false,"上传文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse megerChunk(Long companyId,String fileMd5, int totalFiles, UploadFileParamsDto uploadFileParamsDto) {
        //拿到分块文件的路径
        String chunkPath = getChunkPath(fileMd5);
        //拿到文件的后缀
        String filename = uploadFileParamsDto.getFilename();
        //substring左闭右开，然后从点位置开始一直到结束就是后缀名
        String substring = filename.substring(filename.lastIndexOf("."));
        //根据MD5拿到文件合并和的路径
        String objectName = getmergerPath(fileMd5, substring);
        //拿到分块后的文件
        List<ComposeSource> collect = Stream.iterate(0, i -> ++i).limit(totalFiles)
                .map(i -> ComposeSource.builder().bucket(bucket_video)
                        .object(chunkPath + Integer.toString(i)).build())
                .collect(Collectors.toList());
        //进行合并
        try {
            ObjectWriteResponse response = minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(objectName) //合并后的文件名
                            .sources(collect) //集合
                            .build());
            log.info("合并成功，文件:{}",objectName);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.validfail(false,"合并文件失败");
        }
         //合并成功之后验证MD5,这里需要拿到minio中刚上传的文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        if(file==null){
            log.info("合并后下载文件失败：文件名:{},桶:{},md5{}",objectName,bucket_video,fileMd5);
            return RestResponse.success(false,"文件合并后下载失败");
        }
        //拿到源文件后进行md5对比
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String s = DigestUtils.md5Hex(fileInputStream);
            if(s.equals(fileMd5)){
                log.info("文件校验成功");
            }else{
                log.info("文件校验失败，fileMd5:{}",fileMd5);
                return RestResponse.validfail(false,"文件校验失败");
            }
            //然后设置文件的大小
            uploadFileParamsDto.setFileSize(file.length());
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(file!=null){
                //删除文件(这里是删除本地临时的文件)
                file.delete();
            }
        }

        //校验完成了进行入库信息,因为涉及到了事务，所以用代理对象调用
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles==null){
            log.info("入库失败:{}",fileMd5);
            return RestResponse.validfail(false,"入库失败");
        }
         log.info("入库成功:{}",fileMd5);
        //然后清理分块
        cleanChunkFile(chunkPath,totalFiles);
        return RestResponse.success(true);
    }

    /**
     * 根据md5拿到合并后文件的objectName
     * @param fileMd5
     * @param ext 后缀
     * @return
     */
    public String getmergerPath(String fileMd5,String ext){
        return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+ext;
    }
    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".temp");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public  void cleanChunkFile(String chunkFilepath,int chunkTotal){
        //根据路径和快数转化为list
        List<DeleteObject> collect = Stream.iterate(0, i -> i+1).limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFilepath + i))
                .collect(Collectors.toList());
        RemoveObjectsArgs build = RemoveObjectsArgs.builder().bucket(bucket_video).objects(collect).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(build);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
            }
        });
    }

}




