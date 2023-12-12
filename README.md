#开发工具类

#### 主要是封装一些操作较麻烦的API，提供一些实用的小工具 

>* Redis 封装Jedis，使用需查看JedisConf配置使用，使用连接池，支持哨兵、集群、单点，默认采用GenericFastJsonRedisSerializer存储，可按自行更换
>* UserHelper 接入Hoxinte研发单点登录，标准Restful
>* OSSHelper 接入阿里OSS
>* AwsS3Helper 接入AWS S3
>* MailHelper 邮件服务，可使用Redis队列发送
>* BaseUtil Bean基础操作，极大减少重复代码～
>* LoadUtil 读取配置文件工具，摆脱Spring，静态的方式读取配置参数
>* CalculateUtil 日期处理工具
>* DataSourceUtil Mysql连接工具，短时连接三方数据库很方便
>* HttpUtil Http工具 各种Http请求的发送解析
>* HexUtil Hex转换工具，可用作二进制存储
>* JsonUtil Json工具，结合alibaba.fastjson，简化使用，方便取值
>* RandomUtil 随机工具，常用的各种随机生成，有一个"小"雪花算法，因为没有带机器码，非单点使用需注意

*正在添加支付宝、微信支付、ChatGPT、SSE ...*
