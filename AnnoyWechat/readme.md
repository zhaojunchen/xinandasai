# 信安大赛作品AnnotWechat使用说明文档
## 1.环境配置及其安装
### 1. **概述说明**
本作品的运行环境为Android环境,理论上支持VirtualXposed软件的手机均可正常运行本作品。
### 2. **VirtualXposed安装**
以上软件简称vxp（[详细介绍请参见网址](https://vxposed.com/)）,vxp旨在为程序运行提供root环境，作品基于Xposed框架，vxp出现之前Xposed模块的安装和运行的就必须先root Android。vxp就是提供了这样的一个root虚拟环境，但是vxp适配的android版本不是很全面（Android 7支持比较完善，android目前部分支持，但是这个项目目前还是比较有活力的），部分机型无法使用。理论上支vxp的软件设备就支持作品的运行环境。
***安装部分***: 打开[官网下载页面](https://github.com/android-hacker/VirtualXposed/releases/tag/v0.17.3)下载最新版的vxp软件，正常安装即可。注：无法安装的手机在安装时可能出现闪退情况。

![下载界面](http://ww2.sinaimg.cn/large/006tNc79gy1g3ruhirie2j30ty0l3tc6.jpg)
    
### 3. ***安装微信和作品AnnoyWechat***

作品的apk在项目里面（[点击传送门](https://github.com/zhaojunchen/xinandasai/tree/master/AnnoyWechat/app/release)）,下载微信（截止座屏提交日期，微信的版本号早7.04，建议更新到最新版本7.0.4）。 上述的软件正常安装即可。
   
    
- 添加apk进入vxp
    ![进入设置界面](http://ww2.sinaimg.cn/large/006tNc79gy1g3ruqss2q5j30u01hcadq.jpg)
        
    ![点击进入添加界面](http://ww1.sinaimg.cn/large/006tNc79gy1g3rus6fxukj30u01hctdb.jpg)
       
    ![在列表里面添加AnnoyWechat和微信](http://ww3.sinaimg.cn/large/006tNc79gy1g3ruvypydtj30u01hc10b.jpg)
       
      
    ``` 
    接着在界面就可以看到这连个apk，现在登录微信保存一下初始信息。然后      完整退出即可。
    ```
        
 - 点击设置界面->模块管理->AnnoyWechat   打上勾。
 - 退回到设置界面  点击设置界面->重启
 - 重新登录微信（Annoywechat可以不用打开）
     
#      ** 注意点：首次安装必须先登录微信、完全退出后再来打开模块 以保存初始化信息 当切换用户时必须必须重复上述步骤  当然除了账户切换和首次安装安装软件需要这样做、其他的时候不需要这样做**

## 2.建立加密连接
### 1.发送建立连接
`在双方那个安装了vxp、wechat、AnnoyWechat的情况下`
- 建立连接的一方（简称发送方A）在输入框和对方 `建立连接` ，A的消息监听会接受消息并处理，在双方未建立连接的情况下，将A的SM2公钥发送对方发送给对方。

    ![建立连接](http://ww2.sinaimg.cn/large/006tNc79gy1g3rvpeaspzj30c00gw411.jpg)
    
    ![处理挑战](http://ww1.sinaimg.cn/large/006tNc79gy1g3rvrz065nj30ch0cp0w6.jpg)
    
    
    


- 接收方（简称接收方B）监听来自A的消息（根据wxid鉴别身份）识别特殊格式的A的公钥，添加数据库，生成随机的SM4消息加密的秘钥，显示在B自己的气泡界面。

  ![B监听并处理消息](http://ww3.sinaimg.cn/large/006tNc79gy1g3rvz5qtobj30b90jamzg.jpg)

 
- B将消息copy一下发送给A，A的消息机制识别特殊格式的消息（A方SM2公钥加密的MS4秘钥），A方收到消息后用自己的SM2私钥解密，添加AB协商的SM4秘钥添加的A的数据库。建立SM4消息加密解密机制。
  ![copy一下](http://ww4.sinaimg.cn/large/006tNc79gy1g3rw1jdiwhj30ag0i3af6.jpg)
  
- AB此时成功的建立了连接  B发送的文字被替换为`基于Xposed的加密通信已经建立成功`
![基于Xposed的连接已建立成功](http://ww4.sinaimg.cn/large/006tNc79gy1g3rw56dzwjj30c609qq5w.jpg)

- A再次发送`建立连接` 确认是否？  （不是必要的）此时A会查询自己的数据库查看是否有SM4在库，确认下一步的消息。

- ***至此加密的连接建立成功***

## 2.正式加密通讯
测试结果在文档已经详细介绍。
![](http://ww3.sinaimg.cn/large/006tNc79gy1g3rw9s23dfj30u01hcq9x.jpg) 发现加密解密正常。

当关闭模块后，正常的消息会议prefix（@-------）+sm4密文的形式显示。
## 3.说明
1. 消息加密只会针对已经建立连接的好友，对朋友圈、非加密好友、及搜索等输入均不会起作用，保证了其他功能的正常使用
2. 消息加只针对文本消息，对语音、图片、视屏通话、音频通话无任何干扰
3. 程序在首次运行微信（vxp首次运行微信）切换微信用户必须先只登陆、然后退出。模块打开和关闭时需重启一下(设置界面->重启)
4. 编写程序时作者尽可能去优化一些(通过空间局部性等特点用变量保存user信息减少数据库的查询)，但是由于某些操作仍会较长的时间操作(特别是秘钥查询)。
5. 程序优化和逻辑完整性有待提升，主界面有待添加和美化。由于时间紧迫暂时放弃了界面的美化和信息显示功能。
6. 小组目前有了加密图片的思路。但是操作比较复杂，为来的及完善。








    
    
    
    
    
    
    

    
