CREATE TABLE `authentication` (
  `user_id` int(11) NOT NULL,
  `key` varchar(45) DEFAULT NULL,
  `device` varchar(45) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `mac_address` varchar(45) DEFAULT NULL,
  `geolocation` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FK_authentication_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `following` (
  `user_id` int(11) NOT NULL,
  `followed_id` varchar(45) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `user_id_UNIQUE` (`user_id`),
  KEY `followed_id` (`followed_id`) USING BTREE,
  CONSTRAINT `FK_following_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_content` (
  `content_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `content_type` varchar(10) DEFAULT NULL,
  `content_description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`content_id`),
  UNIQUE KEY `content_id_UNIQUE` (`content_id`),
  KEY `user_id_idx` (`user_id`),
  CONSTRAINT `FK_user_content_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `pass_salt` varchar(45) DEFAULT NULL,
  `pass_hash` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `id_UNIQUE` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;