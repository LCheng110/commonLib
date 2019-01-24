package cn.jpush.im.android.utils.filemng;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.utils.BitmapUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;

public class FastDfSUploader implements IPrivateCloudUploader {
    private static final String TAG = "FastDfSUploader";
//    private static final ExecutorService encoderThreadPool = Executors.newFixedThreadPool(1);
//    private static boolean isFFmpegBinaryLoaded = false;

    private PrivateCloudUploadManager manager;
    private Object fastDFSUtil = null;
    //    private Object ffmpeg = null;
    private Method uploadWithMasterMethod = null;
    private Method uploadWithNewClientMethod = null;
    private Method deleteMethod = null;
    //    private Method executeShellMethod = null;
    private long totalBytes = 0;
    private long transferedBytes = 0;
    private String fileFormat;

    FastDfSUploader(PrivateCloudUploadManager manager) {
        this.manager = manager;
        try {
            Class fastDFSUtilCls = Class.forName("cn.jiguang.im.fastdfs.FastDFSUtil");
            fastDFSUtil = fastDFSUtilCls.getConstructor(String.class, int.class, int.class, String.class, int.class, String.class, int.class,String.class)
                    .newInstance(JMessage.fastDfsTrackerHost, JMessage.fastDfsTrackerPort, JMessage.fastDfsTrackerHttpPort,
                            JMessage.customStorageHostForUpload, JMessage.customStoragePortForUpload,
                            JMessage.customStorageHostForDownload, JMessage.customStoragePortForDownload,JMessage.customStoragePrefixForDownload);

//            Class ffmpegCls = Class.forName("com.github.hiteshsondhi88.libffmpeg.FFmpeg");
//            Method getInstanceMethod = ffmpegCls.getDeclaredMethod("getInstance", Context.class);
//            ffmpeg = getInstanceMethod.invoke(null, JMessage.mContext);
            uploadWithNewClientMethod = fastDFSUtilCls.getDeclaredMethod("upFastdfsFileWithNewClient", byte[].class, String.class);
            uploadWithMasterMethod = fastDFSUtilCls.getDeclaredMethod("upFastdfsFile", String.class, String.class, String.class, byte[].class, String.class);
            deleteMethod = fastDFSUtilCls.getDeclaredMethod("deleteFastdfsFile", String.class, String.class);

//            if (!isFFmpegBinaryLoaded) {
//                Object result = ffmpegCls.getDeclaredMethod("loadBinarySync").invoke(ffmpeg);
//                isFFmpegBinaryLoaded = (Boolean) result;
//                Logger.d(TAG, "load ffmpeg binrary result = " + isFFmpegBinaryLoaded);
//            }
//            executeShellMethod = ffmpegCls.getDeclaredMethod("executeSync", String[].class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doUpload() {
        byte[] data;
        List<SubData> subDatas = null;
        switch (manager.contentType) {
            case image:
                //如果是图片类型的消息，需要在本地生成几种大小的缩略图一起上传
                data = FileUtil.File2byte(manager.file.getAbsolutePath());
                BitmapFactory.Options options = new BitmapFactory.Options();
                if (manager.fileFromMsg) {
                    ImageContent imageContent = (ImageContent) manager.mediaContent;
                    Logger.d(TAG, "image content width is " + imageContent.getWidth() + " height is " + imageContent.getHeight());
                    options.outWidth = imageContent.getWidth();
                    options.outHeight = imageContent.getHeight();
                    subDatas = generateSubDatasForImage(data, options);
                } else {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    Logger.d(TAG, "image width is " + options.outWidth + " height is " + options.outHeight);
                    subDatas = generateSubDatasForImage(data, options);
                }
                fileFormat = manager.fileFormat;
                break;
            case voice:
                fileFormat = "mp3";//fastDFS上传语音时，默认在fileExt中加上".mp3"后缀，保持和公有云行为一致。
                data = FileUtil.File2byte(manager.file.getAbsolutePath());
                //语音文件需要本地转码成mp3之后再上传
//                encodeMp3AndUpload();
                break;
            default:
                //其他类型的文件，直接将file转成byte之后上传
                fileFormat = manager.fileFormat;
                data = FileUtil.File2byte(manager.file.getAbsolutePath());

        }
        uploadInternal(data, subDatas);
    }

    private void uploadInternal(byte[] masterData, List<SubData> subDatas) {
        if (null == masterData || masterData.length <= 0) {
            Logger.ww(TAG, "data been uploaded should not be empty. return from uploadInternal");
            manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
            return;
        }

        boolean isUploadSuccess;
        String mediaId;
        String newResourceId = null;

        totalBytes = masterData.length;
        if (null != subDatas && !subDatas.isEmpty()) {
            for (FastDfSUploader.SubData subData : subDatas) {
                totalBytes += subData.datas.length;
            }
        }

        String[] result = (String[]) uploadFile(masterData, fileFormat);
        if (null != result && result.length > 1) {
            String groupName = result[0];
            String masterFileName = result[1];
            Logger.d(TAG, "group name = " + result[0] + " master file name = " + result[1]);
            newResourceId = groupName + File.separator + masterFileName;
            //update progress.
            updateProgress(masterData.length);
            isUploadSuccess = true;

            if (null != subDatas && !subDatas.isEmpty()) {
                for (FastDfSUploader.SubData subData : subDatas) {
                    Object subResult = uploadFile(groupName, masterFileName, subData.suffixName, subData.datas, fileFormat);
                    if (null != subResult && 0 != Integer.valueOf(((String[]) subResult)[2])) {
                        Logger.d(TAG, "sub file upload failed,delete the master file");
                        deleteFile(groupName, masterFileName);
                        isUploadSuccess = false;
                        break;
                    } else {
                        //update progress.
                        updateProgress(subData.datas.length);
                    }
                    Logger.d(TAG, "upload subData " + subData + " success .");
                }
            }

        } else {
            //master file upload failed
            isUploadSuccess = false;
        }

        if (isUploadSuccess) {
            if (manager.fileFromMsg) {
                mediaId = PrivateCloudUploadManager.PROVIDER_FASTDFS + manager.mediaContent.getMediaID();
                mediaId = mediaId.replace(manager.resourceID, newResourceId);
                manager.mediaContent.setMediaID(mediaId);
            } else {
                mediaId = PrivateCloudUploadManager.PROVIDER_FASTDFS + File.separator + manager.contentType + File.separator +
                        Consts.PLATFORM_ANDROID + File.separator + newResourceId;
            }
            manager.doCompleteCallbackToUser(true, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, mediaId);
        } else {
            manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
        }
        Logger.d(TAG, "upload all finish .");
    }

    private Object uploadFile(byte[] data, String fileExt) {
        try {
            if (null != uploadWithNewClientMethod) {
                return uploadWithNewClientMethod.invoke(fastDFSUtil, data, fileExt);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object uploadFile(String group_name, String master_filename, String prefix_name, byte[] data, String fileExt) {
        try {
            if (null != uploadWithMasterMethod) {
                return uploadWithMasterMethod.invoke(fastDFSUtil, group_name, master_filename, prefix_name, data, fileExt);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object deleteFile(String groupName, String fileName) {
        try {
            if (null != deleteMethod) {
                return deleteMethod.invoke(fastDFSUtil, groupName, fileName);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

//    private Object executeShell(String[] cmd) {
//        try {
//            if (null != executeShellMethod) {
//                return executeShellMethod.invoke(ffmpeg, (Object) cmd);
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private void updateProgress(long byteLength) {
        if (manager.fileFromMsg) {
            transferedBytes += byteLength;
            double percent = transferedBytes / (totalBytes * 1.0);
            String targetID = manager.message.getTargetID();
            int msgID = manager.message.getId();
            String targetAppkey = manager.message.getTargetAppKey();
            FileUploader.updateProgressInCache(targetID, targetAppkey, msgID, percent);
            CommonUtils.doProgressCallbackToUser(targetID, targetAppkey, msgID, percent);
        }
    }

    private List<SubData> generateSubDatasForImage(byte[] masterDatas, BitmapFactory.Options originOpt) {
        List<FastDfSUploader.SubData> result = new ArrayList<SubData>();
        fillSubDataList(result, masterDatas, originOpt, PrivateCloudUploadManager.DENSITY_HDPI, PrivateCloudUploadManager.THUMB_HDPI);
        fillSubDataList(result, masterDatas, originOpt, PrivateCloudUploadManager.DENSITY_XHDPI, PrivateCloudUploadManager.THUMB_XHDPI);
        fillSubDataList(result, masterDatas, originOpt, PrivateCloudUploadManager.DENSITY_XXHDPI, PrivateCloudUploadManager.THUMB_XXHDPI);
        Logger.d(TAG, "generated subDatas = " + result);
        return result;
    }

    private void fillSubDataList(List<SubData> subDataList, byte[] originData, BitmapFactory.Options originOpt, float targetDensity, String thumbSuffix) {
        int sampleSize = BitmapUtils.computeSampleSize(originOpt, targetDensity, BitmapUtils.TUMBNAIL_ORIGIN_EDGE);
        FastDfSUploader.SubData subData;
        BitmapFactory.Options thumbOpt = new BitmapFactory.Options();
        thumbOpt.outHeight = originOpt.outHeight;
        thumbOpt.outWidth = originOpt.outWidth;
        thumbOpt.inSampleSize = sampleSize;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.decodeByteArray(originData, 0, originData.length, thumbOpt).compress(Bitmap.CompressFormat.PNG, 100, stream);
        subData = new FastDfSUploader.SubData();
        subData.datas = stream.toByteArray();
        subData.suffixName = thumbSuffix;
        subDataList.add(subData);
    }

//    //将传进来的语音文件转码成mp3格式之后再上传。
//    private void encodeMp3AndUpload() {
//        //因为ffmpeg只能单个命令串行执行，所以将转码任务放到一个单线程池中去依次执行
//        Task.call(new Callable<Object>() {
//            @Override
//            public Object call() throws Exception {
//                byte[] encodedBytes = null;
//                String[] cmd = new String[3];
//                cmd[0] = "-i";//ffmpeg input file cmd
//                cmd[1] = manager.file.getAbsolutePath(); // input file path
//                File outMp3File = new File(FileUtil.getCacheDirectory(JMessage.mContext, true) + File.separator + "tempVoice"
//                        + System.currentTimeMillis() + new Random().nextInt(1000) + ".mp3"); //output file
//                cmd[2] = outMp3File.getAbsolutePath();
//
//                Boolean result = (Boolean) executeShell(cmd);
//                if (result) {
//                    encodedBytes = FileUtil.File2byte(outMp3File.getAbsolutePath());
//                }
//                outMp3File.delete();
//                uploadInternal(encodedBytes, null);
//                return null;
//            }
//        }, encoderThreadPool);
//    }

    private static class SubData {
        byte[] datas;
        String suffixName;

        @Override
        public String toString() {
            return "SubData{" +
                    "datas length =" + datas.length +
                    ", suffixName='" + suffixName + '\'' +
                    '}';
        }
    }
}
