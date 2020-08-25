DROP TABLE IF EXISTS Flights;
DROP TABLE IF EXISTS Carriers;
DROP TABLE IF EXISTS Months;
DROP TABLE IF EXISTS Weekdays;

CREATE TABLE Carriers (
  cid varchar(7) PRIMARY KEY,
  name varchar(83)
);

CREATE TABLE Months (
  mid int PRIMARY KEY,
  month varchar(9)
);

CREATE TABLE Weekdays (
  did int PRIMARY KEY,
  day_of_week varchar(9)
);

CREATE TABLE Flights (
  fid int PRIMARY KEY,
  month_id int REFERENCES Months(mid),
  day_of_month int,
  day_of_week_id int REFERENCES Weekdays(did),
  carrier_id varchar(7) REFERENCES Carriers(cid),
  flight_num int,
  origin_city varchar(34),
  origin_state varchar(47),
  dest_city varchar(34),
  dest_state varchar(46),
  departure_delay int,
  taxi_out int,
  arrival_delay int,
  canceled int,
  actual_time int,
  distance int,
  capacity int,
  price int
);

PRAGMA foreign_keys = ON;

.mode csv
.import "C:/Users/yuqiu/Desktop/cse414/homeworks/cse414-qiubay/hw/hw2/starter-code/carriers.csv" Carriers
.import "C:/Users/yuqiu/Desktop/cse414/homeworks/cse414-qiubay/hw/hw2/starter-code/months.csv" Months
.import "C:/Users/yuqiu/Desktop/cse414/homeworks/cse414-qiubay/hw/hw2/starter-code/weekdays.csv" Weekdays
.import "C:/Users/yuqiu/Desktop/cse414/homeworks/cse414-qiubay/hw/hw2/starter-code/flights-small.csv" Flights
