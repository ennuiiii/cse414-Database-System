-- Affected rows: 109.
-- 8s
select distinct F.origin_city as city
from Flights as F
where F.actual_time is not null
group by F.origin_city
having MAX(F.actual_time) < 180
order by F.origin_city;

-- first 20 rows
--        CITY
--        Aberdeen SD
--        Abilene TX
--        Alpena MI
--        Ashland WV
--        Augusta GA
--        Barrow AK
--        Beaumont/Port Arthur TX
--        Bemidji MN
--        Bethel AK
--        Binghamton NY
--        Brainerd MN
--        Bristol/Johnson City/Kingsport TN
--        Butte MT
--        Carlsbad CA
--        Casper WY
--        Cedar City UT
--        Chico CA
--        College Station/Bryan TX
--        Columbia MO
--        Columbus GA
