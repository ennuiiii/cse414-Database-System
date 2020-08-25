DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Reservation;

CREATE TABLE Users (
  username varchar(20) PRIMARY KEY,
  password varchar(20),
  balance int
)

CREATE TABLE Reservation (
  rid int PRIMARY KEY,
  user_id varchar(20),
  fid_1 int,
  fid_2 int,
  paid int,
  price int,
  day int
)

Insert into Reservation values (0,null,null,null,null,null,null)
