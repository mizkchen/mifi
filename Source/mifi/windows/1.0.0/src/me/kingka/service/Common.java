package me.kingka.service;

/**
 * 通用常量
 * @author swift.apple
 */
public interface Common {
    /**
     * 数据块大小
     */
    int BLOCK_SIZE = 1048576;
    /**
     * 缓冲区大小
     */
    int BUFFER_SIZE = 10485760;

    /**
     * 获得文件列表
     */
    String LIST_FILES = "1";
    /**
     * 下载文件
     */
    String DOWNLOAD_FILES = "2";
    /**
     * 上传文件
     */
    String UPLOAD_FILES = "3";
    /**
     * 刷新上传缓存
     */
    String FLUSH_UPLOAD_CACHE = "4";

    /**
     * 操作成功
     */
    int SUCCESS = 0;
    /**
     * 密码错误
     */
    int PASSWORD_INVALID = 1;
    /**
     * 缺少参数
     */
    int PARAM_IS_MISSING = 2;
    /**
     * 服务器没有可用磁盘
     */
    int NO_FREE_SPACE = 3;
    /**
     * 服务器没有可用缓冲区
     */
    int NO_ENOUGH_BUFF = 4;
    /**
     * 文件不存在
     */
    int FILE_NOT_FOUND = 5;
    /**
     * 不包含文件数据
     */
    int FILE_NO_DATA = 6;
    /**
     * 下载文件失败
     */
    int FILE_UPLOAD_FAIL = 7;
}
