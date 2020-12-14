package cn.mrxccc.javacv.example;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;

/**
 * @author mrxccc
 * @create 2020/12/14
 */
public class RecordPushTest {
    public static void main(String[] args)
            throws Exception {

        String inputFile = "D:\\常用文档\\个人\\OneDrive\\桌面\\video";

        String outputFile = "rtmp://192.168.205.10:1935/stream/hello";//本地配置的推流地址

        recordPush(inputFile, outputFile, 25);
    }
    /**
     * 转流器
     * @param inputFile
     * @param outputFile
     * @throws Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @throws InterruptedException
     */
    public static void recordPush(String inputFile,String outputFile,int v_rs) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException{

        // url流
        FrameGrabber grabber = getFrameGrabber(inputFile, v_rs);

        //一个opencv视频帧转换器
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        System.out.println("开始获取OpenCVFrameConverter.ToIplImage："+converter);
        Frame grabframe =grabber.grab();
        IplImage grabbedImage =null;
        if(grabframe!=null){
            System.out.println("取到第一帧");
            grabbedImage = converter.convert(grabframe);
        }else{
            System.out.println("没有取到第一帧");
        }
        //如果想要保存图片,可以使用 opencv_imgcodecs.cvSaveImage("hello.jpg", grabbedImage);来保存图片

        // 视频帧录制器
        FrameRecorder recorder;
        try {
            recorder = FrameRecorder.createDefault(outputFile, 1280, 720);
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            throw e;
        }

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
        //封装格式，如果是推送到rtmp就必须是flv封装格式
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);
        System.out.println("准备开始推流...");
        try {
            recorder.start();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            try {
                System.out.println("录制器启动失败，正在重新启动...");
                if(recorder!=null)
                {
                    System.out.println("尝试关闭录制器");
                    recorder.stop();
                    System.out.println("尝试重新开启录制器");
                    recorder.start();
                }

            } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
                throw e;
            }

        }
        System.out.println("开始推流");
        //展示每一帧
        CanvasFrame canvasFrame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        long startTime=0;
        while (canvasFrame.isVisible() && (grabframe=grabber.grab()) != null) {
            canvasFrame.showImage(grabframe);
            grabbedImage = converter.convert(grabframe);
            Frame rotatedFrame = converter.convert(grabbedImage);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));//时间戳
            if(rotatedFrame!=null &&  rotatedFrame.imageHeight > 0 && rotatedFrame.imageWidth > 0){
                recorder.record(rotatedFrame);
            }
        }
        recorder.close();
        grabber.close();
    }

    /**
     * 功能描述: 帧抓取器
     * @Param [inputFile, v_rs]
     * @return FrameGrabber
     * @author caijiacheng
     * @date 2020/12/14 14:31
     */
    public static FrameGrabber getFrameGrabber(String inputFile,int v_rs) throws FrameGrabber.Exception {
        FrameGrabber grabber =new FFmpegFrameGrabber(inputFile);
        try {
            grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重
            grabber.start();
        } catch (Exception e) {
            try {
                System.out.println("获取 grabber的restart："+grabber);
                grabber.restart();
            } catch (Exception e1) {
                System.out.println("出错了："+grabber);
                throw e;
            }
        }
        System.out.println("结束获取FrameGrabber grabber："+grabber);

        return grabber;
    }

}
