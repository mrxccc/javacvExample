package cn.mrxccc.javacv.example;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.IplImage;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;
import static org.bytedeco.opencv.global.opencv_core.cvClearMemStorage;

/**
 * @author mrxccc
 * @create 2020/12/15
 */
public class StreamingApp {
    public static void main(String[] args) throws Exception {

        CanvasFrame frame = new CanvasFrame("webcam");
        FrameGrabber grabber = new OpenCVFrameGrabber(0);

        grabber.setImageHeight(500);
        grabber.setImageWidth(500);
        grabber.start();

        Frame grabbedImage = grabber.grab();
        int width = grabbedImage.imageWidth;
        int height = grabbedImage.imageHeight;

        CvMemStorage storage = CvMemStorage.create();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtsp://ipaddress:portno/testVideoStream.3gp", width, height);



        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("rtsp");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoCodec(13);
        recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        recorder.setVideoBitrate(10 * 1024 * 1024);
        recorder.start();

        int i = 0;

        while (frame.isVisible() && (grabbedImage = grabber.grab()) != null) {
            System.out.println("(" + i++ + ") Invio. . .");
            frame.showImage(grabbedImage);
            recorder.record(grabbedImage);
        }

        cvClearMemStorage(storage);
        recorder.stop();
        grabber.stop();
    }
}
