-- 创建数据库
CREATE DATABASE IF NOT EXISTS secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE secondhand;

-- 1.用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`     VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`     VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname`     VARCHAR(50) COMMENT '昵称',
    `avatar`       VARCHAR(255) COMMENT '头像URL',
    `phone`        VARCHAR(20) COMMENT '手机号',
    `email`        VARCHAR(100) COMMENT '邮箱',
    `credit_score` INT        DEFAULT 100 COMMENT '信用分',
    `role`         TINYINT(1) DEFAULT 0 COMMENT '角色: 9-管理员 0-用户',
    `create_time`  DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 2 .地址表
CREATE TABLE IF NOT EXISTS `address`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
    `receiver_name`  VARCHAR(50)  NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20)  NOT NULL COMMENT '收货人电话',
    `province`       VARCHAR(50)  NOT NULL COMMENT '省份',
    `city`           VARCHAR(50)  NOT NULL COMMENT '城市',
    `district`       VARCHAR(50)  NOT NULL COMMENT '区/县',
    `detail`         VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default`     TINYINT(1) DEFAULT 0 COMMENT '是否默认地址',
    `create_time`    DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='地址表';

-- 3.商品表
CREATE TABLE IF NOT EXISTS `product`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id`     BIGINT         NOT NULL COMMENT '发布者ID',
    `title`       VARCHAR(100)   NOT NULL COMMENT '商品标题',
    `description` TEXT COMMENT '商品描述',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `category_id` INT            NOT NULL COMMENT '分类ID',
    `status`      TINYINT(1) DEFAULT 1 COMMENT '状态：1-在售 2-已售 3-下架',
    `conditions`   TINYINT(1) DEFAULT 1 COMMENT '商品成色：1-全新 2-几乎全新 3-轻微使用痕迹 4-正常使用痕迹 5-明显使用痕迹',
    `view_count`  INT        DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

-- 4.商品图片表
CREATE TABLE IF NOT EXISTS `product_image`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `image_url`   VARCHAR(255) NOT NULL COMMENT '图片URL',
    `sort`        INT      DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品图片表';

-- 5.商品分类表
CREATE TABLE IF NOT EXISTS `category`
(
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`   INT      DEFAULT 0 COMMENT '父分类ID',
    `sort`        INT      DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品分类表';

