package cn.mrxccc.javacv.example.imagepusher;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author mrxccc
 * @create 2020/12/14
 */
public class ImagePusher {
    FFmpegFrameGrabber grabber;
    FFmpegFrameRecorder recorder;
    CanvasFrame canvas;

    public static void main(String[] args) throws Exception {
        ImagePusher imagePusher = new ImagePusher()
                .from("D:\\develop\\ideaWorkSpace\\myproject\\javacvExample\\src\\main\\java\\cn\\mrxccc\\javacv\\example\\imagepusher\\alarm2.jpg", avcodec.AV_CODEC_ID_JPEG2000)
                .show("测试");
        imagePusher
                // rtmp
//                .to("rtmp://192.168.205.10:1935/stream/hello", imagePusher.grabber.getImageWidth(), imagePusher.grabber.getImageHeight())
                .to("rtsp://localhost/testVideoStream", imagePusher.grabber.getImageWidth(), imagePusher.grabber.getImageHeight(), "rtsp")
                .go(50000);
    }

    /**
     * 设置源
     *
     * @param input             流媒体源（inputstream流的形式）
     * @param address           流媒体地址（地址形式，input和address任意一个不为空，如果都为空则抛出异常）
     * @param imageMode         图像像素模式，默认color,gray和color会触发像素格式转换，只有RAW模式不会
     * @param width             转换视频宽度，不设置不会改变视频源宽度，设置后会进行转换
     * @param height            转换视频高度，不设置不会改变视频源高度，设置后会进行转换
     * @param imageScalingFlags 缩放
     * @param videoCodec        视频编码
     * @param pixelFormat       像素格式
     * @param audioCodec        音频编码
     * @param sampleFormat      音频采样格式（会触发重采样）
     * @param sampleRate        音频采样率（会触发重采样）
     * @param audioChannels     音频通道数（1-单声道，2-立体声）（会触发重采样）
     * @author eguid
     */
    public ImagePusher from(InputStream input, String address, FrameGrabber.ImageMode imageMode, int width, int height, int imageScalingFlags, int videoCodec, int pixelFormat, String audioCodecName, int sampleFormat, int sampleRate, int audioChannels) throws Exception {
        if (input != null) {
            grabber = new FFmpegFrameGrabber(input);
        } else if (address != null) {
            grabber = new FFmpegFrameGrabber(address);
        } else {
            throw new RuntimeException("Empty input!");
        }
        //只有设置ImageMode.RAW的时候才不会进行格式转换，设置成color和gray都会触发像素格式转换，默认是ImageMode.COLOR模式
//        grabber.setImageMode(imageMode);

        //如果不触发SwsContext像素格式转换，pixelFormat、width、height和imageScalingFlags参数不会起效果，
        grabber.setImageWidth(width);
        grabber.setImageHeight(height);
        grabber.setImageScalingFlags(imageScalingFlags);
        grabber.setPixelFormat(pixelFormat);//使用avutil中的像素格式常量，例如：avutil.AV_PIX_FMT_NONE

        //grabber.setFormat(format);//这个参数也是没有用的

        grabber.setVideoCodec(videoCodec);//使用avcodec中的编码常量，例如：avcodec.AV_CODEC_ID_NONE

        //grabber.setAudioCodec(audioCodec);//这个audioCodec字段在FFmpegFrameGrabber压根就没有用到，只用到了audioCodecName
        grabber.setAudioCodecName(audioCodecName);//如果为空，会触发自动检索音频编码
        /*设置下面三个参数会触发ffmpeg的swresample音频重采样*/
        //在对音频编码解码成pcm之后，如果sampleFormat与pcm不同，则会对音频采样格式进行转换
        grabber.setSampleFormat(sampleFormat);//音频采样格式,使用avutil中的像素格式常量，例如：avutil.AV_SAMPLE_FMT_NONE
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
        grabber.start();

        return this;
    }

    /**
     * 设置源（使用raw模式，不会进行像素格式转换）
     *
     * @param input      流媒体源
     * @param address    流媒体源地址
     * @param videoCodec 视频编码
     * @return
     * @throws Exception
     */
    public ImagePusher from(InputStream input, String address, int videoCodec) throws Exception {
        return from(input, address, FrameGrabber.ImageMode.RAW, 0, 0, 0, videoCodec, avutil.AV_PIX_FMT_NONE, null, avutil.AV_SAMPLE_FMT_NONE, 0, 0);
    }

    /**
     * 设置源
     *
     * @param input      源
     * @param videoCodec 视频格式
     * @return
     * @throws Exception
     */
    public ImagePusher from(String input, int videoCodec) throws Exception {
        return from(null, input, videoCodec);
    }

