package com.yoyoyt.downloadmanagerdemo;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity  {

    @Bind(R.id.file_name)
    TextView fileName;
    @Bind(R.id.pb_update)
    ProgressBar pbUpdate;
    @Bind(R.id.progress)
    TextView progress;
    @Bind(R.id.liji_down)
    TextView lijiDown;

    /**
     * 下载地址的链接
     */
    private String download_url="https://www.baidu.com/link?url=vGpAfBhn18x3w0HBk2VcrzP8BtSeLDD8hHMDTPMisYvmsQxDs3fjsCobMHZly8CwaG1hLA0D63aWUQzX2RVxSHh9-P77KMY1HcSUYgcbz0G&wd=&eqid=dd78202c0000f19c0000000258b3cbea";
    private DownloadManager downloadManager;
    private Timer timer;
    private TimerTask timerTask;
    long id;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int pro = bundle.getInt("pro");
            String name = bundle.getString("name");
            pbUpdate.setProgress(pro);
            progress.setText(String.valueOf(pro)+"%");
            fileName.setText(name);
        }
    };
    private DownloadManager.Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //得到下载的管理类
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //得到下载地址的请求
        request = new DownloadManager.Request(Uri.parse(download_url));
        //设置下载标题
        request.setTitle("QQ");
        //设置联网状态
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        //设置类型
        request.setMimeType("application/vnd.android.package-archive");
        //设置通知可见
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
        //设置文件存储路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS,"qq.apk");
        pbUpdate.setMax(100);
        //进行查询
        final DownloadManager.Query query = new DownloadManager.Query();
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                Cursor cursor = downloadManager.query(query.setFilterById(id));
                if (cursor!=null&&cursor.moveToFirst()){
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))==DownloadManager.STATUS_SUCCESSFUL){
                        pbUpdate.setMax(100);
                        install(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/qq.apk");
                        timerTask.cancel();
                    }
                    //得到标题
                    String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    //得到本地的uri路径
                    String local_uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    //得到字节总数
                    int byte_download = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    //得到下载的总大小
                    int byte_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int pro = (byte_download * 100) / byte_total;
                    //得到Message对象
                    Message message = Message.obtain();
                    //创建bundle对象
                    Bundle bundle = new Bundle();
                    bundle.putInt("pro",pro);
                    bundle.putString("name",title);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(timerTask,0,1000);

    }

    @OnClick(R.id.liji_down)
    public void onClickDownload(View v) {
        id=downloadManager.enqueue(request);
        timerTask.run();
        //设置按钮不可点击
        lijiDown.setClickable(false);
//        lijiDown.setBackgroundResource(R.drawable.btn);
    }

    /**
     * 安装路径
     * @param path
     */
    private void install(String path){
        //找到intent对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+path),"application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