-- 6.订单表
CREATE TABLE IF NOT EXISTS `orders`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`       VARCHAR(50)    NOT NULL COMMENT '订单编号',
    `buyer_id`       BIGINT         NOT NULL COMMENT '买家ID',
    `seller_id`      BIGINT         NOT NULL COMMENT '卖家ID',
    `product_id`     BIGINT         NOT NULL COMMENT '商品ID',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
    `status`         TINYINT(1)   DEFAULT 1 COMMENT '状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消',
    `address_id`     BIGINT         NOT NULL COMMENT '收货地址ID',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT(1)   DEFAULT 0 COMMENT '是否删除',
    `payment_method` TINYINT(1)   DEFAULT NULL COMMENT '支付方式: 1-支付宝 2-微信支付 3-银行卡',
    `payment_status` TINYINT(1)   DEFAULT 1 COMMENT '支付状态: 1-待支付 2-支付成功 3-支付失败',
    `payment_time`   DATETIME     DEFAULT NULL COMMENT '支付时间',
    `transaction_no` VARCHAR(64)  DEFAULT NULL COMMENT '支付交易号',
    `message`        VARCHAR(255) DEFAULT NULL COMMENT '订单留言',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_address_id` (`address_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- 7.评价表
CREATE TABLE IF NOT EXISTS `comment`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id`    BIGINT NOT NULL COMMENT '订单ID',
    `user_id`     BIGINT NOT NULL COMMENT '评价用户ID',
    `product_id`  BIGINT NOT NULL COMMENT '商品ID',
    `content`     TEXT COMMENT '评价内容',
    `rating`      INT    NOT NULL COMMENT '评分：1-5星',
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评价表';

-- 8.收藏表
CREATE TABLE IF NOT EXISTS `favorite`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

-- 9.消息表
CREATE TABLE IF NOT EXISTS `message`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id`   BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content`     TEXT   NOT NULL COMMENT '消息内容',
    `is_read`     TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';

-- 10.系统通知表
CREATE TABLE IF NOT EXISTS `system_notification`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `content`     TEXT   NOT NULL COMMENT '通知内容',
    `is_read`     TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    `create_time` DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统通知表';

-- 11.推荐表
CREATE TABLE IF NOT EXISTS `recommendation`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='推荐表';

-- 插入测试数据
-- 1. 插入用户数据
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `credit_score`, `role`)
VALUES ('user1', '3b32279c2483cea75a5f7c219d25a331', '张三', '/images/avatars/f1515b626ca44eae8d9a4fd96423544f.jpg',
        '13800138001', 'user1@example.com', 100, 0),
       ('user2', '3b32279c2483cea75a5f7c219d25a331', '李四', '/images/avatars/f1515b626ca44eae8d9a4fd96423544f.jpg',
        '13800138002', 'user2@example.com', 95, 1),
       ('user3', '3b32279c2483cea75a5f7c219d25a331', '王五', '/images/avatars/f1515b626ca44eae8d9a4fd96423544f.jpg',
        '13800138003', 'user3@example.com', 90, 1),
       ('user4', '3b32279c2483cea75a5f7c219d25a331', '赵六', '/images/avatars/avatar4.jpg', '13800138004',
        'user4@example.com', 98, 0),
       ('user5', '3b32279c2483cea75a5f7c219d25a331', '孙七', '/images/avatars/avatar5.jpg', '13800138005',
        'user5@example.com', 92, 0),
       ('user6', '3b32279c2483cea75a5f7c219d25a331', '周八', '/images/avatars/avatar6.jpg', '13800138006',
        'user6@example.com', 85, 0),
       ('user7', '3b32279c2483cea75a5f7c219d25a331', '吴九', '/images/avatars/avatar7.jpg', '13800138007',
        'user7@example.com', 95, 0),
       ('user8', '3b32279c2483cea75a5f7c219d25a331', '郑十', '/images/avatars/avatar8.jpg', '13800138008',
        'user8@example.com', 100, 0),
       ('user9', '3b32279c2483cea75a5f7c219d25a331', '钱十一', '/images/avatars/avatar9.jpg', '13800138009',
        'user9@example.com', 90, 9),
       ('user10', '3b32279c2483cea75a5f7c219d25a331', '孙十二', '/images/avatars/avatar10.jpg', '13800138010',
        'user10@example.com', 93, 0),
       ('user11', '3b32279c2483cea75a5f7c219d25a331', '李十三', '/images/avatars/avatar11.jpg', '13800138011',
        'user11@example.com', 97, 0),
       ('user12', '3b32279c2483cea75a5f7c219d25a331', '王十四', '/images/avatars/avatar12.jpg', '13800138012',
        'user12@example.com', 89, 0),
       ('user13', '3b32279c2483cea75a5f7c219d25a331', '张十五', '/images/avatars/avatar13.jpg', '13800138013',
        'user13@example.com', 94, 0);

-- 2. 插入地址数据
INSERT INTO `address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`,
                       `is_default`)
VALUES (1, '张三', '13800138001', '北京市', '北京市', '海淀区', '中关村大街1号', 1),
       (1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '建国路88号', 0),
       (2, '李四', '13800138002', '上海市', '上海市', '浦东新区', '陆家嘴1号', 1),
       (3, '王五', '13800138003', '广东省', '广州市', '天河区', '天河路1号', 1);
-- 插入地址测试数据
INSERT INTO `address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`,
                       `is_default`)
