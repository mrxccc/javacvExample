package cn.mrxccc.javacv.example;

import javax.swing.JFrame;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;

/**
 * rtsp拉流
 * @author cjc
 */
public class RtspGrabber {

    /**
     * rtsp拉流
     * @author eguid
     * @param input rtsp
     */
    public void pushAndRecordHls(String input) throws Exception, org.bytedeco.javacv.FrameGrabber.Exception{
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
        //
        if(input.indexOf("rtsp")>-1) {
            //虽然rtsp本身是协议，但是对于ffmpeg来说，rtsp只是个多路复用器/解复用器。可以支持普通的rtp传输，也可以支持RDT传输的Real-RTSP协议
            grabber.setFormat("rtsp");
            //设置要从服务器接受的媒体类型，为空默认支持所有媒体类型，支持的媒体类型：[video，audio，data]
            grabber.setOption("allowed_media_types", "video");
            //设置RTSP传输协议为tcp传输模式
            grabber.setOption("rtsp_transport", "tcp");
            /*
             * rtsp_flags:[filter_src,prefer_tcp,listen]
             * filter_src:仅接受来自协商对等地址和端口的数据包。
             * prefer_tcp:如果TCP可用作RTSP RTP传输，请首先尝试使用TCP进行RTP传输。
             * listen:充当rtsp服务器，监听rtsp连接
             * rtp传输首选使用tcp传输模式
             */
            grabber.setOption("rtsp_flags", "prefer_tcp");
            /*
             * 设置等待传入连接最大超时时间（单位：秒），默认值-1无限等待
             * 如果设置此选项，上面的rtsp_flags配置将被设置成“listen”，充当rtsp服务器，监听rtsp连接
             */
//			grabber.setOption("timeout","30");

            //socket网络超时时间
            grabber.setOption("stimeout","3000000");

            //设置要缓冲以处理重新排序的数据包的数据包数量
//			grabber.setOption("reorder_queue_size","");

            //设置本地最小的UDP端口，默认为5000端口。
//			grabber.setOption("min_port","5000");
            //设置本地最大的UDP端口，默认为65000端口。
//			grabber.setOption("max_port","65000");
        }
        grabber.start();

        CanvasFrame canvas = new CanvasFrame("视频预览");// 新建一个窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Frame f = null;
        // 只抓取图像画面，grabber.grab()会包含音视频
        for (;(f = grabber.grabImage()) != null;) {
            canvas.showImage(f);
        }

        grabber.close();//close包含stop和release方法
        canvas.dispose();
    }

    public static void main(String[] args) throws Exception, org.bytedeco.javacv.FrameGrabber.Exception {
        //海康/大华等摄像机的rtsp地址：rtsp://user:password@192.168.16.102:554/Streaing/Channels/1
        //海康/大华等视频平台的rtsp地址：rtsp://192.168.16.6:554/openUrl/6rcShva
        //读者需要换成自己的rtsp地址，或者使用外网测试rtsp：rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov
        new RtspGrabber().pushAndRecordHls("rtsp://10.122.100.146:29122/muck.ts");

    }

}
