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
    `product_quality`   TINYINT(1) DEFAULT 1 COMMENT '商品成色：1-全新 2-几乎全新 3-轻微使用痕迹 4-正常使用痕迹 5-明显使用痕迹',
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
    `is_commented`  TINYINT(1)   DEFAULT 0 COMMENT '是否已评价',
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