VALUES
-- 用户1（已有2条，新增3条）
(1, '张三', '13800138001', '北京市', '北京市', '西城区', '金融街15号', 0),
(1, '张三', '13800138001', '北京市', '北京市', '丰台区', '科技园路20号', 0),
(1, '张三', '13800138001', '河北省', '石家庄市', '长安区', '建设北大街8号', 0),

-- 用户2（已有1条，新增3条）
(2, '李四', '13800138002', '上海市', '上海市', '静安区', '南京西路1266号', 0),
(2, '李四', '13800138002', '江苏省', '苏州市', '工业园区', '星湖街328号', 1),
(2, '李四', '13800138002', '浙江省', '杭州市', '西湖区', '文三路369号', 0),

-- 用户3（已有1条，新增3条）
(3, '王五', '13800138003', '广东省', '深圳市', '南山区', '科技南一路10号', 0),
(3, '王五', '13800138003', '广东省', '珠海市', '香洲区', '情侣中路88号', 0),
(3, '王五', '13800138003', '湖南省', '长沙市', '岳麓区', '麓山南路2号', 1),

-- 用户4（新增2条）
(4, '赵六', '13800138004', '四川省', '成都市', '锦江区', '春熙路1号', 1),
(4, '赵六', '13800138004', '重庆市', '重庆市', '渝中区', '解放碑民权路28号', 0),

-- 用户5（新增2条）
(5, '孙七', '13800138005', '湖北省', '武汉市', '武昌区', '中北路1号', 1),
(5, '孙七', '13800138005', '陕西省', '西安市', '雁塔区', '小寨西路26号', 0),

-- 用户6（新增2条）
(6, '周八', '13800138006', '福建省', '厦门市', '思明区', '环岛南路1号', 1),
(6, '周八', '13800138006', '江苏省', '南京市', '鼓楼区', '中山北路1号', 0),

-- 用户7（新增2条）
(7, '吴九', '13800138007', '天津市', '天津市', '和平区', '南京路189号', 1),
(7, '吴九', '13800138007', '辽宁省', '大连市', '中山区', '人民路1号', 0),

-- 用户8（新增2条）
(8, '郑十', '13800138008', '山东省', '青岛市', '市南区', '香港中路1号', 1),
(8, '郑十', '13800138008', '吉林省', '长春市', '朝阳区', '人民大街1号', 0),

-- 用户9（新增1条）
(9, '钱十一', '13800138009', '河南省', '郑州市', '金水区', '花园路1号', 1),

-- 用户10（新增1条）
(10, '孙十二', '13800138010', '云南省', '昆明市', '五华区', '东风西路1号', 1);

-- 3. 插入分类数据
INSERT INTO `category` (`name`, `parent_id`, `sort`)
VALUES ('电子产品', 0, 1),
       ('手机', 1, 1),
       ('电脑', 1, 2),
       ('服装', 0, 2),
       ('男装', 4, 1),
       ('女装', 4, 2),
       ('书籍', 0, 3),
       ('教材', 7, 1),
       ('小说', 7, 2);

-- 4. 插入商品数据
INSERT INTO `product` (`user_id`, `title`, `description`, `price`, `category_id`, `status`, `view_count`)
VALUES (1, 'iPhone 13 Pro', '99新，使用半年，无划痕，全套配件', 6999.00, 2, 1, 100),
       (1, 'MacBook Pro 2021', '95新，使用一年，性能完好', 12999.00, 3, 1, 80),
       (2, '男士休闲西装', '全新，未拆封，尺码L', 599.00, 5, 1, 50),
       (2, '女士连衣裙', '9成新，只穿过一次，尺码M', 299.00, 6, 1, 60),
       (3, '高等数学教材', '8成新，有少量笔记', 50.00, 8, 1, 30),
       (3, '三体全集', '全新，未拆封', 120.00, 9, 1, 40);

