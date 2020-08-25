-- Affected rows: 334.
-- 15s
select distinct F1.origin_city as origin_city,
    F1.dest_city as dest_city,
    F1.actual_time as time
from Flights as F1,
    (select temp.origin_city as origin_city,
     MAX(temp.actual_time) as actual_time
    from Flights as temp
    group by temp.origin_city) as F2
where F1.origin_city = F2.origin_city
and F1.actual_time = F2.actual_time
order by F1.origin_city, F1.dest_city;

-- The first 20 rows
--ORIGIN_CITY                     DEST_CITY            TIME
--Aberdeen SD                     Minneapolis MN       106
--Abilene TX Dallas/Fort          Worth TX             111
--Adak Island AK                  Anchorage AK         471
--Aguadilla PR                    New York NY          368
--Akron OH                        Atlanta GA           408
--Albany GA                       Atlanta GA           243
--Albany NY                       Atlanta GA           390
--Albuquerque NM                  Houston TX           492
--Alexandria LA                   Atlanta GA           391
--Allentown/Bethlehem/Easton PA   Atlanta GA           456
--Alpena MI                       Detroit MI           80
--Amarillo TX                     Houston TX           390
--Anchorage AK                    Barrow AK            490
--Appleton WI                     Atlanta GA           405
--Arcata/Eureka CA                San Francisco CA     476
--Asheville NC                    Chicago IL           279
--Ashland WV                      Cincinnati OH        84
--Aspen CO                        Los Angeles CA       304
--Atlanta GA                      Honolulu HI          649
--Atlantic City NJ                Fort Lauderdale FL   212
