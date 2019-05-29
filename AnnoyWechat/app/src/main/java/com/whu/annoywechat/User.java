package com.whu.annoywechat;

public class User {
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
     * 加密文字选项 是否加密
     */
    private boolean isDec;
    /**
     * 解密选项    是否解密
     * */

    /**
     * 定义好友列表的属组
     */
    private int BELONG;

    public void setBELONG(int BELONG) {
        this.BELONG = BELONG;
    }

    public int getBELONG() {
        return BELONG;
    }

    /**
     * 解密气泡选项
     */




    public void setId(int id) {
        this.id = id;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }

    public User() {
        this.isDec = false;
        this.isEnc = false;
        this.BELONG = -1;
    }

    public User(int id, String wxid, String note, String key, boolean isEnc, boolean isDec, int BELONG ) {
        this.id = id;
        this.key = key;
        this.note = note;
        this.wxid = wxid;
        this.publicKey = "--";
        this.isDec = isDec;     /** 默认为不加密信息*/
        this.isEnc = isEnc;     /** 默认为不解密气泡*/
        this.BELONG = BELONG;
    }


    public int getId() {
        return id;
    }

    public String getWxid() {
        return wxid;
    }

    public String getNote() {
        return note;
    }

    public String getKey() {
        return key;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean isEnc() {
        return isEnc;
    }

    public boolean isDec() {
        return isDec;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setEnc(boolean enc) {
        isEnc = enc;
    }

    public void setDec(boolean dec) {
        isDec = dec;
    }
}