-- 5. 插入商品图片数据
INSERT INTO `product_image` (`product_id`, `image_url`, `sort`)
VALUES (1, '/images/products/iphone1.jpg', 1),
       (1, '/images/products/iphone2.jpg', 2),
       (2, '/images/products/macbook1.jpg', 1),
       (3, '/images/products/suit1.jpg', 1),
       (4, '/images/products/dress1.jpg', 1),
       (5, '/images/products/math1.jpg', 1),
       (6, '/images/products/threebody1.jpg', 1);
-- 商品表测试数据（30条）
INSERT INTO `product` (`user_id`, `title`, `description`, `price`, `category_id`, `status`, `view_count`)
VALUES
-- 电子产品-手机（5条）
(4, '华为P40 Pro', '99新，在保，全套配件，8+256G', 3499.00, 2, 1, 120),
(5, '小米12 Ultra', '95新，无拆修，12+512G顶配', 4299.00, 2, 1, 180),
(6, 'iPhone 12 128G', '9成新，电池健康88%，美版无锁', 2899.00, 2, 2, 210),
(7, '三星S22+', '全新未拆封，国行带票，12+256G', 5699.00, 2, 1, 95),
(8, '一加10 Pro', '98新，箱说全，哈苏影像系统', 3799.00, 2, 1, 75),

-- 电子产品-电脑（5条）
(9, '联想拯救者Y9000P', 'i7-12700H/RTX3060/16G/1T', 8999.00, 3, 1, 150),
(10, 'MacBook Air M1', '99新，2022款，8+256G', 6499.00, 3, 1, 200),
(11, '华硕天选3', 'R7-6800H/RTX3050/144Hz', 6299.00, 3, 3, 130),
(12, '戴尔XPS13', 'i5-1135G7/16G/512G 4K触控', 6999.00, 3, 1, 85),
(13, 'Surface Pro 8', 'i5/8G/256G 带键盘笔', 5899.00, 3, 1, 110),

-- 服装-男装（5条）
(1, '李宁运动套装', '全新带吊牌，尺码XL，黑色', 299.00, 5, 1, 60),
(2, '优衣库羽绒服', '9成新，尺码L，冬季保暖', 199.00, 5, 1, 90),
(3, '阿迪达斯运动鞋', '42码，经典款，穿着舒适', 259.00, 5, 2, 150),
(4, '杰克琼斯牛仔裤', '全新未拆封，尺码32', 159.00, 5, 1, 45),
(5, '海澜之家商务衬衫', '95新，尺码40，纯棉材质', 89.00, 5, 1, 30),

-- 服装-女装（5条）
(6, 'ZARA连衣裙', 'S码，夏季新款，仅试穿', 199.00, 6, 1, 110),
(7, '太平鸟毛呢大衣', 'M码，冬季厚款，驼色', 359.00, 6, 1, 75),
(8, 'UR休闲裤', '9成新，尺码27，高腰设计', 129.00, 6, 1, 65),
(9, '欧时力针织衫', '全新带吊牌，均码', 169.00, 6, 3, 40),
(10, 'vero moda风衣', 'S码，春秋款，卡其色', 279.00, 6, 1, 85),

-- 书籍-教材（5条）
(11, '线性代数第五版', '同济大学教材，有笔记', 25.00, 8, 1, 50),
(12, 'C++ Primer', '第5版，九成新无划痕', 78.00, 8, 1, 120),
(13, '考研英语词汇', '2023版，全新未使用', 35.00, 8, 1, 90),
(1, '数据结构与算法', '清华大学出版，有少量笔记', 45.00, 8, 2, 150),
(2, '西方经济学', '宏观分册，第7版', 30.00, 8, 1, 60),

-- 书籍-小说（5条）
(3, '活着（余华）', '正版二手，无破损', 18.00, 9, 1, 200),
(4, '三体全集礼盒', '全新未拆，精装收藏版', 199.00, 9, 1, 85),
(5, '百年孤独', '马尔克斯著，9成新', 28.00, 9, 1, 130),
(6, '白夜行（东野圭吾）', '正版二手，无涂画', 25.00, 9, 3, 95),
(7, '哈利波特与魔法石', '英文原版，95新', 65.00, 9, 1, 110);

