package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.UploadObjectArgs;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  * @param companyId 机构id
  * @param uploadFileParamsDto 文件信息
  * @param localFilePath 文件本地路径
  * @return UploadFileResultDto
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName);

 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

 /**
  * 检查文件
  * @param fileMd5
  * @return
  */
 public RestResponse<Boolean> cheakFile(String fileMd5);

 /**
  * 检查分块
  * @param fileMd5
  * @param chunkIndex
  * @return
  */
 public  RestResponse<Boolean> checkChunk(String fileMd5,int chunkIndex);

 /**
  * 上传分块
  * @param fileMde
  * @param localFilePath
  * @param chunk
  * @return
  */
 public RestResponse uploadChunk(String fileMde,String localFilePath,int chunk);

 /**
  * 合并文件
  * @param fileMd5 文件的md5值
  * @param totalFiles 分块的总数量
  * @param uploadFileParamsDto 入库的信息
  * @return
  */
 public RestResponse megerChunk(Long companyId,String fileMd5,int totalFiles,UploadFileParamsDto uploadFileParamsDto);

 /**
  * 下载文件
  * @param bucket 文件的桶
  * @param objectName 文件的地址
  * @return
  */
 public File downloadFileFromMinIO(String bucket, String objectName);

 /**
  * 上传文件到minio
  * @param localFilePath 当地文件地址
  * @param mimeType  文件类型
  * @param bucket  桶
  * @param objectName 名字
  * @return
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

 /**
  * 根据后缀转化为mintimetYpe
  * @param extension
  * @return
  */
 public String getMimeType(String extension);
}
