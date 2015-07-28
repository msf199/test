CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(102) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `displayname` varchar(30) DEFAULT '',
  `registration_timestamp_utc` datetime DEFAULT NULL,
  `publisher` int(1) DEFAULT '0',
  `thumbnail` varchar(255) DEFAULT 'http://wpidiots.com/html/writic/red-writic-template/css/img/demo-images/avatar1.jpg',
  `rights` int(2) DEFAULT '0',
  `cover_photo` varchar(255) DEFAULT 'http://www.desktopwallpaper2.com/desktop-wallpaper-home/Very-Cool-Sport---Parachuting-Art-Picture-widescreen-wallpaper-1024x768-0-542313d514bd4-9123.jpg',
  `slogan` varchar(255) DEFAULT '',
  `newsfeed_processed` datetime DEFAULT NULL,
  `newsfeed_processing` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `id_UNIQUE` (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `phone_UNIQUE` (`phone`),
  KEY `username_INDEX` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11648 DEFAULT CHARSET=utf8;CREATE TABLE `category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) DEFAULT NULL,
  `category_thumbnail` varchar(255) DEFAULT NULL,
  `category_followers` int(11) DEFAULT '0',
  `category_publishers` int(11) DEFAULT '0',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;CREATE TABLE `content_type` (
  `content_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `content_type_name` varchar(45) NOT NULL,
  PRIMARY KEY (`content_type_id`),
  UNIQUE KEY `content_type_name_UNIQUE` (`content_type_name`)
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
  `category_id` int(2) DEFAULT '1',
  PRIMARY KEY (`content_id`),
  UNIQUE KEY `content_id_UNIQUE` (`content_id`),
  KEY `user_id_idx` (`user_id`),
  KEY `content_title_idx` (`content_title`),
  KEY `FK_user_content_content_type_idx` (`content_type`),
  KEY `FK_content_category_id_idx` (`category_id`),
  CONSTRAINT `FK_content_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_user_content_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12616 DEFAULT CHARSET=utf8;CREATE TABLE `newsfeed` (
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
) ENGINE=InnoDB AUTO_INCREMENT=14778 DEFAULT CHARSET=utf8;CREATE TABLE `lists_workout` (
  `content_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`content_id`),
  KEY `fk_lists_workout_content_id_idx` (`content_id`),
  KEY `fk_lists_workout_user_id_idx` (`user_id`),
  CONSTRAINT `fk_lists_workout_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_lists_workout_content_id` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