-- 商品图片表测试数据（45条）
INSERT INTO `product_image` (`product_id`, `image_url`, `sort`)
VALUES
-- 每个商品3张图片（示例部分数据）
(1, '/images/products/huawei_p40_1.jpg', 1),
(1, '/images/products/huawei_p40_2.jpg', 2),
(1, '/images/products/huawei_p40_3.jpg', 3),

(2, '/images/products/xiami12_1.jpg', 1),
(2, '/images/products/xiami12_2.jpg', 2),
(2, '/images/products/xiami12_3.jpg', 3),

(3, '/images/products/iphone12_1.jpg', 1),
(3, '/images/products/iphone12_2.jpg', 2),
(3, '/images/products/iphone12_3.jpg', 3),

(4, '/images/products/samsung_s22_1.jpg', 1),
(4, '/images/products/samsung_s22_2.jpg', 2),
(4, '/images/products/samsung_s22_3.jpg', 3),

(5, '/images/products/oneplus10_1.jpg', 1),
(5, '/images/products/oneplus10_2.jpg', 2),
(5, '/images/products/oneplus10_3.jpg', 3),

(6, '/images/products/lenovo_1.jpg', 1),
(6, '/images/products/lenovo_2.jpg', 2),
(6, '/images/products/lenovo_3.jpg', 3),

(7, '/images/products/macbook_air_1.jpg', 1),
(7, '/images/products/macbook_air_2.jpg', 2),
(7, '/images/products/macbook_air_3.jpg', 3),

(8, '/images/products/asus_1.jpg', 1),
(8, '/images/products/asus_2.jpg', 2),
(8, '/images/products/asus_3.jpg', 3),

(9, '/images/products/dell_xps_1.jpg', 1),
(9, '/images/products/dell_xps_2.jpg', 2),
(9, '/images/products/dell_xps_3.jpg', 3),

(10, '/images/products/surface_1.jpg', 1),
(10, '/images/products/surface_2.jpg', 2),
(10, '/images/products/surface_3.jpg', 3),

-- 部分商品补充图片（如服装类）
(11, '/images/products/lining_1.jpg', 1),
(11, '/images/products/lining_2.jpg', 2),
(12, '/images/products/uniqlo_1.jpg', 1),
(13, '/images/products/adidas_1.jpg', 1),
(13, '/images/products/adidas_2.jpg', 2),
(14, '/images/products/jeans_1.jpg', 1),
(15, '/images/products/shirt_1.jpg', 1),
(16, '/images/products/dress_1.jpg', 1),
(16, '/images/products/dress_2.jpg', 2);

-- 6. 插入订单数据
INSERT INTO `orders` (`order_no`, `buyer_id`, `seller_id`, `product_id`, `price`, `status`, `address_id`,
                      `payment_method`, `payment_status`, `payment_time`, `transaction_no`, `message`)
VALUES ('202404290001', 2, 1, 1, 6999.00, 4, 3, 2, 2, '2024-04-29 15:30:00', '202404290001123456', '请尽快发货'),
       ('202404290002', 3, 1, 2, 12999.00, 3, 4, 1, 2, '2024-04-29 16:45:00', '202404290002123456', '周末送达'),
       ('202404290003', 1, 2, 3, 599.00, 2, 1, 2, 2, '2024-04-29 17:20:00', '202404290003123456', NULL);

-- 7. 插入评价数据
INSERT INTO `comment` (`order_id`, `user_id`, `product_id`, `content`, `rating`)
VALUES (1, 2, 1, '手机很好用，卖家很诚信', 5),
       (2, 3, 2, '电脑性能很好，就是价格有点贵', 4),
       (3, 1, 3, '衣服质量不错，很合身', 5);

