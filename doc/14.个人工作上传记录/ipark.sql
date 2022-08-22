/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3306
 Source Schema         : ipark

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 03/03/2022 14:48:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for black_list
-- ----------------------------
DROP TABLE IF EXISTS `black_list`;
CREATE TABLE `black_list`  (
  `BLACK_LIST_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CAR_ID` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '车牌号',
  `VIOLATION_TIME` datetime(0) NULL DEFAULT NULL COMMENT '列为黑名单时间',
  `VIOLATION_REASON` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '黑名单原因',
  `USER_ID` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`BLACK_LIST_ID`) USING BTREE,
  INDEX `user_id_b`(`USER_ID`) USING BTREE,
  CONSTRAINT `user_id_b` FOREIGN KEY (`USER_ID`) REFERENCES `user_info` (`USER_ID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '黑名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for client_message
-- ----------------------------
DROP TABLE IF EXISTS `client_message`;
CREATE TABLE `client_message`  (
  `MAC_ADDRESS` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `IP_ADDRESS` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `PORT` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `LOCATION` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ENTRANCE_AND_EXIT` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for entry_record
-- ----------------------------
DROP TABLE IF EXISTS `entry_record`;
CREATE TABLE `entry_record`  (
  `CAR_ID` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `CAR_TYPE` datetime(0) NULL DEFAULT NULL COMMENT '车辆类型',
  `ENTRANCE_TIME` datetime(0) NOT NULL COMMENT '进入时间',
  `EXIT_TIME` datetime(0) NULL DEFAULT NULL COMMENT '离开时间',
  `FEE_STANDARD` float NULL DEFAULT NULL COMMENT '费用标准 ',
  `RECORD_ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  PRIMARY KEY (`RECORD_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '停车场信息记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_info
-- ----------------------------
DROP TABLE IF EXISTS `member_info`;
CREATE TABLE `member_info`  (
  `MEMBER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CAR_ID` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车牌号',
  `CAR_TYPE` tinyint(4) NULL DEFAULT NULL COMMENT '车辆类型',
  `ACCOUNT_END_TIME` datetime(0) NULL DEFAULT NULL COMMENT '账户到期时间',
  `USER_ID` int(11) NOT NULL COMMENT '管理员ID',
  PRIMARY KEY (`MEMBER_ID`) USING BTREE,
  UNIQUE INDEX `CAR_ID`(`CAR_ID`) USING BTREE,
  INDEX `user_id_m`(`USER_ID`) USING BTREE,
  CONSTRAINT `user_id_m` FOREIGN KEY (`USER_ID`) REFERENCES `user_info` (`USER_ID`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会员信息记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `USER_ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ACCOUNT` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户名',
  `USER_PASSWORD` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户密码',
  `USRE_ROLE` tinyint(4) NULL DEFAULT NULL COMMENT '用户角色',
  PRIMARY KEY (`USER_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员信息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
