CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(102) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `displayname` varchar(30) DEFAULT '',
  `registration_timestamp_utc` datetime DEFAULT NULL,
  `publisher` int(1) DEFAULT '0',
  `thumbnail` varchar(255) DEFAULT 'https://lh3.googleusercontent.com/-Sa9kdnhuE5E/AAAAAAAAAAI/AAAAAAAAABs/H8dhweNPuFI/photo.jpg',
  `rights` int(2) DEFAULT '0',
  `cover_photo` varchar(255) DEFAULT 'https://s-media-cache-ak0.pinimg.com/736x/13/23/c1/1323c17e88c80e988fa14b31b6fed07b.jpg',
  `slogan` varchar(255) DEFAULT '',
  `newsfeed_processed` datetime DEFAULT NULL,
  `newsfeed_processing` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `id_UNIQUE` (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `phone_UNIQUE` (`phone`),
  KEY `username_INDEX` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11515 DEFAULT CHARSET=utf8;CREATE TABLE `category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) DEFAULT NULL,
  `category_thumbnail` varchar(255) DEFAULT NULL,
  `category_followers` int(11) DEFAULT '0',
  `category_publishers` int(11) DEFAULT '0',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;CREATE TABLE `content_type` (
  `content_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_type_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`content_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;CREATE TABLE `content` (
  `content_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `content_type` int(2) DEFAULT '0',
  `content_url` varchar(255) DEFAULT NULL,
  `content_title` varchar(45) DEFAULT NULL,
  `content_description` varchar(100) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `thumbnail_url` varchar(255) DEFAULT 'http://telecoms.com/wp-content/blogs.dir/1/files/2012/06/euro-football-sport.jpg',
  `content_likes` int(10) DEFAULT '0',
  `content_views` bigint(15) DEFAULT '0',
  `content_displays` bigint(15) DEFAULT '0',
  `content_comments` int(10) DEFAULT '0',
  PRIMARY KEY (`content_id`),
  UNIQUE KEY `content_id_UNIQUE` (`content_id`),
  KEY `user_id_idx` (`user_id`),
  KEY `content_title_idx` (`content_title`),
  KEY `FK_user_content_content_type_idx` (`content_type`),
  CONSTRAINT `FK_user_content_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8418 DEFAULT CHARSET=utf8;CREATE TABLE `newsfeed` (
  `user_id` int(11) NOT NULL,
  `newsfeed_object` longtext NOT NULL,
  `last_generated` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  CONSTRAINT `FK_newsfeed_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `user_following` (
  `user_id` int(11) NOT NULL,
  `following_id` int(11) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  KEY `followed_id` (`following_id`) USING BTREE,
  KEY `user_id` (`user_id`),
  CONSTRAINT `FK_user_following_following_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_user_following_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `content_category` (
  `content_id` int(11) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  KEY `content_id` (`content_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `content_category_ibfk_1` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `content_category_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `category_following` (
  `category_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  KEY `FK_category_following_category_id_idx` (`category_id`),
  KEY `FK_category_following_user_id_idx` (`user_id`),
  CONSTRAINT `FK_category_following_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_category_following_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `category_publishing` (
  `category_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  KEY `FK_category_publishing_category_id_idx` (`category_id`),
  KEY `FK_category_publishing_user_id_idx` (`user_id`),
  CONSTRAINT `FK_category_publishing_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_category_publishing_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `authentication` (
  `authentication_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `key` varchar(45) DEFAULT NULL,
  `device` varchar(45) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `mac_address` varchar(45) DEFAULT NULL,
  `geolocation` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`authentication_id`),
  KEY `FK_authentication_user_id` (`user_id`),
  CONSTRAINT `FK_authentication_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12955 DEFAULT CHARSET=utf8;