-- 8. 插入收藏数据
INSERT INTO `favorite` (`user_id`, `product_id`)
VALUES (1, 2),
       (1, 4),
       (2, 1),
       (2, 3),
       (3, 1),
       (3, 5);

-- 9. 插入消息数据
INSERT INTO `message` (`sender_id`, `receiver_id`, `content`, `is_read`)
VALUES (1, 2, '你好，请问手机还在吗？', 1),
       (2, 1, '还在的，需要的话可以优惠', 1),
       (1, 2, '能便宜多少？', 0);

-- 10. 插入系统通知数据
INSERT INTO `system_notification` (`user_id`, `content`, `is_read`)
VALUES (1, '您的商品已成功上架', 1),
       (2, '您有一个新订单待处理', 0),
       (3, '您的订单已发货', 1);

-- 11. 插入推荐数据
INSERT INTO `recommendation` (`user_id`, `product_id`)
VALUES (1, 2),
       (1, 4),
       (2, 1),
       (2, 3),
       (3, 1),
       (3, 5);
-- 订单表测试数据（20条）
INSERT INTO `orders` (`order_no`, `buyer_id`, `seller_id`, `product_id`, `price`, `status`, `address_id`, `payment_method`, `payment_status`, `payment_time`, `transaction_no`, `message`)
VALUES
-- 已完成订单（状态4）
('202405010001', 2, 1, 1, 6999.00, 4, 3, 1, 2, '2024-05-01 10:15:00', 'TR202405010001', '请包装好一点'),
('202405010002', 3, 4, 14, 159.00, 4, 4, 2, 2, '2024-05-01 11:20:00', 'TR202405010002', '尽快发货谢谢'),
('202405010003', 5, 7, 7, 5699.00, 4, 11, 1, 2, '2024-05-01 13:45:00', 'TR202405010003', '发票抬头：个人'),
('202405010004', 6, 9, 19, 45.00, 4, 13, 2, 2, '2024-05-01 14:30:00', 'TR202405010004', NULL),
('202405010005', 8, 12, 12, 6999.00, 4, 17, 1, 2, '2024-05-01 15:10:00', 'TR202405010005', '周末配送'),

-- 待收货订单（状态3）
('202405010006', 1, 5, 5, 3799.00, 3, 1, 2, 2, '2024-05-02 09:25:00', 'TR202405010006', '检查屏幕有无坏点'),
('202405010007', 4, 8, 8, 129.00, 3, 9, 1, 2, '2024-05-02 10:40:00', 'TR202405010007', '送小礼物谢谢'),
('202405010008', 7, 10, 10, 5899.00, 3, 15, 1, 2, '2024-05-02 11:15:00', 'TR202405010008', '需要办公软件安装'),
('202405010009', 9, 13, 13, 35.00, 3, 19, 2, 2, '2024-05-02 14:00:00', 'TR202405010009', NULL),
('202405010010', 11, 2, 2, 4299.00, 3, 21, 1, 2, '2024-05-02 16:30:00', 'TR202405010010', '到货通知我'),

-- 待发货订单（状态2）
('202405010011', 3, 6, 6, 199.00, 2, 4, 2, 2, '2024-05-03 10:05:00', 'TR202405010011', '发顺丰快递'),
('202405010012', 5, 11, 11, 25.00, 2, 11, 1, 2, '2024-05-03 11:20:00', 'TR202405010012', '不要折损书角'),
('202405010013', 7, 1, 1, 3499.00, 2, 15, 2, 2, '2024-05-03 13:45:00', 'TR202405010013', '送钢化膜吗'),
('202405010014', 10, 3, 3, 2899.00, 2, 20, 1, 2, '2024-05-03 15:00:00', 'TR202405010014', '美版支持电信吗'),
('202405010015', 12, 5, 5, 259.00, 2, 22, 2, 2, '2024-05-03 16:10:00', 'TR202405010015', '鞋盒要完整'),