    /**
     * 设置输出
     *
     * @param outputStream      输出源（outputstream流形式）
     * @param output            输出地址（地址形式）
     * @param width             宽度
     * @param height            高度
     * @param videoCodecName    视频编码格式
     * @param videoCodec        视频编码id
     * @param pixelFormat       像素格式
     * @param imageScalingFlags 图像缩放比例
     * @param gopSize           gop间隔
     * @param frameRate         帧率
     * @param videoBitrate      比特率
     * @param videoQuality      视频质量
     * @param audioCodecName    音频编码名称
     * @param sampleFormat      音频采样格式
     * @param sampleRate        采样率
     * @param audioChannels     音频通道（1-单声道，2-立体声）
     * @author eguid
     */
    public ImagePusher to(OutputStream outputStream, String output, Integer width, Integer height, String videoCodecName, int videoCodec, int pixelFormat, int imageScalingFlags, int gopSize, double frameRate, int videoBitrate, double videoQuality, String audioCodecName, int sampleFormat, int sampleRate, int audioChannels, String format) throws org.bytedeco.javacv.FrameRecorder.Exception {
        if (outputStream == null) {
            recorder = new FFmpegFrameRecorder(output, width, height, audioChannels);
        } else if (output != null) {
            recorder = new FFmpegFrameRecorder(outputStream, width, height, audioChannels);
        } else {
            throw new RuntimeException("output media is null!");
        }
        recorder.setOption("rtsp_transport","tcp");
        recorder.setVideoCodecName(videoCodecName);//优先级高于videoCodec
        recorder.setVideoCodec(videoCodec);//只有在videoCodecName没有设置或者没有找到videoCodecName的情况下才会使用videoCodec
        recorder.setFormat(format);//只支持flv，mp4，3gp和avi四种格式，flv:AV_CODEC_ID_FLV1;mp4:AV_CODEC_ID_MPEG4;3gp:AV_CODEC_ID_H263;avi:AV_CODEC_ID_HUFFYUV;
        recorder.setPixelFormat(pixelFormat);// 只有pixelFormat，width，height三个参数任意一个不为空才会进行像素格式转换
        recorder.setImageScalingFlags(imageScalingFlags);
        recorder.setGopSize(gopSize);
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(videoBitrate);
        recorder.setVideoQuality(videoQuality);
        //下面这三个参数任意一个会触发音频编码
//        recorder.setSampleFormat(sampleFormat);//音频采样格式,使用avutil中的像素格式常量，例如：avutil.AV_SAMPLE_FMT_NONE
//        recorder.setAudioChannels(audioChannels);
//        recorder.setSampleRate(sampleRate);
        recorder.start();
        return this;
    }

    public ImagePusher to(String output, Integer width, Integer height, double frameRate, String format) throws org.bytedeco.javacv.FrameRecorder.Exception {
        return to(null, output, width, height, null, avcodec.AV_CODEC_ID_NONE, avutil.AV_PIX_FMT_NONE, 0, (int) (frameRate * 2), frameRate, -1, -1, null, avutil.AV_SAMPLE_FMT_NONE, 0, 0, format);
    }

    public ImagePusher to(String output, Integer width, Integer height) throws org.bytedeco.javacv.FrameRecorder.Exception {
        return to(output, width, height, 25, "flv");
    }

    public ImagePusher to(String output, Integer width, Integer height, String format) throws org.bytedeco.javacv.FrameRecorder.Exception {
        return to(output, width, height, 25, format);
    }

    /**
     * 显示预览窗口
     *
     * @param title
     * @return
     */
    public ImagePusher show(String title) {
        canvas = new CanvasFrame(title == null ? "预览" : title);// 新建一个图像预览窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        canvas.setVisible(true);
        return this;
    }

    /**
     * 推送单张图片
     *
     * @param image        图片地址
     * @param pushDuration 推送持续时长
     * @author eguid
     */
    public void go(long pushDuration) throws org.bytedeco.javacv.FrameRecorder.Exception, Exception, InterruptedException {
        Frame frame = null;
        boolean hasShow = (canvas != null);
        // 由于图片只有一帧，重复调用grab自然会抛出异常，所以只调用一次
        if (grabber != null) {
            frame = grabber.grabImage();
        }
        long startTime = System.currentTimeMillis();
        long endTime = -1;
        for (int i = 0; (!hasShow || canvas.isVisible()) && frame != null; i++) {
            if (hasShow) {
//                System.out.println("第" + i + "帧");
                canvas.showImage(frame);
            }
//            if (endTime - startTime > pushDuration) {
//                break;
//            }

            if (recorder != null) {
                recorder.record(frame);
                frame.timestamp += 100;
            }
            endTime = System.currentTimeMillis();
        }
        System.out.println("录制结束，持续时长：" + (endTime - startTime));

        close();//回收内存
    }

    public void close() throws org.bytedeco.javacv.FrameRecorder.Exception, Exception {
        if (canvas != null) {
            canvas.dispose();
        }

        if (recorder != null) {
            recorder.close();
        }

        if (grabber != null) {
//            grabber.close();//由于close调用了stop喝release，而stop也是调用release，为了防止重复调用，直接使用release()
            grabber.release();
        }
    }


}
