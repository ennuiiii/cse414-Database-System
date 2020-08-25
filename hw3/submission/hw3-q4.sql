-- Affected rows: 256.
-- 22s
select distinct F2.dest_city as city
from Flights as F1, Flights as F2
where F1.origin_city = 'Seattle WA'
and F1.dest_city = F2.origin_city
and F2.dest_city != 'Seattle WA'
and F2.dest_city not in (select distinct F.dest_city
                        from Flights as F
                        where F.origin_city = 'Seattle WA')
order by city asc;

-- CITY
-- Aberdeen SD
-- Abilene TX
-- Adak Island AK
-- Aguadilla PR
-- Akron OH
-- Albany GA
-- Albany NY
-- Alexandria LA
-- Allentown/Bethlehem/Easton PA
-- Alpena MI
-- Amarillo TX
-- Appleton WI
-- Arcata/Eureka CA
-- Asheville NC
-- Ashland WV
-- Aspen CO
-- Atlantic City NJ
-- Augusta GA
-- Bakersfield CA
-- Bangor ME