-- 待付款订单（状态1）
('202405010016', 2, 7, 7, 5699.00, 1, 3, NULL, 1, NULL, NULL, '考虑下价格'),
('202405010017', 4, 9, 9, 169.00, 1, 9, NULL, 1, NULL, NULL, '等发工资买'),
('202405010018', 6, 12, 12, 6999.00, 1, 13, NULL, 1, NULL, NULL, '咨询下配置'),
('202405010019', 8, 2, 2, 4299.00, 1, 17, NULL, 1, NULL, NULL, '能分期吗'),
('202405010020', 13, 4, 4, 299.00, 1, 23, NULL, 1, NULL, NULL, '尺码确认下');

-- 评价表测试数据（20条）
INSERT INTO `comment` (`order_id`, `user_id`, `product_id`, `content`, `rating`)
VALUES
-- 5星评价（8条）
(1, 2, 1, '手机成色完美，卖家服务好', 5),
(2, 3, 14, '牛仔裤质量超出预期', 5),
(3, 5, 7, '三星手机正品，拍照清晰', 5),
(4, 6, 19, '教材保存完好笔记清晰', 5),
(5, 8, 12, '电脑运行流畅，性价比高', 5),
(6, 1, 5, '一加手机手感一流', 5),
(7, 4, 8, '裤子版型不错，物超所值', 5),
(8, 7, 10, 'Surface办公神器，推荐购买', 5),

-- 4星评价（7条）
(9, 9, 13, '考研书全新，物流稍慢', 4),
(10, 11, 2, '小米手机发热有点明显', 4),
(11, 3, 6, '裙子好看但有点色差', 4),
(12, 5, 11, '线性代数书有少量涂写', 4),
(13, 7, 1, '华为手机电池一般', 4),
(14, 10, 3, 'iPhone美版信号一般', 4),
(15, 12, 5, '运动鞋穿着舒服但有点开胶', 4),

-- 3星及以下评价（5条）
(16, 2, 7, '屏幕有轻微划痕', 3),
(17, 4, 9, '针织衫起球严重', 3),
(18, 6, 12, '电脑风扇声音大', 2),
(19, 8, 2, '相机对焦有问题', 2),
(20, 13, 4, '尺码偏小，退货了', 1);

-- 收藏表测试数据（50条）
INSERT INTO `favorite` (`user_id`, `product_id`)
VALUES
-- 用户1收藏（5条）
(1, 3), (1, 7), (1, 12), (1, 18), (1, 25),
-- 用户2收藏（4条）
(2, 1), (2, 5), (2, 14), (2, 20),
-- 用户3收藏（6条）
(3, 2), (3, 8), (3, 11), (3, 16), (3, 22), (3, 28),
-- 用户4收藏（5条）
(4, 4), (4, 9), (4, 13), (4, 17), (4, 24),
-- 用户5收藏（4条）
(5, 6), (5, 10), (5, 15), (5, 21),
-- 用户6收藏（5条）
(6, 1), (6, 7), (6, 12), (6, 19), (6, 26),
-- 用户7收藏（3条）
(7, 3), (7, 14), (7, 23),
-- 用户8收藏（4条）
(8, 5), (8, 11), (8, 18), (8, 27),
-- 用户9收藏（5条）
(9, 2), (9, 9), (9, 16), (9, 20), (9, 25),
-- 用户10收藏（4条）
(10, 4), (10, 13), (10, 17), (10, 22),
-- 用户11收藏（3条）
(11, 6), (11, 15), (11, 24),
-- 用户12收藏（2条）
(12, 8), (12, 21);

