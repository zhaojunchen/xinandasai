package com.zhao.database;

public class DataSource {
    private int id;
    /**
     * id索引
     */

    private String wxid;
    /**
     * 微信ID
     */
    private String note;
    /**
     * 备注
     */

    private String key;
    /**
     * 对称加密的秘钥
     */
    private String publicKey;
    /**
     * 非对称加密的公钥
     */
    private boolean isEnc;
    /**
     * 加密文字选项
     */
    private boolean isDec;

    /**
     * 解密气泡选项
     */


    public DataSource(int id, String wxid, String note, String key, boolean isEnc, boolean isDec) {
        this.id = id;
        this.key = key;
        this.note = note;
        this.wxid = wxid;
        this.publicKey = "--";
        this.isDec = isDec;     /** 默认为不加密信息*/
        this.isEnc = isEnc;     /** 默认为不解密气泡*/
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDec(boolean dec) {
        this.isDec = dec;
    }

    public void setEnc(boolean enc) {
        this.isEnc = enc;
    }

    public boolean isEnc() {
        return this.isEnc;
    }

    public boolean isDec() {
        return this.isDec;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public String getNote() {
        return this.note;
    }

    public String getWxid() {
        return this.wxid;
    }

    public String getPublicKey() {
        return this.publicKey;
    }


}


