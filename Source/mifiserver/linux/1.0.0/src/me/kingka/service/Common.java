package me.kingka.service;

/**
 * 通用常量
 */
public interface Common {

    String LIST_FILES = "1";//fetch files
    String DOWNLOAD_FILES = "2";// download file from server
    String UPLOAD_FILES = "3";// upload file to server
    String FLUSH_UPLOAD_CACHE = "4";//flush upload cache

    int SUCCESS = 0;
    int PASSWORD_INVALID = 1;
    int PARAM_IS_MISSING = 2;
    int NO_FREE_SPACE = 3;
    int NO_ENOUGH_BUFF = 4;
    int FILE_NOT_FOUND = 5;
    int FILE_NO_DATA = 6;
    int FILE_UPLOAD_FAIL = 7;
}
