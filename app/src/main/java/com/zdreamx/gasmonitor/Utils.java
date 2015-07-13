package com.zdreamx.gasmonitor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 专门用户处理服务器JSON数据返回结构处理 解析 等问题
 * 根据 ashia 版本重新设计
 * Created by zdreamx on 2015/4/3.
 */
public class Utils {
    //每一个节点结构
    public static class NodeStruct {
        int Id;
        String Mobile; //主识别号 就是一个电话号码
        String Nick; //名称
        String Note;
        int Chongciperhz;
        //String GpsX; //暂时不用
        //String GpsY;
        String Createtime;
    }
    //服务器返回节点数情况
    public static class API_Return_Nodes {
        boolean Authorize; //就是返回结果是否正确 如果错误主要是用户名有问题
        NodeStruct[] Nodes;
    }
    //-------------------------------------------------------------
    //每一次设备发送的数据结构
    public static class Gaswellstat {
        long Id;
        String Wellid;    //井号ID 电话号码
        float Wellpress;  //井底流压
        float Welltemp;  //井底温度
        float Taopress;  //套压
        float Gasflow;   //气体工况流量
        float Gasstandflow;   //气体标况流量
        float Gastotalflow;   //气体总流量
        float Gastemperature;  //气体流量计温度
        float Gaspress;  //气体流量计压力
        float Liquidhigh; //液柱高度
        float Motorcurrent;   //电机电流
        float Motorvoltage;   //电机电压
        float Motortemp;   //电机温度 -->电机设定频率
        float Speed;  //电机转速或冲次
        float Motordcbus;   //电机DCBUS值
        float Waterflow;  //水流速
        float Watertotal;  //水的累计流量
        float Famenkaidu;  //阀门开度
        float Gpslongitude;   //经度
        float Gpslatitude;   //纬度
        //Time Createtime;    //go语言中的 time.Time 不知道java怎么表示
    }
    //向服务器请求一个数据(包含最新数据)时服务器返回的值
    public static class NodeDataReturnData {
        boolean Result;
        String Mytime; //服务器转换好的时间格式 2015-05-01 13:01:05
        Gaswellstat Data;
    }
    //服务器一次返回多个数据的结构
    public static class NodeDatasReturnDatas {
        boolean Result;
        int Count;
        NodeDataReturnData[] Datas;
    }

    //报警信息API JSON返回数据格式
    public static class ApiJsonWarnLogReturn {
        boolean  Result;             //计算结果
        int Numbers;              //井数量
        ApiJsonWarnLog[] Info; //具体每一口井的信息
    }

    public static class  ApiJsonWarnLog {
        int Logs;               //这口井的报警记录数据
        String Nick;            //昵称
        String Mobile;          //号码
        String LogTime;         //时间
        WarnParaLog LatestLog; //最新一条记录数据
    }

    public static class WarnParaLog {
        int Id;
        String Mobile;        //井号
        String Trigger;       //触发参数 显示触发条件 比如套压xx(范围xx-xx)
        String Message;       //详细消息 显示所有报警设置和情况
        boolean View;         //是否被查看
        String Createtime;    //创建时间
    }

    //获取当前应用的版本号：
    public static String getVersionName(Context ctx)
    {
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = ctx.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "versionName Error";
        }
    }
}
