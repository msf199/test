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
  `email_verified` int(1) DEFAULT '0',
  `email_expiration` datetime DEFAULT NULL,
  `email_token` varchar(45) DEFAULT NULL,
  `reset_token` varchar(45) DEFAULT NULL,
  `fb_link` int(1) DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `id_UNIQUE` (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `phone_UNIQUE` (`phone`),
  KEY `username_INDEX` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11830 DEFAULT CHARSET=utf8;CREATE TABLE `category` (
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;CREATE TABLE `content` (
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
  `curation_accepted` int(2) DEFAULT '0',
  PRIMARY KEY (`content_id`),
  UNIQUE KEY `content_id_UNIQUE` (`content_id`),
  KEY `user_id_idx` (`user_id`),
  KEY `content_title_idx` (`content_title`),
  KEY `FK_user_content_content_type_idx` (`content_type`),
  KEY `FK_content_category_id_idx` (`category_id`),
  CONSTRAINT `FK_content_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_user_content_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13983 DEFAULT CHARSET=utf8;CREATE TABLE `content_curation` (
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
  UNIQUE KEY `content_url_UNIQUE` (`content_url`),
  KEY `user_id_idx` (`user_id`),
  KEY `content_curation_title_idx` (`content_title`),
  KEY `FK_user_content_curation_content_type_idx` (`content_type`),
  KEY `FK_content_curation_category_id_idx` (`category_id`),
  CONSTRAINT `FK_content_curation_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_user_content_curation_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;CREATE TABLE `newsfeed` (
  `user_id` int(11) NOT NULL,
  `newsfeed_object` longtext NOT NULL,
  `last_generated` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  CONSTRAINT `FK_newsfeed_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `saved_content` (
  `content_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  KEY `fk_lists_workout_content_id_idx` (`content_id`),
  KEY `fk_lists_workout_user_id_idx` (`user_id`),
  CONSTRAINT `fk_lists_workout_content_id` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_lists_workout_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `content_likes` (
  `user_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `like_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`content_id`),
  KEY `FK_content_likes_content_id_idx` (`content_id`),
  CONSTRAINT `FK_content_likes_content_id` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_content_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `feedback` (
  `feedback_id` int(11) NOT NULL AUTO_INCREMENT,
  `feedback_message` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `feedback_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`feedback_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;CREATE TABLE `reporting` (
  `reporting_id` int(11) NOT NULL AUTO_INCREMENT,
  `submitter_user_id` int(11) DEFAULT NULL,
  `reported_user_id` int(11) DEFAULT NULL,
  `reporting_type_id` int(11) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `reporting_timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`reporting_id`),
  KEY `fk_reporting_user_id_idx` (`reported_user_id`),
  KEY `fk_reporting_submitter_id_idx` (`submitter_user_id`),
  CONSTRAINT `FK_user_reporting_user_id` FOREIGN KEY (`reported_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;CREATE TABLE `user_following` (
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
  CONSTRAINT `FK_category_following_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_category_following_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `category_publishing` (
  `category_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  KEY `FK_category_publishing_category_id_idx` (`category_id`),
  KEY `FK_category_publishing_user_id_idx` (`user_id`),
  CONSTRAINT `FK_category_publishing_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_category_publishing_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `device` (
  `device_uuid` varchar(255) NOT NULL DEFAULT '',
  `device_name` varchar(255) DEFAULT NULL,
  `device_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`device_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `authentication` (
  `authentication_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `key` varchar(45) DEFAULT NULL,
  `device_uuid` varchar(255) NOT NULL DEFAULT 'unknown',
  `ip_address` varchar(45) DEFAULT NULL,
  `mac_address` varchar(45) DEFAULT NULL,
  `geolocation` varchar(45) DEFAULT NULL,
  `created` timestamp NULL DEFAULT NULL,
  `expires` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`authentication_id`),
  KEY `FK_authentication_user_id` (`user_id`),
  KEY `FK_authentication_device_uuid_idx` (`device_uuid`),
  CONSTRAINT `FK_authentication_device_uuid` FOREIGN KEY (`device_uuid`) REFERENCES `device` (`device_uuid`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `FK_authentication_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21237 DEFAULT CHARSET=utf8;CREATE TABLE `bundle_match` (
  `parent_content_id` int(11) NOT NULL,
  `child_content_id` int(11) NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`child_content_id`,`parent_content_id`),
  KEY `FK_content_content_id_bundle_match_parent_content_id_idx` (`parent_content_id`),
  CONSTRAINT `FK_content_content_id_bundle_match_parent_content_id` FOREIGN KEY (`parent_content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_content_content_id_bundle_match_child_content_id` FOREIGN KEY (`child_content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `content_comments` (
  `comment_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `content_id` int(11) NOT NULL,
  `comment_value` varchar(255) DEFAULT NULL,
  `comment_timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  UNIQUE KEY `comment_id_UNIQUE` (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8;CREATE TABLE `fb_user` (
  `user_id` int(11) NOT NULL,
  `link_timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idfb_users_UNIQUE` (`user_id`),
  CONSTRAINT `fk_fb_user_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;CREATE TABLE `reporting_type` (
  `reporting_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `reporting_type_name` varchar(255) NOT NULL,
  PRIMARY KEY (`reporting_type_id`),
  UNIQUE KEY `reporting_type_name` (`reporting_type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
