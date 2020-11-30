package cn.mrxccc.javacv.example;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

/**
 * @author mrxccc
 * @create 2020/11/30
 */
public class FrameRecordTest {

    public static volatile Boolean isStart = true;

    public static void main(String[] args) throws Exception {

        String inputFile =
            "rtsp://10.122.100.240:31080/overflowed_garbage/test_013_P_4254P1_baoshan_BLLJ_LJT_20200820_2k_V1.ts";
        // Decodes-encodes
        String outputFile = "recorde.mp4";
        frameRecord(inputFile, outputFile, 1, 10000);
    }

    /**
     * 按帧录制视频
     *
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile                              -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public static void frameRecord(String inputFile, String outputFile, int audioChannel, long time)
        throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        //        boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel);

        ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
        TimerTask task1 = new TimerTask() {
            @Override public void run() {
                try {
                    isStart = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        newScheduledThreadPool.schedule(task1, time, TimeUnit.MILLISECONDS);
        recordByFrame(grabber, recorder);
        newScheduledThreadPool.shutdown();
        System.out.println("关闭");
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder)
        throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;
            System.out.println("开始录制");
            while (isStart && (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
            System.out.println("停止录制");
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }
}