-- 消息表测试数据（60条）
-- 每组对话包含3-5条消息（示例部分数据）
INSERT INTO `message` (`sender_id`, `receiver_id`, `content`, `is_read`)
VALUES
-- 对话1（用户2 ↔ 用户1）
(2, 1, '你好，手机能便宜点吗？', 1),
(1, 2, '最低6900，送原装充电器', 1),
(2, 1, '6500可以吗？诚心要', 1),
(1, 2, '不好意思这价出不了', 0),

-- 对话2（用户3 ↔ 用户4）
(3, 4, '牛仔裤尺码标准吗？', 1),
(4, 3, '标准尺码，建议按平时尺码选', 1),
(3, 4, '那要32码的，包邮吗？', 0),

-- 对话3（用户5 ↔ 用户7）
(5, 7, '三星手机是国行吗？', 1),
(7, 5, '是的，国行带电子发票', 1),
(5, 7, '电池健康度多少？', 1),
(7, 5, '98%，循环不到50次', 0),

-- 对话4（用户6 ↔ 用户9）
(6, 9, '考研书是最新版吗？', 1),
(9, 6, '是2023最新版的', 1),
(6, 9, '能拍下目录页吗？', 0),

-- 对话5（用户8 ↔ 用户12）
(8, 12, '电脑有拆修过吗？', 1),
(12, 8, '无拆无修，在保到2025年', 1),
(8, 12, '能跑个分看看吗？', 0),

-- 其他单条消息（40条随机组合）
(1, 3, '华为手机还在吗？', 1),
(4, 2, '已发货，单号SF123456', 1),
(5, 10, '风衣有实物图吗？', 0),
(7, 13, 'Surface续航多久？', 1),
(9, 5, '教材第几版的？', 0),
(11, 8, '运动鞋防滑吗？', 1),
(2, 6, '周末可以面交吗？', 0),
(3, 7, '发票能开公司吗？', 1),
(10, 4, '连衣裙什么材质？', 0),
(12, 9, '电脑有雷电接口吗？', 1);

-- 系统通知表测试数据（5条）
INSERT INTO `system_notification` (`user_id`, `content`, `is_read`)
VALUES
    (1, '您的商品"iPhone 13 Pro"已售出，请及时发货', 1),
    (3, '您发布的"MacBook Pro 2021"收到新报价', 0),
    (5, '账户安全提醒：请及时修改初始密码', 1),
    (7, '系统维护通知：5月10日0-6点服务暂停', 0),
    (9, '恭喜您获得"优质卖家"称号', 1);

-- 推荐表测试数据（60条）
INSERT INTO `recommendation` (`user_id`, `product_id`)
VALUES
-- 基于用户行为推荐（每个用户4-6条）
-- 用户1（电子产品偏好）
(1, 2), (1, 5), (1, 7), (1, 10), (1, 12),
-- 用户2（服装偏好）
(2, 11), (2, 14), (2, 16), (2, 18), (2, 20),
-- 用户3（图书偏好）
(3, 19), (3, 21), (3, 23), (3, 25), (3, 27),
-- 用户4（综合偏好）
(4, 1), (4, 8), (4, 15), (4, 22), (4, 28),
-- 用户5（高价商品偏好）
(5, 3), (5, 6), (5, 9), (5, 12), (5, 24),
-- 用户6（女性用户推荐）
(6, 13), (6, 16), (6, 18), (6, 20), (6, 26),
-- 用户7（数码爱好者）
(7, 1), (7, 4), (7, 7), (7, 10), (7, 12),
-- 用户8（学生群体）
(8, 19), (8, 21), (8, 23), (8, 25), (8, 27),
-- 用户9（商务人士）
(9, 5), (9, 9), (9, 12), (9, 17), (9, 24),
-- 用户10-13（各5条随机推荐）
(10, 2), (10, 6), (10, 14), (10, 20), (10, 25),
(11, 3), (11, 8), (11, 15), (11, 22), (11, 28),
(12, 4), (12, 9), (12, 16), (12, 21), (12, 26),
(13, 1), (13, 7), (13, 13), (13, 18), (13, 24);



