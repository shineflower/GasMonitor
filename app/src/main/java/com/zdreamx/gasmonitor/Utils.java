package com.zdreamx.gasmonitor;

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
}
