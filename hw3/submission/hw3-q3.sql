-- Affected rows: 327.
-- 15s

select F2.origin_city as origin_city,
      (F1.c * 100.0)/F2.c as percentage
from (select F.origin_city, count(*) as c
      from Flights as F
      where F.actual_time < 180
      and F.actual_time is not NULL
      group by F.origin_city) as F1
right outer join (select T.origin_city, count(*) as c
                  from Flights as T
                  group by T.origin_city) as F2
on F1.origin_city = F2.origin_city
order by percentage;

-- ORIGIN_CITY              PERCENTAGE
-- Guam TT
-- Pago Pago TT
-- Aguadilla PR           29.433962264150
-- Anchorage AK           32.146037399821
-- San Juan PR            33.890360709190
-- Charlotte Amalie VI    40.000000000000
-- Ponce PR               41.935483870967
-- Fairbanks AK           50.691244239631
-- Kahului HI             53.664998528113
-- Honolulu HI            54.908808692277
-- San Francisco CA       56.307656826568
-- Los Angeles CA         56.604107648725
-- Seattle WA             57.755416553349
-- Long Beach CA          62.454116413214
-- Kona HI                63.282107574094
-- New York NY            63.481519772551
-- Las Vegas NV           65.163009288383
-- Christiansted VI       65.333333333333
-- Newark NJ              67.137355584082
-- Worcester MA           67.741935483870
