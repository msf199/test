SELECT * FROM `post`
 WHERE EXISTS
 (SELECT * FROM `friends` WHERE (`friends`.`user_one` = ? AND `post`.`user_id` = `friends`.`user_two`)
 OR   (`friends`.`user_two` = ? AND `post`.`user_id` = `friends`.`user_one`));
