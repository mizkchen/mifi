package me.kingka.service;

/**
 * 数据块
 *
 * @author swift.apple
 */
public class Block {
    /**
     * 数据块在文件中的偏移
     */
    public long offset = 0;
    /**
     * 数据块大小
     */
    public long length = 0;
    /**
     * 是否第一个数据块
     */
    public boolean first = false;

    public Block() {

    }

}