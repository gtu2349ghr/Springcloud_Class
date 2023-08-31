package com.xuecheng.media.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
@Slf4j
public class test {
    public static void main(String[] args)  {
        try {
            hebing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void fenkuai()throws Exception{
        //先拿到分快的地址和源文件的地址
        File source = new File("D:\\BBB\\source\\7.24北京.wcp");
        //分块的地址
       String fenPath="D:\\BBB\\fen\\";
        File fen = new File("D:\\BBB\\fen\\");
        //然后分快，拿到源文件的size,不存在则创建
        if(!fen.exists()){
            fen.mkdirs();
        }
        //定义分块的大小
        long rankSize=1024*1024*1;
        //然后根据大小计算分块的个数
       long size= (long) Math.ceil(source.length()*1.0/rankSize);
       //定义缓冲区
        byte[] bytes = new byte[1024];
        //先难道源文件的读取流
        RandomAccessFile r = new RandomAccessFile(source, "r");
        for(int i=0;i<size;i++){
           File fenFile = new File(fenPath + i);
           //然后创建流进行读取的写入

               //再拿到输出流
               RandomAccessFile rw = new RandomAccessFile(fenFile, "rw");
               int len=-1;
               while((len=r.read(bytes))!=-1){
                   //如果没有读到文件的末尾
                   rw.write(bytes,0,len);
                   //这里因为是分块的,当分块区满了就进行下一个循环
                   if(fenFile.length()>=rankSize){
                       break;
                   }
               }
               //然后每次循环写关闭，因为他向不同的分块文件写
               rw.close();

       }
        //最后读关闭
        r.close();
    }
    public  static void hebing() throws Exception{
        File file = new File("D:\\BBB\\he\\");
        if(!file.exists()){
            file.mkdirs();
        }
        File heFile = new File("D:\\BBB\\he\\123.wcp");
        if(heFile.exists()){
            heFile.delete();
        }
        //如果合并的文件存在则删除重新创建一个新的文件
        heFile.createNewFile();
        //创建一个输出流
        RandomAccessFile rw = new RandomAccessFile(heFile, "rw");
        //然后拿到分块后的数据
        File fenPath = new File("D:\\BBB\\fen");
        //创建缓冲区
        //指针指向文件顶端
        rw.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        File[] files = fenPath.listFiles();
        //因为拿到的文件数组是无序的，所以将他进行排序
        List<File> files1 = Arrays.asList(files);
        Collections.sort(files1, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                //这个比较返回的是一个int值，因为大的要排列再右边，所以你要升序的话就返回一个负数
                return Integer.parseInt(o1.getName())-Integer.parseInt(o2.getName());
            }
        });
        //创建一个输出流

        for(File f:files1){
            //创建写入流
            RandomAccessFile r = new RandomAccessFile(f,"r");
            int len=-1;
            while((len=r.read(b))!=-1){
                rw.write(b,0,len);
            }
            //每一次写入完，读取流关闭
            r.close();
        }
        //全部写完写入流关闭
        rw.close();
        //然后进行校验
        FileInputStream FenInputStream = new FileInputStream(new File("D:\\BBB\\source\\7.24北京.wcp"));
        FileInputStream HeInputStream = new FileInputStream(new File("D:\\BBB\\he\\123.wcp"));
        String s = DigestUtils.md5Hex(FenInputStream);
        String s1 = DigestUtils.md5Hex(HeInputStream);
        if(s.equals(s1)){
            System.out.println("sssssssssss");
        }
    }
}
