SELECT *
  FROM MyRestaurants
 WHERE Date_last_visit < date('now', '-3 month')
   AND Like_nlike = 1;
