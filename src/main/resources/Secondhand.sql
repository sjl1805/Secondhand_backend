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
    `status`         TINYINT(1) DEFAULT 1 COMMENT '状态：1-待付款 2-待发货 3-待收货 4-已完成 5-已取消',
    `address_id`     BIGINT         NOT NULL COMMENT '收货地址ID',
    `create_time`    DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `payment_method` TINYINT(1) DEFAULT NULL COMMENT '支付方式: 1-支付宝 2-微信支付 3-银行卡',
    `payment_status` TINYINT(1) DEFAULT 1 COMMENT '支付状态: 1-待支付 2-支付成功 3-支付失败',
    `payment_time`   DATETIME   DEFAULT NULL COMMENT '支付时间',
    `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '支付交易号',
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
VALUES ('user1', '3b32279c2483cea75a5f7c219d25a331', '张三', '/images/avatars/avatar1.jpg',
        '13800138001', 'user1@example.com', 100, 0),
       ('user2', '3b32279c2483cea75a5f7c219d25a331', '李四', '/images/avatars/avatar2.jpg',
        '13800138002', 'user2@example.com', 95, 1),
       ('user3', '3b32279c2483cea75a5f7c219d25a331', '王五', '/images/avatars/avatar3.jpg',
        '13800138003', 'user3@example.com', 90, 1);

-- 2. 插入地址数据
INSERT INTO `address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`,
                       `is_default`)
VALUES (1, '张三', '13800138001', '北京市', '北京市', '海淀区', '中关村大街1号', 1),
       (1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '建国路88号', 0),
       (2, '李四', '13800138002', '上海市', '上海市', '浦东新区', '陆家嘴1号', 1),
       (3, '王五', '13800138003', '广东省', '广州市', '天河区', '天河路1号', 1);

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



